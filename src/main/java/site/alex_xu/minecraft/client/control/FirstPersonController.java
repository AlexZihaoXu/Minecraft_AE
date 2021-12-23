package site.alex_xu.minecraft.client.control;

import org.joml.Vector3f;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.screen.world.WorldScreen;
import site.alex_xu.minecraft.client.utils.Window;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.world.World;

import static org.lwjgl.glfw.GLFW.*;

public class FirstPersonController extends MinecraftAECore implements Tickable {
    public Window window;
    private boolean locked = false;
    private final Camera camera;
    private Vector3f velocity = new Vector3f();
    private World world;
    private boolean onGround = false;

    public FirstPersonController(Window window, Camera camera, World world) {
        this.window = window;
        this.world = world;
        this.camera = camera;
        window.registerMouseButtonChangeCallback(this::onMouseButtonChange);
        window.registerFocusChangeCallback(this::onFocusChange);
        window.registerMouseMoveCallback(this::onMouseMove);
        window.registerKeyChangeCallback(this::onKeyChange);
    }

    private void onKeyChange(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
            if (onGround) {
                velocity.y += 8.5f;
            }
        }
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
        if (locked) {
            double speed = 100 * deltaTime * (onGround ? 1 : 0.4);
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
//        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_SPACE) == GLFW_PRESS) {
//            velocity.y += speed;
//        }
//        if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
//            velocity.y -= speed;
//        }
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                setLocked(false);
            }
        }
        float dt = (float) Math.min(1 / 20f, deltaTime);
        velocity.x -= velocity.x * dt * 20 * (onGround ? 1 : 0.4);
        velocity.z -= velocity.z * dt * 20 * (onGround ? 1 : 0.4);

        handleCollisions();
        camera.position.x += velocity.x * dt;
        camera.position.y += velocity.y * dt;
        camera.position.z += velocity.z * dt;

        velocity.y -= dt * 32;
        velocity.y = Math.max(velocity.y, -100);

    }

    public void handleCollisions() {
        Block block;
        int bx = (int) Math.round(camera.position.x - 0.5);
        int by = (int) Math.floor(camera.position.y);
        int bz = (int) Math.round(camera.position.z - 0.5);
        block = world.getBlock(bx, by - 2, bz);
        if (block != null && block.settings().material.blocksMovement()) {
            if (camera.position.y < by + 0.65f) {
                camera.position.y = by + 0.65f;
                velocity.y = Math.max(0, velocity.y);
                onGround = true;
            }
        } else {
            onGround = false;
        }

        WorldScreen.debugInfo = "Block Pos: " + bx + " / " + by + " / " + bz;

        for (int i = 0; i < 2; i++) {
            block = world.getBlock(bx + 1, by - i, bz);
            if (block != null && block.settings().material.blocksMovement()) {
                camera.position.x = Math.min(camera.position.x, bx + 0.6f);
            }
            block = world.getBlock(bx - 1, by - i, bz);
            if (block != null && block.settings().material.blocksMovement()) {
                camera.position.x = Math.max(camera.position.x, bx + 0.4f);
            }
            block = world.getBlock(bx, by - i, bz + 1);
            if (block != null && block.settings().material.blocksMovement()) {
                camera.position.z = Math.min(camera.position.z, bz + 0.6f);
            }
            block = world.getBlock(bx, by - i, bz - 1);
            if (block != null && block.settings().material.blocksMovement()) {
                camera.position.z = Math.max(camera.position.z, bz + 0.4f);
            }
        }

    }
}
