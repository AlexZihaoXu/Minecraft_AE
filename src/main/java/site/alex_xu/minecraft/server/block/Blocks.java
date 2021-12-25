package site.alex_xu.minecraft.server.block;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.material.Material;
import site.alex_xu.minecraft.server.models.*;

import java.util.HashMap;

public class Blocks extends MinecraftAECore {
    public static HashMap<String, Block> blocks = new HashMap<>();
    private static final HashMap<Block, String> blockNameMap = new HashMap<>();

    private static Block register(String name, Block block) {
        blocks.put(name, block);
        blockNameMap.put(block, name);
        return block;
    }

    public static String nameOf(Block block) {
        return blockNameMap.get(block);
    }

    // Blocks
    public static final Block AIR = register("air", new Block(BlockSettings.of(Material.AIR).opaque(false), null));
    public static final Block STONE = register("stone", new Block(BlockSettings.of(Material.STONE).opaque(true), new CubeAllModel().setAllTextures("assets/textures/blocks/stone.png")));
    public static final Block GRASS_BLOCK = register("grass_block", new Block(BlockSettings.of(Material.GRASS), new CubeBottomTop().setSide("assets/textures/blocks/grass_side.png").setBottom("assets/textures/blocks/dirt.png").setTop("assets/textures/blocks/grass_top.png")));
    public static final Block CRAFTING_TABLE = register("crafting_table", new Block(BlockSettings.of(Material.WOOD), new CubeModel().setFaceTexture("north", "assets/textures/blocks/crafting_table_front.png").setFaceTexture("south", "assets/textures/blocks/crafting_table_side.png").setFaceTexture("east", "assets/textures/blocks/crafting_table_side.png").setFaceTexture("west", "assets/textures/blocks/crafting_table_side.png").setFaceTexture("top", "assets/textures/blocks/crafting_table_top.png").setFaceTexture("bottom", "assets/textures/blocks/planks_oak.png")));
    public static final Block OAK_LOG = register("oak_log", new Block(BlockSettings.of(Material.WOOD), new CubeColumn().setSide("assets/textures/blocks/log_oak.png").setEnd("assets/textures/blocks/log_oak_top.png")));
    public static final Block OAK_LEAVES = register("oak_leaves", new Block(BlockSettings.of(Material.LEAVES).opaque(false), new CubeAllModel().setAllTextures("assets/textures/blocks/leaves_oak.png")));
    public static final Block WATER = register("water", new WaterSourceBlock(BlockSettings.of(Material.FLUID)));
}
