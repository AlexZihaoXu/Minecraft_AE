package site.alex_xu.minecraft.client.render;

import org.joml.Matrix4f;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.utils.BindableContext;
import site.alex_xu.minecraft.client.utils.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.utils.buffers.VertexArray;
import site.alex_xu.minecraft.client.utils.buffers.VertexBuffer;
import site.alex_xu.minecraft.client.utils.shader.Shader;
import site.alex_xu.minecraft.server.collision.Hitbox;

import static org.lwjgl.opengl.GL11.*;

public class GameObjectRenderer extends ModelRenderer {

    private static VertexBuffer vbo;
    private static VertexArray vao;
    private static ElementBuffer lineEbo;
    private static ElementBuffer fillEbo;
    private Matrix4f modelMat;
    private static Shader shader;
    private float r, g, b, a;

    public GameObjectRenderer color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    public GameObjectRenderer(BindableContext bindableContext) {
        super(bindableContext);
        r = g = b = a = 1;
        if (shader == null) {
            shader = new Shader()
                    .addFromResource("assets/shaders/outline.frag")
                    .addFromResource("assets/shaders/outline.vert")
                    .link();
            vao = new VertexArray();
            vbo = new VertexBuffer(new float[]{
                    0, 0, 1,
                    0, 1, 1,
                    1, 1, 1,
                    1, 0, 1,
                    0, 0, 0,
                    0, 1, 0,
                    1, 1, 0,
                    1, 0, 0,
            });
            vao.configure(vbo)
                    .push(3)
                    .apply();
            lineEbo = new ElementBuffer(new int[]{
                    0, 1, 3, 2, 7, 6, 4, 5,
                    0, 3, 4, 7, 5, 6, 1, 2,
                    1, 5, 2, 6, 0, 4, 3, 7
            });
            fillEbo = new ElementBuffer(new int[]{
                    0, 1, 2,
                    0, 2, 3,
                    3, 2, 6,
                    3, 6, 7,
                    4, 5, 1,
                    4, 1, 0,
                    7, 6, 5,
                    7, 5, 4,
                    1, 5, 6,
                    1, 6, 2,
                    3, 7, 4,
                    3, 4, 0
            });
        }
        modelMat = new Matrix4f();
    }

    public Matrix4f getModelMat() {
        return modelMat;
    }

    public GameObjectRenderer resetModelMat() {
        modelMat = new Matrix4f();
        return this;
    }

    public GameObjectRenderer renderBox(Camera camera, float x, float y, float z, float w, float h, float l, boolean fill) {
        vao.bind();

        Matrix4f mat = new Matrix4f(modelMat).translate(x, y, z).scale(w, h, l);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        shader.bind();
        shader.setMat4("projMat", false, camera.getMatrix(bindableContext));
        shader.setMat4("modelMat", false, mat);
        shader.setVec4("color", r, g, b, a);

        if (fill) {
            fillEbo.bind();
            glDrawElements(GL_TRIANGLES, fillEbo.length(), GL_UNSIGNED_INT, 0);
        } else {
            lineEbo.bind();
            glDrawElements(GL_LINES, lineEbo.length(), GL_UNSIGNED_INT, 0);
        }

        return this;
    }

    public GameObjectRenderer renderBox(Camera camera, float x, float y, float z, float w, float h, float l) {
        return renderBox(camera, x, y, z, w, h, l, false);
    }

    public GameObjectRenderer renderHitbox(Camera camera, Hitbox hitbox) {
        var pos = hitbox.getPosition();
        var w = hitbox.width();
        var h = hitbox.height();
        return color(1, 1, 0, 1).renderBox(camera, pos.x - w / 2, pos.y, pos.z - w / 2, w, h, w);
    }

    public GameObjectRenderer renderBlockSelectionBox(Camera camera, int x, int y, int z) {
        float texel = 1f / 80;
        color(0.15f, 0.15f, 0.15f, 0.65f);
        renderBox(camera, x, y - texel / 2, z - texel / 2, 1, texel, texel, true);
        renderBox(camera, x, y - texel / 2 + 1, z - texel / 2, 1, texel, texel, true);
        renderBox(camera, x, y - texel / 2, z - texel / 2 + 1, 1, texel, texel, true);
        renderBox(camera, x, y - texel / 2 + 1, z - texel / 2 + 1, 1, texel, texel, true);

        renderBox(camera, x - texel / 2, y, z - texel / 2, texel, 1, texel, true);
        renderBox(camera, x - texel / 2, y, z - texel / 2 + 1, texel, 1, texel, true);
        renderBox(camera, x - texel / 2 + 1, y, z - texel / 2, texel, 1, texel, true);
        renderBox(camera, x - texel / 2 + 1, y, z - texel / 2 + 1, texel, 1, texel, true);

        renderBox(camera, x - texel / 2, y - texel / 2, z, texel, texel, 1, true);
        renderBox(camera, x - texel / 2, y - texel / 2 + 1, z, texel, texel, 1, true);
        renderBox(camera, x - texel / 2 + 1, y - texel / 2, z, texel, texel, 1, true);
        renderBox(camera, x - texel / 2 + 1, y - texel / 2 + 1, z, texel, texel, 1, true);
        return this;
    }

}
