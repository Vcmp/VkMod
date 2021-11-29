package vkutils;

import static org.lwjgl.glfw.GLFW.*;

public class threadedPolledRenderer implements Runnable{
    static void extracted()
    {
        glfwPostEmptyEvent();
        //todo: need to thritile/manage to fixed branchless Tockrate to avoid excess Polling.Buffering./Overehad
    }

    @Override
    public void run()
    {
        extracted();
    }
}
