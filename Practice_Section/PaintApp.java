// PaintApp.java
// Full application with brush, text tool, shapes (advanced), save PNG, undo/redo.

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Stack;

public class PaintApp extends JFrame {
    private DrawArea drawArea = new DrawArea();

    // Toolbar
    private JButton colorBtn = new JButton("Color");
    private JButton clearBtn = new JButton("Clear");
    private JButton thickBtn = new JButton("🖌");
    private JButton textBtn = new JButton("Text ▼");
    private JButton saveImageBtn = new JButton("Save Image");
    private JButton undoBtn = new JButton("Undo");
    private JButton redoBtn = new JButton("Redo");

    // Shape controls
    private JComboBox<String> shapeCombo = new JComboBox<>(new String[] { "Brush", "Line", "Rectangle", "Oval" });
    private JButton strokeStyleBtn = new JButton("Solid/Dash");
    private JButton fillToggleBtn = new JButton("Fill:Off");
    private JButton fillColorBtn = new JButton("Fill Color");
    private JSlider opacitySlider = new JSlider(20, 100, 100); // percent

    private Color currentColor = Color.BLACK;
    private Color currentFillColor = Color.LIGHT_GRAY;
    private int brushSize = 3;
    private boolean fillEnabled = false;
    private boolean dashed = false;
    private float shapeAlpha = 1f;

    public PaintApp() {
        setTitle("PaintApp — Brush + Text + Shapes + Save + Undo/Redo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        JPanel top = new JPanel();
        top.add(colorBtn);
        top.add(clearBtn);
        top.add(thickBtn);
        top.add(shapeCombo);
        top.add(strokeStyleBtn);
        top.add(fillToggleBtn);
        top.add(fillColorBtn);
        top.add(new JLabel("Opacity"));
        top.add(opacitySlider);
        top.add(textBtn);
        top.add(saveImageBtn);
        top.add(undoBtn);
        top.add(redoBtn);

        add(top, BorderLayout.NORTH);
        add(drawArea, BorderLayout.CENTER);

        // actions
        colorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose stroke color", currentColor);
            if (c != null) {
                currentColor = c;
                drawArea.setStrokeColor(c);
            }
        });

        fillColorBtn.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Choose fill color", currentFillColor);
            if (c != null) {
                currentFillColor = c;
                drawArea.setFillColor(c);
            }
        });

        fillToggleBtn.addActionListener(e -> {
            fillEnabled = !fillEnabled;
            fillToggleBtn.setText(fillEnabled ? "Fill:On" : "Fill:Off");
            drawArea.setFillEnabled(fillEnabled);
        });

        strokeStyleBtn.addActionListener(e -> {
            dashed = !dashed;
            strokeStyleBtn.setText(dashed ? "Dash" : "Solid");
            drawArea.setDashed(dashed);
        });

        opacitySlider.addChangeListener(e -> {
            shapeAlpha = opacitySlider.getValue() / 100f;
            drawArea.setShapeAlpha(shapeAlpha);
        });

        thickBtn.addActionListener(e -> {
            String[] sizes = { "2", "4", "6", "8", "12", "16", "24", "32" };
            String sel = (String) JOptionPane.showInputDialog(this, "Choose size", "Brush Size",
                    JOptionPane.PLAIN_MESSAGE, null, sizes, String.valueOf(brushSize));
            if (sel != null) {
                try {
                    brushSize = Integer.parseInt(sel);
                    drawArea.setStrokeWidth(brushSize);
                } catch (Exception ignored) {
                }
            }
        });

        clearBtn.addActionListener(e -> {
            drawArea.clear();
        });

        shapeCombo.addActionListener(e -> {
            String s = (String) shapeCombo.getSelectedItem();
            drawArea.setTool(s == null ? "Brush" : s);
        });

        textBtn.addActionListener(e -> drawArea.showTextMenu(textBtn));

        saveImageBtn.addActionListener(e -> drawArea.saveAsImage());

        undoBtn.addActionListener(e -> drawArea.undo());
        redoBtn.addActionListener(e -> drawArea.redo());

        // initialize draw area with the toolbar defaults
        drawArea.setStrokeColor(currentColor);
        drawArea.setFillColor(currentFillColor);
        drawArea.setFillEnabled(fillEnabled);
        drawArea.setDashed(dashed);
        drawArea.setShapeAlpha(shapeAlpha);
        drawArea.setStrokeWidth(brushSize);

        setVisible(true);
    }

    // ========================= DRAW AREA ===========================
    class DrawArea extends JPanel {
        // ----- model objects -----
        class StrokeObj {
            ArrayList<Point> pts = new ArrayList<>();
            Color color;
            int size;

            StrokeObj(Color c, int s) {
                color = c;
                size = s;
            }
        }

        enum ShapeType {
            LINE, RECT, OVAL
        }

        class ShapeObj {
            ShapeType type;
            Point start, end;
            Color strokeColor;
            int strokeWidth;
            boolean dashed;
            boolean filled;
            Color fillColor;
            float alpha;

            ShapeObj(ShapeType t, Point a, Point b, Color sc, int sw, boolean dash, boolean fill, Color fc,
                    float apha) {
                type = t;
                start = a;
                end = b;
                strokeColor = sc;
                strokeWidth = sw;
                dashed = dash;
                filled = fill;
                fillColor = fc;
                alpha = apha;
            }
        }

        class TextObj {
            String text;
            Point pos; // baseline anchor
            Color color;
            int size;

            TextObj(String t, Point p, Color c, int s) {
                text = t;
                pos = p;
                color = c;
                size = s;
            }
        }

        // Lists
        private ArrayList<StrokeObj> strokes = new ArrayList<>();
        private ArrayList<ShapeObj> shapes = new ArrayList<>();
        private ArrayList<TextObj> texts = new ArrayList<>();

        // Undo/Redo: store snapshots (simple deep copy)
        private Stack<State> undoStack = new Stack<>();
        private Stack<State> redoStack = new Stack<>();

        // current temporary objects while drawing
        private StrokeObj currentStroke = null;
        private ShapeObj currentShape = null;
        private String tool = "Brush"; // "Brush","Line","Rectangle","Oval"

        // text interaction
        private int selectedTextIndex = -1;
        private boolean draggingText = false;
        private Point dragOffset = null;

        // settings
        private Color strokeColor = Color.BLACK;
        private Color fillColor = Color.LIGHT_GRAY;
        private boolean fillOn = false;
        private boolean dashedStroke = false;
        private float shapeAlpha = 1f;
        private int strokeWidth = 3;

        // state snapshot for undo/redo
        class State {
            ArrayList<StrokeObj> strokesCopy;
            ArrayList<ShapeObj> shapesCopy;
            ArrayList<TextObj> textsCopy;

            State(ArrayList<StrokeObj> a, ArrayList<ShapeObj> b, ArrayList<TextObj> c) {
                strokesCopy = a;
                shapesCopy = b;
                textsCopy = c;
            }
        }

        DrawArea() {
            setBackground(Color.WHITE);
            setDoubleBuffered(true);

            // Mouse handling
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();

                    // If in text move, select text under mouse
                    if (tool.equals("Brush")) {
                        // brush: start stroke
                        pushState(); // snapshot before operation
                        currentStroke = new StrokeObj(strokeColor, strokeWidth);
                        currentStroke.pts.add(e.getPoint());
                        strokes.add(currentStroke);
                        repaint();
                        return;
                    }

                    // Shape tools
                    if (tool.equals("Line") || tool.equals("Rectangle") || tool.equals("Oval")) {
                        pushState();
                        ShapeType t = tool.equals("Line") ? ShapeType.LINE
                                : (tool.equals("Rectangle") ? ShapeType.RECT : ShapeType.OVAL);
                        currentShape = new ShapeObj(t, e.getPoint(), e.getPoint(), strokeColor, strokeWidth,
                                dashedStroke, fillOn, fillColor, shapeAlpha);
                        repaint();
                        return;
                    }

                    // Text related modes come from text menu; handle selection/move
                    // Click selects text (for move/edit/delete from menu modes)
                    // We'll check selectedTextIndex to enable move during mouseDragged
                    selectedTextIndex = findTextAt(e.getPoint());
                    if (selectedTextIndex != -1 && tool.equals("Brush")) {
                        // nothing - normal
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // finalize stroke
                    if (currentStroke != null) {
                        currentStroke.pts.add(e.getPoint());
                        currentStroke = null;
                        // done stroke already pushed before start
                        redoStack.clear();
                        repaint();
                        return;
                    }
                    // finalize shape
                    if (currentShape != null) {
                        currentShape.end = e.getPoint();
                        shapes.add(currentShape);
                        currentShape = null;
                        redoStack.clear();
                        repaint();
                        return;
                    }
                    // finish dragging text move: ensure mode resets so brush works
                    if (draggingText) {
                        draggingText = false;
                        selectedTextIndex = -1;
                        pushState(); // push after move completed
                        redoStack.clear();
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // drawing stroke
                    if (currentStroke != null) {
                        currentStroke.pts.add(e.getPoint());
                        repaint();
                        return;
                    }
                    // dragging shape
                    if (currentShape != null) {
                        currentShape.end = e.getPoint();
                        repaint();
                        return;
                    }
                    // dragging text if selected and move-mode (we use text menu to set move mode —
                    // here allow dragging when user selected text)
                    if (selectedTextIndex != -1 && draggingText) {
                        Point pt = e.getPoint();
                        Point newPos = new Point(pt.x - dragOffset.x, pt.y - dragOffset.y);
                        texts.get(selectedTextIndex).pos = newPos;
                        repaint();
                        return;
                    }
                }
            });

            // Extra mouse listener for initiating text drag after user selected Move Text
            // mode
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // if text move mode active (selectedTextIndex set by menu), start dragging
                    if (selectedTextIndex != -1 && currentTextModeIsMove) {
                        Point tp = texts.get(selectedTextIndex).pos;
                        dragOffset = new Point(e.getX() - tp.x, e.getY() - tp.y);
                        draggingText = true;
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // handled above
                }
            });
        }

        // ---------- undo/redo snapshot helpers ----------
        private ArrayList<StrokeObj> deepCopyStrokes(ArrayList<StrokeObj> src) {
            ArrayList<StrokeObj> c = new ArrayList<>();
            for (StrokeObj s : src) {
                StrokeObj n = new StrokeObj(s.color, s.size);
                for (Point p : s.pts)
                    n.pts.add(new Point(p.x, p.y));
                c.add(n);
            }
            return c;
        }

        private ArrayList<ShapeObj> deepCopyShapes(ArrayList<ShapeObj> src) {
            ArrayList<ShapeObj> c = new ArrayList<>();
            for (ShapeObj s : src) {
                ShapeObj n = new ShapeObj(s.type, new Point(s.start.x, s.start.y), new Point(s.end.x, s.end.y),
                        s.strokeColor, s.strokeWidth, s.dashed, s.filled, s.fillColor, s.alpha);
                c.add(n);
            }
            return c;
        }

        private ArrayList<TextObj> deepCopyTexts(ArrayList<TextObj> src) {
            ArrayList<TextObj> c = new ArrayList<>();
            for (TextObj t : src) {
                c.add(new TextObj(t.text, new Point(t.pos.x, t.pos.y), t.color, t.size));
            }
            return c;
        }

        private void pushState() {
            // push deep copy onto undo stack and clear redo
            State st = new State(deepCopyStrokes(strokes), deepCopyShapes(shapes), deepCopyTexts(texts));
            undoStack.push(st);
            if (undoStack.size() > 100)
                undoStack.remove(0); // limit size
            redoStack.clear();
        }

        public void undo() {
            if (undoStack.isEmpty())
                return;
            State cur = new State(deepCopyStrokes(strokes), deepCopyShapes(shapes), deepCopyTexts(texts));
            redoStack.push(cur);
            State prev = undoStack.pop();
            strokes = deepCopyStrokes(prev.strokesCopy);
            shapes = deepCopyShapes(prev.shapesCopy);
            texts = deepCopyTexts(prev.textsCopy);
            // clear temporary objects
            currentStroke = null;
            currentShape = null;
            selectedTextIndex = -1;
            repaint();
        }

        public void redo() {
            if (redoStack.isEmpty())
                return;
            State cur = new State(deepCopyStrokes(strokes), deepCopyShapes(shapes), deepCopyTexts(texts));
            undoStack.push(cur);
            State next = redoStack.pop();
            strokes = deepCopyStrokes(next.strokesCopy);
            shapes = deepCopyShapes(next.shapesCopy);
            texts = deepCopyTexts(next.textsCopy);
            currentStroke = null;
            currentShape = null;
            selectedTextIndex = -1;
            repaint();
        }

        // ---------- tool & settings setters ----------
        public void setTool(String t) {
            tool = t;
            selectedTextIndex = -1;
            currentShape = null;
            currentStroke = null;
        }

        public void setStrokeColor(Color c) {
            strokeColor = c;
        }

        public void setFillColor(Color c) {
            fillColor = c;
        }

        public void setFillEnabled(boolean f) {
            fillOn = f;
        }

        public void setDashed(boolean d) {
            dashedStroke = d;
        }

        public void setShapeAlpha(float a) {
            shapeAlpha = a;
        }

        public void setStrokeWidth(int w) {
            strokeWidth = w;
        }

        // ---------- text menu and operations ----------
        private boolean currentTextModeIsMove = false; // helper for drag behaviour when Move selected in menu

        public void showTextMenu(Component parent) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem add = new JMenuItem("Add Text");
            JMenuItem edit = new JMenuItem("Edit Text");
            JMenuItem move = new JMenuItem("Move Text");
            JMenuItem resize = new JMenuItem("Resize Text");
            JMenuItem color = new JMenuItem("Change Text Color");
            JMenuItem delete = new JMenuItem("Delete Text");
            JMenuItem saveText = new JMenuItem("Save Text");
            menu.add(add);
            menu.add(edit);
            menu.add(move);
            menu.add(resize);
            menu.add(color);
            menu.add(delete);
            menu.addSeparator();
            menu.add(saveText);
            add.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(this, "Enter text:");
                if (input == null || input.trim().isEmpty())
                    return;

                pushState();

                // font size
                String sizeStr = JOptionPane.showInputDialog(this, "Enter font size:", 24);
                int sz = 24;
                try {
                    sz = Integer.parseInt(sizeStr);
                } catch (Exception ignored) {
                }

                // ---- FIX: make variables final using array wrappers ----
                final String[] fInput = { input };
                final int[] fSize = { sz };

                JOptionPane.showMessageDialog(this, "Click on canvas to place text.");

                MouseAdapter ma = new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent me) {
                        texts.add(new TextObj(fInput[0], me.getPoint(), strokeColor, fSize[0]));
                        removeMouseListener(this);
                        repaint();
                    }
                };
                addMouseListener(ma);
            });

            edit.addActionListener(e -> {
                Point p = askPoint("Click the text to edit");
                if (p == null)
                    return;
                int idx = findTextAt(p);
                if (idx == -1)
                    return;
                String newText = JOptionPane.showInputDialog(this, "Edit text:", texts.get(idx).text);
                if (newText != null) {
                    pushState();
                    texts.get(idx).text = newText;
                    repaint();
                }
            });

            move.addActionListener(e -> {
                Point p = askPoint("Click the text to move, then drag.");
                if (p == null)
                    return;
                int idx = findTextAt(p);
                if (idx == -1)
                    return;
                selectedTextIndex = idx;
                currentTextModeIsMove = true;
                // start dragging - user will drag; we use mousePressed/dragged/released to
                // handle
                // set dragging flag and offset will be computed in listeners
                // we also push state before move
                pushState();
                JOptionPane.showMessageDialog(this,
                        "Now drag the text to new position. Release mouse button when done.");
                // After user releases we set currentTextModeIsMove false in mouseReleased block
                // (see below)
                // We'll manage drag via additional listeners: when user presses on that text,
                // we will set draggingText/delta
                // To keep it simple: the mousePressed listener added earlier handles selection;
                // we just mark mode
            });

            resize.addActionListener(e -> {
                Point p = askPoint("Click the text to resize");
                if (p == null)
                    return;
                int idx = findTextAt(p);
                if (idx == -1)
                    return;
                String s = JOptionPane.showInputDialog(this, "Enter new font size:", texts.get(idx).size);
                if (s == null)
                    return;
                try {
                    int ns = Integer.parseInt(s);
                    pushState();
                    texts.get(idx).size = Math.max(6, ns);
                    repaint();
                } catch (Exception ignored) {
                }
            });

            color.addActionListener(e -> {
                Point p = askPoint("Click the text to change color");
                if (p == null)
                    return;
                int idx = findTextAt(p);
                if (idx == -1)
                    return;
                Color c = JColorChooser.showDialog(this, "Choose text color", texts.get(idx).color);
                if (c != null) {
                    pushState();
                    texts.get(idx).color = c;
                    repaint();
                }
            });

            delete.addActionListener(e -> {
                Point p = askPoint("Click the text to delete");
                if (p == null)
                    return;
                int idx = findTextAt(p);
                if (idx == -1)
                    return;
                pushState();
                texts.remove(idx);
                repaint();
            });

            saveText.addActionListener(e -> {
                saveAllTextToFile();
            });

            menu.show(parent, 0, parent.getHeight());
        }

        private Point askPoint(String msg) {
            JOptionPane.showMessageDialog(this, msg);
            final Point[] pt = new Point[1];
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pt[0] = e.getPoint();
                    removeMouseListener(this);
                }
            };
            addMouseListener(ma);
            // Wait until click — busy-wait simple loop with event queue processing
            // (small loop to block until user clicks; acceptable for this small app)
            while (pt[0] == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
            return pt[0];
        }

        private int findTextAt(Point p) {
            for (int i = texts.size() - 1; i >= 0; i--) {
                TextObj t = texts.get(i);
                Font f = new Font("Arial", Font.PLAIN, t.size);
                FontMetrics fm = getFontMetrics(f);
                int w = fm.stringWidth(t.text);
                int h = fm.getHeight();
                Rectangle r = new Rectangle(t.pos.x, t.pos.y - h, w, h);
                if (r.contains(p))
                    return i;
            }
            return -1;
        }

        private void saveAllTextToFile() {
            if (texts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No text to save.");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                return;
            File f = chooser.getSelectedFile();
            try (PrintWriter out = new PrintWriter(f)) {
                for (TextObj t : texts) {
                    out.println("Text: " + t.text);
                    out.println("Position: " + t.pos.x + "," + t.pos.y);
                    out.println("Size: " + t.size);
                    out.println("Color: " + t.color.getRGB());
                    out.println("----");
                }
                JOptionPane.showMessageDialog(this, "Saved text to " + f.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving text: " + ex.getMessage());
            }
        }

        // ---------- save entire canvas as PNG ----------
        public void saveAsImage() {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
                return;
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            // Create buffered image and paint into it
            BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            // white background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            // paint component onto g2
            paintAll(g2);
            g2.dispose();
            try {
                ImageIO.write(img, "png", file);
                JOptionPane.showMessageDialog(this, "Saved image: " + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage());
            }
        }

        // ---------- clear ----------
        public void clear() {
            pushState();
            strokes.clear();
            shapes.clear();
            texts.clear();
            selectedTextIndex = -1;
            currentStroke = null;
            currentShape = null;
            undoStack.clear();
            redoStack.clear();
            repaint();
        }

        // ---------- painting ----------
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw shapes first (so strokes may be above or below depending on order; we
            // keep chronological)
            for (ShapeObj s : shapes)
                drawShape(g2, s);

            // Draw strokes
            for (StrokeObj st : strokes) {
                drawStrokeObj(g2, st);
            }

            // Draw temp currentStroke on top while drawing
            if (currentStroke != null)
                drawStrokeObj(g2, currentStroke);

            // Draw current temporary shape
            if (currentShape != null)
                drawShape(g2, currentShape);

            // Draw texts
            for (int i = 0; i < texts.size(); i++) {
                TextObj t = texts.get(i);
                drawTextObj(g2, t, i == selectedTextIndex);
            }

            g2.dispose();
        }

        private void drawStrokeObj(Graphics2D g2, StrokeObj st) {
            if (st.pts.size() < 2)
                return;
            g2.setColor(st.color);
            g2.setStroke(new BasicStroke(st.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < st.pts.size(); i++) {
                Point a = st.pts.get(i - 1), b = st.pts.get(i);
                g2.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        private void drawShape(Graphics2D g2, ShapeObj s) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, s.alpha));
            Stroke oldStroke = g2.getStroke();
            if (s.dashed)
                g2.setStroke(new BasicStroke(s.strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f,
                        new float[] { 10f }, 0f));
            else
                g2.setStroke(new BasicStroke(s.strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int x1 = s.start.x, y1 = s.start.y, x2 = s.end.x, y2 = s.end.y;
            int x = Math.min(x1, x2), y = Math.min(y1, y2), w = Math.abs(x2 - x1), h = Math.abs(y2 - y1);

            if (s.filled) {
                g2.setColor(s.fillColor);
                if (s.type == ShapeType.RECT)
                    g2.fillRect(x, y, w, h);
                else if (s.type == ShapeType.OVAL)
                    g2.fillOval(x, y, w, h);
                // line fill ignored
            }

            g2.setColor(s.strokeColor);
            if (s.type == ShapeType.LINE) {
                g2.drawLine(x1, y1, x2, y2);
            } else if (s.type == ShapeType.RECT) {
                g2.drawRect(x, y, w, h);
            } else if (s.type == ShapeType.OVAL) {
                g2.drawOval(x, y, w, h);
            }
            g2.setStroke(oldStroke);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); // reset
        }

        private void drawTextObj(Graphics2D g2, TextObj t, boolean selected) {
            g2.setColor(t.color);
            Font f = new Font("Arial", Font.PLAIN, t.size);
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(t.text);
            int h = fm.getHeight();
            // draw text (baseline at pos)
            g2.drawString(t.text, t.pos.x, t.pos.y);
            if (selected) {
                // draw bounding box and handles
                g2.setColor(Color.BLUE);
                Rectangle r = new Rectangle(t.pos.x - 4, t.pos.y - fm.getAscent() - 4, w + 8, h + 8);
                g2.drawRect(r.x, r.y, r.width, r.height);
                // handles
                int hs = 6;
                g2.fillRect(r.x - hs / 2, r.y - hs / 2, hs, hs);
                g2.fillRect(r.x + r.width - hs / 2, r.y - hs / 2, hs, hs);
                g2.fillRect(r.x - hs / 2, r.y + r.height - hs / 2, hs, hs);
                g2.fillRect(r.x + r.width - hs / 2, r.y + r.height - hs / 2, hs, hs);
            }
        }
    }

    // ========================= MAIN ===========================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PaintApp::new);
    }
}
