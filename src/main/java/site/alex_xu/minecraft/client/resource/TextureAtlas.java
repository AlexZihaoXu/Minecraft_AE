package site.alex_xu.minecraft.client.resource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import site.alex_xu.minecraft.client.utils.Texture;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.block.Block;
import site.alex_xu.minecraft.server.block.Blocks;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

public final class TextureAtlas extends MinecraftAECore {
    private final HashMap<String, Texture> pathTextureMap = new HashMap<>();
    private final HashMap<String, Rectangle2D.Float> textureBoundMap = new HashMap<>();
    private static TextureAtlas instance = null;
    private Texture atlas = null;
    private final HashSet<String> paths = new HashSet<>();

    public static TextureAtlas getInstance() {
        if (instance == null)
            instance = new TextureAtlas();
        return instance;
    }

    private void addPath(String path) {
        paths.add(path);
    }

    private TextureAtlas() {
        addPath("assets/textures/entity/steve.png");
    }

    public Texture getAtlasBuffer() {
        return atlas;
    }

    void load() {
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
        BufferedImage atlasBuffer = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_4BYTE_ABGR);
        int offset = 0;
        Graphics2D g = (Graphics2D) atlasBuffer.getGraphics();
        for (String path : paths) {
            Image image;
            try {
                image = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(path)));
                g.drawImage(image, 0, offset, null);
                offset += image.getHeight(null);

                textureBoundMap.put(
                        path,
                        new Rectangle2D.Float(
                                0, 1 - offset / (float) atlasHeight,
                                image.getWidth(null) / (float) atlasWidth,
                                image.getHeight(null) / (float) atlasHeight
                        )
                );
            } catch (IOException e) {
                getLogger().warn("Unable load texture from: " + path);
                e.printStackTrace();
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(atlasBuffer, "PNG", outputStream);
            atlas = new Texture(outputStream.toByteArray(), GL_NEAREST, GL_NEAREST_MIPMAP_NEAREST);
            getLogger().info("Generated texture atlas: " + atlasWidth + "x" + atlasHeight);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Unable to generated block texture atlas!");
        }



    }

    public Rectangle2D.Float getTextureBound(String path) {
        return textureBoundMap.get(path);
    }

    public Texture getTextureFromPath(String path) {
        return pathTextureMap.get(path);
    }
}
