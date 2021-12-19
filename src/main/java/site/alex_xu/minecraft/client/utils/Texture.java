package site.alex_xu.minecraft.client.utils;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture extends ImageType {

    public Texture(byte[] data, int magFilter, int minFilter) {

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            IntBuffer b_width = memoryStack.mallocInt(1);
            IntBuffer b_height = memoryStack.mallocInt(1);
            IntBuffer b_channels = memoryStack.mallocInt(1);
            ByteBuffer b_data = memoryStack.malloc(data.length);
            b_data.put(data);
            b_data.flip();

            ByteBuffer b_img = stbi_load_from_memory(b_data, b_width, b_height, b_channels, 0);
            if (b_img == null) {
                throw new IllegalStateException("Unable to load texture.");
            }

            width = b_width.get();
            height = b_height.get();
            channels = b_channels.get();

            textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);

            stbi_set_flip_vertically_on_load(true);
            glTexImage2D(GL_TEXTURE_2D, 0, getFormat(), width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, b_img);
            glGenerateMipmap(GL_TEXTURE_2D);

            stbi_image_free(b_img);
        }

    }

    public Texture(byte[] data) {
        this(data, GL_NEAREST, GL_NEAREST_MIPMAP_NEAREST);
    }

    @Override
    public void onDispose() {
        glDeleteTextures(textureID);
    }
}
