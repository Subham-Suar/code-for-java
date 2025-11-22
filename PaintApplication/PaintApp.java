import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class PaintApp extends JFrame {

    private DrawArea drawArea = new DrawArea();
    private JButton clearBtn = new JButton("Clear");
    private JButton colorBtn = new JButton("Color");
    private JButton thickButton = new JButton("🖌");
    private Color currentColor = Color.BLACK;
    private int brushSize = 3;


    public PaintApp() {
        setTitle("Java Paint Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        

        JPanel buttons = new JPanel();
        buttons.add(colorBtn); 
        buttons.add(clearBtn);
        buttons.add(thickButton);

        add(buttons, BorderLayout.NORTH);
        add(drawArea, BorderLayout.CENTER);

        // Color Chooser Action
        colorBtn.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(null, "Choose a color", currentColor);
            if (selected != null) {
                currentColor = selected;
                drawArea.setBrushColor(currentColor);
            }
        });

        // Clear Button Action
        clearBtn.addActionListener(e -> drawArea.clear());

        // Thickness Button Action
thickButton.addActionListener(e -> {
    String[] sizes = {"2", "4", "6", "8", "12", "16"};
    String selected = (String) JOptionPane.showInputDialog(
            null,
            "Select Brush Thickness",
            "Brush Size",
            JOptionPane.PLAIN_MESSAGE,
            null,
            sizes,
            String.valueOf(brushSize)
    );

    if (selected != null) {
        brushSize = Integer.parseInt(selected);
        drawArea.setBrushSize(brushSize);
    }
});

        setVisible(true);
    }

     public static void main(String[] args) {
        SwingUtilities.invokeLater(PaintApp::new);
    }

    // Inner class for the drawing area
    class DrawArea extends JPanel {
        private ArrayList<Point> points = new ArrayList<>();
        private ArrayList<Color> pointColors = new ArrayList<>();

        public DrawArea() {
            setBackground(Color.WHITE);
             setDoubleBuffered(true);

            addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent e) {
                    points.add(e.getPoint());
                    pointColors.add(currentColor);
                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    points.add(e.getPoint());
                    pointColors.add(currentColor);
                    repaint();
                }
            });
        }
        private ArrayList<Integer> pointSizes = new ArrayList<>();
private int localBrushSize = 3;

public void setBrushSize(int size) {
    localBrushSize = size;
}

        public void setBrushColor(Color color) {
            currentColor = color;
        }

        public void clear() {
            points.clear();
            pointColors.clear();
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 1; i < points.size(); i++) {
                if (points.get(i) != null && points.get(i - 1) != null) {
                    g.setColor(pointColors.get(i));
                    g.drawLine(points.get(i - 1).x, points.get(i - 1).y,
                               points.get(i).x, points.get(i).y);
                }
            }
        }
    }
   
}

 
