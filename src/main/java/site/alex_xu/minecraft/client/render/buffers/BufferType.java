package site.alex_xu.minecraft.client.render.buffers;

import site.alex_xu.minecraft.client.render.Freeable;
import site.alex_xu.minecraft.core.MinecraftAECore;

public abstract class BufferType extends Freeable {

    protected int id;
    protected int length;

    public int length() {
        return length;
    }

    abstract protected void onDispose();

    public int getID() {
        return id;
    }


    abstract protected void bind();

    abstract protected void unbind();


}
