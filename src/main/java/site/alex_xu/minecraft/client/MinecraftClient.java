package site.alex_xu.minecraft.client;

import site.alex_xu.minecraft.client.render.Window;
import site.alex_xu.minecraft.core.Initializer;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.core.Minecraft;

public class MinecraftClient extends MinecraftAECore {
    private static MinecraftClient instance = null;
    private Window window;

    public static MinecraftClient getInstance() {
        if (instance == null)
            instance = new MinecraftClient();
        return instance;
    }

    public static void main(String[] args) {
        getInstance().run();
    }

    // Client Instance

    public MinecraftClient() {
    }

    public void run() {
        getLogger().info("Minecraft (Alex Edition) " + Minecraft.VERSION);
        Initializer.getInstance().initClient();
        window = new Window(this::onRender,"Minecraft(AE) " + Minecraft.VERSION, 856, 482);
        window.run();
    }

    public Window getWindow() {
        return window;
    }

    // Render
    public void onRender(double vdt) {
        window.clear(1, 1, 1, 1);
    }
}
