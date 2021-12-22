package site.alex_xu.minecraft.server.chunk;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;

import javax.security.auth.callback.Callback;
import java.util.HashMap;
import java.util.HashSet;

public class Chunk extends MinecraftAECore implements Tickable {
    private final ChunkSection[] sections = new ChunkSection[16];
    private final HashSet<ChunkCreationCallbackI> chunkCreationCallbacks = new HashSet<>();

    public void addChunkCreationCallback(ChunkCreationCallbackI chunkCreationCallback) {
        chunkCreationCallbacks.add(chunkCreationCallback);
    }

    public void removeChunkCreationCallback(ChunkCreationCallbackI chunkCreationCallback) {
        chunkCreationCallbacks.remove(chunkCreationCallback);
    }

    public Chunk() {
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
            for (ChunkCreationCallbackI chunkCreationCallback : chunkCreationCallbacks) {
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

    public static interface ChunkCreationCallbackI extends Callback {
        void execute(Chunk chunk, ChunkSection section);
    }
}
