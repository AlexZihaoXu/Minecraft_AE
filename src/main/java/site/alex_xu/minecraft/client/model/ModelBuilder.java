package site.alex_xu.minecraft.client.model;

import site.alex_xu.minecraft.client.utils.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.utils.buffers.VertexArray;
import site.alex_xu.minecraft.client.utils.buffers.VertexBuffer;
import site.alex_xu.minecraft.core.MinecraftAECore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

public class ModelBuilder extends MinecraftAECore {
    public static class Vertex {
        float x, y, z, r, g, b, a, tx, ty;

        public Vertex(float x, float y, float z, float r, float g, float b, float a, float tx, float ty) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.tx = tx;
            this.ty = ty;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Vertex vertex = (Vertex) o;

            if (Float.compare(vertex.x, x) != 0) return false;
            if (Float.compare(vertex.y, y) != 0) return false;
            if (Float.compare(vertex.z, z) != 0) return false;
            if (Float.compare(vertex.r, r) != 0) return false;
            if (Float.compare(vertex.g, g) != 0) return false;
            if (Float.compare(vertex.b, b) != 0) return false;
            if (Float.compare(vertex.a, a) != 0) return false;
            if (Float.compare(vertex.tx, tx) != 0) return false;
            return Float.compare(vertex.ty, ty) == 0;
        }

        @Override
        public int hashCode() {
            int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
            result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
            result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
            result = 31 * result + (r != +0.0f ? Float.floatToIntBits(r) : 0);
            result = 31 * result + (g != +0.0f ? Float.floatToIntBits(g) : 0);
            result = 31 * result + (b != +0.0f ? Float.floatToIntBits(b) : 0);
            result = 31 * result + (a != +0.0f ? Float.floatToIntBits(a) : 0);
            result = 31 * result + (tx != +0.0f ? Float.floatToIntBits(tx) : 0);
            result = 31 * result + (ty != +0.0f ? Float.floatToIntBits(ty) : 0);
            return result;
        }
    }

    protected HashMap<Vertex, Integer> indexMap = new HashMap<>(); // Vertices to Index map
    protected ArrayList<Float> vertexBuffer = new ArrayList<>(); // Future VBO
    protected ArrayList<Integer> indexBuffer = new ArrayList<>(); // Future EBO

    public ModelBuilder() {
    }

    /**
     * @return Index of generated vertex in vbo.
     */
    public int vertex(Vertex vertex) {
        if (!indexMap.containsKey(vertex)) {
            vertexBuffer.add(vertex.x);
            vertexBuffer.add(vertex.y);
            vertexBuffer.add(vertex.z);
            vertexBuffer.add(vertex.r);
            vertexBuffer.add(vertex.g);
            vertexBuffer.add(vertex.b);
            vertexBuffer.add(vertex.a);
            vertexBuffer.add(vertex.tx);
            vertexBuffer.add(vertex.ty);
            indexMap.put(vertex, indexMap.size());
        }
        return indexMap.get(vertex);
    }

    public int vertex(float x, float y, float z, float r, float g, float b, float a, float tx, float ty) {
        return vertex(new Vertex(x, y, z, r, g, b, a, tx, ty));
    }

    /**
     * @param v1 vertex 1
     * @param v2 vertex 2
     * @param v3 vertex 3
     *           Add a triangle face based on the 3 vertices provided
     *           NOTE: order must be clockwise!
     */
    public ModelBuilder addFace(int v1, int v2, int v3) {
        indexBuffer.add(v1);
        indexBuffer.add(v2);
        indexBuffer.add(v3);
        return this;
    }

    /**
     * @param v1 vertex 1
     * @param v2 vertex 2
     * @param v3 vertex 3
     * @param v4 vertex 4
     *           Add a quad face based on the 4 vertices provided
     *           NOTE: order must be clockwise!
     */
    public ModelBuilder addFace(int v1, int v2, int v3, int v4) {
        addFace(v1, v2, v3);
        addFace(v1, v3, v4);
        return this;
    }

    public Model build() {
        VertexArray vao = new VertexArray();

        float[] vboData = new float[vertexBuffer.size()];
        for (int i = 0; i < vertexBuffer.size(); i++) {
            vboData[i] = vertexBuffer.get(i);
        }
        int[] eboData = new int[indexBuffer.size()];
        for (int i = 0; i < indexBuffer.size(); i++) {
            eboData[i] = indexBuffer.get(i);
        }

        VertexBuffer vbo = new VertexBuffer(vboData);
        ElementBuffer ebo = new ElementBuffer(eboData);

        vao.configure(vbo)
                .push(3) // x, y, z
                .push(4) // r, g, b, a
                .push(2) // tx, ty
                .apply();

        return new Model(vao, vbo, ebo);
    }
}
