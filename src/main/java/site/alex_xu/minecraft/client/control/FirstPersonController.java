package site.alex_xu.minecraft.client.control;

import org.joml.Vector3f;
import org.joml.Vector3i;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.screen.world.WorldScreen;
import site.alex_xu.minecraft.client.utils.Window;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.entity.PlayerEntity;
import site.alex_xu.minecraft.server.world.World;

import static org.lwjgl.glfw.GLFW.*;

public class FirstPersonController extends MinecraftAECore implements Tickable {
    public Window window;
    private boolean locked = false;
    private final Camera camera;
    private World world;
    private PlayerEntity entity;

    public FirstPersonController(Window window, Camera camera, World world, PlayerEntity entity) {
        this.window = window;
        this.world = world;
        this.camera = camera;
        this.entity = entity;
        window.registerMouseButtonChangeCallback(this::onMouseButtonChange);
        window.registerFocusChangeCallback(this::onFocusChange);
        window.registerMouseMoveCallback(this::onMouseMove);
        window.registerKeyChangeCallback(this::onKeyChange);
    }

    private void onKeyChange(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_SPACE && action == GLFW_PRESS && !entity.inAir()) {
            entity.velocity().y = 9.5f;
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
            Vector3i blockPos = rayCast();
            if (blockPos != null) {
                world.setBlock(Blocks.AIR, blockPos.x, blockPos.y, blockPos.z);
            }
        }
    }

    public Vector3i rayCast() {
        int distance = 5;
        Vector3f pos = new Vector3f(camera.position);
        WorldScreen.debugInfo = "Block: ???";
        float precision = 0.01f;
        for (float i = 0; i < distance; i += precision) {
            Vector3i blockPos = new Vector3i(world.blockXOf(pos.x), world.blockYOf(pos.y), world.blockZOf(pos.z));
            Block block = world.getBlock(blockPos.x, blockPos.y, blockPos.z);
            if (block != null && !block.settings().material.isReplaceable()) {
                WorldScreen.debugInfo = "Block: " + blockPos.x + " / " + blockPos.y + " / " + blockPos.z;
                return blockPos;
            }
            pos.add(new Vector3f(camera.getFront().mul(precision)));
        }
        return null;
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
        camera.position.set(
                entity.position().x,
                entity.position().y + 1.6f,
                entity.position().z
        );
        if (locked) {
            float speed = (float) (43f * deltaTime);
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_W) == GLFW_PRESS) {
                entity.velocity().x += (float) (Math.cos(camera.yaw) * speed);
                entity.velocity().z += (float) (Math.sin(camera.yaw) * speed);
            }
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_S) == GLFW_PRESS) {
                entity.velocity().x += (float) (-Math.cos(camera.yaw) * speed);
                entity.velocity().z += (float) (-Math.sin(camera.yaw) * speed);
            }
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_A) == GLFW_PRESS) {
                entity.velocity().x += (float) (-Math.cos(camera.yaw + Math.PI / 2) * speed);
                entity.velocity().z += (float) (-Math.sin(camera.yaw + Math.PI / 2) * speed);
            }
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_D) == GLFW_PRESS) {
                entity.velocity().x += (float) (Math.cos(camera.yaw + Math.PI / 2) * speed);
                entity.velocity().z += (float) (Math.sin(camera.yaw + Math.PI / 2) * speed);
            }

            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                setLocked(false);
            }
        }

    }

}
