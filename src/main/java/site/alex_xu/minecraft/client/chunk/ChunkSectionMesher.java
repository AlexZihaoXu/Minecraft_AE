package site.alex_xu.minecraft.client.chunk;

import site.alex_xu.minecraft.client.model.Model;
import site.alex_xu.minecraft.client.model.ModelBuilder;
import site.alex_xu.minecraft.client.resource.BlockTextureAtlas;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.chunk.ChunkSection;
import site.alex_xu.minecraft.server.models.BlockModelDef;

import javax.security.auth.callback.Callback;
import java.awt.geom.Rectangle2D;

public class ChunkSectionMesher extends MinecraftAECore {
    protected ChunkSection chunkSection;
    protected Model solidMesh = null;
    protected Model transparentMesh = null;

    public Model getSolidMesh() {
        return solidMesh;
    }

    public Model getLiquidMesh() {
        return transparentMesh;
    }

    protected abstract static class BlockModelApplier extends BlockModelDef {
        public static void apply(BlockModelDef self, ModelBuilder builder, int x, int y, int z, ChunkSection section, CancelingTestFunc cancelingTestFunc) {
            for (Face face : self.faceMap.values()) {
                applyTriangle(self, face, builder, x, y, z, section, true, cancelingTestFunc);
                applyTriangle(self, face, builder, x, y, z, section, false, cancelingTestFunc);
            }

        }

        private static void applyTriangle(BlockModelDef self, Face face, ModelBuilder builder, int x, int y, int z, ChunkSection section, boolean firstTriangle, CancelingTestFunc cancelingTestFunc) {
            var v1 = face.v1();
            var v2 = firstTriangle ? face.v2() : face.v3();
            var v3 = firstTriangle ? face.v3() : face.v4();
            var vv1 = self.vertexMap.get(v1);
            var vv2 = self.vertexMap.get(v2);
            var vv3 = self.vertexMap.get(v3);

            float brightness;
            int direction = -1;

            if (vv1.x() == vv2.x() && vv2.x() == vv3.x()) {
                brightness = 0.7f;
                direction = vv2.x() > 0.5f ? 3 : 2;
            } else if (vv1.z() == vv2.z() && vv2.z() == vv3.z()) {
                brightness = 0.8f;
                direction = vv2.z() > 0.5f ? 1 : 0;
            } else if (vv1.y() == vv2.y() && vv2.y() == vv3.y()) {
                brightness = vv1.y() > 0.5f ? 1 : 0.5f;
                direction = vv2.y() > 0.5f ? 4 : 5;
            } else {
                brightness = 0.4f;
            }

            boolean canceled = false;
            if (direction != -1) {
                if (direction == 0) { // North
                    Block block = section.getBlock(x, y, (z - 1));
                    block = block == null ? Blocks.AIR : block;
                    if (cancelingTestFunc.execute(block)) {
                        canceled = true;
                    }
                } else if (direction == 1) { // South
                    Block block = section.getBlock(x, y, (z + 1));
                    block = block == null ? Blocks.AIR : block;
                    if (cancelingTestFunc.execute(block)) {
                        canceled = true;
                    }
                } else if (direction == 2) { // West
                    Block block = section.getBlock(x - 1, y, z);
                    block = block == null ? Blocks.AIR : block;
                    if (cancelingTestFunc.execute(block)) {
                        canceled = true;
                    }
                } else if (direction == 3) { // East
                    Block block = section.getBlock(x + 1, y, z);
                    block = block == null ? Blocks.AIR : block;
                    if (cancelingTestFunc.execute(block)) {
                        canceled = true;
                    }
                } else if (direction == 4) { // Top
                    Block block = section.getBlock(x, y + 1, z);
                    block = block == null ? Blocks.AIR : block;
                    if (cancelingTestFunc.execute(block)) {
                        canceled = true;
                    }
                } else { // Bottom
                    Block block = section.getBlock(x, y - 1, z);
                    block = block == null ? Blocks.AIR : block;
                    if (cancelingTestFunc.execute(block)) {
                        canceled = true;
                    }
                }
            }

            if (!canceled) {
                Rectangle2D.Float bound = BlockTextureAtlas.getInstance().getTextureBound(self.texturePathMap.get(face.name()));


                int a = builder.vertex(x + vv1.x(), y + vv1.y(), z + vv1.z(), brightness, brightness, brightness, 1, (float) bound.getMinX(), (float) bound.getMinY());
                int b;
                int c;
                if (firstTriangle) {
                    b = builder.vertex(x + vv2.x(), y + vv2.y(), z + vv2.z(), brightness, brightness, brightness, 1, (float) bound.getMinX(), (float) bound.getMaxY());
                    c = builder.vertex(x + vv3.x(), y + vv3.y(), z + vv3.z(), brightness, brightness, brightness, 1, (float) bound.getMaxX(), (float) bound.getMaxY());
                } else {
                    b = builder.vertex(x + vv2.x(), y + vv2.y(), z + vv2.z(), brightness, brightness, brightness, 1, (float) bound.getMaxX(), (float) bound.getMaxY());
                    c = builder.vertex(x + vv3.x(), y + vv3.y(), z + vv3.z(), brightness, brightness, brightness, 1, (float) bound.getMaxX(), (float) bound.getMinY());
                }
                builder.addFace(a, b, c);
            }
        }

        private interface CancelingTestFunc extends Callback {
            boolean execute(Block block);
        }

        public static boolean cancelSolid(Block block) {
            return block.settings().material.isSolid();
        }

        public static boolean cancelLiquid(Block block) {
            return block.settings().material.isLiquid();
        }
    }

    public ChunkSectionMesher(ChunkSection section) {
        this.chunkSection = section;
        section.registerChunkModelUpdateCallback(this::updateMesh);
    }

    public void updateMesh(ChunkSection chunk) {
        if (solidMesh != null) {
            solidMesh.free();
        }
        if (transparentMesh != null) {
            transparentMesh.free();
        }

        ModelBuilder solidBuilder = new ModelBuilder();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block == Blocks.AIR) continue;
                    if (block.settings().material.isLiquid()) continue;
                    BlockModelApplier.apply(block.modelDef(), solidBuilder, x, y, z, chunk, BlockModelApplier::cancelSolid);
                }
            }
        }
        solidMesh = solidBuilder.build();

        ModelBuilder transparentBuilder = new ModelBuilder();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block == Blocks.AIR) continue;
                    if (block.settings().material.isLiquid())
                    BlockModelApplier.apply(block.modelDef(), transparentBuilder, x, y, z, chunk, BlockModelApplier::cancelLiquid);
                }
            }
        }
        transparentMesh = transparentBuilder.build();

    }

}
