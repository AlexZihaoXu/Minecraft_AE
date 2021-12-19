package site.alex_xu.minecraft.client.control;

import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.utils.Window;
import site.alex_xu.minecraft.core.MinecraftAECore;

import static org.lwjgl.glfw.GLFW.*;

public class FirstPersonController extends MinecraftAECore {
    public Window window;
    private boolean locked = false;
    private Camera camera;

    public FirstPersonController(Window window, Camera camera) {
        this.window = window;
        this.camera = camera;
        window.registerMouseButtonChangeCallback(this::onMouseButtonChange);
        window.registerFocusChangeCallback(this::onFocusChange);
        window.registerMouseMoveCallback(this::onMouseMove);
    }

    public void setLocked(boolean locked) {
        if (locked != this.locked) {
            this.locked = locked;
            if (locked) {
                glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                glfwSetCursorPos(window.getWindowHandle(), 0, 0);
            } else {
                glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                glfwSetCursorPos(window.getWindowHandle(), window.getWidth() / 2f, window.getHeight() / 2f);
            }
        }
    }

    public void onMouseButtonChange(int button, boolean pressed) {
        if (button == 0 && pressed) {
            setLocked(true);
        }
    }

    public void onMouseMove(double x, double y) {
        if (locked) {
            camera.yaw += x * 0.01f;
            camera.pitch -= y * 0.01f;
            glfwSetCursorPos(window.getWindowHandle(), 0, 0);

            camera.yaw %= Math.PI * 2;
            camera.pitch = Math.min(Math.PI / 2, Math.max(-Math.PI / 2, camera.pitch));
        }
    }

    public void onFocusChange(boolean focused) {
        if (!focused) {
            setLocked(false);
        }
    }

    public void dispose() {
        window.removeMouseButtonChangeCallback(this::onMouseButtonChange);
        window.removeMouseMoveCallback(this::onMouseMove);
        window.removeFocusChangeCallback(this::onFocusChange);
    }
}
