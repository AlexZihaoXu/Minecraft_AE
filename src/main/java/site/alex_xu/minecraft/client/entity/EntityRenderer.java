package site.alex_xu.minecraft.client.entity;

import org.joml.Vector3f;
import org.joml.Vector4f;
import site.alex_xu.minecraft.client.model.Mesh;
import site.alex_xu.minecraft.client.model.MeshBuilder;
import site.alex_xu.minecraft.client.render.GameObjectRenderer;
import site.alex_xu.minecraft.client.resource.TextureAtlas;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.client.utils.Texture;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.world.World;

public abstract class EntityRenderer extends MinecraftAECore {
    private static Texture texture = null;
    private static boolean initialized = false;
    private final Vector3f position = new Vector3f();

    public Vector3f position() {
        return position;
    }

    protected float units(int u) {
        return u / 16f;
    }

    protected Vector4f rect(int x, int y, int w, int h) {
        return new Vector4f(
                x / (float) texture.getWidth(), (texture.getHeight() - (y + h)) / (float) texture.getHeight(),
                (x + w) / (float) texture.getWidth(), (texture.getHeight() - y) / (float) texture.getHeight()
        );
    }

    public void init() {
        if (!initialized) {
            initialized = true;
            texture = TextureAtlas.getInstance().getTextureFromPath("assets/textures/entity/steve.png");

        }
    }

    public Mesh createBodyPart(
            int width, int height, int length,
            Vector4f topRect,
            Vector4f bottomRect,
            Vector4f leftRect,
            Vector4f rightRect,
            Vector4f frontRect,
            Vector4f backRect) {
        float w = width / 16f;
        float h = height / 16f;
        float l = length / 16f;
        MeshBuilder builder = new MeshBuilder();
        {
            builder.addFace(
                    builder.vertex(-w / 2f, -h / 2f, l / 2f, 1F, 0, 1, 1, backRect.z, backRect.y),
                    builder.vertex(-w / 2f, h / 2f, l / 2f, 1F, 0, 1, 1, backRect.z, backRect.w),
                    builder.vertex(w / 2f, h / 2f, l / 2f, 1F, 0, 1, 1, backRect.x, backRect.w),
                    builder.vertex(w / 2f, -h / 2f, l / 2f, 1F, 0, 1, 1, backRect.x, backRect.y)
            );
            builder.addFace(
                    builder.vertex(w / 2f, -h / 2f, -l / 2f, 1F, 0, 1, 1, frontRect.x, frontRect.y),
                    builder.vertex(w / 2f, h / 2f, -l / 2f, 1F, 0, 1, 1, frontRect.x, frontRect.w),
                    builder.vertex(-w / 2f, h / 2f, -l / 2f, 1F, 0, 1, 1, frontRect.z, frontRect.w),
                    builder.vertex(-w / 2f, -h / 2f, -l / 2f, 1F, 0, 1, 1, frontRect.z, frontRect.y)
            );
            builder.addFace(
                    builder.vertex(w / 2f, h / 2f, -l / 2f, 1F, 0, 1, 1, topRect.x, topRect.y),
                    builder.vertex(w / 2f, h / 2f, l / 2f, 1F, 0, 1, 1, topRect.x, topRect.w),
                    builder.vertex(-w / 2f, h / 2f, l / 2f, 1F, 0, 1, 1, topRect.z, topRect.w),
                    builder.vertex(-w / 2f, h / 2f, -l / 2f, 1F, 0, 1, 1, topRect.z, topRect.y)
            );
            builder.addFace(
                    builder.vertex(-w / 2f, -h / 2f, -l / 2f, 1F, 0, 1, 1, bottomRect.z, bottomRect.y),
                    builder.vertex(-w / 2f, -h / 2f, l / 2f, 1F, 0, 1, 1, bottomRect.z, bottomRect.w),
                    builder.vertex(w / 2f, -h / 2f, l / 2f, 1F, 0, 1, 1, bottomRect.x, bottomRect.w),
                    builder.vertex(w / 2f, -h / 2f, -l / 2f, 1F, 0, 1, 1, bottomRect.x, bottomRect.y)
            );
            builder.addFace(
                    builder.vertex(-w / 2f, -h / 2f, -l / 2f, 1F, 0, 1, 1, leftRect.x, leftRect.y),
                    builder.vertex(-w / 2f, h / 2f, -l / 2f, 1F, 0, 1, 1, leftRect.x, leftRect.w),
                    builder.vertex(-w / 2f, h / 2f, l / 2f, 1F, 0, 1, 1, leftRect.z, leftRect.w),
                    builder.vertex(-w / 2f, -h / 2f, l / 2f, 1F, 0, 1, 1, leftRect.z, leftRect.y)
            );
            builder.addFace(
                    builder.vertex(w / 2f, -h / 2f, l / 2f, 1F, 0, 1, 1, rightRect.x, rightRect.y),
                    builder.vertex(w / 2f, h / 2f, l / 2f, 1F, 0, 1, 1, rightRect.x, rightRect.w),
                    builder.vertex(w / 2f, h / 2f, -l / 2f, 1F, 0, 1, 1, rightRect.z, rightRect.w),
                    builder.vertex(w / 2f, -h / 2f, -l / 2f, 1F, 0, 1, 1, rightRect.z, rightRect.y)
            );
        }

        return builder.build();
    }

    public EntityRenderer() {
        init();
    }

    public Texture getTexture() {
        return texture;
    }

    public abstract void render(Camera camera, GameObjectRenderer renderer, double vdt, World world);
}
