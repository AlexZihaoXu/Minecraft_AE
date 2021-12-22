package site.alex_xu.minecraft.server.chunk;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;

import javax.security.auth.callback.Callback;
import java.util.HashSet;

public class ChunkSection extends MinecraftAECore implements Tickable {
    private final Block[][][] blocks = new Block[16][16][16];
    private final HashSet<ChunkModelUpdateCallbackI> chunkModelUpdateCallbackIs = new HashSet<>();
    private boolean requiresModelUpdate = false;
    private final Chunk chunk;
    private final int sectionY;

    public void registerChunkModelUpdateCallback(ChunkModelUpdateCallbackI chunkModelUpdateCallbackI) {
        chunkModelUpdateCallbackIs.add(chunkModelUpdateCallbackI);
    }

    public void remove(ChunkModelUpdateCallbackI chunkModelUpdateCallbackI) {
        chunkModelUpdateCallbackIs.remove(chunkModelUpdateCallbackI);
    }

    public ChunkSection(Chunk chunk, int sectionY) {
        this.chunk = chunk;
        this.sectionY = sectionY;
        if (chunk.hasSection(sectionY + 1)) {
            chunk.getOrCreateChunkSection(sectionY + 1).requiresModelUpdate = true;
            System.out.println("re-meshing" + (sectionY + 1));
        }
        if (chunk.hasSection(sectionY - 1)) {
            chunk.getOrCreateChunkSection(sectionY - 1).requiresModelUpdate = true;
            System.out.println("re-meshing" + (sectionY - 1));
        }
    }

    public Chunk getChunk() {
        return chunk;
    }

    public int getSectionY() {
        return sectionY;
    }

    public void setBlock(Block block, int x, int y, int z) {
        if (block == null)
            block = Blocks.AIR;
        if (block != getBlock(x, y, z)) {
            blocks[x][15 - z][y] = block;
            requiresModelUpdate = true;
        }
    }

    public Block getBlock(int x, int y, int z) {
        if (x >= 16 || x < 0 || y >= 16 || y < 0 || z >= 16 || z < 0) {
            if (y >= 16 || y < 0) {
                int nextChunkSectionY = (int) (sectionY + Math.floor(y / 16f));
                if (chunk.hasSection(nextChunkSectionY)) {
                    return chunk.getOrCreateChunkSection(nextChunkSectionY).getBlock(x, (y % 16 + 16) % 16, z);
                }
            }
            return null;
        }
        Block block = blocks[x][15 - z][y];
        return block == null ? Blocks.AIR : block;
    }

    protected void modelUpdate() {
        for (ChunkModelUpdateCallbackI chunkModelUpdateCallbackI : chunkModelUpdateCallbackIs) {
            chunkModelUpdateCallbackI.execute(this);
        }
    }

    @Override
    public void onTick(double deltaTime) {
        if (requiresModelUpdate) {
            modelUpdate();
            requiresModelUpdate = false;
        }
    }

    public interface ChunkModelUpdateCallbackI extends Callback {
        void execute(ChunkSection chunk);
    }
}
