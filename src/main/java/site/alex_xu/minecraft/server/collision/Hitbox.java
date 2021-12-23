package site.alex_xu.minecraft.server.collision;

import site.alex_xu.minecraft.core.MinecraftAECore;

public class Hitbox extends MinecraftAECore {
    private final float width, height;

    public Hitbox(float width, float height) {
        this.width = width;
        this.height = height;

    }
}
