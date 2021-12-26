package site.alex_xu.minecraft.client.control;

import org.joml.*;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.screen.world.WorldScreen;
import site.alex_xu.minecraft.client.utils.Window;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.entity.PlayerEntity;
import site.alex_xu.minecraft.server.world.World;

import java.lang.Math;
import java.util.HashSet;

import static org.joml.Math.*;
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
        if (key == GLFW_KEY_SPACE && action == GLFW_PRESS && !entity.inAir() && !entity.isIgnoreGravity()) {
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
        Vector3i[] blockPos = rayCast();
        if (button == 0 && pressed) {
            if (locked) {
                if (blockPos[0] != null) {
                    world.setBlock(Blocks.AIR, blockPos[0].x, blockPos[0].y, blockPos[0].z);
                }
            }
            setLocked(true);
        }
        if (button == 1 && pressed) {
            if (locked) {
                if (blockPos[1] != null) {
                    world.setBlock(Blocks.STONE, blockPos[1].x, blockPos[1].y, blockPos[1].z);
                }
            }
            setLocked(true);
        }
    }

    public Vector3i[] rayCast() {
        int distance = 5;
        Vector3f pos = new Vector3f(camera.position);
        WorldScreen.debugInfo = "Block: ???";
        HashSet<Vector3i> blockPoses = new HashSet<>();
        Vector3f front = camera.getFront();
        for (int i = 0; i < distance; i += 1) {
            Vector3i blockPos = new Vector3i(world.blockXOf(pos.x), world.blockYOf(pos.y), world.blockZOf(pos.z));
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        Block block = world.getBlock(blockPos.x + x, blockPos.y + y, blockPos.z + z);
                        if (block != null && !block.settings().material.isReplaceable()) {
                            blockPoses.add(new Vector3i(blockPos.x + x, blockPos.y + y, blockPos.z + z));
                        }
                    }
                }
            }
            pos.add(new Vector3f(front));
        }

        Vector3i closestBlock = null;
        float nearest = Float.POSITIVE_INFINITY;
        Vector2f nearFar = new Vector2f();
        for (Vector3i blockPos : blockPoses) {
            if (Intersectionf.intersectRayAab(camera.position, front, new Vector3f(blockPos), new Vector3f(blockPos).add(1, 1, 1), nearFar)) {
                if (nearFar.x < nearest) {
                    nearest = nearFar.x;
                    closestBlock = blockPos;
                }
            }
        }
        Vector3i placeLocation = null;
        float scale = 1 / 128f;

        if (closestBlock != null) {
            nearest = Float.POSITIVE_INFINITY;
            if (Intersectionf.intersectRayAab(camera.position, front, new Vector3f(closestBlock).add(0, -scale / 2, 0), new Vector3f(closestBlock).add(1, scale, 1), nearFar)) { // Down
                if (nearFar.x < nearest) {
                    nearest = nearFar.x;
                    placeLocation = new Vector3i(closestBlock).add(0, -1, 0);
                }
            }
            if (Intersectionf.intersectRayAab(camera.position, front, new Vector3f(closestBlock).add(0, 1 - scale / 2, 0), new Vector3f(closestBlock).add(1, 1 + scale, 1), nearFar)) { // Up
                if (nearFar.x < nearest) {
                    nearest = nearFar.x;
                    placeLocation = new Vector3i(closestBlock).add(0, 1, 0);
                }
            }
            if (Intersectionf.intersectRayAab(camera.position, front, new Vector3f(closestBlock).add(-scale / 2, 0, 0), new Vector3f(closestBlock).add(scale, 1, 1), nearFar)) { // West
                if (nearFar.x < nearest) {
                    nearest = nearFar.x;
                    placeLocation = new Vector3i(closestBlock).add(-1, 0, 0);
                }
            }
            if (Intersectionf.intersectRayAab(camera.position, front, new Vector3f(closestBlock).add(1 - scale / 2, 0, 0), new Vector3f(closestBlock).add(1 + scale, 1, 1), nearFar)) { // East
                if (nearFar.x < nearest) {
                    nearest = nearFar.x;
                    placeLocation = new Vector3i(closestBlock).add(1, 0, 0);
                }
            }
            if (Intersectionf.intersectRayAab(camera.position, front, new Vector3f(closestBlock).add(0, 0, -scale / 2), new Vector3f(closestBlock).add(1, 1, scale), nearFar)) { // South
                if (nearFar.x < nearest) {
                    nearest = nearFar.x;
                    placeLocation = new Vector3i(closestBlock).add(0, 0, -1);
                }
            }
            if (Intersectionf.intersectRayAab(camera.position, front, new Vector3f(closestBlock).add(0, 0, 1 - scale / 2), new Vector3f(closestBlock).add(1, 1, 1 + scale), nearFar)) { // North
                if (nearFar.x < nearest) {
                    placeLocation = new Vector3i(closestBlock).add(0, 0, 1);
                }
            }
        }

        if (closestBlock != null) {
            WorldScreen.debugInfo = "Block: " + closestBlock.x + " / " + closestBlock.y + " / " + closestBlock.z + " [" + placeLocation + "]";

        }
        return new Vector3i[]{
                closestBlock,
                placeLocation
        };
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
            float speed = (float) (43f * deltaTime) * 4;
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_W) == GLFW_PRESS) {
                entity.velocity().x += (float) (cos(camera.yaw) * speed);
                entity.velocity().z += (float) (sin(camera.yaw) * speed);
            }
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_S) == GLFW_PRESS) {
                entity.velocity().x += (float) (-cos(camera.yaw) * speed);
                entity.velocity().z += (float) (-sin(camera.yaw) * speed);
            }
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_A) == GLFW_PRESS) {
                entity.velocity().x += (float) (-cos(camera.yaw + Math.PI / 2) * speed);
                entity.velocity().z += (float) (-sin(camera.yaw + Math.PI / 2) * speed);
            }
            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_D) == GLFW_PRESS) {
                entity.velocity().x += (float) (cos(camera.yaw + Math.PI / 2) * speed);
                entity.velocity().z += (float) (sin(camera.yaw + Math.PI / 2) * speed);
            }

            if (entity.isIgnoreGravity()) {
                if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_SPACE) == GLFW_PRESS) {
                    entity.velocity().y += speed;
                }
                if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
                    entity.velocity().y -= speed;
                }
            }

            if (glfwGetKey(window.getWindowHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                setLocked(false);
            }
        }

    }

}
