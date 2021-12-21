package site.alex_xu.minecraft.server.block;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.material.Material;
import site.alex_xu.minecraft.server.models.BlockModelDef;
import site.alex_xu.minecraft.server.models.CubeAllModel;

import java.util.HashMap;

public class Blocks extends MinecraftAECore {
    protected static HashMap<String, Block> blocks = new HashMap<>();

    public static Block register(String name, Block block) {
        blocks.put(name, block);
        return block;
    }

    // Blocks
    public static final Block AIR = register("air", new Block(BlockSettings.of(Material.AIR), null));
    public static final Block STONE = register("grass_block", new Block(BlockSettings.of(Material.STONE), new CubeAllModel().setAllTextures("assets/textures/blocks/stone.png")));
}
