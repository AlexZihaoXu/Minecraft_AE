package site.alex_xu.minecraft.client.screen.world;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import site.alex_xu.minecraft.client.MinecraftClient;
import site.alex_xu.minecraft.client.control.FirstPersonController;
import site.alex_xu.minecraft.client.entity.PlayerRenderer;
import site.alex_xu.minecraft.client.model.Mesh;
import site.alex_xu.minecraft.client.model.MeshBuilder;
import site.alex_xu.minecraft.client.render.GameObjectRenderer;
import site.alex_xu.minecraft.client.render.Renderer2D;
import site.alex_xu.minecraft.client.resource.FontTextureAtlas;
import site.alex_xu.minecraft.client.resource.ResourceManager;
import site.alex_xu.minecraft.client.screen.Screen;
import site.alex_xu.minecraft.client.utils.RenderContext;
import site.alex_xu.minecraft.client.utils.Texture;
import site.alex_xu.minecraft.client.utils.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.utils.buffers.VertexArray;
import site.alex_xu.minecraft.client.utils.buffers.VertexBuffer;
import site.alex_xu.minecraft.client.utils.shader.Shader;
import site.alex_xu.minecraft.client.world.WorldRenderer;
import site.alex_xu.minecraft.core.Minecraft;
import site.alex_xu.minecraft.server.Directions;
import site.alex_xu.minecraft.server.block.Blocks;
import site.alex_xu.minecraft.server.entity.PlayerEntity;
import site.alex_xu.minecraft.server.world.World;

import static org.joml.Math.*;
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
    protected static Mesh sunMoonMesh;
    protected static Texture sunTexture;
    protected static Texture moonTexture;
    protected float time = 0;

    private static final record Star(float pitch, float yaw) {
    }

    private static Star[] stars = null;

    public Camera getCamera() {
        return camera;
    }


    public World world;
    public WorldRenderer worldRenderer;
    public PlayerEntity player;
    public PlayerRenderer playerRenderer;

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

            {
                MeshBuilder builder = new MeshBuilder();
                int v1 = builder.vertex(-0.5f, -0.5f, 0, 1, 1, 1, 1, 0, 0);
                int v2 = builder.vertex(-0.5f, 0.5f, 0, 1, 1, 1, 1, 0, 1);
                int v3 = builder.vertex(0.5f, 0.5f, 0, 1, 1, 1, 1, 1, 1);
                int v4 = builder.vertex(0.5f, -0.5f, 0, 1, 1, 1, 1, 1, 0);
                builder.addFace(v1, v2, v3, v4);
                sunMoonMesh = builder.build();


                sunTexture = new Texture(ResourceManager.getInstance().readBytesFromResource("assets/textures/environment/sun.png"));
                moonTexture = new Texture(ResourceManager.getInstance().readBytesFromResource("assets/textures/environment/moon.png"));
            }

            stars = new Star[256];
            for (int i = 0; i < stars.length; i++) {
                stars[i] = new Star(
                        (float) (Math.random() * Math.random() * PI * 1) * (Math.random() > 0.5 ? -1 : 1),
                        (float) (Math.random() * PI * 2)
                );
            }
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

        for (int x = -100; x < 100; x++) {
            for (int z = -100; z < 100; z++) {
                for (int y = 1; y < 3; y++) {
                    world.setBlock(Blocks.DIRT, x, y, z);
                }
                world.setBlock(Blocks.GRASS_BLOCK, x, 3, z);
                world.setBlock(Blocks.BEDROCK, x, 0, z);
            }
        }

        {
            world.setBlock(Blocks.OAK_LOG, 3, 4, 5);
            world.setBlock(Blocks.OAK_LOG, 3, 5, 5);
            world.setBlock(Blocks.OAK_LOG, 3, 6, 5);
            world.setBlock(Blocks.OAK_LOG, 3, 7, 5);
            world.setBlock(Blocks.OAK_LOG, 3, 4, 10);
            world.setBlock(Blocks.OAK_LOG, 3, 5, 10);
            world.setBlock(Blocks.OAK_LOG, 3, 6, 10);
            world.setBlock(Blocks.OAK_LOG, 3, 7, 10);
            world.setBlock(Blocks.OAK_LOG, -2, 4, 5);
            world.setBlock(Blocks.OAK_LOG, -2, 5, 5);
            world.setBlock(Blocks.OAK_LOG, -2, 6, 5);
            world.setBlock(Blocks.OAK_LOG, -2, 7, 5);
            world.setBlock(Blocks.OAK_LOG, -2, 4, 10);
            world.setBlock(Blocks.OAK_LOG, -2, 5, 10);
            world.setBlock(Blocks.OAK_LOG, -2, 6, 10);
            world.setBlock(Blocks.OAK_LOG, -2, 7, 10);
            world.setBlock(Blocks.OAK_LOG, 2, 7, 5);
            world.setBlock(Blocks.OAK_LOG, 1, 7, 5);
            world.setBlock(Blocks.OAK_LOG, 0, 7, 5);
            world.setBlock(Blocks.OAK_LOG, -1, 7, 5);
            world.setBlock(Blocks.OAK_LOG, -2, 7, 9);
            world.setBlock(Blocks.OAK_LOG, -2, 7, 8);
            world.setBlock(Blocks.OAK_LOG, -2, 7, 7);
            world.setBlock(Blocks.OAK_LOG, -2, 7, 6);
            world.setBlock(Blocks.OAK_LOG, 2, 7, 10);
            world.setBlock(Blocks.OAK_LOG, 2, 7, 9);
            world.setBlock(Blocks.OAK_LOG, 1, 7, 10);
            world.setBlock(Blocks.OAK_LOG, 0, 7, 10);
            world.setBlock(Blocks.OAK_LOG, -1, 7, 10);
            world.setBlock(Blocks.OAK_LOG, 3, 7, 9);
            world.setBlock(Blocks.OAK_LOG, 3, 7, 8);
            world.setBlock(Blocks.OAK_LOG, 3, 7, 7);
            world.setBlock(Blocks.OAK_LOG, 3, 7, 6);
            world.setBlock(Blocks.OAK_LOG, 1, 7, 9);
            world.setBlock(Blocks.OAK_LOG, 2, 7, 8);
            world.setBlock(Blocks.OAK_LOG, 2, 7, 6);
            world.setBlock(Blocks.OAK_LOG, 2, 7, 7);
            world.setBlock(Blocks.OAK_LOG, 1, 7, 6);
            world.setBlock(Blocks.OAK_LOG, 1, 7, 7);
            world.setBlock(Blocks.OAK_LOG, 1, 7, 8);
            world.setBlock(Blocks.OAK_LOG, 0, 7, 9);
            world.setBlock(Blocks.OAK_LOG, -1, 7, 9);
            world.setBlock(Blocks.OAK_LOG, 0, 7, 8);
            world.setBlock(Blocks.OAK_LOG, -1, 7, 8);
            world.setBlock(Blocks.OAK_LOG, -1, 7, 7);
            world.setBlock(Blocks.OAK_LOG, 0, 7, 7);
            world.setBlock(Blocks.OAK_LOG, 0, 7, 6);
            world.setBlock(Blocks.OAK_LOG, -1, 7, 6);
        }
        {
            world.setBlock(Blocks.OAK_PLANKS, 3, 4, 6);
            world.setBlock(Blocks.OAK_PLANKS, 3, 5, 6);
            world.setBlock(Blocks.OAK_PLANKS, 3, 6, 6);
            world.setBlock(Blocks.OAK_PLANKS, 3, 4, 7);
            world.setBlock(Blocks.OAK_PLANKS, 3, 5, 7);
            world.setBlock(Blocks.OAK_PLANKS, 3, 6, 7);
            world.setBlock(Blocks.OAK_PLANKS, 3, 4, 9);
            world.setBlock(Blocks.OAK_PLANKS, 3, 4, 8);
            world.setBlock(Blocks.OAK_PLANKS, 3, 5, 9);
            world.setBlock(Blocks.OAK_PLANKS, 3, 5, 8);
            world.setBlock(Blocks.OAK_PLANKS, 3, 6, 9);
            world.setBlock(Blocks.OAK_PLANKS, 3, 6, 8);
            world.setBlock(Blocks.OAK_PLANKS, 2, 4, 10);
            world.setBlock(Blocks.OAK_PLANKS, 2, 5, 10);
            world.setBlock(Blocks.OAK_PLANKS, 1, 4, 10);
            world.setBlock(Blocks.OAK_PLANKS, 1, 5, 10);
            world.setBlock(Blocks.OAK_PLANKS, 2, 6, 10);
            world.setBlock(Blocks.OAK_PLANKS, 1, 6, 10);
            world.setBlock(Blocks.OAK_PLANKS, 0, 4, 10);
            world.setBlock(Blocks.OAK_PLANKS, 0, 5, 10);
            world.setBlock(Blocks.OAK_PLANKS, 0, 6, 10);
            world.setBlock(Blocks.OAK_PLANKS, -1, 4, 10);
            world.setBlock(Blocks.OAK_PLANKS, -1, 5, 10);
            world.setBlock(Blocks.OAK_PLANKS, -1, 6, 10);
            world.setBlock(Blocks.OAK_PLANKS, -2, 4, 9);
            world.setBlock(Blocks.OAK_PLANKS, -2, 4, 8);
            world.setBlock(Blocks.OAK_PLANKS, -2, 4, 7);
            world.setBlock(Blocks.OAK_PLANKS, -2, 4, 6);
            world.setBlock(Blocks.OAK_PLANKS, -2, 5, 9);
            world.setBlock(Blocks.OAK_PLANKS, -2, 5, 8);
            world.setBlock(Blocks.OAK_PLANKS, -2, 5, 7);
            world.setBlock(Blocks.OAK_PLANKS, -2, 5, 6);
            world.setBlock(Blocks.OAK_PLANKS, -2, 6, 9);
            world.setBlock(Blocks.OAK_PLANKS, -2, 6, 8);
            world.setBlock(Blocks.OAK_PLANKS, -2, 6, 7);
            world.setBlock(Blocks.OAK_PLANKS, -2, 6, 6);
            world.setBlock(Blocks.OAK_PLANKS, 2, 4, 5);
            world.setBlock(Blocks.OAK_PLANKS, 2, 5, 5);
            world.setBlock(Blocks.OAK_PLANKS, 2, 6, 5);
            world.setBlock(Blocks.OAK_PLANKS, -1, 4, 5);
            world.setBlock(Blocks.OAK_PLANKS, -1, 5, 5);
            world.setBlock(Blocks.OAK_PLANKS, -1, 6, 5);
            world.setBlock(Blocks.OAK_PLANKS, 0, 6, 5);
            world.setBlock(Blocks.OAK_PLANKS, 1, 6, 5);
            world.setBlock(Blocks.OAK_PLANKS, 0, 4, 5);
            world.setBlock(Blocks.OAK_PLANKS, 0, 5, 5);
        }
        world.setBlock(Blocks.CRAFTING_TABLE, 0, 4, 7);
        playerRenderer = new PlayerRenderer();
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
        time = world.getTime();
        {
            Vector3f day = new Vector3f(0.72f, 0.83f, 1);
            Vector3f sun = new Vector3f(0.78f, 0.33f, 0.21f);
            Vector3f night = new Vector3f(0.04f, 0.05f, 0.08f);

            Vector3f[] colors = new Vector3f[]{
                    sun,
                    day,
                    day,
                    day,
                    day,
                    day,
                    sun,
                    night,
                    night,
                    night,
                    night,
                    night,
                    sun
            };
            int i = (int) (time * (colors.length - 1));
            float r = (time - i / (float) (colors.length - 1)) * (colors.length - 1);
            Vector3f a = colors[i];
            Vector3f b = colors[i + 1];
            Vector3d color = new Vector3d(
                    a.x * a.x * (1.0 - r) + b.x * b.x * (r),
                    a.y * a.y * (1.0 - r) + b.y * b.y * (r),
                    a.z * a.z * (1.0 - r) + b.z * b.z * (r)
            );

            context.clear((float) sqrt(color.x), (float) sqrt(color.y), (float) sqrt(color.z), 1f);
        }

        skyVao.bind();
        skyEbo.bind();
        skyShader.setFloat("time", time);
        skyShader.setMat4("projMat", false, camera.getMatrix(context));
        skyShader.setMat4("modelMat", false,
                new Matrix4f().translate(camera.position.x, 0, camera.position.z)
        );
        skyShader.setFloat("yOffset", Math.min(camera.position.y - 1f, camera.position.y * 0.5f - 5));
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        skyShader.setInt("layer", 0);
        glDrawElements(GL_TRIANGLES, skyEbo.length(), GL_UNSIGNED_INT, 0);
        skyShader.setFloat("yOffset", max(50, camera.position.y + 50));
        skyShader.setInt("layer", 1);
        glDrawElements(GL_TRIANGLES, skyEbo.length(), GL_UNSIGNED_INT, 0);

        sunMoonMesh.resetModelMatrix();
        sunMoonMesh.getModelMatrix().translate(camera.position);
        sunMoonMesh.getModelMatrix().scale(100);
        sunMoonMesh.getModelMatrix().rotateZ((float) (time * Math.PI * 2));
        sunMoonMesh.getModelMatrix().translate(5, 0, 0);
        sunMoonMesh.getModelMatrix().scale(2);
        sunMoonMesh.getModelMatrix().rotateY((float) -PI / 2);
        glBlendFunc(GL_ONE, GL_ONE);
        context.getRenderer().get3D().render(camera, sunMoonMesh, 0, sunTexture);
        sunMoonMesh.resetModelMatrix();
        sunMoonMesh.getModelMatrix().translate(camera.position);
        sunMoonMesh.getModelMatrix().scale(100);
        sunMoonMesh.getModelMatrix().rotateZ((float) ((time * Math.PI * 2) + Math.PI));
        sunMoonMesh.getModelMatrix().translate(5, 0, 0);
        sunMoonMesh.getModelMatrix().scale(2);
        sunMoonMesh.getModelMatrix().rotateY((float) -PI / 2);
        context.getRenderer().get3D().render(camera, sunMoonMesh, 0, moonTexture);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Stars
        if (!(0.05f < time && time < 0.45f)) {
            GameObjectRenderer renderer = new GameObjectRenderer(context);
            float a = 0;
            if (time > 0.45) {
                a = (time - 0.45f) / 0.55f;
            } else if (time < 0.05) {
                a = 1 - time / 0.05f;
            }
            renderer.color(1, 1, 1, a);
            for (Star star : stars) {
                renderer.resetModelMat();

                float distance = 250;
                renderer.getModelMat().translate(camera.position);
                renderer.getModelMat().rotateZ((float) (time * PI * 2));
                renderer.getModelMat().rotateY(star.yaw);
                renderer.getModelMat().rotateZ((star.pitch));
                renderer.getModelMat().translate(distance, 0, 0);
                renderer.renderBox(camera, 0, 0, 0, 1, 1, 1, true);
            }
        }
        debugInfo = "Time: " + String.format("%.2f %%", time * 100) + " | Light Level: " + world.getEnvironmentLight(
                world.blockXOf(player.position().x),
                world.blockYOf(player.position().y),
                world.blockZOf(player.position().z)
        );
    }

    @Override
    public void onRender(RenderContext context, double vdt) {

        world.onTick(vdt);
        player.onTick(vdt);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        renderSky(context);

        firstPersonController.onTick(vdt);
        worldRenderer.render(context.getRenderer().get3D(), getCamera());

        var objectRenderer = new GameObjectRenderer(context);

        {
            Vector3i blockPos = firstPersonController.rayCast()[0];
            if (blockPos != null) {
                objectRenderer.renderBlockSelectionBox(camera, blockPos.x, blockPos.y, blockPos.z);
            }

            playerRenderer.position().y = 4;
            playerRenderer.render(camera, objectRenderer, vdt);
            if (new Vector3f(playerRenderer.position()).add(0, 1.8f, 0).distanceSquared(camera.position) < 10) {
                playerRenderer.pitch = Directions.lookAt(new Vector3f(playerRenderer.position()).add(0, 1.8f, 0), camera.position).x;
                playerRenderer.yaw = Directions.lookAt(playerRenderer.position(), camera.position).y;
            }

            {
                if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_I) == GLFW_PRESS) {
                    playerRenderer.pitch += vdt;
                }
                if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_K) == GLFW_PRESS) {
                    playerRenderer.pitch -= vdt;
                }
                if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_J) == GLFW_PRESS) {
                    playerRenderer.yaw -= vdt;
                }
                if (glfwGetKey(MinecraftClient.getInstance().getWindow().getWindowHandle(), GLFW_KEY_L) == GLFW_PRESS) {
                    playerRenderer.yaw += vdt;
                }
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
