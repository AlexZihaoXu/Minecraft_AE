package site.alex_xu.minecraft.client.utils;

import site.alex_xu.minecraft.core.MinecraftAECore;

public abstract class Freeable extends MinecraftAECore {
    private boolean freed = false;

    abstract protected void onDispose();

    public final boolean isFreed() {
        return freed;
    }

    public final void free() {
        if (isFreed())
            return;
        onDispose();
        freed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        free();
    }
}
