package site.alex_xu.minecraft.server;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.joml.Math.*;

public final class Directions {
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int WEST = 2;
    public static final int EAST = 3;
    public static final int TOP = 4;
    public static final int BOTTOM = 5;

    public static Vector2f lookAt(Vector3f camPos, Vector3f targetPos) {
        Vector2f result = new Vector2f(
                -atan2(camPos.y - targetPos.y, sqrt((camPos.x - targetPos.x) * (camPos.x - targetPos.x) + (camPos.z - targetPos.z) * (camPos.z - targetPos.z))),
                atan2(camPos.z - targetPos.z, camPos.x - targetPos.x)
        );
        return result;
    }
}
