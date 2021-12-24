package site.alex_xu.minecraft.client.screen.world;

import site.alex_xu.minecraft.client.MinecraftClient;
import site.alex_xu.minecraft.client.control.FirstPersonController;
import site.alex_xu.minecraft.client.render.GameObjectRenderer;
import site.alex_xu.minecraft.client.render.Renderer2D;
import site.alex_xu.minecraft.client.resource.FontTextureAtlas;
import site.alex_xu.minecraft.client.screen.Screen;
import site.alex_xu.minecraft.client.utils.RenderContext;
import site.alex_xu.minecraft.client.world.WorldRenderer;
import site.alex_xu.minecraft.core.Minecraft;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.collision.Hitbox;
import site.alex_xu.minecraft.server.entity.Entity;
import site.alex_xu.minecraft.server.world.World;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class WorldScreen extends Screen {

    public static String debugInfo = "";

    protected Camera camera = new Camera();
    protected FirstPersonController firstPersonController;
    protected boolean showDebugInformation = false;
    protected long lastCountTime = System.currentTimeMillis();
    protected int fps = 0;
    protected int frameCount = 0;

    public Camera getCamera() {
        return camera;
    }


    public World world;
    public WorldRenderer worldRenderer;
    public Entity entity1;
    public Entity entity2;

    @Override
    public void onSetup() {
        camera.yaw = Math.PI / 2;
        camera.position.y = 10;
        world = new World();
        worldRenderer = new WorldRenderer(world);
        firstPersonController = new FirstPersonController(MinecraftClient.getInstance().getWindow(), camera, world);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        MinecraftClient.getInstance().getWindow().registerKeyChangeCallback(this::onKeyChange);

        var blocks = new ArrayList<>(Blocks.blocks.values());

        for (int x = -16; x < 16; x++) {
            for (int z = -16; z < 16; z++) {
                for (int y = 0; y < 3; y++) {
                    world.setBlock(blocks.get(2), x, y, z);
                }
            }
        }

        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                for (int y = 0; y < 256; y++) {
                    world.setBlock(blocks.get((int) (Math.random() * blocks.size())), x + 13, y, z + 13);
                }
            }
        }

        world.setBlock(Blocks.CRAFTING_TABLE, 0, 3, 0);

        entity1 = new Entity(world, new Hitbox(0.5f, 1)).setPosition(0, 3, 0);
        entity2 = new Entity(world, new Hitbox(0.5f, 1)).setPosition(0.5f, 5.5f, 0.5f);

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
        world.onTick(vdt);
        firstPersonController.onTick(vdt);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Tick
//        entity2.onTick(vdt);
        //

        context.getRenderer().clear(0.8f);
        worldRenderer.render(context.getRenderer().get3D(), getCamera());

        var objectRenderer = new GameObjectRenderer(context);
        objectRenderer.color(0.2f, 0.2f, 0.2f, 0.8f)
                .renderBox(camera, 0, 4, 0, 1, 1, 1);

        objectRenderer.renderHitbox(camera, entity1.hitbox());
        entity1.onTick(vdt);

//        objectRenderer.renderHitbox(camera, entity2.hitbox());
        {

        }
        render2D(context, vdt);
    }

    public void render2D(RenderContext context, double vdt) {

        long now = System.currentTimeMillis();
        if (now - lastCountTime > 1000) {
            float dt = (now - lastCountTime) / 1000f;
            fps = (int) (1 / dt * frameCount);
            frameCount = 0;
            lastCountTime = now;
        }
        frameCount++;

        glDisable(GL_DEPTH_TEST);
        if (showDebugInformation) {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            var renderer = context.getRenderer().get2D();
            String[] informationList = new String[]{
                    "Minecraft(AE) " + Minecraft.VERSION,
                    fps + " FPS ",
                    "XYZ: " + String.format("%.3f / %.3f / %.3f", camera.position.x, camera.position.y, camera.position.z),
                    debugInfo
            };

            for (int i = 0; i < informationList.length; i++) {
                renderer.text(informationList[i], 0, i * FontTextureAtlas.FONT_LINE_HEIGHT, Renderer2D.BACKGROUND_NONE);
            }

        }

        var window = MinecraftClient.getInstance().getWindow();
        glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO);
        context.getRenderer().get2D()
                .translate(window.getWidth() / 2f, window.getHeight() / 2f)
                .color(1, 1, 1, 1)
                .fillRect(-14, -2f, 27, 3)
                .fillRect(-2, -14, 3, 12)
                .fillRect(-2, 1, 3, 12);
    }
}
