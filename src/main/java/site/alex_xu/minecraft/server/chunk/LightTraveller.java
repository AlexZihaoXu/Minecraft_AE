package site.alex_xu.minecraft.server.chunk;

import site.alex_xu.minecraft.server.block.Block;

import javax.security.auth.callback.Callback;
import java.util.LinkedList;
import java.util.TreeSet;

class LightTraveller {

    public ChunkSection section;
    public TreeSet<Integer> sources = new TreeSet<>();
    public LightInfoGetter lightInfoGetter;

    public LightTraveller(ChunkSection section, LightInfoGetter lightInfoGetter) {
        this.section = section;
        this.lightInfoGetter = lightInfoGetter;
    }

    private static int intFromXYZ(int x, int y, int z) {
        return ((x + 16) * 48 + (y + 16)) * 48 + (z + 16);
    }

    private static int xOf(int xyz) {
        return xyz / (48 * 48) - 16;
    }

    private static int yOf(int xyz) {
        return (xyz / 48) - (xOf(xyz) + 16) * 48 - 16;
    }

    private static int zOf(int xyz) {
        return xyz % 48 - 16;
    }

    public static boolean inChunkRange(int x, int y, int z) {
        return x >= 0 && x < 16 && y >= 0 && y < 16 && z >= 0 && z < 16;
    }

    public static byte max(byte a, byte b) {
        return a > b ? a : b;
    }

    public void addSource(int x, int y, int z) {
        sources.add(intFromXYZ(x, y, z));
    }

    public LightInformation getLightInformation() {
        return lightInfoGetter.execute(section);
    }

    public static int idOf(int x, int y, int z) {
        return (((x + 1) * 3 + (y + 1)) * 3 + (z + 1));
    }

    public void setLevel(byte level, int x, int y, int z) {
        if (inChunkRange(x, y, z)) {
            getLightInformation().setLevel(level, 0, x, y, z);
        } else {
            ChunkSection newSection = section.nearby(
                    Math.floorDiv(x, 16),
                    Math.floorDiv(y, 16),
                    Math.floorDiv(z, 16)
            );
            if (newSection != null)
                lightInfoGetter.execute(newSection).setLevel(level, idOf(Math.floorDiv(x, 16), Math.floorDiv(y, 16), Math.floorDiv(z, 16)), Math.floorMod(x, 16), Math.floorMod(y, 16), Math.floorMod(z, 16));
        }
    }

    public byte getLevel(int x, int y, int z) {
        if (inChunkRange(x, y, z)) {
            return getLightInformation().getLevel(x, y, z);
        } else {
            ChunkSection newSection = section.nearby(
                    Math.floorDiv(x, 16),
                    Math.floorDiv(y, 16),
                    Math.floorDiv(z, 16)
            );
            if (newSection != null)
                return lightInfoGetter.execute(newSection).getLevel(idOf(Math.floorDiv(x, 16), Math.floorDiv(y, 16), Math.floorDiv(z, 16)), Math.floorMod(x, 16), Math.floorMod(y, 16), Math.floorMod(z, 16));
        }
        return 0;
    }

    public void travel() {
        getLightInformation().reset();
        LinkedList<Integer> planned = new LinkedList<>(sources);
        boolean[] visited = new boolean[intFromXYZ(31, 31, 31)];

        for (Integer pos : planned) {
            setLevel((byte) 15, xOf(pos), yOf(pos), zOf(pos));
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (!(x == 0 && y == 0 && z == 0)) {
                        ChunkSection newSection = section.nearby(x, y, z);
                        if (newSection != null)
                            lightInfoGetter.execute(newSection).reset(idOf(x, y, z));
                    }
                }
            }
        }

        while (!planned.isEmpty()) {
            int rawPos = planned.removeLast();

            if (rawPos < 0 || rawPos >= visited.length)
                continue;

            int x = xOf(rawPos);
            int y = yOf(rawPos);
            int z = zOf(rawPos);
            if (visited[rawPos]) {
                continue;
            }
            visited[rawPos] = true;

            Block block = section.getBlock(x, y, z);
            if (block != null && block.settings().material.blocksLight()) {
                setLevel((byte) 0, x, y, z);
                continue;
            }

            byte level = getLevel(x, y, z);
            byte maxLevelNearby = 0;
            maxLevelNearby = max(maxLevelNearby, getLevel(x + 1, y, z));
            maxLevelNearby = max(maxLevelNearby, getLevel(x - 1, y, z));
            maxLevelNearby = max(maxLevelNearby, getLevel(x, y + 1, z));
            maxLevelNearby = max(maxLevelNearby, getLevel(x, y - 1, z));
            maxLevelNearby = max(maxLevelNearby, getLevel(x, y, z + 1));
            maxLevelNearby = max(maxLevelNearby, getLevel(x, y, z - 1));

            if (maxLevelNearby <= 1) {
                continue;
            }

            if (level < maxLevelNearby - 1) {
                level = (byte) (maxLevelNearby - 1);
                setLevel(level, x, y, z);
            }

            if (getLevel(x, y + 1, z) < level)
                planned.addFirst(intFromXYZ(x, y + 1, z));
            if (getLevel(x, y - 1, z) < level)
                planned.addFirst(intFromXYZ(x, y - 1, z));
            if (getLevel(x + 1, y, z) < level)
                planned.addFirst(intFromXYZ(x + 1, y, z));
            if (getLevel(x - 1, y, z) < level)
                planned.addFirst(intFromXYZ(x - 1, y, z));
            if (getLevel(x, y, z + 1) < level)
                planned.addFirst(intFromXYZ(x, y, z + 1));
            if (getLevel(x, y, z - 1) < level)
                planned.addFirst(intFromXYZ(x, y, z - 1));

        }
    }

    interface LightInfoGetter extends Callback {
        LightInformation execute(ChunkSection section);
    }
}
