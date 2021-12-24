package site.alex_xu.minecraft.server.models;

public class FluidSource extends BlockModelDef {
    public FluidSource() {

        /*
          5------6
         /|     /|
        1-|----2 |
        | 4----|-7
        |/     |/
        0------3

          Y
          |
          O--X
         /
        Z
        */
        vertex(0, 0, 0, 1);
        vertex(1, 0, 0.6f, 1);
        vertex(2, 1, 0.6f, 1);
        vertex(3, 1, 0, 1);
        vertex(4, 0, 0, 0);
        vertex(5, 0, 0.6f, 0);
        vertex(6, 1, 0.6f, 0);
        vertex(7, 1, 0, 0);

        face("north", 7, 6, 5, 4);
        face("south", 0, 1, 2, 3);
        face("west", 4, 5, 1, 0);
        face("east", 3, 2, 6, 7);
        face("top", 1, 5, 6, 2);
        face("bottom", 3, 7, 4, 0);

    }

    public FluidSource setSide(String path) {
        setFaceTexture("north", path);
        setFaceTexture("south", path);
        setFaceTexture("east", path);
        setFaceTexture("west", path);
        return this;
    }

    public FluidSource setEnd(String path) {
        setFaceTexture("top", path);
        setFaceTexture("bottom", path);
        return this;
    }
}
