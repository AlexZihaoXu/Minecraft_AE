package site.alex_xu.minecraft.client.chunk;

import site.alex_xu.minecraft.client.model.Model;
import site.alex_xu.minecraft.client.render.ModelRenderer;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.chunk.Chunk;
import site.alex_xu.minecraft.server.chunk.ChunkSection;

public class ChunkRenderer extends MinecraftAECore {
    public ChunkSectionMesher[] chunkSectionMeshers = new ChunkSectionMesher[16];
    private final Chunk chunk;

    public ChunkRenderer(Chunk chunk) {
        chunk.registerChunkCreationCallback(this::onCreatingSection);
        this.chunk = chunk;
    }

    public void onCreatingSection(Chunk chunk, ChunkSection section) {
        chunkSectionMeshers[section.getSectionY()] = new ChunkSectionMesher(section);
    }

    public void render(ModelRenderer renderer, Camera camera) {
        for (int i = 0; i < 16; i++) {
            if (chunkSectionMeshers[i] == null)
                continue;
            Model model = chunkSectionMeshers[i].mesh;
            model.resetModelMatrix().getModelMatrix().translate(chunk.getX() * 16, 16 * i, chunk.getY() * 16);
            renderer.render(camera, model);
        }
    }
}
