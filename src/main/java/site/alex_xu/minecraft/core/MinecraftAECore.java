package site.alex_xu.minecraft.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

public class MinecraftAECore {

    public Logger getLogger(String name) {
        return LogManager.getLogger(name);
    }

    public Logger getLogger() {
        return getLogger(getClass().getSimpleName());
    }
}
