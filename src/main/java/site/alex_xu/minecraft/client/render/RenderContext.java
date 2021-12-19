package site.alex_xu.minecraft.client.render;

import site.alex_xu.minecraft.core.Minecraft;
import site.alex_xu.minecraft.core.MinecraftAECore;

import static org.lwjgl.opengl.GL11.*;

public abstract class RenderContext extends MinecraftAECore {
    protected int width = -1, height = -1;
    protected int contextID = 0;

    public abstract void bindContext();

    // Quick Methods

    public void clear(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
