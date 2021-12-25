package site.alex_xu.minecraft.server.block;

import site.alex_xu.minecraft.server.models.BlockModelDef;
import site.alex_xu.minecraft.server.models.FluidSource;

public class WaterSourceBlock extends Block {
    public WaterSourceBlock(BlockSettings settings) {
        super(settings, new FluidSource().setSide("assets/textures/blocks/water_flow.png").setEnd("assets/textures/blocks/water_still.png"));
    }
}
