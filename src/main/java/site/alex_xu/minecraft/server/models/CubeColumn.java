package site.alex_xu.minecraft.server.models;

public class CubeColumn extends CubeBottomTop {
    public CubeColumn setEnd(String path) {
        setTop(path);
        setBottom(path);
        return this;
    }

    public CubeColumn setSide(String path) {
        return (CubeColumn) super.setSide(path);
    }
}
