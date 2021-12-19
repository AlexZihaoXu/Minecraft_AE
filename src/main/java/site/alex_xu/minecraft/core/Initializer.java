package site.alex_xu.minecraft.core;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.PrintStream;

import static org.lwjgl.glfw.GLFW.*;

public final class Initializer extends MinecraftAECore {
    private static Initializer initializer = null;

    public static Initializer getInstance() {
        if (initializer == null)
            initializer = new Initializer();
        return initializer;
    }

    public void initCommon() {
        initLogger();
    }

    public void initClient() {
        initCommon();
        initGLFW();
    }

    private void initLogger() {
        Configurator.setRootLevel(Level.ALL);
        System.setOut(new LogPrintStream(System.out, "STDOUT"));
        System.setErr(new LogPrintStream(System.err, "STDERR"));
    }

    public void initGLFW() {

        glfwSetErrorCallback((error, description) -> {
            getLogger("GLFW").error(org.lwjgl.glfw.GLFWErrorCallback.getDescription(description));
        });
        if (!glfwInit()) {
            String msg = "Could not initialize GLFW System!";
            getLogger().fatal(msg);
            throw new IllegalStateException(msg);
        }

        // Show Version Information
        getLogger().info("LWJGL Version: " + org.lwjgl.Version.getVersion());
        getLogger().info("GLFW Version: " + glfwGetVersionString());
    }


    private static class LogPrintStream extends PrintStream {
        private final String level;

        public LogPrintStream(PrintStream original, String level) {
            super(original);
            this.level = level;
        }

        @Override
        public void println(String line) {
            var logger = LogManager.getLogger(level);
            if (level.equalsIgnoreCase("stderr")) {
                logger.error(line);
            } else {
                logger.info(line);
            }
        }

    }

}
