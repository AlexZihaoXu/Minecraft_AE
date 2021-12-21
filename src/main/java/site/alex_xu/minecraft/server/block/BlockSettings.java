package site.alex_xu.minecraft.server.block;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.material.Material;

public class BlockSettings extends MinecraftAECore {

    public Material material;
    public float resistance = 1.0f;
    public float hardness = 1.0f;
    public boolean opaque = true;

    public static BlockSettings of(Material material) {
        return new BlockSettings(material);
    }

    protected BlockSettings(Material material) {
        this.material = material;
    }

    public BlockSettings resistance(float resistance) {
        this.resistance = Math.max(0, resistance);
        return this;
    }

    public BlockSettings hardness(float hardness) {
        this.hardness = hardness;
        return this;
    }

    public BlockSettings opaque(boolean opaque) {
        this.opaque = opaque;
        return this;
    }
}
