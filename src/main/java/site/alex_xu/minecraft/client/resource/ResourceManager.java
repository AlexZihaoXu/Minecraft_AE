package site.alex_xu.minecraft.client.resource;

import site.alex_xu.minecraft.core.MinecraftAECore;

import java.io.IOException;
import java.io.InputStream;

public class ResourceManager extends MinecraftAECore {

    private static ResourceManager instance = null;

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    public void init() {
        TextureAtlas.getInstance().load();
    }

    public byte[] readBytesFromResource(String path) {
        byte[] bytes = null;
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            if (stream == null)
                return null;
            bytes = stream.readAllBytes();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

}
