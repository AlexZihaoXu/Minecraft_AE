package site.alex_xu.minecraft.server.chunk;

import org.joml.Vector3i;

import javax.security.auth.callback.Callback;
import java.util.HashSet;
import java.util.LinkedList;

import static java.lang.Math.floorDiv;
import static java.lang.Math.floorMod;

final class LightTraveller {

    private final LightInformationGetter _lightInformationGetter;
    private final ChunkSection section;
    private final HashSet<ChunkSection> changedChunks = new HashSet<>();

    public LightTraveller(ChunkSection section, LightInformationGetter lightInformationGetter) {
        this._lightInformationGetter = lightInformationGetter;
        this.section = section;
    }

    public LightInformation lightInformationOf(ChunkSection section) {
        return _lightInformationGetter.execute(section);
    }

    public boolean inChunkRange(int x, int y, int z) {
        return 0 <= x && x < 16 && 0 <= y && y < 16 && 0 <= z && z < 16;
    }

    public byte getLevel(int x, int y, int z) {
        if (inChunkRange(x, y, z)) {
            return lightInformationOf(section).getLevel(x, y, z);
        } else {
            ChunkSection s = section.getNearby(floorDiv(x, 16), floorDiv(y, 16), floorDiv(z, 16));
            if (s != null) {
                return lightInformationOf(s).getLevel(floorMod(x, 16), floorMod(y, 16), floorMod(z, 16));
            }
        }
        return 0;
    }

    public void setLevel(byte level, int x, int y, int z) {
        if (inChunkRange(x, y, z)) {
            lightInformationOf(section).setLevel(level, x, y, z);
        } else {
            ChunkSection s = section.getNearby(floorDiv(x, 16), floorDiv(y, 16), floorDiv(z, 16));
            if (s != null) {
                changedChunks.add(s);
            }
        }
    }

    public boolean isSource(int x, int y, int z) {
        if (inChunkRange(x, y, z)) {
            return lightInformationOf(section).getLightSources().contains(new Vector3i(x, y, z));
        } else {
            ChunkSection s = section.getNearby(floorDiv(x, 16), floorDiv(y, 16), floorDiv(z, 16));
            if (s != null) {
                return lightInformationOf(s).getLightSources().contains(new Vector3i(floorMod(x, 16), floorMod(y, 16), floorMod(z, 16)));
            }
        }
        return false;
    }

    public void tryUpdateInvolvedChunks() {
        for (ChunkSection changedChunk : changedChunks) {
            changedChunk.tryUpdate();
        }
    }

    public static byte max(byte a, byte b) {
        return a > b ? a : b;
    }

    public byte getDesiredLevel(int x, int y, int z) {
        byte level = getLevel(x, y, z);
        return max(level, (byte) (getNearbyMaxLevel(x, y, z) - 1));
    }

    public byte getNearbyMaxLevel(int x, int y, int z) {
        byte nearbyMaximum = 0;
        nearbyMaximum = max(nearbyMaximum, getLevel(x + 1, y, z));
        nearbyMaximum = max(nearbyMaximum, getLevel(x - 1, y, z));
        nearbyMaximum = max(nearbyMaximum, getLevel(x, y + 1, z));
        nearbyMaximum = max(nearbyMaximum, getLevel(x, y - 1, z));
        nearbyMaximum = max(nearbyMaximum, getLevel(x, y, z + 1));
        nearbyMaximum = max(nearbyMaximum, getLevel(x, y, z - 1));
        return nearbyMaximum;
    }

    /**
     * @return true if any change had been done
     */
    public boolean decayAndSpread() {
        boolean changed = false;

        LinkedList<Vector3i> planned = new LinkedList<>();
        for (Vector3i lightSource : lightInformationOf(section).getLightSources()) {
            planned.add(lightSource);
            setLevel(lightInformationOf(section).getSourceLevel(lightSource.x, lightSource.y, lightSource.z), lightSource.x, lightSource.y, lightSource.z);
        }

        HashSet<Vector3i> visited = new HashSet<>();

        while (!planned.isEmpty()) {
            Vector3i pos = planned.removeLast();
            int x = pos.x;
            int y = pos.y;
            int z = pos.z;

            if (visited.contains(pos)) {
                continue;
            }
            visited.add(pos);
            if (!inChunkRange(x, y, z)) {
                continue;
            }

            if (isSource(x, y, z)) {
                setLevel(getLevel(x, y, z), x, y, z);
            } else {
                if (section.getBlock(x, y, z).settings().material.blocksLight()) {
                    setLevel((byte) 0, x, y, z);
                } else {
                    byte level = getLevel(x, y, z);
                    byte desiredLevel = getDesiredLevel(x, y, z);
                    if (desiredLevel != level) {
                        changed = true;
                        setLevel(desiredLevel, x, y, z);
                        tryAddNearbyChunkAsInvolvedChunk(x, y, z);
                    }

                    planned.addFirst(new Vector3i(x + 1, y, z));
                    planned.addFirst(new Vector3i(x - 1, y, z));
                    planned.addFirst(new Vector3i(x, y + 1, z));
                    planned.addFirst(new Vector3i(x, y - 1, z));
                    planned.addFirst(new Vector3i(x, y, z + 1));
                    planned.addFirst(new Vector3i(x, y, z - 1));

                }
            }

        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 16; y++) {
                    if (isSource(x, y, z))
                        continue;
                    byte desiredLevel = max((byte) 0, (byte) (getNearbyMaxLevel(x, y, z) - 1));
                    if (section.getBlock(x, y, z).settings().material.blocksLight()) {
                        desiredLevel = 0;
                    }
                    if (getLevel(x, y, z) != desiredLevel) {
                        setLevel(desiredLevel, x, y, z);
                        changed = true;
                        tryAddNearbyChunkAsInvolvedChunk(x, y, z);
                    }
                }
            }
        }

        return changed;
    }

    public boolean perform() {
        long time = System.currentTimeMillis();
        long now = time;
        while (now - time < 50) {
            now = System.currentTimeMillis();
            if (!decayAndSpread())
                return false;
        }
        return true;
    }

    private void tryAddNearbyChunkAsInvolvedChunk(int x, int y, int z) {
        if (x == 0) {
            if (section.west() != null) {
                changedChunks.add(section.west());
            }
        }
        if (x == 15) {
            if (section.east() != null) {
                changedChunks.add(section.east());
            }
        }
        if (y == 0) {
            if (section.bottom() != null) {
                changedChunks.add(section.bottom());
            }
        }
        if (y == 15) {
            if (section.top() != null) {
                changedChunks.add(section.top());
            }
        }
        if (z == 0) {
            if (section.north() != null) {
                changedChunks.add(section.north());
            }
        }
        if (z == 15) {
            if (section.south() != null) {
                changedChunks.add(section.south());
            }
        }
    }

    public interface LightInformationGetter extends Callback {
        LightInformation execute(ChunkSection section);
    }
}
