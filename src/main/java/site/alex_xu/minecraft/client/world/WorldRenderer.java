package site.alex_xu.minecraft.client.world;

import site.alex_xu.minecraft.client.chunk.ChunkRenderer;
import site.alex_xu.minecraft.client.render.ModelRenderer;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.chunk.Chunk;
import site.alex_xu.minecraft.server.world.World;

import java.util.TreeMap;

public class WorldRenderer extends MinecraftAECore {
    private final TreeMap<World.ChunkPos, ChunkRenderer> chunkRenderers = new TreeMap<>();

    public WorldRenderer(World world) {
        world.registerChunkCreationCallback(this::onChunkCreation);
    }

    public void onChunkCreation(World world, Chunk chunk) {
        chunkRenderers.put(new World.ChunkPos(chunk.getX(), chunk.getY()), new ChunkRenderer(chunk));
    }

    public void render(ModelRenderer renderer, Camera camera) {
        for (ChunkRenderer chunkRenderer : chunkRenderers.values()) {
            chunkRenderer.render(renderer, camera);
        }
    }
}
