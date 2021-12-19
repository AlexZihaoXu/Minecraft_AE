package site.alex_xu.minecraft.client;

import site.alex_xu.minecraft.client.render.Window;
import site.alex_xu.minecraft.client.render.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.render.buffers.VertexArray;
import site.alex_xu.minecraft.client.render.buffers.VertexBuffer;
import site.alex_xu.minecraft.client.render.shader.Shader;
import site.alex_xu.minecraft.core.Initializer;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Minecraft;

import static org.lwjgl.opengl.GL11.*;

public class MinecraftClient extends MinecraftAECore {
    private static MinecraftClient instance = null;
    private Window window;


    // Client Instance

    public MinecraftClient() {
    }

    public static MinecraftClient getInstance() {
        if (instance == null)
            instance = new MinecraftClient();
        return instance;
    }

    public static void main(String[] args) {
        getInstance().run();
    }

    public void run() {
        getLogger().info("Minecraft (Alex Edition) " + Minecraft.VERSION);
        Initializer.getInstance().initClient();
        window = new Window(this::onSetup, this::onDispose, this::onRender, "Minecraft(AE) " + Minecraft.VERSION, 856, 482);
        window.run();
    }

    public Window getWindow() {
        return window;
    }

    // Events
    public void onSetup() {
        vao = new VertexArray();
        vbo = new VertexBuffer(new float[]{
                -0.5f, -0.5f, 0,
                -0.5f, 0.5f, 0,
                0.5f, 0.5f, 0,
                0.5f, -0.5f, 0
        });
        vao.configure(vbo)
                .push(3)
                .apply();
        shader = new Shader()
                .addFromResource("test.vert")
                .addFromResource("test.frag")
                .link();
        ebo = new ElementBuffer(new int[] {
                0, 1, 2,
                2, 3, 0
        });
    }

    public void onRender(double vdt) {
        window.clear(1, 1, 1, 1);
        shader.bind();
        vao.bind();
        ebo.bind();

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }

    public void onDispose() {

    }

    // Test

    public VertexArray vao;
    public VertexBuffer vbo;
    public ElementBuffer ebo;
    private Shader shader;

}
