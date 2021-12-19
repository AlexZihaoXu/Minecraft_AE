package site.alex_xu.minecraft.client.render;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

import javax.security.auth.callback.Callback;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL43.*;

public class Window extends RenderContext {
    private final _OnRenderCallbackI renderCallback;
    private String title;
    private long windowHandle;
    private boolean vsyncEnabled = false;

    public Window(_OnRenderCallbackI renderCallback, String title, int width, int height) {
        this.title = title;
        this.renderCallback = renderCallback;
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

    public void setVsyncEnabled(boolean vsyncEnabled) {
        this.vsyncEnabled = vsyncEnabled;
        glfwSwapInterval(vsyncEnabled ? 1 : 0);
    }

    protected void createWindow() {
        if (windowHandle != 0) {
            glfwFreeCallbacks(windowHandle);
            glfwDestroyWindow(windowHandle);
        }

        setupWindowHint();
        windowHandle = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        glfwSetFramebufferSizeCallback(getWindowHandle(), this::onResize);
        glfwSetMouseButtonCallback(getWindowHandle(), this::onMouseButtonChange);
        glfwSetCursorEnterCallback(getWindowHandle(), this::onMouseEnterLeave);
        glfwSetKeyCallback(getWindowHandle(), this::onKeyChange);
        glfwSetCursorPosCallback(getWindowHandle(), this::onMouseMove);

        glEnable(GL_DEBUG_OUTPUT);
        if (!System.getProperty("os.name").toLowerCase().strip().contains("mac"))
            glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
                if (severity >= GL_DEBUG_SEVERITY_LOW)
                    getLogger("GL").warn(GLDebugMessageCallback.getMessage(length, message));
            }, 0);

        glfwSetWindowSizeLimits(getWindowHandle(), 256, 256, -1, -1);
    }

    // Events

    public void onResize(long window, int width, int height) {

    }

    public void onMouseButtonChange(long window, int button, int action, int mods) {

    }

    public void onMouseMove(long window, double x, double y) {

    }

    public void onMouseEnterLeave(long window, boolean entered) {

    }

    public void onKeyChange(long window, int key, int scancode, int action, int mods) {

    }

    // Getters

    public long getWindowHandle() {
        return windowHandle;
    }


    // Loop

    public void run() {

        createWindow();

        double lastRenderTime = glfwGetTime();
        while (!glfwWindowShouldClose(getWindowHandle())) {

            double now = glfwGetTime();
            renderCallback.execute(now - lastRenderTime);
            lastRenderTime = now;

            glfwSwapBuffers(getWindowHandle());
            glfwPollEvents();
        }
    }

    // Context

    @Override
    public void bindContext() {
        glfwMakeContextCurrent(windowHandle);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);
    }

    // Interface

    public interface _OnRenderCallbackI extends Callback {
        void execute(double videoDeltaTime);
    }
}
