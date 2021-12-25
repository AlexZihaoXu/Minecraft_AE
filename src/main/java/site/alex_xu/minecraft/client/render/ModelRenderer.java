package site.alex_xu.minecraft.client.render;

import site.alex_xu.minecraft.client.model.Model;
import site.alex_xu.minecraft.client.resource.BlockTextureAtlas;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.utils.BindableContext;
import site.alex_xu.minecraft.client.utils.shader.Shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class ModelRenderer extends Renderer {
    private static Shader shader;

    public ModelRenderer(BindableContext bindableContext) {
        super(bindableContext);
    }

    public Shader getShader() {
        if (shader == null) {
            shader = new Shader()
                    .addFromResource("assets/shaders/world_renderer.frag")
                    .addFromResource("assets/shaders/world_renderer.vert")
                    .link();
        }
        return shader;
    }

    public void render(Camera camera, Model model, int mode) {
        getShader().bind();
        glActiveTexture(GL_TEXTURE0);
        BlockTextureAtlas.getInstance().getAtlasBuffer().bind();
        getShader().setInt("texture0", 0);
        getShader().setInt("mode", mode);
        getShader().setMat4("projMat", false, camera.getMatrix(bindableContext));
        getShader().setMat4("modelMat", false, model.getModelMatrix());
        if (getShader().hasUniform("texWidth"))
            getShader().setFloat("texWidth", BlockTextureAtlas.getInstance().getAtlasBuffer().getWidth());
        if (getShader().hasUniform("texHeight"))
            getShader().setFloat("texHeight", BlockTextureAtlas.getInstance().getAtlasBuffer().getHeight());

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        model.draw();
    }

    public void render(Camera camera, Model model) {
        render(camera, model, 0);
    }
}
