package site.alex_xu.minecraft.client.control;

import org.joml.Vector3f;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.utils.Window;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;

import static org.lwjgl.glfw.GLFW.*;

public class FirstPersonController extends MinecraftAECore implements Tickable {
    public Window window;
    private boolean locked = false;
    private final Camera camera;
    private Vector3f velocity = new Vector3f();

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
            camera.yaw += x * 0.0024f;
            camera.pitch -= y * 0.0024f;
            glfwSetCursorPos(window.getWindowHandle(), 0, 0);

            camera.yaw %= Math.PI * 2;
            camera.pitch = Math.min(Math.PI / 2 - 1 / 1e4, Math.max(-Math.PI / 2 + 1 / 1e4, camera.pitch));
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

    @Override
    public void onTick(double deltaTime) {
        double speed = 100 * deltaTime;
        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_W) == GLFW_PRESS) {
            velocity.x += Math.cos(camera.yaw) * speed;
            velocity.z += Math.sin(camera.yaw) * speed;
        }
        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_S) == GLFW_PRESS) {
            velocity.x -= Math.cos(camera.yaw) * speed;
            velocity.z -= Math.sin(camera.yaw) * speed;
        }
        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_A) == GLFW_PRESS) {
            velocity.x -= Math.cos(camera.yaw + Math.PI / 2) * speed;
            velocity.z -= Math.sin(camera.yaw + Math.PI / 2) * speed;
        }
        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_D) == GLFW_PRESS) {
            velocity.x += Math.cos(camera.yaw + Math.PI / 2) * speed;
            velocity.z += Math.sin(camera.yaw + Math.PI / 2) * speed;
        }
        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_SPACE) == GLFW_PRESS) {
            velocity.y += speed;
        }
        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            velocity.y -= speed;
        }
        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            setLocked(false);
        }
        float dt = (float) Math.min(1 / 20f, deltaTime);
        velocity.x -= velocity.x * dt * 10;
        velocity.y -= velocity.y * dt * 10;
        velocity.z -= velocity.z * dt * 10;

        camera.position.x += velocity.x * deltaTime;
        camera.position.y += velocity.y * deltaTime;
        camera.position.z += velocity.z * deltaTime;
    }
}
