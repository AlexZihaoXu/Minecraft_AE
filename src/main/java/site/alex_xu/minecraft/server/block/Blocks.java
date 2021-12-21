package site.alex_xu.minecraft.server.block;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.material.Material;
import site.alex_xu.minecraft.server.models.BlockModelDef;
import site.alex_xu.minecraft.server.models.CubeAllModel;
import site.alex_xu.minecraft.server.models.CubeBottomTop;

import java.util.HashMap;

public class Blocks extends MinecraftAECore {
    public static HashMap<String, Block> blocks = new HashMap<>();

    public static Block register(String name, Block block) {
        blocks.put(name, block);
        return block;
    }

    // Blocks
    public static final Block AIR = register("air", new Block(BlockSettings.of(Material.AIR).opaque(false), null));
    public static final Block STONE = register("stone", new Block(BlockSettings.of(Material.STONE).opaque(true), new CubeAllModel().setAllTextures("assets/textures/blocks/stone.png")));
    public static final Block GRASS_BLOCK = register("grass_block", new Block(BlockSettings.of(Material.GRASS), new CubeBottomTop().setSide("assets/textures/blocks/grass_side.png").setBottom("assets/textures/blocks/dirt.png").setTop("assets/textures/blocks/grass_top.png")));
}
