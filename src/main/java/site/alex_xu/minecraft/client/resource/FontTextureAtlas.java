package site.alex_xu.minecraft.client.resource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import site.alex_xu.minecraft.client.utils.Framebuffer;
import site.alex_xu.minecraft.client.utils.Texture;
import site.alex_xu.minecraft.core.MinecraftAECore;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FontTextureAtlas extends MinecraftAECore {
    private static FontTextureAtlas instance = null;
    private Texture atlas;

    public static FontTextureAtlas getInstance() {
        if (instance == null)
            instance = new FontTextureAtlas();
        return instance;
    }

    private FontTextureAtlas() {
    }

    void load() {
        try {
            int FONTSIZE = 32;
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(ResourceManager.class.getClassLoader().getResourceAsStream("assets/fonts/Minecraftia.ttf")))
                    .deriveFont((float) FONTSIZE);

            TreeMap<Integer, Rectangle2D> displayableBoundMap = new TreeMap<>();
            ArrayList<Integer> displayables = new ArrayList<>();

            AffineTransform affinetransform = new AffineTransform();
            FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

            int maxWidth = 0;
            int maxHeight = 0;
            for (int i = 0; i < Character.MAX_VALUE; i++) {
                if (font.canDisplay(i)) {
                    var bound = font.getStringBounds("" + (char) (i), frc);
                    maxWidth = (int) Math.max(bound.getWidth(), maxWidth);
                    maxHeight = (int) Math.max(bound.getHeight(), maxHeight);
                    displayableBoundMap.put(i, bound);
                    displayables.add(i);
                }
            }

            int sideLength = (int) Math.ceil(Math.sqrt(displayableBoundMap.size()));
            int bImageWidth = sideLength * maxWidth;
            int bImageHeight = sideLength * maxHeight;

            BufferedImage bufferedImage = new BufferedImage(bImageWidth, bImageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
            g.setFont(font);
            int index = 0;
            for (int x = 0; x < sideLength; x++) {
                for (int y = 0; y < sideLength; y++) {
                    if (index >= displayables.size()) {
                        break;
                    }
                    char c = (char) (displayables.get(index).intValue());
                    g.drawString(String.valueOf(c), x * maxWidth, (y + 1) * maxHeight);
                    index++;
                }
                if (index >= displayables.size())
                    break;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", outputStream);
            atlas = new Texture(outputStream.toByteArray());
            outputStream.close();

            getLogger().info("Created font texture atlas: " + bImageWidth + "x" + bImageHeight);


        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Unable to load fonts!");
        }
    }

    public Texture getAtlas() {
        return atlas;
    }
}
