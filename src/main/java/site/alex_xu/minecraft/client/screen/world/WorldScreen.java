package site.alex_xu.minecraft.client.screen.world;

import site.alex_xu.minecraft.client.MinecraftClient;
import site.alex_xu.minecraft.client.chunk.ChunkSectionMesher;
import site.alex_xu.minecraft.client.control.FirstPersonController;
import site.alex_xu.minecraft.client.render.Renderer2D;
import site.alex_xu.minecraft.client.resource.FontTextureAtlas;
import site.alex_xu.minecraft.client.screen.Screen;
import site.alex_xu.minecraft.client.utils.RenderContext;
import site.alex_xu.minecraft.core.Minecraft;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.chunk.ChunkSection;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;

public class WorldScreen extends Screen {


    protected Camera camera = new Camera();
    protected FirstPersonController firstPersonController;
    protected ChunkSection chunk;
    protected ChunkSectionMesher chunkRenderer;
    protected boolean showDebugInformation = false;
    protected long lastCountTime = System.currentTimeMillis();
    protected int fps = 0;
    protected int frameCount = 0;

    public Camera getCamera() {
        return camera;
    }


    @Override
    public void onSetup() {
        firstPersonController = new FirstPersonController(MinecraftClient.getInstance().getWindow(), camera);
        camera.yaw = Math.PI / 2;

        chunk = new ChunkSection();
        chunkRenderer = new ChunkSectionMesher(chunk);
        var blocks = new ArrayList<>(Blocks.blocks.values());
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    chunk.setBlock(blocks.get((int) (Math.random() * blocks.size())), x, y, z);
                }
            }
        }
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        MinecraftClient.getInstance().getWindow().registerKeyChangeCallback(this::onKeyChange);
    }

    @Override
    public void onDispose() {
        firstPersonController.dispose();
        MinecraftClient.getInstance().getWindow().removeKeyChangeCallback(this::onKeyChange);
    }

    public void onKeyChange(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_F3 && action == GLFW_PRESS) {
            showDebugInformation = !showDebugInformation;
        }
    }

    @Override
    public void onRender(RenderContext context, double vdt) {
        chunk.onTick(vdt);
        firstPersonController.onTick(vdt);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        long now = System.currentTimeMillis();
        if (now - lastCountTime > 1000) {
            float dt = (now - lastCountTime) / 1000f;
            fps = (int) (1 / dt * frameCount);
            frameCount = 0;
            lastCountTime = now;
        }
        frameCount++;

        context.getRenderer().clear(0.8f);
        glDisable(GL_DEPTH_TEST);
        context.getRenderer().get3D()
                .render(camera, chunkRenderer.getModel());
        if (showDebugInformation) {
            var renderer = context.getRenderer().get2D();
            String[] informationList = new String[]{
                    "Minecraft(AE) " + Minecraft.VERSION,
                    fps + " FPS ",
                    "XYZ: " + String.format("%.3f / %.3f / %.3f", camera.position.x, camera.position.y, camera.position.z)
            };

            for (int i = 0; i < informationList.length; i++) {
                renderer.text(informationList[i], 0, i * FontTextureAtlas.FONT_LINE_HEIGHT, Renderer2D.BACKGROUND_FILL);
            }

        }

        var window = MinecraftClient.getInstance().getWindow();
        glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO);
        context.getRenderer().get2D()
                .translate(window.getWidth() / 2f, window.getHeight() / 2f)
                .color(1, 1, 1, 1)
                .fillRect(-14, -2f, 27, 3)
                .fillRect(-2f, -14, 3, 27);
    }
}
