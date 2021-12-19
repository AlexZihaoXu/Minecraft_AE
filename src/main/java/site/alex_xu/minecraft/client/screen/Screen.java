package site.alex_xu.minecraft.client.screen;

import site.alex_xu.minecraft.client.utils.RenderContext;
import site.alex_xu.minecraft.core.MinecraftAECore;

public abstract class Screen extends MinecraftAECore {

    // Called when this screen is loaded
    public abstract void onSetup();

    // Called when this screen is closed or switched off
    public abstract void onDispose();

    // Called when this screen is required to render
    public abstract void onRender(RenderContext context, double vdt);
}
