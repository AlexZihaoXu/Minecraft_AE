package site.alex_xu.minecraft.client.render;

import site.alex_xu.minecraft.client.utils.BindableContext;
import site.alex_xu.minecraft.core.MinecraftAECore;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

public class Renderer extends MinecraftAECore {

    protected final BindableContext bindableContext;

    public Renderer(BindableContext bindableContext) {
        this.bindableContext = bindableContext;
    }

    protected void prepareRendering() {
        bindableContext.bindContext();
    }

    // Rendering Methods
    public Renderer clear(float r, float g, float b, float a) {
        prepareRendering();
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        return this;
    }

    public Renderer clear(float r, float g, float b) {
        return clear(r, g, b, 1);
    }

    public Renderer clear(float brightness) {
        return clear(brightness, brightness, brightness);
    }

    public Renderer2D get2D() {
        return new Renderer2D(this.bindableContext);
    }
}
