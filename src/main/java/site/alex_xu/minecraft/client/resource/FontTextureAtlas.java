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
    public static final int FONT_SIZE = 24;
    public static final int FONT_LINE_HEIGHT = FONT_SIZE + 5;
    private TreeMap<Integer, Rectangle2D> boundMap = new TreeMap<>();
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
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(ResourceManager.class.getClassLoader().getResourceAsStream("assets/fonts/Minecraftia.ttf")))
                    .deriveFont((float) FONT_SIZE);

            TreeMap<Integer, Rectangle2D> displayableBoundMap = new TreeMap<>();
            ArrayList<Integer> displayables = new ArrayList<>();

            AffineTransform affinetransform = new AffineTransform();
            FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

            int maxWidth = 0;
            for (int i = 0; i < Character.MAX_VALUE; i++) {
                if (font.canDisplay(i)) {
                    var bound = font.getStringBounds("" + (char) (i), frc);
                    maxWidth = (int) Math.max(bound.getWidth(), maxWidth);
                    displayableBoundMap.put(i, bound);
                    displayables.add(i);
                }
            }
            maxWidth += 3;
            int maxHeight = FONT_LINE_HEIGHT + 6;

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
                    boundMap.put(displayables.get(index), new Rectangle(
                            x * maxWidth, (y) * maxHeight + 3,
                            (int) displayableBoundMap.get(displayables.get(index)).getWidth(), maxHeight - 3
                    ));
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

    public Rectangle2D getBoundOf(char c) {
        return boundMap.get((int) c);
    }

    public Texture getAtlas() {
        return atlas;
    }
}
