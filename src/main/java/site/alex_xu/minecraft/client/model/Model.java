package site.alex_xu.minecraft.client.model;

import org.joml.Matrix4f;
import site.alex_xu.minecraft.client.utils.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.utils.buffers.VertexArray;
import site.alex_xu.minecraft.client.utils.buffers.VertexBuffer;
import site.alex_xu.minecraft.core.MinecraftAECore;

import static org.lwjgl.opengl.GL11.*;

public class Model extends MinecraftAECore {
    protected VertexArray vao;
    protected VertexBuffer vbo;
    protected ElementBuffer ebo;
    protected Matrix4f modelMatrix;

    protected Model(VertexArray vao, VertexBuffer vbo, ElementBuffer ebo) {
        this.vao = vao;
        this.vbo = vbo;
        this.ebo = ebo;
        resetModelMatrix();
    }

    public Model resetModelMatrix() {
        modelMatrix = new Matrix4f(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
        return this;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public void draw() {
        vao.bind();
        ebo.bind();
        glDrawElements(GL_TRIANGLES, ebo.length(), GL_UNSIGNED_INT, 0);
    }

    public void free() {
        this.vao.free();
        this.vbo.free();
        this.ebo.free();
    }
}
