package site.alex_xu.minecraft.client.utils;

import site.alex_xu.minecraft.client.render.Renderer;
import site.alex_xu.minecraft.core.MinecraftAECore;

import static org.lwjgl.opengl.GL11.*;

public abstract class RenderContext extends MinecraftAECore implements BindableContext {
    protected int width = -1, height = -1;
    protected int contextID = 0;

    public abstract void bindContext();

    // Quick Methods

    public void clear(float r, float g, float b, float a) {
        bindContext();
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public Renderer getRenderer() {
        return new Renderer(this);
    }

    // Getters

    public int getContextID() {
        return contextID;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
