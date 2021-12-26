package site.alex_xu.minecraft.server.entity;

import site.alex_xu.minecraft.server.collision.Hitbox;
import site.alex_xu.minecraft.server.world.World;

public class PlayerEntity extends Entity{
    public PlayerEntity(World world) {
        super(world, new Hitbox(0.6f, 1.8f));
    }

}
