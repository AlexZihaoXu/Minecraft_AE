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
    private final HashSet<ChunkDisposeCallbackI> chunkDisposeCallbacks = new HashSet<>();
    private final int x, y;
    private final World world;

    public World getWorld() {
        return world;
    }

    public void registerChunkCreationCallback(ChunkSectionCreationCallbackI chunkCreationCallback) {
        chunkCreationCallbacks.add(chunkCreationCallback);
    }

    public void removeChunkCreationCallback(ChunkSectionCreationCallbackI chunkCreationCallback) {
        chunkCreationCallbacks.remove(chunkCreationCallback);
    }

    public void registerChunkDisposeCallback(ChunkDisposeCallbackI chunkDisposeCallback) {
        chunkDisposeCallbacks.add(chunkDisposeCallback);
    }

    public void removeChunkDisposeCallback(ChunkDisposeCallbackI chunkDisposeCallback) {
        chunkDisposeCallbacks.remove(chunkDisposeCallback);
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
        if (sectionY >= 0 && sectionY < 16) {
            if (sections[sectionY] == null) {
                return Blocks.AIR;
            }
            return sections[sectionY].getBlock(x, Math.floorMod(y, 16), z);
        }
        return Blocks.AIR;
    }

    public void setBlock(Block block, int x, int y, int z) {
        int sectionY = y / sections.length;
        getOrCreateChunkSection(sectionY).setBlock(block, x, Math.floorMod(y, 16), z);
    }

    public void setBlockLightLevel( int  level, int x, int y, int z) {
        int sectionY = y / sections.length;
        getOrCreateChunkSection(sectionY).setBlockLightLevel(level, x, Math.floorMod(y, 16), z);
    }

    public  int  getBlockLightLevel(int x, int y, int z) {
        int sectionY = y / sections.length;
        if (sectionY >= 0 && sectionY < 16) {
            if (sections[sectionY] == null) {
                return 0;
            }
            return sections[sectionY].getBlockLightLevel(x, Math.floorMod(y, 16), z);
        }
        return 0;
    }

    public void setEnvironmentLightLevel( int  level, int x, int y, int z) {
        int sectionY = y / sections.length;
        getOrCreateChunkSection(sectionY).setEnvironmentLightLevel(level, x, Math.floorMod(y, 16), z);
    }

    public  int  getEnvironmentLightLevel(int x, int y, int z) {
        int sectionY = y / sections.length;
        if (sectionY >= 0 && sectionY < 16) {
            if (sections[sectionY] == null) {
                return 0;
            }
            return sections[sectionY].getEnvironmentLightLevel(x, Math.floorMod(y, 16), z);
        }
        return 0;
    }

    public  int  getLightLevelAt(int x, int y, int z) {
        int sectionY = y / sections.length;
        if (sectionY >= 0 && sectionY < 16) {
            if (sections[sectionY] == null) {
                return 0;
            }
            return sections[sectionY].getLightLevelAt(x, Math.floorMod(y, 16), z);
        }
        return 0;
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

    public interface ChunkSectionCreationCallbackI extends Callback {
        void execute(Chunk chunk, ChunkSection section);
    }

    public interface ChunkDisposeCallbackI extends Callback {
        void execute(Chunk chunk);
    }
}
