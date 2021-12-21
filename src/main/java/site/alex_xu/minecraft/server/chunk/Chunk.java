package site.alex_xu.minecraft.server.chunk;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;

import javax.security.auth.callback.Callback;
import java.util.HashSet;

public class Chunk extends MinecraftAECore {
    private final Block[][][] blocks = new Block[16][16][256];
    private final HashSet<ChunkModelUpdateCallbackI> chunkModelUpdateCallbackIs = new HashSet<>();

    public void registerChunkModelUpdateCallback(ChunkModelUpdateCallbackI chunkModelUpdateCallbackI) {
        chunkModelUpdateCallbackIs.add(chunkModelUpdateCallbackI);
    }

    public void remove(ChunkModelUpdateCallbackI chunkModelUpdateCallbackI) {
        chunkModelUpdateCallbackIs.remove(chunkModelUpdateCallbackI);
    }

    public Chunk() {

    }

    public void setBlock(Block block, int x, int y, int z) {
        if (block == null)
            block = Blocks.AIR;
        if (block != getBlock(x, y, z)) {
            blocks[x][z][y] = block;
            modelUpdate();
        }
    }

    public Block getBlock(int x, int y, int z) {
        Block block = blocks[x][z][y];
        return block == null ? Blocks.AIR : block;
    }

    protected void modelUpdate() {
        for (ChunkModelUpdateCallbackI chunkModelUpdateCallbackI : chunkModelUpdateCallbackIs) {
            chunkModelUpdateCallbackI.execute(this);
        }
    }

    public interface ChunkModelUpdateCallbackI extends Callback {
        void execute(Chunk chunk);
    }
}
