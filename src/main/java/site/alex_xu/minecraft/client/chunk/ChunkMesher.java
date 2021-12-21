package site.alex_xu.minecraft.client.chunk;

import org.joml.Vector3f;
import site.alex_xu.minecraft.client.model.Model;
import site.alex_xu.minecraft.client.model.ModelBuilder;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.chunk.Chunk;
import site.alex_xu.minecraft.server.models.BlockModelDef;

public class ChunkMesher extends MinecraftAECore {
    protected Chunk chunk;
    protected Model mesh = null;

    public Model getModel() {
        return mesh;
    }

    protected abstract static class BlockModelApplier extends BlockModelDef {
        public static void apply(BlockModelDef self, ModelBuilder builder, int x, int y, int z, Chunk chunk) {
            for (Face face : self.faceMap.values()) {
                applyTriangle(self, face.v1(), face.v2(), face.v3(), builder, x, y, z, chunk);
                applyTriangle(self, face.v1(), face.v3(), face.v4(), builder, x, y, z, chunk);
            }

        }

        private static void applyTriangle(BlockModelDef self, int v1, int v2, int v3, ModelBuilder builder, int x, int y, int z, Chunk chunk) {
            var vv1 = self.vertexMap.get(v1);
            var vv2 = self.vertexMap.get(v2);
            var vv3 = self.vertexMap.get(v3);

            float brightness;
            int direction = -1;

            if (vv1.x() == vv2.x() && vv2.x() == vv3.x()) {
                brightness = 0.9f;
                direction = vv2.x() > 0.5f ? 3 : 2;
            } else if (vv1.z() == vv2.z() && vv2.z() == vv3.z()) {
                brightness = 0.95f;
                direction = vv2.z() > 0.5f ? 1 : 0;
            } else if (vv1.y() == vv2.y() && vv2.y() == vv3.y()) {
                brightness = vv1.y() > 0.5f ? 1 : 0.85f;
                direction = vv2.y() > 0.5f ? 4 : 5;
            } else {
                brightness = 0.8f;
            }

            boolean canceled = false;
            if (direction != -1) {
                if (direction == 0) { // North
                    Block block = chunk.getBlock(x, y, z - 1);
                    block = block == null ? Blocks.AIR : block;
                    if (block.settings().opaque) {
                        canceled = true;
                    }
                } else if (direction == 1) { // South
                    Block block = chunk.getBlock(x, y, z + 1);
                    block = block == null ? Blocks.AIR : block;
                    if (block.settings().opaque) {
                        canceled = true;
                    }
                } else if (direction == 2) { // West
                    Block block = chunk.getBlock(x - 1, y, z);
                    block = block == null ? Blocks.AIR : block;
                    if (block.settings().opaque) {
                        canceled = true;
                    }
                } else if (direction == 3) { // East
                    Block block = chunk.getBlock(x + 1, y, z);
                    block = block == null ? Blocks.AIR : block;
                    if (block.settings().opaque) {
                        canceled = true;
                    }
                } else if (direction == 4) { // Top
                    Block block = chunk.getBlock(x, y + 1, z);
                    block = block == null ? Blocks.AIR : block;
                    if (block.settings().opaque) {
                        canceled = true;
                    }
                } else { // Bottom
                    Block block = chunk.getBlock(x, y - 1, z);
                    block = block == null ? Blocks.AIR : block;
                    if (block.settings().opaque) {
                        canceled = true;
                    }
                }
            }

            if (!canceled) {
                int a = builder.vertex(x + vv1.x(), y + vv1.y(), z + vv1.z(), brightness, brightness, brightness, 1, 0, 0);
                int b = builder.vertex(x + vv2.x(), y + vv2.y(), z + vv2.z(), brightness, brightness, brightness, 1, 0, 0);
                int c = builder.vertex(x + vv3.x(), y + vv3.y(), z + vv3.z(), brightness, brightness, brightness, 1, 0, 0);
                builder.addFace(a, b, c);
            }
        }
    }

    public ChunkMesher(Chunk chunk) {
        this.chunk = chunk;
        chunk.registerChunkModelUpdateCallback(this::updateMesh);
    }

    public void updateMesh(Chunk chunk) {
        ModelBuilder builder = new ModelBuilder();
        if (mesh != null) {
            mesh.free();
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block == Blocks.AIR) continue;
                    BlockModelApplier.apply(block.modelDef(), builder, x, y, z, chunk);
                }
            }
        }
        mesh = builder.build();
    }

}
