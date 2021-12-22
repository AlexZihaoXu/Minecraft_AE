package site.alex_xu.minecraft.server.world;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.chunk.Chunk;

import javax.security.auth.callback.Callback;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class World extends MinecraftAECore {

    private final HashSet<ChunkCreationCallbackI> chunkCreationCallbacks = new HashSet<>();

    public void onTick(double deltaTime) {
        for (Chunk chunk : chunks.values()) {
            chunk.onTick(deltaTime);
        }
    }

    public void setBlock(Block block, int x, int y, int z) {
        getOrCreateChunk(x / 16, z / 16).setBlock(block, ((x % 16) + 16) % 16, y, (((15 - z) % 16) + 16) % 16);
    }

    public record ChunkPos(int x, int y) implements Comparable<ChunkPos> {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChunkPos chunkPos = (ChunkPos) o;

            if (x != chunkPos.x) return false;
            return y == chunkPos.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        @Override
        public int compareTo(ChunkPos pos) {
            if (pos.x > this.x) {
                return 1;
            } else {
                if (pos.x < this.x) {
                    return -1;
                } else {
                    if (pos.y > this.y) {
                        return 1;
                    } else if (pos.y < this.y) {
                        return -1;
                    }
                    return 0;
                }
            }
        }
    }

    private final TreeMap<ChunkPos, Chunk> chunks = new TreeMap<>();

    public void registerChunkCreationCallback(ChunkCreationCallbackI chunkCreationCallback) {
        chunkCreationCallbacks.add(chunkCreationCallback);
    }

    public void removeChunkCreationCallback(ChunkCreationCallbackI chunkCreationCallback) {
        chunkCreationCallbacks.remove(chunkCreationCallback);
    }

    public boolean hasChunk(int x, int y) {
        return chunks.containsKey(new ChunkPos(x, y));
    }

    public Chunk getOrCreateChunk(int x, int y) {
        ChunkPos pos = new ChunkPos(x, y);
        if (!hasChunk(x, y)) {
            Chunk chunk = new Chunk(this, x, y);
            chunks.put(pos, chunk);
            for (ChunkCreationCallbackI chunkCreationCallback : chunkCreationCallbacks) {
                chunkCreationCallback.execute(this, chunk);
            }
        }
        return chunks.get(pos);
    }

    public World() {

    }

    public static interface ChunkCreationCallbackI extends Callback {
        void execute(World world, Chunk chunk);
    }
}
