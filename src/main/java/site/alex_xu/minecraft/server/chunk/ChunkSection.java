package site.alex_xu.minecraft.server.chunk;

import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.Directions;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.world.World;

import javax.security.auth.callback.Callback;
import java.util.*;

import static org.joml.Math.max;

public class ChunkSection extends MinecraftAECore implements Tickable {

    private final Block[][][] blocks = new Block[16][16][16];
    private final HashSet<ChunkEventCallbackI> chunkModelUpdateCallbackIs = new HashSet<>();
    private final HashSet<ChunkEventCallbackI> chunkDisposeCallbackIs = new HashSet<>();
    private final LightInformation envLightInformation = new LightInformation();
    private final Chunk chunk;
    private final int sectionY;

    // Lights

    public LightInformation getEnvLightInformation() {
        return envLightInformation;
    }

    public byte getEnvLightLevel(int x, int y, int z) {

        if (sectionY == 0 && y < 0)
            return 15;
        if (isTopSection() && y > 15)
            return 15;
        if (x >= 16 || x < 0 || y >= 16 || y < 0 || z >= 16 || z < 0) {
            var world = getChunk().getWorld();
            var originX = getChunk().getX() * 16;
            var originY = sectionY * 16;
            var originZ = getChunk().getY() * 16;
            return world.getEnvLight(originX + x, originY + y, originZ + z);
        }
        return getEnvLightInformation().getLevel(x, y, z);
    }

    // Callbacks
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

    // Chunk Section

    public ChunkSection(Chunk chunk, int sectionY) {
        this.chunk = chunk;
        this.sectionY = sectionY;
        tryUpdateNearbyChunk(Directions.TOP);
        tryUpdateNearbyChunk(Directions.BOTTOM);
        tryUpdateNearbyChunk(Directions.NORTH);
        tryUpdateNearbyChunk(Directions.SOUTH);
        tryUpdateNearbyChunk(Directions.WEST);
        tryUpdateNearbyChunk(Directions.EAST);
    }

    public boolean tryUpdateNearbyChunk(int direction) {
        if (direction == Directions.TOP && chunk.hasSection(sectionY + 1)) { // TOP
            top().tryUpdate();
            return true;
        } else if (direction == Directions.BOTTOM && chunk.hasSection(sectionY - 1)) { // BOTTOM
            bottom().tryUpdate();
            return true;
        } else if (direction == Directions.NORTH && chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() - 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).hasSection(sectionY)) { // North
            north().tryUpdate();
            return true;
        } else if (direction == Directions.SOUTH && chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() + 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).hasSection(sectionY)) { // South
            south().tryUpdate();
            return true;
        } else if (direction == Directions.WEST && chunk.getWorld().hasChunk(chunk.getX() - 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).hasSection(sectionY)) { // West
            west().tryUpdate();
            return true;
        } else if (direction == Directions.EAST && chunk.getWorld().hasChunk(chunk.getX() + 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).hasSection(sectionY)) { // East
            east().tryUpdate();
            return true;
        }
        return false;
    }

    // Nearby Chunks

    public ChunkSection east() {
        if (chunk.getWorld().hasChunk(chunk.getX() + 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).hasSection(sectionY)) {
            return chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).getOrCreateChunkSection(sectionY);
        }
        return null;
    }

    public ChunkSection west() {
        if (chunk.getWorld().hasChunk(chunk.getX() - 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).hasSection(sectionY)) {
            return chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).getOrCreateChunkSection(sectionY);
        }
        return null;
    }

    public ChunkSection south() {
        if (chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() + 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).hasSection(sectionY)) {
            return chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).getOrCreateChunkSection(sectionY);
        }
        return null;
    }

    public ChunkSection north() {
        if (chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() - 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).hasSection(sectionY)) {
            return chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).getOrCreateChunkSection(sectionY);
        }
        return null;
    }

    public ChunkSection bottom() {
        if (chunk.hasSection(sectionY - 1)) {
            return chunk.getOrCreateChunkSection(sectionY - 1);
        }
        return null;
    }

    public ChunkSection top() {
        if (chunk.hasSection(sectionY + 1)) {
            return chunk.getOrCreateChunkSection(sectionY + 1);
        }
        return null;
    }

    public ChunkSection getNearby(int x, int y, int z, boolean createIfNotExist) {
        if (createIfNotExist) {
            chunk.getWorld().getOrCreateChunk(chunk.getX() + x, chunk.getY() + z).getOrCreateChunkSection(sectionY + y);
        } else {
            if (chunk.getWorld().hasChunk(chunk.getX() + x, chunk.getY() + z) && chunk.getWorld().getOrCreateChunk(chunk.getX() + x, chunk.getY() + z).hasSection(sectionY + y)) {
                return chunk.getWorld().getOrCreateChunk(chunk.getX() + x, chunk.getY() + z).getOrCreateChunkSection(sectionY + y);
            }
        }
        return null;
    }

    public ChunkSection getNearby(int x, int y, int z) {
        return getNearby(x, y, z, false);
    }

    //

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
//            if (x == 0) {
//                if (chunk.getWorld().hasChunk(chunk.getX() - 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).hasSection(sectionY)) { // West
//                    chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
//                }
//            }
//            if (x == 15) {
//                if (chunk.getWorld().hasChunk(chunk.getX() + 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).hasSection(sectionY)) { // West
//                    chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
//                }
//            }
//            if (z == 0) {
//                if (chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() - 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).hasSection(sectionY)) { // North
//                    chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
//                }
//            }
//            if (z == 15) {
//                if (chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() + 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).hasSection(sectionY)) { // South
//                    chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
//                }
//            }
//            if (y == 0) {
//                if (chunk.hasSection(sectionY - 1)) { // BOTTOM
//                    chunk.getOrCreateChunkSection(sectionY - 1).requiresModelUpdate = true;
//                }
//            }
//            if (y == 15) {
//                if (chunk.hasSection(sectionY + 1)) { // TOP
//                    chunk.getOrCreateChunkSection(sectionY + 1).requiresModelUpdate = true;
//                }
//            }
            Chunk chunk = getChunk();
            World world = chunk.getWorld();
            int sx = chunk.getX();
            int sy = chunk.getY();
            int sh = getSectionY();
            for (int i = sx - 1; i <= sx + 1; i++) {
                for (int j = sy - 1; j <= sy + 1; j++) {
                    for (int k = sh - 1; k <= sh + 1; k++) {
                        if (world.hasChunk(i, j) && world.getOrCreateChunk(i, j).hasSection(k)) {
                            world.getOrCreateChunk(i, j).getOrCreateChunkSection(k).tryUpdate();
                        }
                    }
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

    public boolean isTopSection() {
        return getChunk().getTopSection() == this;
    }

    public void onChunkSectionModelUpdate() {
        if (isTopSection()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    boolean source = true;
                    for (int y = 15; y >= 0; y--) {
                        if (getBlock(x, y, z).settings().material.blocksLight())
                            source = false;
                        if (source)
                            envLightInformation.setSource(15, x, y, z);
                        else
                            envLightInformation.removeSource(x, y, z);
                    }
                }
            }
        } else {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    boolean source = true;
                    if (getEnvLightLevel(x, 16, z) == 15) {
                        for (int y = 15; y >= 0; y--) {
                            if (getBlock(x, y, z).settings().material.blocksLight())
                                source = false;
                            if (source)
                                envLightInformation.setSource(15, x, y, z);
                            else
                                envLightInformation.removeSource(x, y, z);
                        }
                    } else {
                        for (int y = 15; y >= 0; y--) {
                            envLightInformation.removeSource(x, y, z);
                        }
                        envLightInformation.setLevel(getEnvLightLevel(x, 16, z), x, 15, z);
                    }
                }
            }
        }

        LightTraveller traveller = new LightTraveller(this, ChunkSection::getEnvLightInformation);
        if (traveller.perform()) {
            tryUpdate();
        }
        traveller.tryUpdateInvolvedChunks();

        for (ChunkEventCallbackI chunkModelUpdateCallbackI : chunkModelUpdateCallbackIs) {
            chunkModelUpdateCallbackI.execute(this);
        }
    }

    @Override
    public void onTick(double deltaTime) {
    }

    public void tryUpdate() {
        getChunk().getWorld().queueUpdatingSection(this);
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
