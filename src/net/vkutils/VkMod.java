package vkutils;

import org.lwjgl.glfw.GLFW;

import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.system.JNI.invokePI;

public class VkMod {
    public  static void main(String[] args)
    {
        System.out.println(Paths.get("").toAbsolutePath().toString());
        VkUtils2.extracted();

        long l = System.currentTimeMillis();
//            int i = 0;
        while (invokePI(VkUtils2.window, GLFW.Functions.WindowShouldClose)==0)
        {


            renderer2.Renderer2.drawFrame();

            renderer2.handleInputs();


            if (System.currentTimeMillis()>l) {

                System.out.println(renderer2.Renderer2.frps);
//                System.out.println(VkUtils2.Renderer.i);
//                System.out.println(VkUtils2.Renderer.FenceStat);
                renderer2.Renderer2.frps=0;
                renderer2.Renderer2.i=0;
//                VkUtils2.Renderer.FenceStat=0;
                l += 1000L;
            }

        }
        renderer2.cleanup();
        glfwTerminate();
    }

}
