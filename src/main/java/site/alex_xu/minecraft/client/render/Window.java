package site.alex_xu.minecraft.client.render;

import site.alex_xu.minecraft.core.MinecraftAECore;

import static org.lwjgl.glfw.GLFW.*;

public class Window extends MinecraftAECore {
    private int width, height;
    private static interface OnRenderCallbackI {
        public void execute();
    }

    private static interface  OnUpdateCallbackI {

    }

    public Window(int width, int height) {
        this.width = width;
        this.height = height;
    }

    protected void setupWindowHint() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 0);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
