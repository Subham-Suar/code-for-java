import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Paint extends JFrame {

    private DrawArea drawArea = new DrawArea();
    private JButton clearBtn = new JButton("Clear");
    private JButton colorBtn = new JButton("Color");
    private Color currentColor = Color.BLACK;

    public Paint() {
        setTitle("Java Paint Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel buttons = new JPanel();
        buttons.add(colorBtn); 
        buttons.add(clearBtn);

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

        setVisible(true);
    }
    public static void main(String[]args){
        new Paint();
    }
}
