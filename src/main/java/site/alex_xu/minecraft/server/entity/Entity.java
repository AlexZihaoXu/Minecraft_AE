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

import static org.lwjgl.glfw.GLFW.*;

public class Entity extends MinecraftAECore implements Tickable {
    protected Hitbox hitbox;
    protected Vector3f velocity = new Vector3f();
    protected World world;

    public Vector3f velocity() {
        return velocity;
    }

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
        int collisionScale = (int) (velocity.distanceSquared(0, 0, 0) / (100 * 100)) + 1;
        for (int i = 0; i < collisionScale; i++) {
            onCollisionTick(deltaTime / collisionScale);
        }
        onGravityTick(deltaTime);
        WorldScreen.debugInfo = "[" + collisionScale + "]velocity: " + velocity.distance(0, 0, 0);
    }

    private void onGravityTick(double deltaTime) {
        velocity().y -= deltaTime * 38f;
        if (!inAir()) {
            velocity.add(new Vector3f(velocity.x, 0, velocity.z).mul(-(float) deltaTime * 2));
        }
    }

    public void onCollisionTick(double dt) {
        float gap = 0.002f;
        if (velocity.distanceSquared(0, 0, 0) > 0.1) {
            velocity.add(new Vector3f(velocity.x, 0, velocity.z).mul(-(float) dt * 8));
            position().add(new Vector3f(velocity).mul((float) dt));
        }
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
                    Block block = world.getBlock(x, y, z);
                    if (block != null && block.settings().material.blocksMovement()) {
                        int blockZMax = z + 1;
                        int blockXMax = x + 1;
                        int blockYMax = y + 1;

                        float xMin = Math.max(x, boxXMin);
                        float xMax = Math.min(blockXMax, boxXMax);
                        float yMin = Math.max(y, boxYMin);
                        float yMax = Math.min(blockYMax, boxYMax);
                        float zMin = Math.max(z, boxZMin);
                        float zMax = Math.min(blockZMax, boxZMax);

                        float xDiff = xMax - xMin;
                        float yDiff = yMax - yMin;
                        float zDiff = zMax - zMin;

                        if (yDiff < gap) {
                            yDiff = 0;
                        }
                        WorldScreen.debugInfo = "yDiff: " + yDiff;
                        if (yDiff <= xDiff && yDiff <= zDiff) {
                            if (boxYMax > blockYMax) {
                                position().y = blockYMax;
                                velocity.y = Math.max(0, velocity.y);
                            } else if (boxYMin < y) {
                                position().y = y - hitbox().height();
                                velocity.y = Math.min(0, velocity.y);
                            }
                        } else if (xDiff <= yDiff && xDiff <= zDiff) {
                            if (boxXMax > blockXMax) {
                                position().x = blockXMax + hitbox().width() / 2;
                                velocity.x = Math.max(0, velocity.x);
                            } else if (boxXMin < x) {
                                position().x = x - hitbox().width() / 2;
                                velocity.x = Math.min(0, velocity.x);
                            }
                        } else {
                            if (boxZMax > blockZMax) {
                                position().z = blockZMax + hitbox().width() / 2;
                                velocity.z = Math.max(0, velocity.z);
                            } else if (boxZMin < z) {
                                position().z = z - hitbox().width() / 2;
                                velocity.z = Math.min(0, velocity.z);
                            }
                        }


                    }
                }
            }
        }


    }

    public boolean inAir() {
        for (int x = getWorld().blockXOf(position().x - hitbox().width() / 2f); x <= getWorld().blockXOf(position().x + hitbox().width() / 2f); x++) {
            for (int z = getWorld().blockXOf(position().z - hitbox().width() / 2f); z <= getWorld().blockXOf(position().z + hitbox().width() / 2f); z++) {
                Block block = getWorld().getBlock(x, getWorld().blockYOf(position().y - 0.1f), z);
                if (block != null && block.settings().material.blocksMovement()) {
                    return false;
                }
            }
        }
        return true;
    }
}
