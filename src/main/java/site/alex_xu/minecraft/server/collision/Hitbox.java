package site.alex_xu.minecraft.server.collision;

import org.joml.Vector3f;
import site.alex_xu.minecraft.client.model.Model;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.world.World;

public class Hitbox extends MinecraftAECore {
    private final float width, height;
    private final Vector3f position = new Vector3f();

    public Vector3f getPosition() {
        return position;
    }

    public Hitbox(float width, float height) {
        this.width = width;
        this.height = height;

    }

    public void handleCollision(World world) {

    }
}
