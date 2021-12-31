package site.alex_xu.minecraft.server.chunk;

public class LightInformation {
    private byte[][][][] lightsMap;

    public LightInformation() {
        reset();
    }

    public void setLevel(byte level, int id, int x, int y, int z) {
        lightsMap[x][y][z][id] = level;
    }

    public void setLevel(byte level, int x, int y, int z) {
        setLevel(level, 0, x, y, z);
    }

    public byte getLevel(int id, int x, int y, int z) {
        return lightsMap[x][y][z][id];
    }

    public byte getLevel(int x, int y, int z) {
        return getLevel(0, x, y, z);
    }

    public byte getMaxLevel(int x, int y, int z) {
        byte max = -1;
        for (byte level : lightsMap[x][y][z]) {
            if (level > max)
                max = level;
        }
        return max;
    }

    void reset(int id) {
        if (lightsMap == null)
            lightsMap = new byte[16][16][16][27];
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    lightsMap[x][y][z][id] = 0;
                }
            }
        }
    }

    void reset() {
        reset(0);
    }
}
