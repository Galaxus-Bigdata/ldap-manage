import javax.swing.*;
import java.awt.event.*;

public class MouseWheelButtonPress {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Mouse Wheel Button Press Example");
        JPanel panel = new JPanel();

        // Add a mouse listener to the panel
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Check if the mouse button pressed is the middle button (mouse wheel button)
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    System.out.println("Mouse wheel button pressed");
                }
            }
        });

        frame.add(panel);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
