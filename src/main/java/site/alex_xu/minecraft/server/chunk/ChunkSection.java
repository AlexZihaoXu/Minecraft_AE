package site.alex_xu.minecraft.server.chunk;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;

import javax.security.auth.callback.Callback;
import java.util.HashSet;

public class ChunkSection extends MinecraftAECore implements Tickable {
    private final Block[][][] blocks = new Block[16][16][16];
    private final short[][][] lightLevels = new short[16][16][16];
    private final HashSet<ChunkEventCallbackI> chunkModelUpdateCallbackIs = new HashSet<>();
    private final HashSet<ChunkEventCallbackI> chunkDisposeCallbackIs = new HashSet<>();
    private boolean requiresModelUpdate = false;
    private final Chunk chunk;
    private final int sectionY;

    public void registerChunkModelUpdateCallback(ChunkEventCallbackI chunkModelUpdateCallbackI) {
        chunkModelUpdateCallbackIs.add(chunkModelUpdateCallbackI);
    }

    public void removeChunkModelUpdateCallback(ChunkEventCallbackI chunkModelUpdateCallbackI) {
        chunkModelUpdateCallbackIs.remove(chunkModelUpdateCallbackI);
    }

    public void registerChunkDisposeCallback(ChunkEventCallbackI chunkDisposeCallbackI) {
        chunkDisposeCallbackIs.add(chunkDisposeCallbackI);
    }

    public void removeChunkDisposeCallback(ChunkEventCallbackI chunkDisposeCallbackI) {
        chunkDisposeCallbackIs.remove(chunkDisposeCallbackI);
    }

    public ChunkSection(Chunk chunk, int sectionY) {
        this.chunk = chunk;
        this.sectionY = sectionY;
        if (chunk.hasSection(sectionY + 1)) { // TOP
            chunk.getOrCreateChunkSection(sectionY + 1).requiresModelUpdate = true;
        }
        if (chunk.hasSection(sectionY - 1)) { // BOTTOM
            chunk.getOrCreateChunkSection(sectionY - 1).requiresModelUpdate = true;
        }
        if (chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() - 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).hasSection(sectionY)) { // North
            chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
        }
        if (chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() + 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).hasSection(sectionY)) { // South
            chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
        }
        if (chunk.getWorld().hasChunk(chunk.getX() - 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).hasSection(sectionY)) { // West
            chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
        }
        if (chunk.getWorld().hasChunk(chunk.getX() + 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).hasSection(sectionY)) { // West
            chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
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
            blocks[x][z][y] = block;
            requiresModelUpdate = true;
            if (x == 0) {
                if (chunk.getWorld().hasChunk(chunk.getX() - 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).hasSection(sectionY)) { // West
                    chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
                }
            }
            if (x == 15) {
                if (chunk.getWorld().hasChunk(chunk.getX() + 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).hasSection(sectionY)) { // West
                    chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
                }
            }
            if (z == 0) {
                if (chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() - 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).hasSection(sectionY)) { // North
                    chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
                }
            }
            if (z == 15) {
                if (chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() + 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).hasSection(sectionY)) { // South
                    chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
                }
            }
            if (y == 0) {
                if (chunk.hasSection(sectionY - 1)) { // BOTTOM
                    chunk.getOrCreateChunkSection(sectionY - 1).requiresModelUpdate = true;
                }
            }
            if (y == 15) {
                if (chunk.hasSection(sectionY + 1)) { // TOP
                    chunk.getOrCreateChunkSection(sectionY + 1).requiresModelUpdate = true;
                }
            }
        }
    }

    public Block getBlock(int x, int y, int z) {
        if (x >= 16 || x < 0 || y >= 16 || y < 0 || z >= 16 || z < 0) {
            var world = getChunk().getWorld();
            var originX = getChunk().getX() * 16;
            var originY = sectionY * 16;
            var originZ = getChunk().getY() * 16;
            return world.getBlock(originX + x, originY + y, originZ + z);
        }
        Block block = blocks[x][z][y];
        return block == null ? Blocks.AIR : block;
    }

    public short getLightLevel(int x, int y, int z) {
        return (short) Math.max(1, lightLevels[x][z][y]);
    }

    public void setLightLevel(short level, int x, int y, int z) {
        lightLevels[x][z][y] = level;
    }

    protected void modelUpdate() {
        for (ChunkEventCallbackI chunkModelUpdateCallbackI : chunkModelUpdateCallbackIs) {
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

    public interface ChunkEventCallbackI extends Callback {
        void execute(ChunkSection chunk);
    }

    public void onDispose() {
        for (ChunkEventCallbackI chunkDisposeCallback : chunkDisposeCallbackIs) {
            chunkDisposeCallback.execute(this);
        }
    }
}
