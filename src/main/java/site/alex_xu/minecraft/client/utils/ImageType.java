package site.alex_xu.minecraft.client.utils;

import site.alex_xu.minecraft.core.MinecraftAECore;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public abstract class ImageType extends MinecraftAECore {
    private boolean freed = false;
    protected int textureID;
    protected int width, height, channels;

    public int getTextureID() {
        return textureID;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getChannels() {
        return channels;
    }

    public int getFormat() {
        return channels == 3 ? GL_RGB8 : GL_RGBA8;
    }

    abstract public void onDispose();

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureID);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public boolean isFreed() {
        return freed;
    }

    public final void free() {
        if (freed)
            return;
        onDispose();
        freed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        free();
    }
}
