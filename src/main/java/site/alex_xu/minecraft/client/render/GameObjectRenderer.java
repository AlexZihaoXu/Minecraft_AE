package site.alex_xu.minecraft.client.render;

import org.joml.Matrix4f;
import site.alex_xu.minecraft.client.model.Model;
import site.alex_xu.minecraft.client.model.ModelBuilder;
import site.alex_xu.minecraft.client.resource.BlockTextureAtlas;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.utils.BindableContext;
import site.alex_xu.minecraft.client.utils.Framebuffer;
import site.alex_xu.minecraft.client.utils.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.utils.buffers.VertexArray;
import site.alex_xu.minecraft.client.utils.buffers.VertexBuffer;
import site.alex_xu.minecraft.client.utils.shader.Shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class GameObjectRenderer extends ModelRenderer {

    private static VertexBuffer vbo;
    private static VertexArray vao;
    private static ElementBuffer ebo;
    private static Matrix4f modelMat;
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
            ebo = new ElementBuffer(new int[]{
                    0, 1, 3, 2, 7, 6, 4, 5,
                    0, 3, 4, 7, 5, 6, 1, 2,
                    1, 5, 2, 6, 0, 4, 3, 7
            });
            modelMat = new Matrix4f();
        }
    }


    public GameObjectRenderer renderBox(Camera camera, float x, float y, float z, float w, float h, float l) {
        vao.bind();
        ebo.bind();

        modelMat = new Matrix4f().translate(x, y, z).scale(w, h, l);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        shader.bind();
        shader.setMat4("projMat", false, camera.getMatrix(bindableContext));
        shader.setMat4("modelMat", false, modelMat);
        shader.setVec4("color", r, g, b, a);

        glDrawElements(GL_LINES, ebo.length(), GL_UNSIGNED_INT, 0);
        return this;
    }

}
