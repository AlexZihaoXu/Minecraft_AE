package site.alex_xu.minecraft.client.chunk;

import site.alex_xu.minecraft.client.model.Model;
import site.alex_xu.minecraft.client.render.ModelRenderer;
import site.alex_xu.minecraft.client.screen.world.Camera;
import site.alex_xu.minecraft.core.MinecraftAECore;
import site.alex_xu.minecraft.server.chunk.Chunk;
import site.alex_xu.minecraft.server.chunk.ChunkSection;

public class ChunkRenderer extends MinecraftAECore {
    public ChunkSectionMesher[] chunkSectionMeshers = new ChunkSectionMesher[16];

    public ChunkRenderer(Chunk chunk) {
        chunk.addChunkCreationCallback(this::onCreatingSection);
    }

    public void onCreatingSection(Chunk chunk, ChunkSection section) {
        chunkSectionMeshers[section.getSectionY()] = new ChunkSectionMesher(section);
    }

    public void render(ModelRenderer renderer, Camera camera) {
        for (int i = 0; i < 16; i++) {
            if (chunkSectionMeshers[i] == null)
                continue;
            Model model = chunkSectionMeshers[i].mesh;
            model.resetModelMatrix().getModelMatrix().translate(0, 16 * i, 0);
            renderer.render(camera, model);
        }
    }
}
