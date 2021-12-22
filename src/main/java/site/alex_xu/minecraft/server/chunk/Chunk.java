package site.alex_xu.minecraft.server.chunk;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.world.World;

import javax.security.auth.callback.Callback;
import java.util.HashSet;

public class Chunk extends MinecraftAECore implements Tickable {
    private final ChunkSection[] sections = new ChunkSection[16];
    private final HashSet<ChunkSectionCreationCallbackI> chunkCreationCallbacks = new HashSet<>();
    private final int x, y;
    private final World world;

    public World getWorld() {
        return world;
    }

    public void addChunkCreationCallback(ChunkSectionCreationCallbackI chunkCreationCallback) {
        chunkCreationCallbacks.add(chunkCreationCallback);
    }

    public void removeChunkCreationCallback(ChunkSectionCreationCallbackI chunkCreationCallback) {
        chunkCreationCallbacks.remove(chunkCreationCallback);
    }

    public Chunk(World world, int x, int y) {
        this.x = x;
        this.y = y;
        this.world = world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Block getBlock(int x, int y, int z) {
        int sectionY = y / sections.length;
        if (sections[sectionY] == null) {
            return Blocks.AIR;
        }
        return sections[sectionY].getBlock(x, y % 16, z);
    }

    public ChunkSection getOrCreateChunkSection(int sectionY) {
        if (sections[sectionY] == null) {
            sections[sectionY] = new ChunkSection(this, sectionY);
            for (ChunkSectionCreationCallbackI chunkCreationCallback : chunkCreationCallbacks) {
                chunkCreationCallback.execute(this, sections[sectionY]);
            }
        }
        return sections[sectionY];
    }

    public void setBlock(Block block, int x, int y, int z) {
        int sectionY = y / sections.length;
        getOrCreateChunkSection(sectionY).setBlock(block, x, y % 16, z);
    }

    @Override
    public void onTick(double deltaTime) {
        for (ChunkSection section : sections) {
            if (section != null) {
                section.onTick(deltaTime);
            }
        }
    }

    public boolean hasSection(int chunkSectionY) {
        return chunkSectionY >= 0 && chunkSectionY < 16 && sections[chunkSectionY] != null;
    }

    public static interface ChunkSectionCreationCallbackI extends Callback {
        void execute(Chunk chunk, ChunkSection section);
    }
}
