package site.alex_xu.minecraft.client.world;

import site.alex_xu.minecraft.client.chunk.ChunkRenderer;
import site.alex_xu.minecraft.client.render.ModelRenderer;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.chunk.Chunk;
import site.alex_xu.minecraft.server.world.World;

import java.util.TreeMap;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class WorldRenderer extends MinecraftAECore {
    private final TreeMap<World.ChunkPos, ChunkRenderer> chunkRenderers = new TreeMap<>();
    private final World world;

    public WorldRenderer(World world) {
        this.world = world;
        world.registerChunkCreationCallback(this::onChunkCreation);
    }

    public World getWorld() {
        return world;
    }

    public void onChunkCreation(World world, Chunk chunk) {
        chunkRenderers.put(new World.ChunkPos(chunk.getX(), chunk.getY()), new ChunkRenderer(chunk));
    }

    public void render(ModelRenderer renderer, Camera camera) {
        if (renderer.getShader().hasUniform("animationTime"))
            renderer.getShader().setFloat("animationTime", (float) glfwGetTime());
        if (renderer.getShader().hasUniform("time"))
            renderer.getShader().setFloat("time", (float) world.getTime());
        for (ChunkRenderer chunkRenderer : chunkRenderers.values()) {
            chunkRenderer.renderSolid(renderer, camera);
        }
        for (ChunkRenderer chunkRenderer : chunkRenderers.values()) {
            chunkRenderer.renderLiquid(renderer, camera);
        }
    }
}
