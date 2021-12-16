package vkutils;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.JNI.invokePI;
import static org.lwjgl.system.MemoryUtil.memGetLong;

public final class VkMod {
static boolean a = true;
    private static boolean aa;

    public static void main(String[] args)
    {
        //System.out.println(Paths.get("").toAbsolutePath().toString());

        ScalarArray scalarArray = new ScalarArray(4);
        scalarArray.Put(0, 1120);
        scalarArray.Put(1, 10);
        scalarArray.Put(2, 34);
        scalarArray.Put(3, 34, 35, 36);
        long aLongx = scalarArray.getLong(0);
        long aLong = scalarArray.getLong(1);
        long aLong1 = scalarArray.getLong(2);
        long aLong2 = scalarArray.getLong(3);
        long aLong3 = scalarArray.getLong(4);
        long aLong4 = scalarArray.getLong(5);
        System.out.println(aLongx);
        System.out.println(aLong);
        System.out.println(aLong1);
        System.out.println(aLong2);
        System.out.println(aLong3);
        System.out.println(aLong4);
        scalarArray.Del();
        VkUtils2.extracted();


//            int i = 0;
        new Thread(VkMod::run).start();
//        new Thread(VkMod::run2).start();
        while (invokePI(VkUtils2.window, Functions.WindowShouldClose) == 0)
        {

            /*while (aa) */{

            renderer2.Renderer2.drawFrame();
            }
//aa=true;
            glfwPollEvents();



        }
        a=false;

        renderer2.cleanup();
        glfwTerminate();
    }

   /* private static void run2()
    {
        while (a)
        {
            renderer2.UniformBufferObject.updateUniformBuffer(1%renderer2.Renderer2.currentFrame);
        }
    }*/

    //todo: Wake from callBack...
    private static void run()
    {
        long l = System.currentTimeMillis();
//            renderer2.UniformBufferObject.updateUniformBuffer(0);
        while (a) {

//        renderer2.Renderer2.drawFrame(0);
            //glfwWaitEventsTimeout(1);

//            renderer2.UniformBufferObject.updateUniformBuffer(renderer2.Renderer2.currentFrame);
            //aa=true;
            if (System.currentTimeMillis() > l) {
                //a=invokePI(VkUtils2.window, Functions.WindowShouldClose)==0;
//                /*aa=false;*/glfwPollEvents();
                //todo: need to thritile/manage to fixed branchless Tockrate to avoid excess Polling.Buffering./Overehad

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
