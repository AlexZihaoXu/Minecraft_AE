package site.alex_xu.minecraft.server.chunk;

import org.joml.Vector3i;

import java.util.HashSet;
import java.util.Iterator;

public class LightInformation {
    private final byte[][][][] lightMap = new byte[16][16][16][2];
    private final HashSet<Vector3i> lightSources = new HashSet<>();

    public LightInformation() {
    }

    public byte getLevel(int x, int y, int z) {
        return lightMap[x][y][z][0];
    }

    public void setLevel(byte level, int x, int y, int z) {
        lightMap[x][y][z][0] = level;
    }

    public void setLevel(int level, int x, int y, int z) {
        setLevel((byte) level, x, y, z);
    }

    public void setSource(byte level, int x, int y, int z) {
        lightSources.add(new Vector3i(x, y, z));
        lightMap[x][y][z][1] = level;
    }

    public void setSource(int level, int x, int y, int z) {
        setSource((byte) level, x, y, z);
    }

    public void removeSource(int x, int y, int z) {
        Vector3i pos = new Vector3i(x, y, z);
        lightSources.remove(pos);
        lightMap[x][y][z][1] = 0;
    }

    public HashSet<Vector3i> getLightSources() {
        return lightSources;
    }

    public byte getSourceLevel(int x, int y, int z) {
        return lightMap[x][y][z][1];
    }
}
