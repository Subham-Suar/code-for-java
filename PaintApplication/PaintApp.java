// Save this file as PaintApp.java
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Stack;


public class PaintApp extends JFrame {

    private DrawArea drawArea = new DrawArea();

    // Top controls buttons
    private JButton clearBtn = new JButton("Clear");
    private JButton colorBtn = new JButton("Color");
    private JButton brushBtn = new JButton("Brush");
    private JButton thickButton = new JButton("🖌");
    private JButton textBtn = new JButton("Text");
    private JButton eraserBtn = new JButton("Eraser");
    private JButton eraserSizeBtn = new JButton("Eraser Size");
    private JButton shapeBtn = new JButton("Shapes");
    private JButton fillToggleBtn = new JButton("Fill: OFF");
    private JButton selectBtn = new JButton("Select");
    private JComboBox<String> layerCombo = new JComboBox<>();
    private JButton addLayerBtn = new JButton("+Layer");
    private JButton removeLayerBtn = new JButton("-Layer");

    private Color currentColor = Color.BLACK;
    private int brushSize = 3;
    int eraserSize = 15; // default size
    private boolean fillShapes = false;

    public PaintApp() {
        setTitle("PaintApp");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // Build menu bar (File, Edit, View)
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save As...");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        editMenu.add(undoItem);
        editMenu.add(redoItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem clearView = new JMenuItem("Clear Canvas");
        viewMenu.add(clearView);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        toolBar.add(colorBtn);
        toolBar.add(brushBtn);
        toolBar.add(thickButton);
        toolBar.add(textBtn);
        toolBar.add(eraserBtn);
        toolBar.add(eraserSizeBtn); // added button
        toolBar.add(shapeBtn);
        toolBar.add(fillToggleBtn);
        toolBar.add(selectBtn);
        toolBar.add(clearBtn);

        toolBar.addSeparator();
        toolBar.add(new JLabel(" Layers: "));
        toolBar.add(layerCombo);
        toolBar.add(addLayerBtn);
        toolBar.add(removeLayerBtn);

        // small color swatch panel
        JPanel swatch = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        swatch.setPreferredSize(new Dimension(120, 32));
        Color[] swatches = {Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.RED, Color.ORANGE,
                Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.PINK};
        for (Color col : swatches) {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(18, 18));
            b.setBackground(col);
            b.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            b.addActionListener(e -> {
                currentColor = col;
                drawArea.setBrushColor(col);
            });
            swatch.add(b);
        }
        toolBar.add(swatch);

        add(toolBar, BorderLayout.NORTH);
        add(drawArea, BorderLayout.CENTER);

        // Hook up actions

        // File menu save
        saveItem.addActionListener(e -> doSave());

        exitItem.addActionListener(e -> System.exit(0));

        // Edit menu
        undoItem.addActionListener(e -> drawArea.undo());
        redoItem.addActionListener(e -> drawArea.redo());

        // View clear
        clearView.addActionListener(e -> drawArea.clear());

        // Color chooser
        colorBtn.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(null, "Choose a color", currentColor);
            if (selected != null) {
                currentColor = selected;
                drawArea.setBrushColor(currentColor);
            }
        });

        // Brush button: switch to freehand drawing mode
        brushBtn.addActionListener(e -> {
            drawArea.setMode(DrawArea.Mode.BRUSH);
            brushBtn.setEnabled(false);
            selectBtn.setEnabled(true);
            eraserBtn.setEnabled(true);
            shapeBtn.setEnabled(true);
        });

        // Thickness chooser
        thickButton.addActionListener(e -> {
            String[] sizes = {"1", "2", "3", "4", "6", "8", "12", "16", "24", "36"};
            String selected = (String) JOptionPane.showInputDialog(
                    null, "Select Brush Thickness", "Brush Size",
                    JOptionPane.PLAIN_MESSAGE, null, sizes, String.valueOf(brushSize));
            if (selected != null) {
                brushSize = Integer.parseInt(selected);
                drawArea.setBrushSize(brushSize);
            }
        });

        // Text menu
        textBtn.addActionListener(e -> drawArea.showTextMenu(textBtn));

        // Eraser
        eraserBtn.addActionListener(e -> {
            drawArea.setMode(DrawArea.Mode.ERASER);
        });

        // Eraser size chooser (popup)
        eraserSizeBtn.addActionListener(e -> {
            String[] sizes = {"5", "10", "15", "20", "30", "40", "60", "80"};
            String selected = (String) JOptionPane.showInputDialog(
                    null,
                    "Select Eraser Size (px)",
                    "Eraser Size",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    sizes,
                    String.valueOf(eraserSize)
            );
            if (selected != null) {
                try {
                    int newSize = Integer.parseInt(selected);
                    eraserSize = newSize;
                    drawArea.setEraserSize(newSize);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid number");
                }
            }
        });

        // Shape button
        shapeBtn.addActionListener(e -> drawArea.showShapesMenu(shapeBtn));

        // Fill toggle
        fillToggleBtn.addActionListener(e -> {
            fillShapes = !fillShapes;
            fillToggleBtn.setText("Fill: " + (fillShapes ? "ON" : "OFF"));
            drawArea.setFillMode(fillShapes);
        });

        // Select / Move
        selectBtn.addActionListener(e -> {
            drawArea.setMode(DrawArea.Mode.SELECT);
            brushBtn.setEnabled(true);
            selectBtn.setEnabled(false);
        });

        // Clear
        clearBtn.addActionListener(e -> drawArea.clear());

        // Layers management
        addLayerBtn.addActionListener(e -> {
            drawArea.addLayer();
            refreshLayerCombo();
        });
        removeLayerBtn.addActionListener(e -> {
            drawArea.removeCurrentLayer();
            refreshLayerCombo();
        });
        layerCombo.addActionListener(e -> {
            int idx = layerCombo.getSelectedIndex();
            if (idx >= 0) drawArea.setCurrentLayer(idx);
        });

        // Initialize one layer
        drawArea.addLayer();
        refreshLayerCombo();
        layerCombo.setSelectedIndex(0);

        // Keyboard shortcuts for undo/redo as well
        drawArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");
        drawArea.getActionMap().put("Undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                drawArea.undo();
            }
        });

        drawArea.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
        drawArea.getActionMap().put("Redo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                drawArea.redo();
            }
        });

        setVisible(true);
    }

    private void refreshLayerCombo() {
        layerCombo.removeAllItems();
        for (int i = 0; i < drawArea.getLayerCount(); i++) {
            layerCombo.addItem("Layer " + (i + 1) + (i == drawArea.getCurrentLayerIndex() ? " (active)" : ""));
        }
        if (drawArea.getLayerCount() > 0) {
            layerCombo.setSelectedIndex(drawArea.getCurrentLayerIndex());
        }
    }

    private void doSave() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Image", "png"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Image", "jpg", "jpeg"));
        int rv = chooser.showSaveDialog(this);
        if (rv == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String ext = "png";
            String fname = f.getName().toLowerCase();
            if (chooser.getFileFilter() instanceof FileNameExtensionFilter) {
                FileNameExtensionFilter ff = (FileNameExtensionFilter) chooser.getFileFilter();
                String[] exts = ff.getExtensions();
                if (exts.length > 0) ext = exts[0];
            } else if (fname.endsWith(".jpg") || fname.endsWith(".jpeg")) {
                ext = "jpg";
            } else if (fname.endsWith(".png")) {
                ext = "png";
            }
            File out = f;
            if (!fname.endsWith("." + ext)) {
                out = new File(f.getAbsolutePath() + "." + ext);
            }
            try {
                BufferedImage img = new BufferedImage(drawArea.getWidth(), drawArea.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                drawArea.paint(g2);
                g2.dispose();
                ImageIO.write(img, ext, out);
                JOptionPane.showMessageDialog(this, "Saved: " + out.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PaintApp::new);
    }

    // ---------- DrawArea & supporting classes ----------
    class DrawArea extends JPanel {
        // Modes
        enum Mode {
            BRUSH, ERASER, TEXT, SELECT, SHAPE, NONE
        }

        private Mode mode = Mode.BRUSH;
        private int localBrushSize = brushSize;
        private Color brushColor = currentColor;
        private boolean fillMode = false;

        // Layers: each layer contains its own geometry
        class Layer {
            ArrayList<Point> points = new ArrayList<>();         // freehand points (null is stroke separator)
            ArrayList<Color> pointColors = new ArrayList<>();
            ArrayList<Integer> pointSizes = new ArrayList<>();

            ArrayList<Shape> shapes = new ArrayList<>();
            ArrayList<Color> shapeColors = new ArrayList<>();
            ArrayList<Integer> shapeSizes = new ArrayList<>();
            ArrayList<Boolean> shapeFilled = new ArrayList<>();

            ArrayList<String> texts = new ArrayList<>();
            ArrayList<Point> textPoints = new ArrayList<>();
            ArrayList<Color> textColors = new ArrayList<>();
            ArrayList<Integer> textSizes = new ArrayList<>();
        }

        private ArrayList<Layer> layers = new ArrayList<>();
        private int currentLayer = 0;

        // Undo/redo stacks (snapshots)
        private Stack<Snapshot> undoStack = new Stack<>();
        private Stack<Snapshot> redoStack = new Stack<>();

        // Shape tool
        enum ShapeType {
            NONE, LINE, CURVE, RECTANGLE, ROUNDED_RECT, SQUARE, OVAL, CIRCLE,
            TRIANGLE, RIGHT_TRIANGLE, DIAMOND, PENTAGON, HEXAGON, STAR5, STAR6,
            ARROW_LEFT, ARROW_RIGHT, ARROW_UP, ARROW_DOWN, CALL_OUT, HEART, LIGHTNING, CLOUD
        }

        private ShapeType currentShape = ShapeType.NONE;
        private Point shapeStart = null;
        private Shape previewShape = null;

        // Selection
        private int selLayerIndex = -1;
        private int selShapeIndex = -1;
        private Shape selectedShape = null;
        private Color selectedShapeColor = null;
        private boolean selectedShapeFilled = false;
        private int selectedShapeSize = 1;
        private Point lastMouse = null;

        // Freehand drag tracking
        private boolean dragging = false;

        // Snapshot for undo
        class Snapshot {
            ArrayList<Layer> layersSnapshot;
            int activeLayer;
            ShapeType shape;
            boolean fill;
            Snapshot cloneSnapshot() {
                Snapshot s = new Snapshot();
                s.activeLayer = activeLayer;
                s.shape = shape;
                s.fill = fill;
                s.layersSnapshot = new ArrayList<>();
                for (Layer L : layers) {
                    Layer nl = new Layer();
                    nl.points = new ArrayList<>(L.points);
                    nl.pointColors = new ArrayList<>(L.pointColors);
                    nl.pointSizes = new ArrayList<>(L.pointSizes);
                    nl.shapes = new ArrayList<>(L.shapes);   // shallow copy of Shape references — acceptable for this app
                    nl.shapeColors = new ArrayList<>(L.shapeColors);
                    nl.shapeSizes = new ArrayList<>(L.shapeSizes);
                    nl.shapeFilled = new ArrayList<>(L.shapeFilled);
                    nl.texts = new ArrayList<>(L.texts);
                    nl.textPoints = new ArrayList<>(L.textPoints);
                    nl.textColors = new ArrayList<>(L.textColors);
                    nl.textSizes = new ArrayList<>(L.textSizes);
                    s.layersSnapshot.add(nl);
                }
                return s;
            }
        }

        DrawArea() {
            setBackground(Color.WHITE);
            setDoubleBuffered(true);
            setFocusable(true);

            MouseAdapter ma = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    dragging = false;
                    lastMouse = e.getPoint();

                    // Save snapshot for undo BEFORE editing actions
                    if (mode == Mode.BRUSH || mode == Mode.SHAPE || mode == Mode.ERASER || mode == Mode.SELECT || mode == Mode.TEXT) {
                        saveSnapshot();
                    }

                    if (mode == Mode.BRUSH) {
                        // add initial point
                        Layer L = getCurrentLayer();
                        L.points.add(e.getPoint());
                        L.pointColors.add(brushColor);
                        L.pointSizes.add(localBrushSize);
                    } else if (mode == Mode.ERASER) {
                        eraseAt(e.getPoint());
                    } else if (mode == Mode.SHAPE) {
                        shapeStart = e.getPoint();
                        previewShape = null;
                    } else if (mode == Mode.SELECT) {
                        // attempt to select topmost shape at point
                        chooseShapeAt(e.getPoint());
                        repaint();
                    } else if (mode == Mode.TEXT) {
                        addTextAt(e.getPoint());
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if (mode == Mode.BRUSH) {
                        // end stroke (separator)
                        Layer L = getCurrentLayer();
                        L.points.add(null);
                        L.pointColors.add(null);
                        L.pointSizes.add(null);
                    } else if (mode == Mode.SHAPE && shapeStart != null) {
                        Point end = e.getPoint();
                        Shape sh = buildShape(shapeStart, end, currentShape);
                        if (sh != null && currentShape != ShapeType.NONE) {
                            Layer L = getCurrentLayer();
                            L.shapes.add(sh);
                            L.shapeColors.add(brushColor);
                            L.shapeSizes.add(localBrushSize);
                            L.shapeFilled.add(fillMode);
                        }
                        previewShape = null;
                        shapeStart = null;
                    }
                    dragging = false;
                    lastMouse = null;
                    repaint();
                }

                public void mouseDragged(MouseEvent e) {
                    dragging = true;
                    Point p = e.getPoint();
                    if (mode == Mode.BRUSH) {
                        Layer L = getCurrentLayer();
                        L.points.add(p);
                        L.pointColors.add(brushColor);
                        L.pointSizes.add(localBrushSize);
                        repaint();
                    } else if (mode == Mode.ERASER) {
                        eraseAt(p);
                    } else if (mode == Mode.SHAPE && shapeStart != null) {
                        previewShape = buildShape(shapeStart, p, currentShape);
                        repaint();
                    } else if (mode == Mode.SELECT && selectedShape != null && lastMouse != null) {
                        // translate selected shape by delta
                        int dx = p.x - lastMouse.x;
                        int dy = p.y - lastMouse.y;
                        if (dx != 0 || dy != 0) {
                            AffineTransform at = AffineTransform.getTranslateInstance(dx, dy);
                            Shape moved = at.createTransformedShape(selectedShape);
                            // commit moved shape back into layer
                            Layer L = layers.get(selLayerIndex);
                            L.shapes.set(selShapeIndex, moved);
                            selectedShape = moved;
                            lastMouse = p;
                            repaint();
                        }
                    }
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        // ----- Layers -----
        public void addLayer() {
            Layer L = new Layer();
            layers.add(L);
            currentLayer = layers.size() - 1;
        }

        public void removeCurrentLayer() {
            if (layers.size() <= 1) {
                JOptionPane.showMessageDialog(this, "Cannot remove the last layer.");
                return;
            }
            layers.remove(currentLayer);
            currentLayer = Math.max(0, currentLayer - 1);
        }

        public int getLayerCount() {
            return layers.size();
        }

        public int getCurrentLayerIndex() {
            return currentLayer;
        }

        public void setCurrentLayer(int idx) {
            if (idx >= 0 && idx < layers.size()) {
                currentLayer = idx;
                // clear selection when switching layers
                clearSelection();
                repaint();
            }
        }

        private Layer getCurrentLayer() {
            if (layers.isEmpty()) {
                addLayer();
            }
            return layers.get(currentLayer);
        }

        // ----- Undo / Redo -----
        private void saveSnapshot() {
            Snapshot s = new Snapshot();
            s.activeLayer = currentLayer;
            s.shape = currentShape;
            s.fill = fillMode;
            s.layersSnapshot = new ArrayList<>();
            for (Layer L : layers) {
                Layer nl = new Layer();
                nl.points = new ArrayList<>(L.points);
                nl.pointColors = new ArrayList<>(L.pointColors);
                nl.pointSizes = new ArrayList<>(L.pointSizes);
                nl.shapes = new ArrayList<>(L.shapes);
                nl.shapeColors = new ArrayList<>(L.shapeColors);
                nl.shapeSizes = new ArrayList<>(L.shapeSizes);
                nl.shapeFilled = new ArrayList<>(L.shapeFilled);
                nl.texts = new ArrayList<>(L.texts);
                nl.textPoints = new ArrayList<>(L.textPoints);
                nl.textColors = new ArrayList<>(L.textColors);
                nl.textSizes = new ArrayList<>(L.textSizes);
                s.layersSnapshot.add(nl);
            }
            s.activeLayer = currentLayer;
            s.shape = currentShape;
            s.fill = fillMode;
            undoStack.push(s);
            redoStack.clear();
        }

        public void undo() {
            if (undoStack.isEmpty()) return;
            Snapshot s = undoStack.pop();
            // push current to redo
            Snapshot current = new Snapshot();
            current.layersSnapshot = new ArrayList<>();
            for (Layer L : layers) {
                Layer nl = new Layer();
                nl.points = new ArrayList<>(L.points);
                nl.pointColors = new ArrayList<>(L.pointColors);
                nl.pointSizes = new ArrayList<>(L.pointSizes);
                nl.shapes = new ArrayList<>(L.shapes);
                nl.shapeColors = new ArrayList<>(L.shapeColors);
                nl.shapeSizes = new ArrayList<>(L.shapeSizes);
                nl.shapeFilled = new ArrayList<>(L.shapeFilled);
                nl.texts = new ArrayList<>(L.texts);
                nl.textPoints = new ArrayList<>(L.textPoints);
                nl.textColors = new ArrayList<>(L.textColors);
                nl.textSizes = new ArrayList<>(L.textSizes);
                current.layersSnapshot.add(nl);
            }
            current.activeLayer = currentLayer;
            current.shape = currentShape;
            current.fill = fillMode;
            redoStack.push(current);

            // restore s
            layers.clear();
            for (Layer L : s.layersSnapshot) {
                Layer nl = new Layer();
                nl.points = new ArrayList<>(L.points);
                nl.pointColors = new ArrayList<>(L.pointColors);
                nl.pointSizes = new ArrayList<>(L.pointSizes);
                nl.shapes = new ArrayList<>(L.shapes);
                nl.shapeColors = new ArrayList<>(L.shapeColors);
                nl.shapeSizes = new ArrayList<>(L.shapeSizes);
                nl.shapeFilled = new ArrayList<>(L.shapeFilled);
                nl.texts = new ArrayList<>(L.texts);
                nl.textPoints = new ArrayList<>(L.textPoints);
                nl.textColors = new ArrayList<>(L.textColors);
                nl.textSizes = new ArrayList<>(L.textSizes);
                layers.add(nl);
            }
            currentLayer = s.activeLayer;
            currentShape = s.shape;
            fillMode = s.fill;
            repaint();
        }

        public void redo() {
            if (redoStack.isEmpty()) return;
            Snapshot s = redoStack.pop();
            // push current to undo
            saveSnapshot();
            // restore
            layers.clear();
            for (Layer L : s.layersSnapshot) {
                Layer nl = new Layer();
                nl.points = new ArrayList<>(L.points);
                nl.pointColors = new ArrayList<>(L.pointColors);
                nl.pointSizes = new ArrayList<>(L.pointSizes);
                nl.shapes = new ArrayList<>(L.shapes);
                nl.shapeColors = new ArrayList<>(L.shapeColors);
                nl.shapeSizes = new ArrayList<>(L.shapeSizes);
                nl.shapeFilled = new ArrayList<>(L.shapeFilled);
                nl.texts = new ArrayList<>(L.texts);
                nl.textPoints = new ArrayList<>(L.textPoints);
                nl.textColors = new ArrayList<>(L.textColors);
                nl.textSizes = new ArrayList<>(L.textSizes);
                layers.add(nl);
            }
            currentLayer = s.activeLayer;
            currentShape = s.shape;
            fillMode = s.fill;
            repaint();
        }

        // ----- Text -----
        public void showTextMenu(Component button) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem add = new JMenuItem("Add Text");
            JMenuItem edit = new JMenuItem("Edit Text");
            JMenuItem move = new JMenuItem("Move Text");
            JMenuItem delete = new JMenuItem("Delete Text");
            menu.add(add);
            menu.add(edit);
            menu.add(move);
            menu.add(delete);

            add.addActionListener(e -> setMode(Mode.TEXT));
            edit.addActionListener(e -> {
                String txt = JOptionPane.showInputDialog("Enter text to add:");
                if (txt != null) {
                    Layer L = getCurrentLayer();
                    L.texts.add(txt);
                    L.textPoints.add(new Point(50, 50));
                    L.textColors.add(brushColor);
                    L.textSizes.add(24);
                    repaint();
                }
            });
            move.addActionListener(e -> setMode(Mode.SELECT));
            delete.addActionListener(e -> {
                // simple delete dialog
                Layer L = getCurrentLayer();
                if (L.texts.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No text to delete on this layer.");
                    return;
                }
                String[] choices = new String[L.texts.size()];
                for (int i = 0; i < choices.length; i++) choices[i] = (i + 1) + ": " + L.texts.get(i);
                String sel = (String) JOptionPane.showInputDialog(this, "Choose text to delete", "Delete Text",
                        JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
                if (sel != null) {
                    int idx = Integer.parseInt(sel.substring(0, sel.indexOf(":"))) - 1;
                    L.texts.remove(idx);
                    L.textPoints.remove(idx);
                    L.textColors.remove(idx);
                    L.textSizes.remove(idx);
                    repaint();
                }
            });

            menu.show(button, 0, button.getHeight());
        }

        private void addTextAt(Point p) {
            String input = JOptionPane.showInputDialog("Enter Text:");
            if (input != null && !input.trim().isEmpty()) {
                Layer L = getCurrentLayer();
                L.texts.add(input);
                L.textPoints.add(p);
                L.textColors.add(brushColor);
                L.textSizes.add(24);
                repaint();
            }
            setMode(Mode.NONE);
        }

        // ----- Shapes menu / selection -----
        public void showShapesMenu(Component invoker) {
            JPopupMenu menu = new JPopupMenu();
            ShapeType[] arr = {ShapeType.LINE, ShapeType.CURVE, ShapeType.RECTANGLE, ShapeType.ROUNDED_RECT,
                    ShapeType.SQUARE, ShapeType.OVAL, ShapeType.CIRCLE, ShapeType.TRIANGLE,
                    ShapeType.RIGHT_TRIANGLE, ShapeType.DIAMOND, ShapeType.PENTAGON, ShapeType.HEXAGON,
                    ShapeType.STAR5, ShapeType.STAR6, ShapeType.ARROW_LEFT, ShapeType.ARROW_RIGHT,
                    ShapeType.ARROW_UP, ShapeType.ARROW_DOWN, ShapeType.CALL_OUT, ShapeType.HEART,
                    ShapeType.LIGHTNING, ShapeType.CLOUD};
            for (ShapeType st : arr) {
                JMenuItem item = new JMenuItem(humanName(st));
                item.addActionListener(e -> {
                    currentShape = st;
                    setMode(Mode.SHAPE);
                });
                menu.add(item);
            }
            menu.addSeparator();
            JMenuItem selToggleFill = new JMenuItem("Toggle fill selected");
            selToggleFill.addActionListener(e -> {
                if (selectedShape != null && selLayerIndex >= 0 && selShapeIndex >= 0) {
                    Layer L = layers.get(selLayerIndex);
                    boolean cur = L.shapeFilled.get(selShapeIndex);
                    L.shapeFilled.set(selShapeIndex, !cur);
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "No shape selected.");
                }
            });
            menu.add(selToggleFill);

            JMenuItem resizeItem = new JMenuItem("Resize selected (scale %)");
            resizeItem.addActionListener(e -> {
                if (selectedShape != null && selLayerIndex >= 0 && selShapeIndex >= 0) {
                    String val = JOptionPane.showInputDialog("Enter scale percent (e.g. 150 for 150%):", "100");
                    if (val != null) {
                        try {
                            double p = Double.parseDouble(val) / 100.0;
                            // scale shape about its center
                            Shape sh = selectedShape;
                            Rectangle2D b = sh.getBounds2D();
                            double cx = b.getCenterX();
                            double cy = b.getCenterY();
                            AffineTransform at = AffineTransform.getTranslateInstance(cx, cy);
                            at.scale(p, p);
                            at.translate(-cx, -cy);
                            Shape ns = at.createTransformedShape(sh);
                            layers.get(selLayerIndex).shapes.set(selShapeIndex, ns);
                            selectedShape = ns;
                            repaint();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Invalid number");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "No shape selected.");
                }
            });
            menu.add(resizeItem);

            menu.show(invoker, 0, invoker.getHeight());
        }

        private String humanName(ShapeType st) {
            switch (st) {
                case LINE: return "Line";
                case CURVE: return "Curve";
                case RECTANGLE: return "Rectangle";
                case ROUNDED_RECT: return "Rounded Rectangle";
                case SQUARE: return "Square";
                case OVAL: return "Oval";
                case CIRCLE: return "Circle";
                case TRIANGLE: return "Triangle";
                case RIGHT_TRIANGLE: return "Right Triangle";
                case DIAMOND: return "Diamond";
                case PENTAGON: return "Pentagon";
                case HEXAGON: return "Hexagon";
                case STAR5: return "5-Point Star";
                case STAR6: return "6-Point Star";
                case ARROW_LEFT: return "Left Arrow";
                case ARROW_RIGHT: return "Right Arrow";
                case ARROW_UP: return "Up Arrow";
                case ARROW_DOWN: return "Down Arrow";
                case CALL_OUT: return "Callout (Speech Bubble)";
                case HEART: return "Heart";
                case LIGHTNING: return "Lightning";
                case CLOUD: return "Cloud";
                default: return st.name();
            }
        }

        private void chooseShapeAt(Point p) {
            // choose topmost shape across layers starting from top (highest index)
            selLayerIndex = -1;
            selShapeIndex = -1;
            selectedShape = null;
            for (int li = layers.size() - 1; li >= 0; li--) {
                Layer L = layers.get(li);
                for (int si = L.shapes.size() - 1; si >= 0; si--) {
                    Shape sh = L.shapes.get(si);
                    if (shapeContainsPoint(sh, p, L.shapeSizes.get(si))) {
                        selLayerIndex = li;
                        selShapeIndex = si;
                        selectedShape = sh;
                        selectedShapeColor = L.shapeColors.get(si);
                        selectedShapeFilled = L.shapeFilled.get(si);
                        selectedShapeSize = L.shapeSizes.get(si);
                        // set active layer to that layer for convenience
                        currentLayer = li;
                        return;
                    }
                }
            }
        }

        private boolean shapeContainsPoint(Shape sh, Point p, int strokeWidth) {
            try {
                if (sh instanceof Line2D) {
                    double d = ((Line2D) sh).ptSegDist(p);
                    return d <= strokeWidth;
                } else {
                    if (sh.contains(p)) return true;
                    BasicStroke bs = new BasicStroke(Math.max(1, strokeWidth) + 2);
                    Shape stroked = bs.createStrokedShape(sh);
                    return stroked.contains(p);
                }
            } catch (Exception ex) {
                Rectangle2D b = sh.getBounds2D();
                return b.contains(p);
            }
        }

        private void clearSelection() {
            selLayerIndex = -1;
            selShapeIndex = -1;
            selectedShape = null;
        }

        // ----- Eraser -----
        private void eraseAt(Point p) {
            // Erase only in the current active layer using eraserSize as diameter
            Layer L = getCurrentLayer();
            double radius = Math.max(1, eraserSize / 2.0);

            // Erase freehand points that lie within the circle
            for (int i = L.points.size() - 1; i >= 0; i--) {
                Point pt = L.points.get(i);
                if (pt != null) {
                    if (pt.distance(p) <= radius) {
                        L.points.remove(i);
                        L.pointColors.remove(i);
                        L.pointSizes.remove(i);
                    }
                }
            }

            // Erase shapes that intersect the eraser circle
            Ellipse2D.Double eraserCircle = new Ellipse2D.Double(p.x - radius, p.y - radius, radius * 2, radius * 2);
            for (int i = L.shapes.size() - 1; i >= 0; i--) {
                Shape sh = L.shapes.get(i);
                try {
                    // If the shape's stroked area or filled area intersects the eraser circle, remove it
                    Rectangle2D bounds = sh.getBounds2D();
                    if (bounds.intersects(eraserCircle.getBounds2D())) {
                        // coarse check passed -> do precise check
                        if (sh.intersects(eraserCircle.getBounds2D()) || sh.contains(p) ||
                                shapeContainsPoint(sh, p, L.shapeSizes.get(i))) {
                            L.shapes.remove(i);
                            L.shapeColors.remove(i);
                            L.shapeSizes.remove(i);
                            L.shapeFilled.remove(i);
                        }
                    }
                } catch (Exception ex) {
                    // fallback: remove if bounding box contains point
                    Rectangle2D b = sh.getBounds2D();
                    if (b.contains(p)) {
                        L.shapes.remove(i);
                        L.shapeColors.remove(i);
                        L.shapeSizes.remove(i);
                        L.shapeFilled.remove(i);
                    }
                }
            }

            // Erase text if clicked inside
            for (int i = L.textPoints.size() - 1; i >= 0; i--) {
                Point tp = L.textPoints.get(i);
                FontMetrics fm = getFontMetrics(new Font("Arial", Font.PLAIN, L.textSizes.get(i)));
                Rectangle box = new Rectangle(tp.x, tp.y - fm.getHeight(), fm.stringWidth(L.texts.get(i)), fm.getHeight());
                // check if eraser circle intersects the text box (approx)
                if (eraserCircle.intersects(box)) {
                    L.texts.remove(i);
                    L.textPoints.remove(i);
                    L.textColors.remove(i);
                    L.textSizes.remove(i);
                }
            }

            repaint();
        }

        // ----- Shape building helpers -----
        private Shape buildShape(Point s, Point e, ShapeType t) {
            if (s == null || e == null) return null;
            int x1 = s.x, y1 = s.y, x2 = e.x, y2 = e.y;
            int x = Math.min(x1, x2), y = Math.min(y1, y2);
            int w = Math.abs(x2 - x1), h = Math.abs(y2 - y1);
            if (w == 0) w = 1;
            if (h == 0) h = 1;

            switch (t) {
                case LINE:
                    return new Line2D.Double(x1, y1, x2, y2);
                case CURVE: {
                    Path2D.Double p = new Path2D.Double();
                    p.moveTo(x1, y1);
                    double cx = (x1 + x2) / 2.0;
                    double cy = Math.min(y1, y2) - Math.max(w, h) * 0.3;
                    p.quadTo(cx, cy, x2, y2);
                    return p;
                }
                case RECTANGLE:
                    return new Rectangle2D.Double(x, y, w, h);
                case ROUNDED_RECT:
                    return new RoundRectangle2D.Double(x, y, w, h, Math.min(w, h) * 0.15, Math.min(w, h) * 0.15);
                case SQUARE: {
                    int size = Math.max(w, h);
                    int sx = x1 <= x2 ? x1 : x1 - size;
                    int sy = y1 <= y2 ? y1 : y1 - size;
                    return new Rectangle2D.Double(sx, sy, size, size);
                }
                case OVAL:
                    return new Ellipse2D.Double(x, y, w, h);
                case CIRCLE: {
                    int size = Math.max(w, h);
                    int cx = x1 <= x2 ? x1 : x1 - size;
                    int cy = y1 <= y2 ? y1 : y1 - size;
                    return new Ellipse2D.Double(cx, cy, size, size);
                }
                case TRIANGLE: {
                    Path2D.Double p = new Path2D.Double();
                    int ax = (x1 + x2) / 2;
                    p.moveTo(ax, y1);
                    p.lineTo(x1, y2);
                    p.lineTo(x2, y2);
                    p.closePath();
                    return p;
                }
                case RIGHT_TRIANGLE: {
                    Path2D.Double p = new Path2D.Double();
                    p.moveTo(x1, y1);
                    p.lineTo(x2, y1);
                    p.lineTo(x1, y2);
                    p.closePath();
                    return p;
                }
                case DIAMOND: {
                    Path2D.Double p = new Path2D.Double();
                    double cx = (x1 + x2) / 2.0, cy = (y1 + y2) / 2.0;
                    p.moveTo(cx, y);
                    p.lineTo(x + w, cy);
                    p.lineTo(cx, y + h);
                    p.lineTo(x, cy);
                    p.closePath();
                    return p;
                }
                case PENTAGON:
                case HEXAGON: {
                    int sides = (t == ShapeType.PENTAGON) ? 5 : 6;
                    return regularPolygon(x + w / 2.0, y + h / 2.0, Math.max(w, h) / 2.0, sides);
                }
                case STAR5:
                case STAR6: {
                    int pointsCount = (t == ShapeType.STAR5) ? 5 : 6;
                    return starPolygon(x + w / 2.0, y + h / 2.0, Math.max(w, h) / 2.0, pointsCount);
                }
                case ARROW_LEFT:
                case ARROW_RIGHT:
                case ARROW_UP:
                case ARROW_DOWN:
                    return arrowShape(x, y, w, h, t);
                case CALL_OUT: {
                    double arc = Math.min(w, h) * 0.1;
                    RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(x, y, w, h, arc, arc);
                    Path2D.Double callout = new Path2D.Double(bubble);
                    double tx1 = x + w * 0.15;
                    double ty1 = y + h;
                    double tx2 = tx1 + w * 0.12;
                    double ty2 = ty1 + h * 0.12;
                    callout.moveTo(tx1, ty1);
                    callout.lineTo(tx2, ty2);
                    callout.lineTo(tx1 + w * 0.28, ty1);
                    return callout;
                }
                case HEART: {
                    double cx = x + w / 2.0;
                    Path2D.Double heart = new Path2D.Double();
                    double scale = Math.min(w, h) / 2.0;
                    heart.moveTo(cx, y + h);
                    heart.curveTo(cx - scale * 1.6, y + h * 0.75, x, y + h * 0.4, cx, y + h * 0.35);
                    heart.curveTo(x + w * 0.5, y + h * 0.25, x + w, y + h * 0.4, cx, y + h);
                    heart.closePath();
                    return heart;
                }
                case LIGHTNING: {
                    Path2D.Double bolt = new Path2D.Double();
                    bolt.moveTo(x + w * 0.6, y);
                    bolt.lineTo(x + w * 0.2, y + h * 0.55);
                    bolt.lineTo(x + w * 0.5, y + h * 0.55);
                    bolt.lineTo(x + w * 0.4, y + h);
                    bolt.lineTo(x + w * 0.8, y + h * 0.45);
                    bolt.lineTo(x + w * 0.5, y + h * 0.45);
                    bolt.closePath();
                    return bolt;
                }
                case CLOUD: {
                    Path2D.Double cloud = new Path2D.Double();
                    double rx = w / 5.0;
                    double ry = h / 3.0;
                    double cx1 = x + rx * 1.0;
                    double cx2 = x + rx * 2.2;
                    double cx3 = x + rx * 3.4;
                    double cyc = y + ry;
                    cloud.append(new Ellipse2D.Double(cx1 - rx, cyc - ry, 2 * rx, 2 * ry), false);
                    cloud.append(new Ellipse2D.Double(cx2 - rx, y - ry * 0.2, 2 * rx, 2 * ry), false);
                    cloud.append(new Ellipse2D.Double(cx3 - rx, cyc - ry, 2 * rx, 2 * ry), false);
                    cloud.append(new Rectangle2D.Double(x + rx * 0.5, y + ry, w - rx, ry * 1.2), false);
                    return cloud;
                }
                default:
                    return null;
            }
        }

        private Shape regularPolygon(double cx, double cy, double radius, int sides) {
            Path2D.Double p = new Path2D.Double();
            for (int i = 0; i < sides; i++) {
                double a = Math.toRadians(-90 + 360.0 * i / sides);
                double px = cx + radius * Math.cos(a);
                double py = cy + radius * Math.sin(a);
                if (i == 0) p.moveTo(px, py);
                else p.lineTo(px, py);
            }
            p.closePath();
            return p;
        }

        private Shape starPolygon(double cx, double cy, double outerR, int points) {
            Path2D.Double p = new Path2D.Double();
            double innerR = outerR * 0.45;
            for (int i = 0; i < points * 2; i++) {
                double a = Math.toRadians(-90 + 360.0 * i / (points * 2));
                double r = (i % 2 == 0) ? outerR : innerR;
                double px = cx + r * Math.cos(a);
                double py = cy + r * Math.sin(a);
                if (i == 0) p.moveTo(px, py);
                else p.lineTo(px, py);
            }
            p.closePath();
            return p;
        }

        private Shape arrowShape(int x, int y, int w, int h, ShapeType t) {
            Path2D.Double p = new Path2D.Double();
            switch (t) {
                case ARROW_LEFT:
                    p.moveTo(x + w, y + 0.2 * h);
                    p.lineTo(x + 0.35 * w, y + 0.2 * h);
                    p.lineTo(x + 0.35 * w, y);
                    p.lineTo(x, y + 0.5 * h);
                    p.lineTo(x + 0.35 * w, y + h);
                    p.lineTo(x + 0.35 * w, y + 0.8 * h);
                    p.lineTo(x + w, y + 0.8 * h);
                    p.closePath();
                    return p;
                case ARROW_RIGHT:
                    p.moveTo(x, y + 0.2 * h);
                    p.lineTo(x + w - 0.35 * w, y + 0.2 * h);
                    p.lineTo(x + w - 0.35 * w, y);
                    p.lineTo(x + w, y + 0.5 * h);
                    p.lineTo(x + w - 0.35 * w, y + h);
                    p.lineTo(x + w - 0.35 * w, y + 0.8 * h);
                    p.lineTo(x, y + 0.8 * h);
                    p.closePath();
                    return p;
                case ARROW_UP:
                    p.moveTo(x + 0.2 * w, y + h);
                    p.lineTo(x + 0.2 * w, y + 0.35 * h);
                    p.lineTo(x, y + 0.35 * h);
                    p.lineTo(x + 0.5 * w, y);
                    p.lineTo(x + w, y + 0.35 * h);
                    p.lineTo(x + 0.8 * w, y + 0.35 * h);
                    p.lineTo(x + 0.8 * w, y + h);
                    p.closePath();
                    return p;
                case ARROW_DOWN:
                    p.moveTo(x + 0.2 * w, y);
                    p.lineTo(x + 0.2 * w, y + 0.65 * h);
                    p.lineTo(x, y + 0.65 * h);
                    p.lineTo(x + 0.5 * w, y + h);
                    p.lineTo(x + w, y + 0.65 * h);
                    p.lineTo(x + 0.8 * w, y + 0.65 * h);
                    p.lineTo(x + 0.8 * w, y);
                    p.closePath();
                    return p;
                default:
                    return null;
            }
        }

        // ----- paintComponent: draws all layers, shapes (outline or filled), freehand strokes, text
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            // Smooth rendering
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // draw all layers in order (0 is bottom)
            for (int li = 0; li < layers.size(); li++) {
                Layer L = layers.get(li);

                // shapes
                for (int si = 0; si < L.shapes.size(); si++) {
                    Shape sh = L.shapes.get(si);
                    Color c = L.shapeColors.get(si);
                    Integer sz = L.shapeSizes.get(si);
                    boolean filled = L.shapeFilled.get(si);
                    if (sh == null || c == null || sz == null) continue;
                    g2.setStroke(new BasicStroke(sz, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    if (filled) {
                        g2.setColor(c);
                        try {
                            g2.fill(sh);
                            // draw border
                            g2.setColor(c.darker());
                            g2.draw(sh);
                        } catch (Exception ex) {
                            g2.draw(sh);
                        }
                    } else {
                        g2.setColor(c);
                        g2.draw(sh);
                    }
                }

                // freehand strokes: draw segments separated by nulls
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 1; i < L.points.size(); i++) {
                    Point p1 = L.points.get(i - 1);
                    Point p2 = L.points.get(i);
                    Color col = L.pointColors.get(i);
                    Integer s = L.pointSizes.get(i);
                    if (p1 == null || p2 == null) continue;
                    if (col == null || s == null) continue;
                    g2.setStroke(new BasicStroke(s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(col);
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }

                // single dots
                for (int i = 0; i < L.points.size(); i++) {
                    Point p = L.points.get(i);
                    if (p == null) continue;
                    boolean prev = i > 0 && L.points.get(i - 1) != null;
                    boolean next = i < L.points.size() - 1 && L.points.get(i + 1) != null;
                    if (!prev && !next) {
                        Integer s = L.pointSizes.get(i);
                        Color c = L.pointColors.get(i);
                        if (s != null && c != null) {
                            g2.setColor(c);
                            g2.fillOval(p.x - s / 2, p.y - s / 2, s, s);
                        }
                    }
                }

                // text
                for (int i = 0; i < L.texts.size(); i++) {
                    g2.setColor(L.textColors.get(i));
                    g2.setFont(new Font("Arial", Font.PLAIN, L.textSizes.get(i)));
                    Point tp = L.textPoints.get(i);
                    g2.drawString(L.texts.get(i), tp.x, tp.y);
                }
            }

            // draw preview shape (during drag)
            if (previewShape != null) {
                g2.setStroke(new BasicStroke(Math.max(1, localBrushSize), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(brushColor);
                g2.draw(previewShape);
                if (fillMode) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                    g2.fill(previewShape);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }

            // highlight selected shape
            if (selectedShape != null) {
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f,
                        new float[]{4f, 4f}, 0f));
                g2.setColor(Color.BLUE);
                g2.draw(selectedShape.getBounds2D());
            }

            g2.dispose();
        }

        // ----- utility setters -----
        public void setBrushSize(int size) {
            localBrushSize = size;
        }

        public void setBrushColor(Color color) {
            brushColor = color;
        }

        public void setMode(Mode m) {
            mode = m;
            // toggle UI (caller side handles toolbar enable/disable)
            if (m == Mode.BRUSH) {
                clearSelection();
            }
            repaint();
        }

        public void setFillMode(boolean f) {
            fillMode = f;
        }

        // expose setter so main UI can change eraser size
        public void setEraserSize(int size) {
            // Keep brush and eraser sizes independent if desired.
            // We use the outer class variable `eraserSize` (declared in PaintApp)
            // but for immediate local checks you could also set a local field here.
            // No additional action required here because eraseAt uses the outer eraserSize.
        }

        public void clear() {
            saveSnapshot();
            for (Layer L : layers) {
                L.points.clear();
                L.pointColors.clear();
                L.pointSizes.clear();
                L.shapes.clear();
                L.shapeColors.clear();
                L.shapeSizes.clear();
                L.shapeFilled.clear();
                L.texts.clear();
                L.textPoints.clear();
                L.textColors.clear();
                L.textSizes.clear();
            }
            clearSelection();
            repaint();
        }
    }
}
