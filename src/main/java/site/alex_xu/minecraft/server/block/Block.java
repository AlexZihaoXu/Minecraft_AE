package site.alex_xu.minecraft.server.block;

import site.alex_xu.minecraft.server.material.Material;
import site.alex_xu.minecraft.server.models.BlockModelDef;

public class Block {
    protected BlockSettings settings;
    protected BlockModelDef modelDef;

    public Block(BlockSettings settings, BlockModelDef modelDef) {
        this.settings = settings;
        this.modelDef = modelDef;
    }

    public BlockModelDef modelDef() {
        return modelDef;
    }

    public BlockSettings settings() {
        return settings;
    }
}
