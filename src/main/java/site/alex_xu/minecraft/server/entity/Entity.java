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

        objectRenderer.color(0, 1, 0, 1).renderBox(
                camera,
                (float) baseRect.x,
                position().y,
                (float) baseRect.y,
                (float) baseRect.width,
                0.2f,
                (float) baseRect.height
        );

        ArrayList<Rectangle2D.Float> potentialHitsBottom = new ArrayList<>();

        for (int x = world.blockXOf((float) baseRect.getMinX()); x <= world.blockXOf((float) baseRect.getMaxX()); x++) {
            for (int z = world.blockZOf((float) baseRect.getMinY()); z <= world.blockZOf((float) baseRect.getMaxY()); z++) {
                Block block = world.getBlock(x, world.blockYOf(position().y - 0.5f), z);
                if (block.settings().material.blocksMovement()) {
                    potentialHitsBottom.add(new Rectangle2D.Float(
                            x, z,
                            1, 1
                    ));
                }
            }
        }

        WorldScreen.debugInfo = "Block: " + Blocks.nameOf(world.getBlock(baseRect.getMinX(), position().y - 0.5f, baseRect.getMinY()));

        for (Rectangle2D.Float potentialHit : potentialHitsBottom) {
            objectRenderer.color(0, 1, 1, 1).renderBox(
                    camera,
                    (float) potentialHit.x,
                    world.blockYOf(position().y - 0.5f),
                    (float) potentialHit.y,
                    (float) potentialHit.width,
                    1f,
                    (float) potentialHit.height
            );
        }



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
