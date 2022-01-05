package vkutils;

import org.joml.Math;
import org.lwjgl.vulkan.*;
import vkutils.setup.*;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public final class renderer2 {

    public static final class Renderer2 {
        public static final long AvailableSemaphore;
        private static final int MAX_FRAMES_IN_FLIGHT = 3;


        private static final long FinishedSemaphore;


        private static final long address2;
        private static final long TmUt = 1000000000;

        private static final long VkPresentInfoKHR1;
        private static final long swapchain = memGetLong(SwapChainSupportDetails.swapChain);
        static long frps;
        private static final long aa = memGetLong(MemSysm.address)+0x200;
        private static int currentFrame;

        private static final long value = MemSysm.address + MemSysm.size;
        //        private static final FloatBuffer pointerBuffer = memFloatBuffer(memGetLong(MemSysm.address)+0x200, 32);

        static {
            {

                //                FinishedSemaphore = PipeLine.doPointerAllocSafe(semaphoreInfo, PipeLine.capabilities.vkCreateSemaphore);

                /* for(int i = 0;i < MAX_FRAMES_IN_FLIGHT;i++)*/
                {
//                    long vkSemaphoreCreateInfo = doAbsCalloc2(VkSemaphoreCreateInfo.SIZEOF,VkSemaphoreCreateInfo.ALIGNOF)-4L;
                    VkSemaphoreCreateInfo vkSemaphoreCreateInfo = VkSemaphoreCreateInfo.create(MemSysm.malloc3(VkSemaphoreCreateInfo.SIZEOF)).sType$Default();
                    AvailableSemaphore= MemSysm.doPointerAllocSafe3(vkSemaphoreCreateInfo, Queues.device.getCapabilities().vkCreateSemaphore);
                    FinishedSemaphore= MemSysm.doPointerAllocSafe3(vkSemaphoreCreateInfo, Queues.device.getCapabilities().vkCreateSemaphore);


                    VkSubmitInfo info = VkSubmitInfo.create(MemSysm.malloc3(VkSubmitInfo.SIZEOF))
                    		.set(
                    		VK_STRUCTURE_TYPE_SUBMIT_INFO,
                    		NULL,
                    		1,
                    		MemSysm.stack.longs(AvailableSemaphore),
                    		MemSysm.ints(VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT), MemSysm.longs(Buffers.commandBuffers),
                    		MemSysm.stack.longs(FinishedSemaphore)
                    		);
                    MemSysm.Memsys2.free(info);
                    address2=info.address();


                    VkPresentInfoKHR1= VkPresentInfoKHR.create(MemSysm.malloc(VkPresentInfoKHR.SIZEOF)).address();
                    VkPresentInfoKHR.nsType(VkPresentInfoKHR1, VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore));
                    memPutInt(VkPresentInfoKHR1 + VkPresentInfoKHR.SWAPCHAINCOUNT, 1);
                    memPutLong(address2 + VkSubmitInfo.PCOMMANDBUFFERS, value);
                    VkSubmitInfo.ncommandBufferCount(address2, 1);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PIMAGEINDICES, MemSysm.address);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PSWAPCHAINS, SwapChainSupportDetails.swapChain);
                    //nmemFree(VkPresentInfoKHR1);
//                    VkPresentInfoKHR.nsType(VkPresentInfoKHR1, VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
//                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore));
//                    memPutInt(VkPresentInfoKHR1 + VkPresentInfoKHR.SWAPCHAINCOUNT, 1);
                    // Align address to the specified alignment
                    VkSubmitInfo.validate(address2);
                    VkPresentInfoKHR.validate(VkPresentInfoKHR1);
//                    MemSysm.Memsys2.decFrm(VkPresentInfoKHR1); //Not alloctaed witH Contgous-Non_frag Malloc alloctaion, propb shouldn;t be here

                    System.out.println(MemSysm.sizeof(address2));
                    System.out.println("Descriptor Attachment: "+ (memGetLong(MemSysm.address) + 0x200));

                }

            }

        }


    /* todo: Possible me issue: uusing msiafterburner sems yo dratically and perminantly improve the Frarate even after it is closed:
            pehaes a mem sync effect introduced by the OSD overlay which is helping to synchronise/talise the queue/Pipeline/CommandBufferSubmissions e.g. .etc i.e.
            [might be afence/Blocking.Brarier problem as intially had WaiFencesand rdetfencescalls removed intially
        */

        /*todo:
         *  Use TWo Modes: Push Constants Mode and Deferred MemBuffer/DescripterSet Mode*/
        static void drawFrame()
        {


            {
                //Must Push andPop/Pop/Push at the Exact Currect Intervals

                nvkAcquireNextImageKHR(Queues.device, swapchain, TmUt, AvailableSemaphore, VK_NULL_HANDLE, MemSysm.address);


                /*mulAffineL(
                    (float) Math.sin(angle),
                    (float) Math.cos(angle+Half_Pi)
            );*/

                updateUniformBuffer();
//                PipeLine.imagesInFlight.get(PipeLine.currentFrame);
                //Wait frames

//                    imagesInFlight[PipeLine.currentFrame]= (PipeLine.currentFrame);

//                    VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack.stack);
//                    submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
//                            .waitSemaphoreCount(1)
//                    VkSubmitInfo.npWaitSemaphores((AvailableSemaphore));
//                    VkSubmitInfo.npSignalSemaphores((FinishedSemaphore));
//                            .pWaitDstStageMask(stack.stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
//                            .pSignalSemaphores(stack.stack.longs(FinishedSemaphore))
//                            .pCommandBuffers(stack.stack.pointers(PipeLine.commandBuffers[(imageIndex)]));
//                    vkWaitForFences(Queues.device, vkFence, false, TmUt);


                memPutLong(value, memGetLong(Buffers.commandBuffers.address(currentFrame)));


                nvkQueueSubmit(Queues.graphicsQueue, 1, address2, VK_NULL_HANDLE);

                memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore));


                nvkQueuePresentKHR(Queues.presentQueue, VkPresentInfoKHR1);
                //                   i+= vkResetFences(Queues.device, vkFenceA);


                currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
//                 System.out.println(stack.stack.getPointer());
                frps++;

            }
        }

        //@Benchmark
        static void updateUniformBuffer()
        {
            final double v = ((Math.sin(cursorPos.x / 125) - Math.cos(cursorPos.y / 125)) * UniformBufferObject.aFloat);
            double dirX = Math.sin(v);
            double dirY = Math.cosFromSin(dirX, v);
            //rotateTowardsXY2();//.getToAddress(Renderer2.aa);
            UniformBufferObject.mvp.rotateTowardsXY((float) dirX, (float) dirY, UniformBufferObject.trans).getToAddress(aa);
    //            final double sin = Math.sin(glfwGetTime() / 1000);
    //            trans.translate(x, y+= sin, z).getToAddress(Renderer2.aa);
    //            trans.transform((float) (-cursorPos.y / -cursorPos.x)).getToAddress(Renderer2.aa);
    //            memcpy2();

        }
    }


}