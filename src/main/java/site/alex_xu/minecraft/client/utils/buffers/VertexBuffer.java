package site.alex_xu.minecraft.client.utils.buffers;

import static org.lwjgl.opengl.GL15.*;

public class VertexBuffer extends BufferType {

    public VertexBuffer(float[] vertexes) {
        length = vertexes.length;
        id = glGenBuffers();
        if (id == 0) {
            String message = "Unable create vertex buffer object!";
            getLogger().error(message);
            throw new IllegalStateException(message);
        }
        bind();
        glBufferData(GL_ARRAY_BUFFER, vertexes, GL_STATIC_DRAW);
    }

    @Override
    protected void onDispose() {
        glDeleteBuffers(id);
    }

    @Override
    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    @Override
    protected void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
