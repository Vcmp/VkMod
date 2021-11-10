package vkutils;

import org.lwjgl.glfw.GLFW;

import java.nio.file.Paths;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWaitEventsTimeout;
import static org.lwjgl.system.JNI.invokePI;

public class Main {
    public  static void main(String[] args)
    {
        System.out.println(Paths.get("").toAbsolutePath().toString());
        VkUtils2.createInstance();
        VkUtils2.setupDebugMessenger();
        VkUtils2.Queues.createSurface();
        VkUtils2.Queues.pickPhysicalDevice();
        VkUtils2.VKUtilsSafe.createLogicalDevice();
        VkUtils2.VKUtilsSafe.createSwapChain();
        VkUtils2.SwapChainSupportDetails.createImageViews();
        VkUtils2.PipeLine.createRenderPasses(true);
        VkUtils2.UniformBufferObject.createDescriptorSetLayout();
        VkUtils2.PipeLine.createGraphicsPipelineLayout();

        VkUtils2.PipeLine.createCommandPool();
        VkUtils2.PipeLine.createDepthResources();
        VkUtils2.SwapChainSupportDetails.createFramebuffers();
        VkUtils2.PipeLine.createTextureImage();
        VkUtils2.PipeLine.createTextureImageView();
        VkUtils2.PipeLine.createTextureSampler();
        VkUtils2.PipeLine.createVertexBufferStaging();
        VkUtils2.PipeLine.createIndexBuffer();
        VkUtils2.PipeLine.creanupBufferStaging();
        VkUtils2.UniformBufferObject.createUniformBuffers();
        VkUtils2.UniformBufferObject.createDescriptorPool();
        VkUtils2.UniformBufferObject.createDescriptorSets();
        VkUtils2.PipeLine.createCommandBuffers();
        VkUtils2.stack.stack().pop();

        long l = System.currentTimeMillis();
//            int i = 0;
        while (invokePI(VkUtils2.window, GLFW.Functions.WindowShouldClose)==0)
        {


            VkUtils2.Renderer.drawFrame();

            VkUtils2.handleInputs();


            if (System.currentTimeMillis()>l) {

                System.out.println(VkUtils2.PipeLine.frps);
//                System.out.println(VkUtils2.Renderer.i);
//                System.out.println(VkUtils2.Renderer.FenceStat);
                VkUtils2.PipeLine.frps=0;
                VkUtils2.Renderer.i=0;
//                VkUtils2.Renderer.FenceStat=0;
                l += 1000L;
            }

        }
        VkUtils2.VKUtilsSafe.cleanup();
        glfwTerminate();
    }

}
