package site.alex_xu.minecraft.client.screen;

import site.alex_xu.minecraft.client.utils.RenderContext;
import site.alex_xu.minecraft.core.MinecraftAECore;

import java.util.ArrayList;

public class ScreenManager extends MinecraftAECore {
    protected ArrayList<Screen> screenStack = new ArrayList<>();

    public void pushScreen(Screen screen) {
        screenStack.add(screen);
        screen.onSetup();
    }

    public int stackSize() {
        return screenStack.size();
    }

    public void clearStack() {
        for (int i = 0; i < stackSize(); i++) {
            popScreen();
        }
    }

    public void popScreen() {
        screenStack.get(screenStack.size() - 1).onDispose();
        screenStack.remove(screenStack.size() - 1);
    }

    public void render(RenderContext context, double vdt) {
        for (Screen screen : screenStack) {
            screen.onRender(context, vdt);
        }
    }
}
