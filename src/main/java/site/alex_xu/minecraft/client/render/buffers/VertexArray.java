package site.alex_xu.minecraft.client.render.buffers;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class VertexArray extends BufferType{
    public static class VertexArrayLayout {
        private final VertexArray vao;
        private final VertexBuffer vbo;
        private int stride = 0;
        private final ArrayList<Integer> pushedList = new ArrayList<>();

        protected VertexArrayLayout(VertexArray vao, VertexBuffer vbo) {
            this.vao = vao;
            this.vbo = vbo;
        }

        public VertexArrayLayout push(int count) {
            pushedList.add(count);
            stride += count * 4;

            return this;
        }

        public void apply() {
            vao.bind();
            vbo.bind();

            int offset = 0;

            for (int i = 0; i < pushedList.size(); i++) {
                glEnableVertexAttribArray(i);
                glVertexAttribPointer(i, pushedList.get(i), GL_FLOAT, false, stride, offset);
                offset += pushedList.get(i) * 4;
            }
        }
    }

    public VertexArray() {
        id = glGenVertexArrays();
        if (id == 0) {
            String reason = "Unable create vertex array object!";
            getLogger().error(reason);
            throw new IllegalStateException(reason);
        }
        bind();
    }

    public VertexArrayLayout configure(VertexBuffer vbo) {
        return new VertexArrayLayout(this, vbo);
    }

    @Override
    protected void onDispose() {
        glDeleteVertexArrays(id);
    }

    @Override
    public void bind() {
        glBindVertexArray(id);
    }

    @Override
    protected void unbind() {
        glBindVertexArray(0);
    }
}
