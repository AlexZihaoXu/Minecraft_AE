package site.alex_xu.minecraft.server.entity;

import org.joml.Vector3f;
import site.alex_xu.minecraft.client.MinecraftClient;
import site.alex_xu.minecraft.client.render.GameObjectRenderer;
import site.alex_xu.minecraft.client.screen.world.WorldScreen;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.collision.Hitbox;
import site.alex_xu.minecraft.server.world.World;

import java.awt.geom.Rectangle2D;

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
        onCollisionTick(deltaTime);

//        float speed = 4.3f;
        float speed = 1f;
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_UP) == GLFW_PRESS) {
            velocity.z = -speed;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_DOWN) == GLFW_PRESS) {
            velocity.z = speed;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_RIGHT) == GLFW_PRESS) {
            velocity.x = speed;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_LEFT) == GLFW_PRESS) {
            velocity.x = -speed;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_PAGE_DOWN) == GLFW_PRESS) {
            velocity.y = -speed;
        }
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_PAGE_UP) == GLFW_PRESS) {
            velocity.y = speed;
        }
    }

    public void onCollisionTick(double dt) {
//        velocity.y -= dt * 9.8f / 100f;
//        position().y += velocity.y * dt;


        var objectRenderer = new GameObjectRenderer(MinecraftClient.getInstance().getWindow());
        var camera = ((WorldScreen) MinecraftClient.getInstance().getScreenManager().get(0)).getCamera();
        Rectangle2D.Float baseRect = new Rectangle2D.Float(position().x - hitbox().width() / 2, position().z - hitbox().width() / 2, hitbox().width(), hitbox().width());

        float boxZMin = position().z - hitbox().width() / 2;
        float boxZMax = position().z + hitbox().width() / 2;
        float boxXMin = position().x - hitbox().width() / 2;
        float boxXMax = position().x + hitbox().width() / 2;
        float boxYMin = position().y;
        float boxYMax = position().y + hitbox().height();

        for (int x = world.blockXOf((float) baseRect.getMinX()); x <= world.blockXOf((float) baseRect.getMaxX()); x++) {
            for (int z = world.blockZOf((float) baseRect.getMinY()); z <= world.blockZOf((float) baseRect.getMaxY()); z++) {
                for (int y = world.blockYOf(position().y); y <= world.blockYOf(position().y + hitbox().height()); y++) {
                    if (world.getBlock(x, y, z).settings().material.blocksMovement())
                        objectRenderer.color(1, 1, 0, 1);
                    else
                        objectRenderer.color(1, 1, 1, 0.5f);
                    objectRenderer.renderBox(
                            camera,
                            x, y, z,
                            1, 1, 1
                    );
                    if (world.getBlock(x, y, z).settings().material.blocksMovement()) {
                        int blockZMin = z;
                        int blockZMax = z + 1;
                        int blockXMin = x;
                        int blockXMax = x + 1;
                        int blockYMin = y;
                        int blockYMax = y + 1;

                        float xMin = Math.max(blockXMin, boxXMin);
                        float xMax = Math.min(blockXMax, boxXMax);
                        float yMin = Math.max(blockYMin, boxYMin);
                        float yMax = Math.min(blockYMax, boxYMax);
                        float zMin = Math.max(blockZMin, boxZMin);
                        float zMax = Math.min(blockZMax, boxZMax);

                        float xDiff = xMax - xMin;
                        float yDiff = yMax - yMin;
                        float zDiff = zMax - zMin;

                        objectRenderer.color(1, 0, 1, 1).renderBox(
                                camera,
                                xMin, yMin, zMin,
                                xDiff, yDiff, zDiff
                        );


                    }
                }
            }
        }

        int south = world.blockZOf((float) baseRect.getMinY());
        int north = world.blockZOf((float) baseRect.getMaxY());
        int west = world.blockXOf((float) baseRect.getMinX());
        int east = world.blockXOf((float) baseRect.getMaxX());
        int top = world.blockYOf(position().y + hitbox().height());
        int bottom = world.blockYOf(position().y);


        position().add(new Vector3f(velocity).mul((float) dt));
        velocity.add(new Vector3f(velocity).mul(-(float) dt * 10));

        WorldScreen.debugInfo = "Block: " + Blocks.nameOf(world.getBlock(baseRect.getMinX(), position().y + 0.5f + hitbox.height(), baseRect.getMinY()));

    }
}
