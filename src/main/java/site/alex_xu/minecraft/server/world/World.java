package site.alex_xu.minecraft.server.world;

import org.joml.Vector3f;
import site.alex_xu.minecraft.client.MinecraftClient;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.chunk.Chunk;

import javax.security.auth.callback.Callback;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import static org.lwjgl.glfw.GLFW.*;

public class World extends MinecraftAECore {

    private final HashSet<ChunkCreationCallbackI> chunkCreationCallbacks = new HashSet<>();

    private float time = 0;

    public void onTick(double deltaTime) {
        if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_ENTER) == GLFW_PRESS) {
            time += deltaTime;
        }
        time += deltaTime / 60 / 20;
        time %= 1.0;
        for (Chunk chunk : chunks.values()) {
            chunk.onTick(deltaTime);
        }
    }

    public void setBlock(Block block, int x, int y, int z) {
        getOrCreateChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)).setBlock(block, Math.floorMod(x, 16), y, Math.floorMod(z, 16));
    }

    public Block getBlock(int x, int y, int z) {
        if (y >= 0 && y < 256) {
            if (hasChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) {
                return getOrCreateChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)).getBlock(Math.floorMod(x, 16), y, Math.floorMod(z, 16));
            }
            return Blocks.AIR;
        }
        return null;
    }

    public int getLightLevelAt(int x, int y, int z) {
        if (y >= 0 && y < 256) {
            if (hasChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) {
                return getOrCreateChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)).getLightLevelAt(Math.floorMod(x, 16), y, Math.floorMod(z, 16));
            }
            return 0;
        }
        return 0;
    }

    public int getEnvironmentLight(int x, int y, int z) {
        if (y >= 0 && y < 256) {
            if (hasChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) {
                return getOrCreateChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)).getEnvironmentLightLevel(Math.floorMod(x, 16), y, Math.floorMod(z, 16));
            }
            return 0;
        }
        return 0;
    }


    public int getBlockLight(int x, int y, int z) {
        if (y >= 0 && y < 256) {
            if (hasChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16))) {
                return getOrCreateChunk(Math.floorDiv(x, 16), Math.floorDiv(z, 16)).getBlockLightLevel(Math.floorMod(x, 16), y, Math.floorMod(z, 16));
            }
            return 0;
        }
        return 0;
    }

    public Block getBlock(float x, float y, float z) {
        return getBlock(blockXOf(x), blockYOf(y), blockZOf(z));
    }

    public Block getBlock(double x, double y, double z) {
        return getBlock((float) x, (float) y, (float) z);
    }

    public float getTime() {
        return time;
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

    public int blockXOf(float x) {
        return (int) Math.round(x - 0.5);
    }

    public int blockZOf(float z) {
        return (int) Math.round(z - 0.5);
    }

    public int blockYOf(float y) {
        return (int) Math.floor(y);
    }

    public Vector3f blockPosOf(float x, float y, float z) {
        return new Vector3f(blockXOf(x), blockYOf(y), blockZOf(z));
    }

    public Vector3f blockPosOf(Vector3f pos) {
        return blockPosOf(pos.x, pos.y, pos.z);
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

    public interface ChunkCreationCallbackI extends Callback {
        void execute(World world, Chunk chunk);
    }
}
