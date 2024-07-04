import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class GlobalMouseListenerExample implements NativeMouseInputListener {

    public void nativeMouseClicked(NativeMouseEvent e) {
        if (e.getButton() == NativeMouseEvent.BUTTON3) { // BUTTON3 is the middle mouse button
            System.out.println("Mouse wheel button pressed");
        }
    }

    public void nativeMousePressed(NativeMouseEvent e) {
        // Do nothing
    }

    public void nativeMouseReleased(NativeMouseEvent e) {
        // Do nothing
    }

    public void nativeMouseMoved(NativeMouseEvent e) {
        // Do nothing
    }

    public void nativeMouseDragged(NativeMouseEvent e) {
        // Do nothing
    }

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        GlobalScreen.addNativeMouseListener(new GlobalMouseListenerExample());
    }
}
