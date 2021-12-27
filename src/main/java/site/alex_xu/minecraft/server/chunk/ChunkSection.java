package site.alex_xu.minecraft.server.chunk;

import org.joml.Vector3i;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import static org.joml.Math.max;

class LightTraveler {
    public boolean[][][] visited = new boolean[16][16][16];
    public ChunkSection section;

    public LightTraveler(ChunkSection section) {
        this.section = section;
    }

    public boolean canProceed(int x, int y, int z) {
        if (x >= 16 || x < 0 || y >= 16 || y < 0 || z >= 16 || z < 0) {
            return false;
        }
        if (section.getBlock(x, y, z).settings().material.blocksLight())
            return false;
        return !visited[x][y][z];
    }

    public void markVisited(int x, int y, int z) {
        visited[x][y][z] = true;
    }

    public void visit(int x, int y, int z) {
        if (canProceed(x, y, z)) {
            markVisited(x, y, z);


            int level = section.getEnvironmentLightLevel(x, y + 1, z);
            level = max(level, section.getEnvironmentLightLevel(x, y - 1, z));
            level = max(level, section.getEnvironmentLightLevel(x + 1, y, z));
            level = max(level, section.getEnvironmentLightLevel(x - 1, y, z));
            level = max(level, section.getEnvironmentLightLevel(x, y, z + 1));
            level = max(level, section.getEnvironmentLightLevel(x, y, z - 1));

            section.setEnvironmentLightLevel(max(section.getEnvironmentLightLevel(x, y, z), level - 1), x, y, z);

            visit(x, y + 1, z);
            visit(x, y - 1, z);
            visit(x - 1, y, z);
            visit(x + 1, y, z);
            visit(x, y, z + 1);
            visit(x, y, z - 1);
        }
    }

    public void visitRest() {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (visited[x][y][z]) {
                        continue;
                    }
                    visit(x, y, z);
                }
            }
        }
    }
}

public class ChunkSection extends MinecraftAECore implements Tickable {
    private final Block[][][] blocks = new Block[16][16][16];
    private final int[][][] blockLightLevels = new int[16][16][16];
    private int[][][] envLightLevels = new int[16][16][16];
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

    public int getBlockLightLevel(int x, int y, int z) {
        if (x >= 16 || x < 0 || y >= 16 || y < 0 || z >= 16 || z < 0) {
            var world = getChunk().getWorld();
            var originX = getChunk().getX() * 16;
            var originY = sectionY * 16;
            var originZ = getChunk().getY() * 16;
            return world.getBlockLight(originX + x, originY + y, originZ + z);
        }
        return Math.max(0, blockLightLevels[x][z][y]);
    }

    public void setBlockLightLevel(int level, int x, int y, int z) {

        blockLightLevels[x][z][y] = level;
    }

    public int getEnvironmentLightLevel(int x, int y, int z) {
        if (x >= 16 || x < 0 || y >= 16 || y < 0 || z >= 16 || z < 0) {
            var world = getChunk().getWorld();
            var originX = getChunk().getX() * 16;
            var originY = sectionY * 16;
            var originZ = getChunk().getY() * 16;
            return world.getEnvironmentLight(originX + x, originY + y, originZ + z);
        }
        return Math.max(0, envLightLevels[x][z][y]);
    }

    public void setEnvironmentLightLevel(int level, int x, int y, int z) {
        envLightLevels[x][z][y] = level;
    }

    public void calculateEnvironmentLight() {

        ChunkSection upSection = null;

        envLightLevels = new int[16][16][16];

        for (int i = sectionY + 1; i < 16; i++) {
            if (chunk.hasSection(i)) {
                upSection = chunk.getOrCreateChunkSection(i);
                break;
            }
        }

        ArrayList<Vector3i> fullLights = new ArrayList<>();

        if (upSection == null) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 15; y >= 0; y--) {
                        if (getBlock(x, y, z).settings().material.blocksLight()) {
                            break;
                        }
                        setEnvironmentLightLevel(15, x, y, z);
                        fullLights.add(new Vector3i(x, y, z));
                    }
                }
            }
        }

        LightTraveler traveler = new LightTraveler(this);
        for (Vector3i lightPos : fullLights) {
            traveler.visit(lightPos.x, lightPos.y, lightPos.z);
        }
        traveler.visitRest();

    }

    public int getLightLevelAt(int x, int y, int z) {
        return max(getBlockLightLevel(x, y, z), getEnvironmentLightLevel(x, y, z));
    }

    protected void modelUpdate() {
        calculateEnvironmentLight();
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
