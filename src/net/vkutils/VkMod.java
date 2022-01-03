package vkutils;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.JNI.invokePI;
import static org.lwjgl.system.JNI.invokeV;

public final class VkMod {
private static boolean a = true;
//    private static boolean aa;

    public static void main(String[] args)
    {

        VkUtils2.extracted();

//            int i = 0;
         new Thread(VkMod::run).start();

        while (invokePI(VkUtils2.window, Functions.WindowShouldClose) == 0)
        {


            renderer2.Renderer2.drawFrame();

            invokeV(Functions.PollEvents);


        }
        a=false;

        renderer2.cleanup();
        glfwTerminate();
    }

    //todo: Wake from callBack...
    private static void run()
    {
        long l = System.currentTimeMillis();
        cursorPos.invocationcallbacks.isActive();;//glfwPollEvents();
        while (a) {

            glfwWaitEventsTimeout(1);

            if (System.currentTimeMillis() > l) {

                //todo: need to thritile/manage to fixed branchless Tockrate to avoid excess Polling.Buffering./Overehad

                System.out.println(renderer2.Renderer2.frps);
                renderer2.Renderer2.frps = 0;
                l += 1000L;
            }

        }
    }

}
