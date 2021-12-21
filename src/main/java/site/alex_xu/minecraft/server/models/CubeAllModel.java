package site.alex_xu.minecraft.server.models;

public class CubeAllModel extends CubeModel {
    public CubeAllModel setAllTextures(String path) {
        setFaceTexture("north", path);
        setFaceTexture("south", path);
        setFaceTexture("east", path);
        setFaceTexture("west", path);
        setFaceTexture("top", path);
        setFaceTexture("bottom", path);
        return this;
    }
}
