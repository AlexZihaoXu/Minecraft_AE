package site.alex_xu.minecraft.client.screen.world;

import site.alex_xu.minecraft.client.MinecraftClient;
import site.alex_xu.minecraft.client.control.FirstPersonController;
import site.alex_xu.minecraft.client.model.Model;
import site.alex_xu.minecraft.client.model.ModelBuilder;
import site.alex_xu.minecraft.client.screen.Screen;
import site.alex_xu.minecraft.client.utils.RenderContext;
import site.alex_xu.minecraft.client.utils.shader.Shader;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class WorldScreen extends Screen {


    protected Camera camera = new Camera();
    protected FirstPersonController firstPersonController;

    public Camera getCamera() {
        return camera;
    }

    Model model;

    @Override
    public void onSetup() {
        ModelBuilder builder = new ModelBuilder();
        int a = builder.vertex(-0.5f, 0, 0, 1, 1, 1, 1, 0, 0);
        int b = builder.vertex(0.5f, 0, 0, 1, 1, 1, 1, 0, 0);
        int c = builder.vertex(0, 0.5f, 0, 1, 1, 1, 1, 0, 0);
        builder.addFace(a, b, c);
        model = builder.build();
        firstPersonController = new FirstPersonController(MinecraftClient.getInstance().getWindow(), camera);
        camera.yaw = Math.PI / 2;
    }

    @Override
    public void onDispose() {
        firstPersonController.dispose();
    }

    @Override
    public void onRender(RenderContext context, double vdt) {

        model.resetModelMatrix().getModelMatrix().translate(0, 0, 5).rotateY((float) (glfwGetTime()));

        context.getRenderer().clear(0.5f);
        context.getRenderer().get3D()
                .render(camera, model);
    }
}
