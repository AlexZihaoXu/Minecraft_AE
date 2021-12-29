package site.alex_xu.minecraft.server.chunk;

import org.apache.logging.log4j.LogManager;
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

class LightTraveller {
    ChunkSection section;
    HashSet<Vector3i> sources = new HashSet<>();
    int[][][] lightLevels = new int[48][48][48];
    LightLevelGetter lightLevelGetter;

    public void printDebug(String info) {
        if (ChunkSection.printDebugInfo)
            LogManager.getLogger("Light Traveller").debug(info);
    }

    public LightTraveller(ChunkSection section, LightLevelGetter lightLevelGetter) {
        this.section = section;
        this.lightLevelGetter = lightLevelGetter;
    }

    public void addSources(Collection<Vector3i> sources, int x, int y, int z) {
        for (Vector3i source : sources) {
            this.sources.add(new Vector3i(source.x + x, source.y + y, source.z + z));
        }
    }

    public void autoAddSource(SourceSelectorFunc selector) {
        addSources(selector.execute(section), 0, 0, 0); // Center
        Chunk chunk = section.getChunk();
        World world = chunk.getWorld();
        int sx = chunk.getX();
        int sy = chunk.getY();
        int sh = section.getSectionY();
        for (int x = sx - 1; x <= sx + 1; x++) {
            for (int y = sy - 1; y <= sy + 1; y++) {
                for (int h = sh - 1; h <= sh + 1; h++) {
                    if (world.hasChunk(x, y) && world.getOrCreateChunk(x, y).hasSection(h)) {
                        addSources(selector.execute(world.getOrCreateChunk(x, y).getOrCreateChunkSection(h)), (x - sx) * 16, (h - sh) * 16, (y - sy) * 16);
                    }
                }
            }
        }
    }

    public void setLevel(int level, int x, int y, int z) {
//        if ((x < 0 || x >= 16 || y < 0 || y >= 16 || z < 0 || z >= 16) && level != lightLevelGetter.execute(section, x, y, z)) {
//            if (x > 0 && section.east() != null) {
//                section.tryUpdateNearbyChunk(Directions.EAST);
//            }
//            if (x < 0 && section.west() != null) {
//                section.tryUpdateNearbyChunk(Directions.WEST);
//            }
//        }
        lightLevels[x + 16][y + 16][z + 16] = level;
    }

    public int getLevel(int x, int y, int z) {
        if (inRange(x, y, z)) {
            return lightLevels[x + 16][y + 16][z + 16];
        }
        return lightLevelGetter.execute(section, x, y, z);
    }

    public int[][][] travel() {

        boolean[][][] visited = new boolean[48][48][48];

        LinkedList<Vector3i> planned = new LinkedList<>();

        for (Vector3i source : sources) {
            planned.add(source);
            setLevel(15, source.x, source.y, source.z);
        }

        while (!planned.isEmpty()) {
            Vector3i pos = planned.removeLast();
            int x = pos.x;
            int y = pos.y;
            int z = pos.z;

            Block block = section.getBlock(x, y, z);
            if (inRange(x, y, z) && !visited[x + 16][y + 16][z + 16] && !(block == null ? Blocks.AIR : block).settings().material.blocksLight()) {
                visited[x + 16][y + 16][z + 16] = true;


                int nearbyMaximumLevel = 0;
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x + 1, y, z));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x - 1, y, z));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y + 1, z));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y - 1, z));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y, z + 1));
                nearbyMaximumLevel = max(nearbyMaximumLevel, getLevel(x, y, z - 1));

                setLevel(max(getLevel(x, y, z), nearbyMaximumLevel - 1), x, y, z);

                if (getLevel(x + 1, y, z) != 15)
                    planned.addFirst(new Vector3i(x + 1, y, z));
                if (getLevel(x - 1, y, z) != 15)
                    planned.addFirst(new Vector3i(x - 1, y, z));
                if (getLevel(x, y + 1, z) != 15)
                    planned.addFirst(new Vector3i(x, y + 1, z));
                if (getLevel(x, y - 1, z) != 15)
                    planned.addFirst(new Vector3i(x, y - 1, z));
                if (getLevel(x, y, z + 1) != 15)
                    planned.addFirst(new Vector3i(x, y, z + 1));
                if (getLevel(x, y, z - 1) != 15)
                    planned.addFirst(new Vector3i(x, y, z - 1));
            }
        }
        int[][][] result = new int[16][16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    result[x][z][y] = getLevel(x, y, z);
                }
            }
        }
        return result;
    }

    public boolean inRange(int x, int y, int z) {
        return -16 <= x && x < 32 && -16 <= y && y < 32 && -16 <= z && z < 32;
    }

    interface SourceSelectorFunc extends Callback {
        HashSet<Vector3i> execute(ChunkSection section);
    }

    interface LightLevelGetter extends Callback {
        int execute(ChunkSection section, int x, int y, int z);
    }

}

public class ChunkSection extends MinecraftAECore implements Tickable {
    public static boolean printDebugInfo = false;

    private final Block[][][] blocks = new Block[16][16][16];
    private final HashSet<Vector3i> envLightSources = new HashSet<>();
    private final HashSet<Vector3i> blockLightSources = new HashSet<>();
    final int[][][] blockLightLevels = new int[16][16][16];
    int[][][] envLightLevels = new int[16][16][16];
    private final HashSet<ChunkEventCallbackI> chunkModelUpdateCallbackIs = new HashSet<>();
    private final HashSet<ChunkEventCallbackI> chunkDisposeCallbackIs = new HashSet<>();
    private boolean requiresModelUpdate = false;
    private final Chunk chunk;
    private final int sectionY;
    private final LinkedList<ChunkSection> tryUpdatingSections = new LinkedList<>();

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
            tryUpdatingSections.add(top());
            return true;
        } else if (direction == Directions.BOTTOM && chunk.hasSection(sectionY - 1)) { // BOTTOM
            tryUpdatingSections.add(bottom());
            return true;
        } else if (direction == Directions.NORTH && chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() - 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() - 1).hasSection(sectionY)) { // North
            tryUpdatingSections.add(north());
            return true;
        } else if (direction == Directions.SOUTH && chunk.getWorld().hasChunk(chunk.getX(), chunk.getY() + 1) && chunk.getWorld().getOrCreateChunk(chunk.getX(), chunk.getY() + 1).hasSection(sectionY)) { // South
            tryUpdatingSections.add(south());
            return true;
        } else if (direction == Directions.WEST && chunk.getWorld().hasChunk(chunk.getX() - 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() - 1, chunk.getY()).hasSection(sectionY)) { // West
            tryUpdatingSections.add(west());
            return true;
        } else if (direction == Directions.EAST && chunk.getWorld().hasChunk(chunk.getX() + 1, chunk.getY()) && chunk.getWorld().getOrCreateChunk(chunk.getX() + 1, chunk.getY()).hasSection(sectionY)) { // East
            tryUpdatingSections.add(east());
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

    private void calculateLights() {
        envLightSources.clear();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 16; y >= 0; y--) {
                    if (getBlock(x, y, z).settings().material.blocksLight())
                        break;
                    envLightSources.add(new Vector3i(x, y, z));
                }
            }
        }

        LightTraveller traveler = new LightTraveller(this, ChunkSection::getEnvironmentLightLevel);
        traveler.autoAddSource(section -> section.envLightSources);
        envLightLevels = traveler.travel();
    }

    public int getLightLevelAt(int x, int y, int z) {
        return max(getBlockLightLevel(x, y, z), getEnvironmentLightLevel(x, y, z));
    }

    protected void modelUpdate() {
        calculateLights();
        for (ChunkEventCallbackI chunkModelUpdateCallbackI : chunkModelUpdateCallbackIs) {
            chunkModelUpdateCallbackI.execute(this);
        }
        for (ChunkSection tryUpdatingSection : tryUpdatingSections) {
            tryUpdatingSection.requiresModelUpdate = true;
        }
        tryUpdatingSections.clear();
    }

    @Override
    public void onTick(double deltaTime) {
        if (requiresModelUpdate) {
            modelUpdate();
            requiresModelUpdate = false;
        }
    }

    public void tryUpdate() {
        requiresModelUpdate = true;
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
