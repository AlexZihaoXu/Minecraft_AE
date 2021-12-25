package site.alex_xu.minecraft.client.resource;

import site.alex_xu.minecraft.client.render.Renderer2D;
import site.alex_xu.minecraft.client.utils.Framebuffer;
import site.alex_xu.minecraft.client.utils.Texture;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.models.BlockModelDef;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;

public final class BlockTextureAtlas extends MinecraftAECore {
    private final HashMap<String, Texture> pathTextureMap = new HashMap<>();
    private final HashMap<String, Rectangle2D.Float> textureBoundMap = new HashMap<>();
    private static BlockTextureAtlas instance = null;
    private Framebuffer atlasBuffer;

    public static BlockTextureAtlas getInstance() {
        if (instance == null)
            instance = new BlockTextureAtlas();
        return instance;
    }

    private BlockTextureAtlas() {

    }

    public Framebuffer getAtlasBuffer() {
        return atlasBuffer;
    }

    void load() {
        HashSet<String> paths = new HashSet<>();
        for (Block block : Blocks.blocks.values()) {
            if (block.modelDef() != null)
                paths.addAll(block.modelDef().texturePathMap.values());
        }
        int atlasWidth = 0;
        int atlasHeight = 0;
        for (String path : paths) {
            Texture texture = new Texture(ResourceManager.getInstance().readBytesFromResource(path));
            pathTextureMap.put(path, texture);
            atlasWidth = Math.max(atlasWidth, texture.getWidth());
            atlasHeight += texture.getHeight();
        }
        atlasBuffer = new Framebuffer(atlasWidth, atlasHeight);
        int offset = 0;
        Renderer2D renderer = atlasBuffer.getRenderer().get2D();
        for (String path : paths) {
            Texture texture = getTextureFromPath(path);
            renderer.image(texture, 0, offset);
            offset += texture.getHeight();

            textureBoundMap.put(
                    path,
                    new Rectangle2D.Float(
                            0, 1 - offset / (float) atlasHeight,
                            texture.getWidth() / (float) atlasWidth,
                            texture.getHeight() / (float) atlasHeight
                    )
            );
        }
        getLogger().info("Generated texture atlas: " + atlasWidth + "x" + atlasHeight);

    }

    public Rectangle2D.Float getTextureBound(String path) {
        return textureBoundMap.get(path);
    }

    public Texture getTextureFromPath(String path) {
        return pathTextureMap.get(path);
    }
}
