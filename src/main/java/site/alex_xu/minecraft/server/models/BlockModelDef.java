package site.alex_xu.minecraft.server.models;

import site.alex_xu.minecraft.core.MinecraftAECore;

import java.util.Arrays;
import java.util.HashMap;

public class BlockModelDef extends MinecraftAECore {

    protected record Vertex(float x, float y, float z) {
    }

    protected record Face(String name, int v1, int v2, int v3, int v4) {
    }

    public HashMap<Integer, Vertex> vertexMap = new HashMap<>();
    public HashMap<String, Face> faceMap = new HashMap<>();
    public HashMap<String, String> texturePathMap = new HashMap<>();

    public BlockModelDef() {
    }

    public BlockModelDef vertex(int index, float x, float y, float z) {
        vertexMap.put(index, new Vertex(x, y, z));
        return this;
    }

    public BlockModelDef face(String name, int v1, int v2, int v3, int v4) {
        faceMap.put(name, new Face(name, v1, v2, v3, v4));
        return this;
    }

    public BlockModelDef setFaceTexture(String name, String path) {
        texturePathMap.put(name, path);
        return this;
    }
}
