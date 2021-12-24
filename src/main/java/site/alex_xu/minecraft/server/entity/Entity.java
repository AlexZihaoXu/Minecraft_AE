package site.alex_xu.minecraft.server.entity;

import org.joml.Vector3f;
import site.alex_xu.minecraft.client.MinecraftClient;
import site.alex_xu.minecraft.client.render.GameObjectRenderer;
import site.alex_xu.minecraft.client.screen.world.WorldScreen;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.collision.Hitbox;
import site.alex_xu.minecraft.server.world.World;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class Entity extends MinecraftAECore implements Tickable {
    protected Hitbox hitbox;
    protected Vector3f velocity = new Vector3f();
    protected World world;


    public Entity(World world, Hitbox hitbox) {
        this.hitbox = hitbox;
        this.world = world;

    }

    // Getters

    public World getWorld() {
        return world;
    }

    // Setters

    public Entity setPosition(float x, float y, float z) {
        position().set(x, y, z);
        return this;
    }

    public Entity setWorld(World world) {
        this.world = world;
        return this;
    }

    //

    public Vector3f position() {
        return hitbox.getPosition();
    }

    public Hitbox hitbox() {
        return hitbox;
    }

    @Override
    public void onTick(double deltaTime) {
        onGravityTick(deltaTime);
    }

    public void onGravityTick(double dt) {
//        velocity.y -= dt * 9.8f / 100f;
//        position().y += velocity.y * dt;

        var objectRenderer = new GameObjectRenderer(MinecraftClient.getInstance().getWindow());
        var camera = ((WorldScreen) MinecraftClient.getInstance().getScreenManager().get(0)).getCamera();
        var baseRect = new Rectangle2D.Double(position().x - hitbox().width() / 2, position().z - hitbox().width() / 2, hitbox().width(), hitbox().width());
        var sideRectX = new Rectangle2D.Double(position().z - hitbox.width() / 2, position().y + 0.05f, hitbox().width(), hitbox().height() - 0.05f);
        var sideRectZ = new Rectangle2D.Double(position().x - hitbox.width() / 2, position().y + 0.05f, hitbox().width(), hitbox().height() - 0.05f);

        Vector3f motionVec = new Vector3f();
        Vector3f newPos = null;


        // Up Down
        for (int x = world.blockXOf((float) baseRect.getMinX()); x <= world.blockXOf((float) baseRect.getMaxX()); x++) {
            for (int z = world.blockZOf((float) baseRect.getMinY()); z <= world.blockZOf((float) baseRect.getMaxY()); z++) {
                Block block = world.getBlock(x, world.blockYOf(position().y - 0.5f), z);
                if (block.settings().material.blocksMovement()) {
                    if (position().y < world.blockYOf(position().y - 0.5f) + 1) {
                        position().y = Math.max(position().y, world.blockYOf(position().y - 0.5f) + 1);
                        velocity.y = Math.max(0, velocity.y);
                    }
                }
                block = world.getBlock(x, world.blockYOf(position().y + 0.5f + hitbox().height()), z);
                if (block.settings().material.blocksMovement()) {
                    if (position().y + hitbox().height() > world.blockYOf(position().y + 0.5f + hitbox().height())) {
                        position().y = Math.min(position().y, world.blockYOf(position().y + 0.5f + hitbox().height()) - hitbox.height());
                        velocity.y = Math.min(0, velocity.y);
                    }
                }
            }
        }

        // X-Sides
        for (int z = world.blockZOf((float) sideRectX.getMinX()); z <= world.blockZOf((float) sideRectX.getMaxX()); z++) {
            for (int y = world.blockYOf((float) sideRectX.getMinY()); y <= world.blockYOf((float) sideRectX.getMaxY()); y++) {
                Block block = world.getBlock(world.blockXOf(position().x + hitbox().width() / 2), y, z);
                if (block.settings().material.blocksMovement()) {
                    if (position().x > world.blockXOf(position().x + hitbox().width() / 2) - hitbox().width() / 2) {
                        position().x = Math.min(position().x, world.blockXOf(position().x + hitbox().width() / 2) - hitbox().width() / 2);
                    }
                }
                block = world.getBlock(world.blockXOf(position().x - hitbox().width() / 2), y, z);
                if (block.settings().material.blocksMovement()) {
                    if (position().x < world.blockXOf(position().x - hitbox().width() / 2) + hitbox().width() / 2 + 1) {
                        position().x = Math.max(position().x, world.blockXOf(position().x - hitbox().width() / 2) + hitbox().width() / 2 + 1);
                    }
                }
            }
        }

        // Y-Sides
        for (int x = world.blockXOf((float) sideRectZ.getMinX()); x <= world.blockXOf((float) sideRectZ.getMaxX()); x++) {
            for (int y = world.blockYOf((float) sideRectZ.getMinY()); y <= world.blockYOf((float) sideRectZ.getMaxY()); y++) {
                Block block = world.getBlock(x, y, world.blockZOf(position().z + hitbox().width() / 2));
                if (block.settings().material.blocksMovement()) {
                    if (position().z > world.blockXOf(position().z + hitbox().width() / 2) - hitbox().width() / 2) {
                        position().z = Math.min(position().z, world.blockXOf(position().z + hitbox().width() / 2) - hitbox().width() / 2);
                    }
                }
                block = world.getBlock(x, y, world.blockXOf(position().z - hitbox().width() / 2));
                if (block.settings().material.blocksMovement()) {
                    if (position().z < world.blockXOf(position().z - hitbox().width() / 2) + hitbox().width() / 2 + 1) {
                        position().z = Math.max(position().z, world.blockXOf(position().z - hitbox().width() / 2) + hitbox().width() / 2 + 1);
                    }
                }
            }
        }

        WorldScreen.debugInfo = "Block: " + Blocks.nameOf(world.getBlock(baseRect.getMinX(), position().y + 0.5f + hitbox.height(), baseRect.getMinY()));


        dt *= 2;
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_UP) == GLFW_PRESS) {
            position().z -= dt;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_DOWN) == GLFW_PRESS) {
            position().z += dt;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_RIGHT) == GLFW_PRESS) {
            position().x += dt;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_LEFT) == GLFW_PRESS) {
            position().x -= dt;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_PAGE_DOWN) == GLFW_PRESS) {
            position().y -= dt;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_PAGE_UP) == GLFW_PRESS) {
            position().y += dt;
        }
    }
}
