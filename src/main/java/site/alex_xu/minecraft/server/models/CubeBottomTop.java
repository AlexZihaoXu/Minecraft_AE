package site.alex_xu.minecraft.server.models;

public class CubeBottomTop extends CubeModel {
    public CubeBottomTop setSide(String path) {
        setFaceTexture("north", path);
        setFaceTexture("south", path);
        setFaceTexture("east", path);
        setFaceTexture("west", path);
        return this;
    }

    public CubeBottomTop setTop(String path) {
        setFaceTexture("top", path);
        return this;
    }

    public CubeBottomTop setBottom(String path) {
        setFaceTexture("bottom", path);
        return this;
    }
}
