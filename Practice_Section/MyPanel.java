import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {
    private int x = 50;

    public void move() {
        x += 10;
        repaint(); // Tell Swing to redraw the panel
    }

    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.fillOval(x, 50, 30, 30); // Draw something that changes
    }
}
