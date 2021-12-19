package site.alex_xu.minecraft.client.utils.buffers;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.*;

public class ElementBuffer extends BufferType {
    private int length = 0;

    public ElementBuffer(int[] indexes) {
        id = glGenBuffers();
        length = indexes.length;
        if (id == 0) {
            String message = "Unable create element buffer object!";
            getLogger().error(message);
            throw new IllegalStateException(message);
        }
        bind();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexes, GL_STATIC_DRAW);
    }

    @Override
    protected void onDispose() {
        glDeleteBuffers(id);
    }

    @Override
    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    @Override
    protected void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int length() {
        return length;
    }

    public void draw(int mode) {
        glDrawElements(mode, length, GL_UNSIGNED_INT, 0);
    }

    public void draw() {
        draw(GL_TRIANGLES);
    }
}
