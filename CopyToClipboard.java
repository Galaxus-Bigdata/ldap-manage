import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.awt.event.KeyEvent;

public class ClipboardAndRobotExample {

    // Method to copy content to clipboard
    public static void copyToClipboard(String content) {
        StringSelection stringSelection = new StringSelection(content);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    // Method to simulate paste operation using Robot
    public static void pasteFromClipboard() {
        try {
            Robot robot = new Robot();
            
            // Simulate Ctrl+V keystroke (Cmd+V on Mac)
            robot.keyPress(KeyEvent.VK_CONTROL); // Change to KeyEvent.VK_META for Mac
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL); // Change to KeyEvent.VK_META for Mac
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Example content to copy
        String contentToCopy = "Hello, this is the content to copy!";
        System.out.println("Copying to clipboard: " + contentToCopy);
        copyToClipboard(contentToCopy);

        // Wait a few seconds to give you time to focus on another application
        System.out.println("You have 5 seconds to focus on the target application...");
        Thread.sleep(5000);

        // Simulate pasting the content
        pasteFromClipboard();
    }
}
