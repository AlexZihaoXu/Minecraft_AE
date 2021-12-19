package site.alex_xu.minecraft.client;

import site.alex_xu.minecraft.client.utils.Framebuffer;
import site.alex_xu.minecraft.client.utils.Texture;
import site.alex_xu.minecraft.client.utils.Window;
import site.alex_xu.minecraft.client.utils.buffers.ElementBuffer;
import site.alex_xu.minecraft.client.utils.buffers.VertexArray;
import site.alex_xu.minecraft.client.utils.buffers.VertexBuffer;
import site.alex_xu.minecraft.client.utils.shader.Shader;
import site.alex_xu.minecraft.client.resource.ResourceManager;
import site.alex_xu.minecraft.core.Initializer;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Minecraft;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

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
                -0.5f, -0.5f, 0, 0, 0,
                -0.5f, 0.5f, 0, 0, 1,
                0.5f, 0.5f, 0, 1, 1,
                0.5f, -0.5f, 0, 1, 0
        });
        vao.configure(vbo)
                .push(3)
                .push(2)
                .apply();
        shader = new Shader()
                .addFromResource("test.vert")
                .addFromResource("test.frag")
                .link();
        ebo = new ElementBuffer(new int[] {
                0, 1, 2,
                2, 3, 0
        });
        texture = new Texture(ResourceManager.getInstance().readBytesFromResource("assets/textures/blocks/dirt.png"));
        fbo = new Framebuffer(512, 512);
        fbo.bindContext();
        glClearColor(1, 0, 1, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    }

    public void onRender(double vdt) {
        window.bindContext();
        window.clear(1, 1, 1, 1);
        shader.bind();
        vao.bind();
        ebo.bind();

        glActiveTexture(GL_TEXTURE0);
        shader.setInt("texture0", 0);
        fbo.bind();

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        window.getRenderer().get2D()
                .clear(0.5f)
                .image(texture, 0, 0, 16, 16, 0, 0, 16, 16);
    }

    public void onDispose() {

    }

    // Test

    public VertexArray vao;
    public VertexBuffer vbo;
    public ElementBuffer ebo;
    public Framebuffer fbo;
    private Shader shader;
    private Texture texture;

}
