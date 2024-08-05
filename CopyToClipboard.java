import java.awt.datatransfer.*;
import java.awt.Toolkit;
import java.io.*;

public class ClipboardExample {

    // Method to copy content to clipboard
    public static void copyToClipboard(String content) {
        StringSelection stringSelection = new StringSelection(content);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    // Method to paste content from clipboard
    public static String pasteFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            Transferable transferable = clipboard.getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        // Example content to copy
        String contentToCopy = "Hello, this is the content to copy!";
        System.out.println("Copying to clipboard: " + contentToCopy);
        copyToClipboard(contentToCopy);

        // Pasting the content from clipboard
        String pastedContent = pasteFromClipboard();
        System.out.println("Pasted from clipboard: " + pastedContent);
    }
}
