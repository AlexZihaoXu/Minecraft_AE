package site.alex_xu.minecraft.client.screen.world;

import org.joml.Matrix4f;
import org.joml.Vector3i;
import site.alex_xu.minecraft.client.MinecraftClient;
import site.alex_xu.minecraft.client.control.FirstPersonController;
import site.alex_xu.minecraft.client.render.GameObjectRenderer;
import site.alex_xu.minecraft.client.render.Renderer2D;
import site.alex_xu.minecraft.client.resource.FontTextureAtlas;
import site.alex_xu.minecraft.client.screen.Screen;
import site.alex_xu.minecraft.client.utils.RenderContext;
import site.alex_xu.minecraft.client.utils.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.utils.buffers.VertexArray;
import site.alex_xu.minecraft.client.utils.buffers.VertexBuffer;
import site.alex_xu.minecraft.client.utils.shader.Shader;
import site.alex_xu.minecraft.client.world.WorldRenderer;
import site.alex_xu.minecraft.core.Minecraft;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.entity.PlayerEntity;
import site.alex_xu.minecraft.server.world.World;

import static org.joml.Math.max;
import static org.joml.Math.min;
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
    protected static Shader skyShader;
    protected static VertexArray skyVao;
    protected static VertexBuffer skyVbo;
    protected static ElementBuffer skyEbo;

    public Camera getCamera() {
        return camera;
    }


    public World world;
    public WorldRenderer worldRenderer;
    public PlayerEntity player;


    @Override
    public void onSetup() {

        if (skyShader == null) {
            skyShader = new Shader()
                    .addFromResource("assets/shaders/sky.frag")
                    .addFromResource("assets/shaders/sky.vert")
                    .link();
            skyVao = new VertexArray();
            skyVbo = new VertexBuffer(new float[]{
                    -1, -1,
                    -1, +1,
                    +1, +1,
                    +1, -1
            });
            skyEbo = new ElementBuffer(new int[]{
                    0, 1, 2,
                    2, 3, 0
            });
            skyVao.configure(skyVbo)
                    .push(2)
                    .apply();

        }

        camera.yaw = Math.PI / 2;
        camera.position.y = 10;
        world = new World();
        worldRenderer = new WorldRenderer(world);
        player = new PlayerEntity(world);
        player.position().y = 5;
        player.velocity().y = 0;
        firstPersonController = new FirstPersonController(MinecraftClient.getInstance().getWindow(), camera, world, player);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        MinecraftClient.getInstance().getWindow().registerKeyChangeCallback(this::onKeyChange);

        for (int x = -3; x < 3; x++) {
            for (int z = -3; z < 3; z++) {
                for (int y = 1; y < 3; y++) {
                    world.setBlock(Blocks.DIRT, x, y, z);
                }
                world.setBlock(Blocks.GRASS_BLOCK, x, 3, z);
                world.setBlock(Blocks.BEDROCK, x, 0, z);
            }
        }

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

    protected void renderSky(RenderContext context) {

        context.clear(0.7f, 0.82f, 1f, 1f);

        skyVao.bind();
        skyEbo.bind();
        skyShader.setMat4("projMat", false, camera.getMatrix(context));
        skyShader.setMat4("modelMat", false,
                    new Matrix4f().translate(camera.position.x, 0, camera.position.z)
                );
        skyShader.setFloat("yOffset", Math.min(camera.position.y - 1f, camera.position.y * 0.5f - 5));
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDrawElements(GL_TRIANGLES, skyEbo.length(), GL_UNSIGNED_INT, 0);
        skyShader.setFloat("yOffset", max(50, camera.position.y + 50));
        glDrawElements(GL_TRIANGLES, skyEbo.length(), GL_UNSIGNED_INT, 0);
    }

    @Override
    public void onRender(RenderContext context, double vdt) {

        world.onTick(vdt);
        player.onTick(vdt);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        renderSky(context);

        firstPersonController.onTick(vdt);
        // Tick
//        entity2.onTick(vdt);
        //

        worldRenderer.render(context.getRenderer().get3D(), getCamera());

        var objectRenderer = new GameObjectRenderer(context);


//        objectRenderer.renderHitbox(camera, entity2.hitbox());
        {
            Vector3i blockPos = firstPersonController.rayCast()[0];
            if (blockPos != null) {
                glDisable(GL_DEPTH_TEST);
                objectRenderer.renderBlockSelectionBox(camera, blockPos.x, blockPos.y, blockPos.z);
            }
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
