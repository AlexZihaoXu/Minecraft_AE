package site.alex_xu.minecraft.server.chunk;

import org.joml.Vector3i;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Tickable;
import site.alex_xu.minecraft.server.Directions;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.world.World;

import javax.security.auth.callback.Callback;
import java.util.*;

import static org.joml.Math.max;

class LightTraveler {
    public HashSet<Vector3i> lightSources = new HashSet<>();
    public int[][][] lightLevels;
    public ChunkSection section;
    public GetLightLevelFunc getLightLevel;

    public LightTraveler(ChunkSection section, GetLightLevelFunc lightLevelFunc) {
        this.lightLevels = new int[16][16][16];
        this.section = section;
        this.getLightLevel = lightLevelFunc;
    }

    public void addLightSource(int x, int y, int z) {
        lightSources.add(new Vector3i(x, y, z));
    }

    public void travel() {
        boolean[][][] visited = new boolean[16][16][16];
        LinkedList<Vector3i> planned = new LinkedList<>();

        for (Vector3i sourcePos : lightSources) {
            int x = sourcePos.x;
            int y = sourcePos.y;
            int z = sourcePos.z;
            planned.push(new Vector3i(x, y, z));
            setLevel(15, x, y, z);
        }

        int stage = 0;
        while (stage < 2) {
            Vector3i pos = planned.removeFirst();
            int x = pos.x;
            int y = pos.y;
            int z = pos.z;

            if (inRange(x, y, z) && !visited[x][z][y]) {
                visited[x][z][y] = true;
                int level = getLevel(x, y, z);
                if (section.getBlock(x, y, z).settings().material.blocksLight()) {
                    setLevel(0, x, y, z);
                } else {
                    int nearbyMaximumLevel = 0;
                    nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x + 1, y, z));
                    nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x - 1, y, z));
                    nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y + 1, z));
                    nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y - 1, z));
                    nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y, z + 1));
                    nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y, z - 1));

                    level = max(level, nearbyMaximumLevel - 1);
                    setLevel(level, x, y, z);
                    planned.addLast(new Vector3i(x + 1, y, z));
                    planned.addLast(new Vector3i(x - 1, y, z));
                    planned.addLast(new Vector3i(x, y + 1, z));
                    planned.addLast(new Vector3i(x, y - 1, z));
                    planned.addLast(new Vector3i(x, y, z + 1));
                    planned.addLast(new Vector3i(x, y, z - 1));
                }
            } else if (!inRange(x, y, z)) {
                int nearbyMaximumLevel = 0;
                int level = getLevel(x, y, z);
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x + 1, y, z));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x - 1, y, z));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y + 1, z));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y - 1, z));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y, z + 1));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y, z - 1));
                if (level != nearbyMaximumLevel - 1)
                    if (x > 15) {
                        section.tryUpdateNearbyChunk(Directions.EAST);
                    } else if (x < 0) {
                        section.tryUpdateNearbyChunk(Directions.WEST);
                    } else if (y > 15) {
                        section.tryUpdateNearbyChunk(Directions.TOP);
                    } else if (y < 0) {
                        section.tryUpdateNearbyChunk(Directions.BOTTOM);
                    } else if (z > 15) {
                        section.tryUpdateNearbyChunk(Directions.NORTH);
                    } else {
                        section.tryUpdateNearbyChunk(Directions.SOUTH);
                    }
            }
            if (planned.isEmpty()) {
                if (stage == 0) {
                    for (int ex = 0; ex < 16; ex++) {
                        for (int ey = 0; ey < 16; ey++) {
                            planned.addLast(new Vector3i(ex, 15, ey)); // TOP
                            planned.addLast(new Vector3i(ex, 0, ey)); // BOTTOM
                            planned.addLast(new Vector3i(ex, ey, 0)); // NORTH
                            planned.addLast(new Vector3i(ex, ey, 15)); // SOUTH
                            planned.addLast(new Vector3i(15, ex, ey)); // EAST
                            planned.addLast(new Vector3i(0, ex, ey)); // WEST
                        }
                    }
                }
                stage++;
            }
        }
    }

    protected boolean inRange(int x, int y, int z) {
        return 0 <= x && x < 16 && 0 <= y && y < 16 && 0 <= z && z < 16;
    }

    protected int getLevel(int x, int y, int z) {
        if (inRange(x, y, z)) {
            return lightLevels[x][z][y];
        }
        return getLightLevel.execute(section, x, y, z);
    }

    protected void setLevel(int level, int x, int y, int z) {
        lightLevels[x][z][y] = level;
    }

    public int[][][] getLightLevels() {
        return lightLevels;
    }

    public static int getEnvLights(ChunkSection section, int x, int y, int z) {
        return section.getEnvironmentLightLevel(x, y, z);
    }

    public static int getBlockLights(ChunkSection section, int x, int y, int z) {
        return section.getBlockLightLevel(x, y, z);
    }

    protected interface GetLightLevelFunc extends Callback {
        int execute(ChunkSection section, int x, int y, int z);
    }
}

public class ChunkSection extends MinecraftAECore implements Tickable {
    private final Block[][][] blocks = new Block[16][16][16];
    final int[][][] blockLightLevels = new int[16][16][16];
    int[][][] envLightLevels = new int[16][16][16];
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
        tryUpdateNearbyChunk(Directions.TOP);
        tryUpdateNearbyChunk(Directions.BOTTOM);
        tryUpdateNearbyChunk(Directions.NORTH);
        tryUpdateNearbyChunk(Directions.SOUTH);
        tryUpdateNearbyChunk(Directions.WEST);
        tryUpdateNearbyChunk(Directions.EAST);
    }

    public boolean tryUpdateNearbyChunk(int direction) {
        if (direction == Directions.TOP && chunk.hasSection(sectionY + 1)) { // TOP
            chunk.getOrCreateChunkSection(sectionY + 1).requiresModelUpdate = true;
            return true;
        } else if (direction == Directions.BOTTOM && chunk.hasSection(sectionY - 1)) { // BOTTOM
            chunk.getOrCreateChunkSection(sectionY - 1).requiresModelUpdate = true;
            return true;
        } else if (direction == Directions.NORTH && chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() - 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).hasSection(sectionY)) { // North
            chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
            return true;
        } else if (direction == Directions.SOUTH && chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() + 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).hasSection(sectionY)) { // South
            chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
            return true;
        } else if (direction == Directions.WEST && chunk.getWorld().hasChunk(chunk.getX() - 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).hasSection(sectionY)) { // West
            chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
            return true;
        } else if (direction == Directions.EAST && chunk.getWorld().hasChunk(chunk.getX() + 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).hasSection(sectionY)) { // East
            chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).getOrCreateChunkSection(sectionY).requiresModelUpdate = true;
            return true;
        }
        return false;
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
        LightTraveler traveler = new LightTraveler(this, LightTraveler::getEnvLights);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 15; y >= 0; y--) {
                    Block block = getBlock(x, y, z);
                    if (block.settings().material.blocksLight())
                        break;
                    traveler.addLightSource(x, y, z);
                }
            }
        }
        traveler.travel();
        envLightLevels = traveler.getLightLevels();

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
