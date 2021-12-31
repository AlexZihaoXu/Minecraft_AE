package site.alex_xu.minecraft.client.render;

import site.alex_xu.minecraft.client.model.Mesh;
import site.alex_xu.minecraft.client.resource.TextureAtlas;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.utils.BindableContext;
import site.alex_xu.minecraft.client.utils.ImageType;
import site.alex_xu.minecraft.client.utils.shader.Shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class ModelRenderer extends Renderer {
    private static Shader shader;
    private float lightLevel = 1.0f;

    public void setLightLevel(float level) {
        lightLevel = level;
    }

    public float getLightLevel() {
        return lightLevel;
    }

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

    public void render(Camera camera, Mesh model, int mode, ImageType imageType) {
        getShader().bind();
        glActiveTexture(GL_TEXTURE0);
        imageType.bind();
        getShader().setFloat("lightLevel", lightLevel);
        getShader().setInt("texture0", 0);
        getShader().setInt("mode", mode);
        getShader().setMat4("projMat", false, camera.getMatrix(bindableContext));
        getShader().setMat4("modelMat", false, model.getModelMatrix());
        if (getShader().hasUniform("texWidth"))
            getShader().setFloat("texWidth", TextureAtlas.getInstance().getAtlasBuffer().getWidth());
        if (getShader().hasUniform("texHeight"))
            getShader().setFloat("texHeight", TextureAtlas.getInstance().getAtlasBuffer().getHeight());

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        model.draw();
    }

    public void render(Camera camera, Mesh model) {
        render(camera, model, 0, TextureAtlas.getInstance().getAtlasBuffer());
    }

    public void render(Camera camera, Mesh model, int mode) {
        render(camera, model, mode, TextureAtlas.getInstance().getAtlasBuffer());
    }
}
