// import javax.swing.JFrame; // JFrame is a class here 

// class Paint {
//     public Paint(){
//         JFrame frame = new JFrame("my first frame");
//         frame.setSize(400,800);
//         frame.setVisible(true);
//          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//     }

//     public static void main(String[] args) {
//         Paint p = new Paint();
//     }
// }
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

class Paint extends JFrame {
    //private DrawArea drawArea = new DrawArea();
    private JButton colorBtn = new JButton("color");
    private JButton clearBtn = new JButton("clear");
    private Color currentColor = Color.BLACK;

    public Paint() {
        setTitle("Java Paint Application");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setVisible(true);

        JPanel buttons = new JPanel();
        buttons.add(colorBtn);
        // buttons.add(clearBtn);

        add(buttons , BorderLayout.NORTH);
    }
    public static void main(String[]args){
        new Paint();
    }
}
