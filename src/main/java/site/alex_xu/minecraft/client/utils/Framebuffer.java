package site.alex_xu.minecraft.client.utils;

import site.alex_xu.minecraft.client.render.Renderer;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.*;

public class Framebuffer extends ImageType implements BindableContext {

    private final int frameBufferID;
    private final int renderBufferID;

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.channels = 4;

        frameBufferID = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        renderBufferID = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderBufferID);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBufferID);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Unable to create frame buffer!");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }

    public void genMipMap() {
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public Renderer getRenderer() {
        return new Renderer(this);
    }

    public void bindContext() {
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);
        glViewport(0, 0, width, height);
    }

    public void unbindContext() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onDispose() {
        glDeleteTextures(textureID);
        glDeleteFramebuffers(frameBufferID);
        glDeleteBuffers(renderBufferID);
    }
}
