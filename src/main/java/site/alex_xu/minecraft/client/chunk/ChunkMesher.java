package site.alex_xu.minecraft.client.chunk;

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
        public static void apply(BlockModelDef self, ModelBuilder builder, int x, int y, int z) {
            for (Face face : self.faceMap.values()) {
                applyTriangle(self, face.v1(), face.v2(), face.v3(), builder, x, y, z);
                applyTriangle(self, face.v1(), face.v3(), face.v4(), builder, x, y, z);
            }

        }

        private static void applyTriangle(BlockModelDef self, int v1, int v2, int v3, ModelBuilder builder, int x, int y, int z) {
            var vv1 = self.vertexMap.get(v1);
            var vv2 = self.vertexMap.get(v2);
            var vv3 = self.vertexMap.get(v3);
            int a = builder.vertex(vv1.x(), vv1.y(), vv1.z(), 0, 1, 1, 1, 0, 0);
            int b = builder.vertex(vv2.x(), vv2.y(), vv2.z(), 1, 1, 1, 1, 0, 0);
            int c = builder.vertex(vv3.x(), vv3.y(), vv3.z(), 1, 1, 1, 1, 0, 0);
            builder.addFace(a, b, c);
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
                    BlockModelApplier.apply(block.modelDef(), builder, x, y, z);
                }
            }
        }
        mesh = builder.build();
    }

}
