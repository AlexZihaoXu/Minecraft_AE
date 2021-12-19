package site.alex_xu.minecraft.client.render;

import org.joml.Matrix4f;
import site.alex_xu.minecraft.client.utils.BindableContext;
import site.alex_xu.minecraft.client.utils.ImageType;
import site.alex_xu.minecraft.client.utils.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.utils.buffers.VertexArray;
import site.alex_xu.minecraft.client.utils.buffers.VertexBuffer;
import site.alex_xu.minecraft.client.utils.shader.Shader;

import java.util.Stack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Renderer2D extends Renderer {
    private final static int DRAW_MODE_SHAPE = 0;
    private final static int DRAW_MODE_IMAGE = 1;
    private static Shader rectShader = null;
    private static VertexArray rectVAO = null;
    private static VertexBuffer rectVBO = null;
    private static ElementBuffer rectEBO = null;
    private final Stack<Matrix4f> matrixStack = new Stack<>();
    private float r, g, b, a;
    private Matrix4f transform;
    private boolean transformChanged = true;

    public Renderer2D(BindableContext bindableContext) {
        super(bindableContext);
        resetTransform();
        init();
        r = g = b = a = 0;
        color(1, 1, 1, 1);
    }


    private static void init() {
        if (rectShader == null) {
            rectShader = new Shader()
                    .addFromResource("assets/shaders/renderer2d.vert")
                    .addFromResource("assets/shaders/renderer2d.frag")
                    .link();
            rectVAO = new VertexArray();
            rectVBO = new VertexBuffer(new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,});
            rectVAO.configure(rectVBO).push(2).apply();
            rectEBO = new ElementBuffer(new int[]{0, 1, 2, 2, 3, 0});
        }
    }

    protected void prepareRendering() {
        super.prepareRendering();
        if (transformChanged) {
            rectShader.setMat4("transform", false, transform);
            transformChanged = false;
        }
        rectShader.bind();
    }

    // Transformations

    public Renderer2D resetTransform() {
        transform = new Matrix4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).ortho(0, bindableContext.getWidth(), bindableContext.getHeight(), 0, 0, 1);
        return this;
    }

    public Renderer2D translate(float x, float y) {
        transformChanged = true;
        transform.translate(x, y, 0);
        return this;
    }

    public Renderer2D rotate(float radians) {
        transformChanged = true;
        transform.rotateZ(radians);
        return this;
    }

    public Renderer2D scale(float x, float y) {
        transformChanged = true;
        transform.scale(x, y, 1);
        return this;
    }

    public Renderer2D clearMatrixStack() {
        matrixStack.clear();
        return this;
    }

    public Renderer2D pushMatrix() {
        matrixStack.push(new Matrix4f(transform));
        return this;
    }

    public Renderer2D popMatrix() {
        transform.set(matrixStack.pop());
        return this;
    }

    // Getters

    public int getStackLevel() {
        return matrixStack.size();
    }

    // Rendering

    public Renderer2D clear(float r, float g, float b, float a) {
        super.clear(r, g, b, a);
        return this;
    }

    public Renderer2D color(float r, float g, float b, float a) {
        if (r != this.r || g != this.g || b != this.b || a != this.a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            rectShader.setVec4("color", r, g, b, a);
        }
        return this;
    }

    public Renderer2D color(float r, float g, float b) {
        return color(r, g, b, 1);
    }

    public Renderer2D color(float brightness, float alpha) {
        return color(brightness, brightness, brightness, alpha);
    }

    public Renderer2D color(float brightness) {
        return color(brightness, 1);
    }

    @Override
    public Renderer2D clear(float r, float g, float b) {
        return clear(r, g, b, 1);
    }

    @Override
    public Renderer2D clear(float brightness) {
        return clear(brightness, brightness, brightness);
    }

    public Renderer2D fillRect(float x, float y, float w, float h) {
        prepareRendering();

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        rectVAO.bind();
        rectShader.setInt("drawMode", DRAW_MODE_SHAPE);
        rectShader.setVec4("rect", x, y, w, h);

        rectEBO.draw();

        return this;
    }

    public Renderer2D rect(float x, float y, float w, float h) {
        prepareRendering();

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        rectVAO.bind();
        rectShader.setInt("drawMode", DRAW_MODE_SHAPE);
        rectShader.setVec4("rect", x, y, w, h);

        rectEBO.draw();

        return this;
    }

    public Renderer2D image(ImageType image, float srcX, float srcY, float srcW, float srcH, float dstX, float dstY, float dstW, float dstH) {
        prepareRendering();

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        rectShader.bind();
        rectShader.setVec4("rect", dstX, dstY, dstW, dstH);
        rectShader.setVec4("srcRect", srcX, srcY, srcW, srcH);
        rectShader.setFloat("texWidth", image.getWidth());
        rectShader.setFloat("texHeight", image.getHeight());
        rectShader.setInt("drawMode", DRAW_MODE_IMAGE);
        rectShader.setInt("texture0", 0);

        glActiveTexture(GL_TEXTURE0);
        image.bind();

        rectVAO.bind();
        rectEBO.bind();
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        return this;
    }
}
