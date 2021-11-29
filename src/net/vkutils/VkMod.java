package vkutils;

import org.lwjgl.glfw.GLFW;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.JNI.invokePI;

public final class VkMod {
static boolean a = true;
    private static boolean aa;

    public static void main(String[] args)
    {
        //System.out.println(Paths.get("").toAbsolutePath().toString());
        VkUtils2.extracted();


//            int i = 0;
        new Thread(VkMod::run).start();

        while (a)
        {

            /*while (aa) */{
                renderer2.Renderer2.drawFrame();
            }
//aa=true;
            glfwPollEvents();



        }

        renderer2.cleanup();
        glfwTerminate();
    }
//todo: Wake from callBack...
    private static void run()
    {
        long l = System.currentTimeMillis();
        while (a) {

            //glfwWaitEventsTimeout(1);


            //aa=true;

            if (System.currentTimeMillis() > l) {
//                /*aa=false;*/glfwPollEvents();
                //todo: need to thritile/manage to fixed branchless Tockrate to avoid excess Polling.Buffering./Overehad
                a= invokePI(VkUtils2.window, Functions.WindowShouldClose)==0;
//                glfwWaitEventsTimeout(1);
                System.out.println(renderer2.Renderer2.frps);
//                System.out.println(VkUtils2.Renderer.i);
//                System.out.println(VkUtils2.Renderer.FenceStat);
                renderer2.Renderer2.frps = 0;
                renderer2.Renderer2.i = 0;
//                VkUtils2.Renderer.FenceStat=0;
                l += 1000L;
            }
        }
    }

}
