package vkutils;

import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.VkUtils2.Queues.*;

final class renderer2 {
    static void cleanup() {


//            for (int i =0; i< Renderer.MAX_FRAMES_IN_FLIGHT;i++)
        glfwPostEmptyEvent();
        vkDeviceWaitIdle(device);
//            vkWaitForFences(Queues.device, Renderer.vkFence, true, 1000000000);

        _mainDeletionQueue();

        vkDestroyDevice(device, VkUtils2.MemSys.pAllocator());
        vkDestroySurfaceKHR(VkUtils2.vkInstance, surface[0], VkUtils2.MemSys.pAllocator());
        vkDestroyInstance(VkUtils2.vkInstance, VkUtils2.MemSys.pAllocator());
        /*glfwDestroyWindow(window);*/


//            vkDestroyInstance(vkInstance, pAllocator);


        glfwDestroyWindow(VkUtils2.window);

        glfwTerminate();
    }

    private static void _mainDeletionQueue()
    {
        doDestroyFreeAlloc(Buffers.vkImage[0], Buffers.capabilities.vkDestroyImage);
        doDestroyFreeAlloc(Buffers.vkAllocMemory[0], Buffers.capabilities.vkFreeMemory);
        doDestroyFreeAlloc(UniformBufferObject.textureSampler[0], Buffers.capabilities.vkDestroySampler);
        doDestroyFreeAlloc(UniformBufferObject.textureImageView[0], Buffers.capabilities.vkDestroyImageView);

        {
            vkDestroySemaphore(device, Renderer2.AvailableSemaphore[0], VkUtils2.MemSys.pAllocator());
//                vkDestroySemaphore(device, VkUtils2.Renderer.FinishedSemaphore[0], pAllocator);
            //vkDestroyFence(device, Renderer2.vkFence[0], VkUtils2.MemSysm.pAllocator());
        }
        vkDestroyDescriptorSetLayout(device, UniformBufferObject.descriptorSetLayout[0], VkUtils2.MemSys.pAllocator());
        vkDestroyDescriptorPool(device, UniformBufferObject.descriptorPool[0], VkUtils2.MemSys.pAllocator());
        for (int i = 0; i < 3; i++)
        {
            vkDestroySwapchainKHR(device, VkUtils2.SwapChainSupportDetails.swapChainImages[i], VkUtils2.MemSys.pAllocator());
            vkDestroyImageView(device, VkUtils2.SwapChainSupportDetails.swapChainImageViews[i], VkUtils2.MemSys.pAllocator());
            vkFreeCommandBuffers(device, Buffers.commandPool[0], Buffers.commandBuffers[i]);
//            vkDestroyFramebuffer(device, VkUtils2.SwapChainSupportDetails.swapChainFramebuffers[i], VkUtils2.MemSys.pAllocator());
            vkFreeMemory(device, UniformBufferObject.uniformBuffersMemory[i], VkUtils2.MemSys.pAllocator());
            vkDestroyBuffer(device, UniformBufferObject.uniformBuffers[i], VkUtils2.MemSys.pAllocator());

        }
        {

        }




//            vkFreeMemory(Queues.device, PipeLine.stagingBufferMemory, pAllocator);
        vkFreeMemory(device, Buffers.vertexBufferMemory[0], VkUtils2.MemSys.pAllocator());
        vkFreeMemory(device, Buffers.indexBufferMemory[0], VkUtils2.MemSys.pAllocator());
        vkDestroyBuffer(device, Buffers.vertexBuffer[0], VkUtils2.MemSys.pAllocator());
//            vkDestroyBuffer(Queues.device, PipeLine.stagingBuffer, pAllocator);
        vkDestroyBuffer(device, Buffers.indexBuffer[0], VkUtils2.MemSys.pAllocator());
        vkDestroyCommandPool(device, Buffers.commandPool[0], VkUtils2.MemSys.pAllocator());

        vkDestroyPipeline(device, Buffers.graphicsPipeline, VkUtils2.MemSys.pAllocator());

        vkDestroyPipelineLayout(device, Buffers.vkLayout[0], VkUtils2.MemSys.pAllocator());

        vkDestroyRenderPass(device, VkUtils2.SwapChainSupportDetails.renderPass[0], VkUtils2.MemSys.pAllocator());


    }

    private static void doDestroyFreeAlloc(long textureImageView, long vkDestroyImageView)
    {
        System.out.println("Destroying:+ " + textureImageView);
        callPJPV(device.address(), textureImageView, NULL, vkDestroyImageView);
    }

    static final class Renderer2 {
        //        private static final int[] imagesInFlight = new int[MAX_FRAMES_IN_FLIGHT];
        private static final long[] AvailableSemaphore = {0};
        //        private static final long[] vkFenceA;
        //        static final long[] vkFence = {0};
        private static final int MAX_FRAMES_IN_FLIGHT = 3;


        private static final long[] FinishedSemaphore = {0};


        private static final long address2;
        private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
        private static final long TmUt = 1000000000;
        //        private static final VKCapabilitiesDevice capabilities1 = Queues.graphicsQueue.getCapabilities();
//        private static final long vkAcquireNextImageKHR = PipeLine.capabilities.vkAcquireNextImageKHR;
        //                 vkWaitForFences(Queues.device, longs, true, UINT64_MAX);

        // Align address to the specified alignment
//        private static final long address3 = MemSysm.address  + stack2.getPointer() - 4 & -4L;
//        static int a;

//                    VkPresentInfoKHR calloc = VkPresentInfoKHR.calloc(stack.stack())
//                    presentInfoKHR1.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
//                            .pWaitSemaphores(stack.stack().longs(FinishedSemaphore))
//                            .swapchainCount(1)
//                            .pSwapchains(stack.stack().longs(swapChain))
//                    memPutLong(presentInfoKHR1.address() + VkPresentInfoKHR.PIMAGEINDICES, ((address3)));

//        private static final long address = presentQueue.address();
        //                    vkResetFences(Queues.device, longs2);
//                 stack.stack().pop();


//        private static final long address1 = graphicsQueue.address();
        //        private static final long pFences;


        // Align address to the specified alignment


        private static final long VkPresentInfoKHR1 = MemSysm.malloc(VkPresentInfoKHR.SIZEOF);
        public static long frps;
//        static int i;
        //static int FenceStat;
        private static int currentFrame;
        private final static long aa = memGetLong(MemSysm.address)+0x200;


        private static final long value = MemSysm.address + MemSysm.size;

        static {
            {

                //                FinishedSemaphore = PipeLine.doPointerAllocSafe(semaphoreInfo, PipeLine.capabilities.vkCreateSemaphore);

                /* for(int i = 0;i < MAX_FRAMES_IN_FLIGHT;i++)*/
                {
//                    long vkSemaphoreCreateInfo = doAbsCalloc2(VkSemaphoreCreateInfo.SIZEOF,VkSemaphoreCreateInfo.ALIGNOF)-4L;
                    long vkSemaphoreCreateInfo = MemSysm.malloc3(VkSemaphoreCreateInfo.SIZEOF);
                    VkSemaphoreCreateInfo.nsType(vkSemaphoreCreateInfo, VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
                    MemSysm.doPointerAllocSafeExtrm2(vkSemaphoreCreateInfo, device.getCapabilities().vkCreateSemaphore, AvailableSemaphore);
                    MemSysm.doPointerAllocSafeExtrm2(vkSemaphoreCreateInfo, device.getCapabilities().vkCreateSemaphore, FinishedSemaphore);
                    //nmemFree(vkSemaphoreCreateInfo);

//                    long vkFenceCreateInfo = doAbsCalloc(VkFenceCreateInfo.SIZEOF,VkFenceCreateInfo.ALIGNOF);
                    /*long vkFenceCreateInfo = VkUtils2.MemSys.ncalloc(VkFenceCreateInfo.SIZEOF);
                    VkFenceCreateInfo.nsType(vkFenceCreateInfo, VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
                    VkFenceCreateInfo.nflags(vkFenceCreateInfo, VK_FENCE_CREATE_SIGNALED_BIT);
                    MemSysm.doPointerAllocSafeExtrm2(vkFenceCreateInfo, device.getCapabilities().vkCreateFence, vkFence);*/
//                    vkFence[1] = PipeLine.doPointerAllocSafeExtrm(vkFenceCreateInfo, PipeLine.capabilities.vkCreateFence);
//                    vkFence[2] = PipeLine.doPointerAllocSafeExtrm(vkFenceCreateInfo, PipeLine.capabilities.vkCreateFence);
                    //nmemFree(vkFenceCreateInfo);
//                    vkFenceA = PipeLine.doPointerAllocSafeExtrm(vkFenceCreateInfo, PipeLine.capabilities.vkCreateFence);


//

//                    longs2 = stack.stack().longs(vkFence);
//                    pFences = memAddress0(longs2);
                    // Align address to the specified alignment
//                    long address = (stack.stack().getAddress() + stack.stack().getPointer() - 8) & -8L;
                    address2 = MemSysm.malloc3(VkSubmitInfo.SIZEOF);//+ VkSubmitInfo.STYPE;//(ncalloc());
                    VkSubmitInfo.nsType(address2, VK_STRUCTURE_TYPE_SUBMIT_INFO);
                    VkSubmitInfo.nwaitSemaphoreCount(address2, 1);
                    VkSubmitInfo.npNext(address2, NULL);

//                    stack2.setPointer((stack2.getPointer() - 8));

                    memPutLong(address2 + VkSubmitInfo.PWAITSEMAPHORES, VkUtils2.MemSys.stack().pointers((AvailableSemaphore[0])).address0());
                    memPutLong(address2 + VkSubmitInfo.PWAITDSTSTAGEMASK, memAddress((MemSysm.ints(VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT))));
                    memPutLong(address2 + VkSubmitInfo.PSIGNALSEMAPHORES, VkUtils2.MemSys.stack().pointers((FinishedSemaphore[0])).address0());
//                    MemSysm.Memsys2.decFrm(address2);


                    //memSet(VkPresentInfoKHR1, 0, VkPresentInfoKHR.SIZEOF);
                    VkPresentInfoKHR.nsType(VkPresentInfoKHR1, VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore[0]));
                    memPutInt(VkPresentInfoKHR1 + VkPresentInfoKHR.SWAPCHAINCOUNT, 1);
                    memPutLong(address2 + VkSubmitInfo.PCOMMANDBUFFERS, value);
                    VkSubmitInfo.ncommandBufferCount(address2, 1);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PIMAGEINDICES, MemSysm.address);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PSWAPCHAINS, VkUtils2.MemSys.stack().pointers(VkUtils2.SwapChainSupportDetails.swapChain[0]).address0());
                    //nmemFree(VkPresentInfoKHR1);
//                    VkPresentInfoKHR.nsType(VkPresentInfoKHR1, VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
//                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore));
//                    memPutInt(VkPresentInfoKHR1 + VkPresentInfoKHR.SWAPCHAINCOUNT, 1);
                    // Align address to the specified alignment
                    VkSubmitInfo.validate(address2);
                    VkPresentInfoKHR.validate(VkPresentInfoKHR1);
//                    MemSysm.Memsys2.decFrm(VkPresentInfoKHR1); //Not alloctaed witH Contgous-Non_frag Malloc alloctaion, propb shouldn;t be here

                    System.out.println(MemSysm.tracker);
                    System.out.println(MemSysm.sizeof(address2));
                    System.out.println("Descriptor Attachment: "+aa);

                }

            }

        }


    /*private static long ncalloc()
    {
        int bytes = VkSubmitInfo.ALIGNOF * VkSubmitInfo.SIZEOF;
        // Align address to the specified alignment


        long address = (VkUtils2.stack.stack().getAddress() + VkUtils2.stack.stack().getPointer() - VkSubmitInfo.SIZEOF) & ~Integer.toUnsignedLong(VkSubmitInfo.ALIGNOF - 1);
        VkUtils2.stack.stack().setPointer((int) (address - VkUtils2.stack.stack().getAddress()));


        memSet(address, 0, bytes);
        return address;
    }

    private static long doAbsCalloc(int sizeof, int alignof)
    {
        return (VkUtils2.stack.stack().getAddress() + VkUtils2.stack.stack().getPointer() - sizeof) & -alignof;
    }*/

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
//                 i += JNI.callPPI(device.address(), vkFence.length, vkFence, device.getCapabilities().vkResetFences);
//                    i+= vkWaitForFences(device, vkFence, false, TmUt);
                /*if(currentFrame!=frame)
                {
                    return;
                }*/
                nvkAcquireNextImageKHR(device, VkUtils2.SwapChainSupportDetails.swapChain[0], TmUt, AvailableSemaphore[0], VK_NULL_HANDLE, MemSysm.address);


                renderer2.UniformBufferObject.updateUniformBuffer();
//                PipeLine.imagesInFlight.get(PipeLine.currentFrame);
                //Wait frames

//                    imagesInFlight[PipeLine.currentFrame]= (PipeLine.currentFrame);

//                    VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack.stack());
//                    submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
//                            .waitSemaphoreCount(1)
//                    VkSubmitInfo.npWaitSemaphores((AvailableSemaphore));
//                    VkSubmitInfo.npSignalSemaphores((FinishedSemaphore));
//                            .pWaitDstStageMask(stack.stack().ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
//                            .pSignalSemaphores(stack.stack().longs(FinishedSemaphore))
//                            .pCommandBuffers(stack.stack().pointers(PipeLine.commandBuffers[(imageIndex)]));
//                    vkWaitForFences(Queues.device, vkFence, false, TmUt);


                memPutLong(value, Buffers.commandBuffers[currentFrame].address());

                nvkQueueSubmit(graphicsQueue, 1, address2, VK_NULL_HANDLE);

//                i += VK10.vkWaitForFences(device, vkFence, false, TmUt); //Don;t know why a Waiti sneeded here: lil;ey for Vailable/Finished Sempahore as Queue present needs both fences and Semphors to all be Synchronised prior to SHowing teh Farmebuffer/Swa[Chain
//                    FenceStat += vkGetFenceStatus(device, vkFence[0]); //For some reson callinf fence Status seem to help prevebt exeution erros/valdiatiomne rrors the  reserting Siad fences at the begging of the next DrawLoopCall?iteration/CallLoop/Invokation FrameAquisition e.g. e.g.
                memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore[0]));


                nvkQueuePresentKHR(presentQueue, VkPresentInfoKHR1);
//                   i+= vkResetFences(Queues.device, vkFenceA);


                currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
//                 System.out.println(stack.stack().getPointer());
                frps++;

            }
        }

    }


    static final class Buffers {
        public static final VkCommandBuffer[] commandBuffers = new VkCommandBuffer[3];
        static final short[] indices = new short[VkUtils2.PipeLine.indicesTemp.length*64];
        static final int size = Short.BYTES * indices.length;
        static final VkOffset2D set = VkOffset2D.calloc(VkUtils2.MemSys.stack()).set(0, 0);
        static final long[] commandPool ={VK_NULL_HANDLE};
        static final long[] vkLayout ={0};
        private static final long[] vertexBufferMemory = {0};
        static final long[] stagingBuffer = {0};
        static final long[] stagingBufferMemory ={0};
        private static final long[] indexBufferMemory ={0};
        static final long[] vkAllocMemory = {0};
        static final long[] vkImage ={0};
        static final long[] depthImageView ={0};
        private static final LongBuffer offsets = MemSysm.longs(0);
        //        private static final int VERT_SIZE= (OFFSET_POS + OFFSETOF_COLOR + OFFSETOF_TEXTCOORDS) / Float.BYTES;
        static final int VERTICESSTRIDE = 32;//vertices.length*6/indices.length;
        static final VKCapabilitiesDevice capabilities;
        //        private static long stagingBuffer;
        static final long[] vertexBuffer ={0};
        static final long[] indexBuffer ={0};
        private static final int U = 0;
        private static final int V = 0;
        static final float[] verticesTemp = { //TODO: Auto connetc when adding diitonal evrticies: e.g. if now Block.Cube added, rese preisiting vertocoes to avoid rudplciate/reudicnat faces/vetrcoe sbeign drawnbthat are not vosoable/occluded due to adjency, may be psob;e top use a Indicieis buffer to do thins inherently without the need to manually/systamtcially ascerain verticioe sduplciates manually /methodcially e.g.etc ie.e., preme;tively

                0, 0, 0,  1, 0, 0,    U, V,
                1, 1, 0,   0, 1, 0,    U+1, V,
                1, 1, 0,    0, 0, 1,    U+1, V+1,
                0, 1, 0,   1, 1, 1,    U, V+1,

                0, 0, 1,  1, 0, 0,    U, V,
                1, 0, 1,   0, 1, 0,    U+1, V,
                1, 1, 1,    0, 0, 1,    U+1, V+1,
                0, 1, 1,   1, 1, 1,    U, V+1,

                0, 0, 0,  1, 0, 0,    U, V,
                1, 0, 0,   0, 1, 0,    U+1, V,
                1, 0, 1,    0, 0, 1,    U+1, V+1,
                0, 0, 1,   1, 1, 1,    U, V+1,

                0, 1, 1,  1, 0, 0,    U, V,
                1, 1, 1,   0, 1, 0,    U+1, V,
                1, 1, 0,    0, 0, 1,    U+1, V+1,
                0, 1, 0,   1, 1, 1,    U, V+1,

                0, 0, 1,  1, 0, 0,    U, V,
                0, 1, 1,   0, 1, 0,    U+1, V,
                0, 1, 0,    0, 0, 1,    U+1, V+1,
                0, 0, 0,   1, 1, 1,    U, V+1,

                1, 0, 0,  1, 0, 0,    U, V,
                1, 1, 0,   0, 1, 0,    U+1, V,
                1, 1, 1,    0, 0, 1,    U+1, V+1,
                1, 0, 1,   1, 1, 1,    U, V+1,


        };
        static int dest= -verticesTemp.length;
        static final float[] vertices = new float[(verticesTemp.length)*64];
        static final int value = /*SIZEOF **/ vertices.length* Float.BYTES;

        private static final int VERT_SIZE= vertices.length/ 8;


        static long graphicsPipeline;
        private static VkCommandBufferBeginInfo beginInfo1;
        private static VkRenderPassBeginInfo renderPassInfo;
        private static final VkSubmitInfo submitInfo1 = VkSubmitInfo.create(MemSysm.calloc(VkSubmitInfo.SIZEOF))//VkUtils2.MemSysm.nmalloc(VkSubmitInfo.ALIGNOF, VkSubmitInfo.SIZEOF);
                .sType$Default();

        static
        {
            capabilities = device.getCapabilities();

            doBufferAlloc();


            for (int i =-2; i< 2; i++)
                for (int ii = -2; ii < 2; ii++)
                    for (int iii = -2; iii < 2; iii++)
                        doBufferAlloc2(i, ii, iii);
            //setup/doDepupeface/FaceCukking/
            //Still Likley better to reply in indicies buffer to inherent deuplcitaion/culling aslingas/if/based upon /which as  aconsequence of deplictaing same
            //may also be posibel to use offsets to negate x,y,andz parametsr for eahc rpesove block/veretx, alsmot a bit like  Identity Vertex reference, avoding tey need to check all vericies in the Buffer manually...

            //Dierctional face/Normal rletaion.imferemmt.orientation, used diesgnatiosnbabsed on the position of the new.propro.Old bLock to be inserted relative to adje ces fances, if palcong forward and can aplcoyatiosn rlativ eot curetnt blcok psoiton can togoleprpetivly falgs to outomaticlaly disocrd face.cull tiles. ters.Abs.DCOmpsotive.Ineteeracsosiint IQuead withotu teh need to carry out evalauations comapritive.euqalty.eqianelt operTIon .prooeebts.oidedundbf ,anuallye .g. etc i.e. .etc .Msic. ejegd

            //Do VBO/Poriton.chunk offset relatine to current.relative position instea dof need ing to verificy.ascertain Pos.Coords dierct


            setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, value, vertexBuffer, VK_SHARING_MODE_EXCLUSIVE);
            createBuffer(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, vertexBuffer, vertexBufferMemory);
            setBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, value, stagingBuffer, VK_SHARING_MODE_EXCLUSIVE);
            createBuffer(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
                    VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer, stagingBufferMemory);
            setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT, Short.BYTES * indices.length, indexBuffer, VK_SHARING_MODE_EXCLUSIVE);
            createBuffer(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, indexBuffer, indexBufferMemory);

        }

        static void createCommandBuffers() {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.create(MemSysm.malloc3(VkCommandBufferAllocateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(getRef())
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(VkUtils2.SwapChainSupportDetails.swapChainFramebuffers.length);

//            PointerBuffer pCommandBuffers = stack.stack().mallocPointer(SwapChainSupportDetails.swapChainFramebuffers.length);

            //nmemFree(allocateInfo);
            long[] descriptorSets = new long[allocateInfo.commandBufferCount()];
//            MemSysm.Memsys2.free(allocateInfo);
            doPointerAllocS(allocateInfo, capabilities.vkAllocateCommandBuffers, descriptorSets);
            commandBuffers[0]=new VkCommandBuffer(descriptorSets[0], device);
            commandBuffers[1]=new VkCommandBuffer(descriptorSets[1], device);
            commandBuffers[2]=new VkCommandBuffer(descriptorSets[2], device);


            beginInfo1 = VkCommandBufferBeginInfo.create(MemSysm.malloc3(VkCommandBufferBeginInfo.SIZEOF)).sType$Default()
                    .flags(VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT);

//            VkRenderPassAttachmentBeginInfo vkRenderPassAttachmentBeginInfo1 = VkRenderPassAttachmentBeginInfo.create(MemSysm.malloc(VkRenderPassAttachmentBeginInfo .SIZEOF))
//                    .sType$Default();
            //.pAttachments(VkUtils2.MemSys.stack().longs(VkUtils2.SwapChainSupportDetails.swapChainImageViews));
//            memPutAddress(vkRenderPassAttachmentBeginInfo1.address() + VkRenderPassAttachmentBeginInfo.PATTACHMENTS,  VkUtils2.SwapChainSupportDetails.swapChainImageViews[0]); //TODO: Attempt to Imp;eme nt imageless FrameBuffers
//            memPutAddress(vkRenderPassAttachmentBeginInfo1.address() + VkRenderPassAttachmentBeginInfo.PATTACHMENTS,  VkUtils2.SwapChainSupportDetails.swapChainImageViews[1]); //TODO: Attempt to Imp;eme nt imageless FrameBuffers
//            VkRenderPassAttachmentBeginInfo.nattachmentCount(vkRenderPassAttachmentBeginInfo1.address(), 2);
            VkRect2D renderArea = VkRect2D.create(MemSysm.malloc3(VkRect2D.SIZEOF))
                    .offset(set)
                    .extent(VkUtils2.SwapChainSupportDetails.swapChainExtent);

            //todo: multiple attachments with VK_ATTACHMENT_LOAD_OP_CLEAR: https://vulkan-tutorial.com/en/Depth_buffering#page_Clear-values

            VkClearValue.Buffer clearValues = VkClearValue.create(MemSysm.malloc3(VkClearValue.SIZEOF*2L), 2);
            clearValues.get(0).color().float32(VkUtils2.MemSys.stack().floats(0.0f, 0.0f, 0.0f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);


            renderPassInfo = VkRenderPassBeginInfo.create(MemSysm.malloc3(VkRenderPassBeginInfo.SIZEOF)).sType$Default()
//                    .pNext(vkRenderPassAttachmentBeginInfo1)
                    .pClearValues(clearValues)
                    .renderPass(VkUtils2.SwapChainSupportDetails.renderPass[0])
                    .renderArea(renderArea);



//            memPutLong(renderPassInfo.address() + VkRenderPassBeginInfo.PCLEARVALUES, clearValues.address0());
//            VkRenderPassBeginInfo.nclearValueCount(renderPassInfo.address(), clearValues.remaining());

            //todo: Mmeory addres sranegs for Uniform bufefrs are opened here propr to the vkCMD Command Buffer records in an attempt to potenetial. reuce overal
            /*todo: Performance Hack: allow Memepry to be premptively mapped prior to FUll Initialisation/Drawing/rendering/Setup/prior to VKCOmmand Buffer recording to allow to avoid overhead/adiitonal when attem0ng to modifey vertex.Buffer data when itilsied and Mapped to a specific memry range(s),
             * This has the darwback of requiring specific alignment when when Obtaining.Plaicng.reading/Writing references.memory Adresses, which at leas in the case of Command Buffers.Uniform buffers seems to be exactly 512, which may potnetial decrease stabilility due to lack of potentoa,s afty wth prevent.prpettcing. mana ging agaonst/with/for Acess Viplations/Segfaults e.g.

             */
//            nvkMapMemory(device, UniformBufferObject.uniformBuffersMemory[0], 0, UniformBufferObject.capacity, 0, MemSysm.address);
            nvkMapMemory(device, UniformBufferObject.uniformBuffersMemory[1], 0, UniformBufferObject.capacity, 0, MemSysm.address);

            /*VkPushConstantRange vkPushConstantRange = VkPushConstantRange.calloc(VkUtils2.MemSysm.stack())
                    .offset(0)
                    .size(UniformBufferObject.proj2.length*Float.BYTES);*/

            for (int i = 0; i < commandBuffers.length; i++) {
                extracted(i);

            }

        }

        private static void extracted(int i)
        {
            VkCommandBuffer commandBuffer = commandBuffers[i];
            vkBeginCommandBuffer(commandBuffer, beginInfo1);


            renderPassInfo.framebuffer(VkUtils2.SwapChainSupportDetails.swapChainFramebuffers[i]);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
//                    nvkCmdBindVertexBuffers(commandBuffer, 0, 1, (put), memAddress0(offsets));
                vkCmdBindVertexBuffers(commandBuffer, 0, MemSysm.longs(vertexBuffer), offsets);
                vkCmdBindIndexBuffer(commandBuffer, indexBuffer[0], 0, VK_INDEX_TYPE_UINT16);
                /*double angle = (glfwGetTime()* UniformBufferObject.aFloat);

                //            model.identity();
//            System.arraycopy(proj3, 0, proj2, 0, proj2.length);

            UniformBufferObject.mulAffineL(
                    (float) Math.sin(angle),
                    (float) Math.cos(angle+ UniformBufferObject.Half_Pi)
            );//vkPushConstant
            //vkPushConstant vkPushConstant=new vkPushConstant(MemSysm.getHandle(i), UniformBufferObject.proj2);

            vkCmdPushConstants(commandBuffer, vkLayout[0], VK_SHADER_STAGE_VERTEX_BIT, 0, (UniformBufferObject.proj2));*/
                UniformBufferObject.mvp.getToAddress(memGetLong(MemSysm.address)+0x200);
                long pointerBuffer = VkUtils2.MemSys.stack().pointers((UniformBufferObject.descriptorSets[0])).address0();

                // Convert to long to support addressing up to 2^31-1 elements, regardless of sizeof(element).
                // The unsigned conversion helps the JIT produce code that is as fast as if int was returned.
                nvkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, vkLayout[0], 0, 1, (pointerBuffer), 0, 0);
                vkCmdDrawIndexed(commandBuffer, indices.length, 1, 0, 0, 0);
                MemSysm.Memsys2.free8(pointerBuffer);


            }
            vkCmdEndRenderPass(commandBuffer);

            System.out.println("Using: " + VERTICESSTRIDE + " Vertices");
            System.out.println("Using: " + VERT_SIZE + " Vert SIze/ Tris");
            vkEndCommandBuffer(commandBuffer);
        }

        private static long getRef()
        {
            return commandPool[0];
        }

        public static void doPointerAllocS(Pointer.Default allocateInfo, long vkAllocateDescriptorSets, long[] descriptorSets) {

            callPPPI(device.address(), allocateInfo.address(), descriptorSets, vkAllocateDescriptorSets);
            MemSysm.Memsys2.free(allocateInfo);
//            return address;

        }

        //todo: need to do thsi seperately from createBuffer as Java cannot handle references/pointrs by default and the buffre must be a field due to the bindVertexBuffers call needing acces to teh same VertexBuffer that is cerated, and a methdo/upu cannto return multiple variables/refercnes/valeus at teh same time
        static void setBuffer(int usage, int size, long[] a, int sharingMode) {
            VkBufferCreateInfo allocateInfo = VkBufferCreateInfo.create(MemSysm.malloc(VkBufferCreateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(usage)
                    .sharingMode(sharingMode);

            MemSysm.Memsys2.doPointerAllocSafe2(allocateInfo, capabilities.vkCreateBuffer, a);
            //nmemFree(allocateInfo);
        }

        static long setBuffer(int usage, int size) {
            //long allocateInfo = VkUtils2.MemSysm.malloc(VkBufferCreateInfo.SIZEOF);
            VkBufferCreateInfo allocateInfo = VkBufferCreateInfo.create(MemSysm.calloc(VkBufferCreateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(usage)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            //nmemFree(allocateInfo);
            MemSysm.Memsys2.free(allocateInfo);
            return MemSysm.doPointerAllocSafe(allocateInfo, capabilities.vkCreateBuffer);
        }

        static void createBuffer(int properties, long[] currentBuffer, long[] vertexBufferMemory) {

            //long vkMemoryRequirements = VkUtils2.MemSysm.malloc(VkMemoryRequirements.SIZEOF);

            nvkGetBufferMemoryRequirements(device, currentBuffer[0], MemSysm.address);


            VkMemoryAllocateInfo allocateInfo1 = VkMemoryAllocateInfo.create(MemSysm.calloc(VkMemoryAllocateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(VkMemoryRequirements.nsize(MemSysm.address))
                    .memoryTypeIndex(findMemoryType(VkMemoryRequirements.nmemoryTypeBits(MemSysm.address), properties));
//            nmemFree(vkMemoryRequirements);
            MemSysm.Memsys2.doPointerAllocSafe2(allocateInfo1, capabilities.vkAllocateMemory, vertexBufferMemory);
//            nmemFree(allocateInfo1);

            vkBindBufferMemory(device, currentBuffer[0], vertexBufferMemory[0], 0);

        }

        static int findMemoryType(int typeFilter, int properties) {
            VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.create(MemSysm.malloc(VkPhysicalDeviceMemoryProperties.SIZEOF));
            vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProperties);
            for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
                if ((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                    MemSysm.Memsys2.free(memProperties);
                    return i;
                }
            }

            throw new RuntimeException("Failed to find suitable memory type");
        }

        static long createBuffer(long currentBuffer) {

            nvkGetBufferMemoryRequirements(device, currentBuffer, MemSysm.address);

            long[] vertexBufferMemory={0};
            VkMemoryAllocateInfo allocateInfo1 = VkMemoryAllocateInfo.create(MemSysm.malloc0())
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(VkMemoryRequirements.nsize(MemSysm.address))
                    .memoryTypeIndex(findMemoryType(VkMemoryRequirements.nmemoryTypeBits(MemSysm.address), VK_MEMORY_PROPERTY_HOST_CACHED_BIT));
            MemSysm.Memsys2.doPointerAllocSafe2(allocateInfo1, capabilities.vkAllocateMemory, vertexBufferMemory);

            vkBindBufferMemory(device, currentBuffer, vertexBufferMemory[0], 0);

            //        private static final long[] vertexBufferMemory=new long [1];

            return vertexBufferMemory[0];

        }

        private static void doBufferAlloc2(float x, float y, float z)
        {
            System.arraycopy(verticesTemp, 0, vertices, dest += verticesTemp.length, verticesTemp.length);
            for(int v = dest; v< dest + verticesTemp.length; v+=8)
            {
                vertices[v]+=x;
                vertices[v+1]+=y;
                vertices[v+2]+=z;
            }
        }

        private static void doBufferAlloc()
        {
            int a=2;
            int r= verticesTemp.length/8;
            System.arraycopy(VkUtils2.PipeLine.indicesTemp, 0, indices, 0, VkUtils2.PipeLine.indicesTemp.length);
            for (int w = 36; w< indices.length; w+=36) {
                {
                    System.arraycopy(VkUtils2.PipeLine.indicesTemp, 0, indices, w, VkUtils2.PipeLine.indicesTemp.length);
                    for(int i = w; i< VkUtils2.PipeLine.indicesTemp.length*a; i++)
                    {
                        indices[i]+=r;
                    }
                    r+= verticesTemp.length/8;

                    a++;
                }
            }


        }

        static void copyBuffer(long[] dstBuffer, int size) {

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();

            VkBufferCopy.nsrcOffset(MemSysm.address, 0);
            VkBufferCopy.ndstOffset(MemSysm.address, 0);
            VkBufferCopy.nsize(MemSysm.address, size);
//            nmemFree(vkBufferCopy);
            nvkCmdCopyBuffer(commandBuffer, stagingBuffer[0], dstBuffer[0], 1, MemSysm.address);
            endSingleTimeCommands(commandBuffer);

        }

        static @NotNull VkCommandBuffer beginSingleTimeCommands() {

            final long allocateInfo = MemSysm.calloc(VkCommandBufferAllocateInfo.SIZEOF);
            VkCommandBufferAllocateInfo.nsType(allocateInfo, VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            VkCommandBufferAllocateInfo.nlevel(allocateInfo, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            VkCommandBufferAllocateInfo.ncommandPool(allocateInfo, commandPool[0]);
            VkCommandBufferAllocateInfo.ncommandBufferCount(allocateInfo, 1);

            VkCommandBuffer commandBuffer = MemSysm.Memsys2.doPointerAllocAlt(allocateInfo, capabilities.vkAllocateCommandBuffers);
            long vkCommandBufferBeginInfo = MemSysm.calloc(VkCommandBufferBeginInfo.SIZEOF);
            VkCommandBufferBeginInfo.nsType(vkCommandBufferBeginInfo, VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            VkCommandBufferBeginInfo.nflags(vkCommandBufferBeginInfo, VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            nvkBeginCommandBuffer(commandBuffer, vkCommandBufferBeginInfo);
            return commandBuffer;
        }

        static void endSingleTimeCommands(VkCommandBuffer commandBuffer) {
            vkEndCommandBuffer(commandBuffer);

            final long pointers = memAddress0(VkUtils2.MemSys.stack().longs(commandBuffer.address()));
            memPutAddress(submitInfo1.address() + VkSubmitInfo.PCOMMANDBUFFERS, pointers);
            VkSubmitInfo.ncommandBufferCount(submitInfo1.address(), 1);

//            VkSubmitInfo.ncommandBufferCount(submitInfo1, 1);

            nvkQueueSubmit(presentQueue, 1, submitInfo1.address(), VK_NULL_HANDLE);
//            callPI(presentQueue.address(), presentQueue.getCapabilities().vkQueueWaitIdle);
            MemSysm.Memsys2.free8(pointers);
            nvkFreeCommandBuffers(device, commandPool[0], 1, pointers);
        }

        static void createDescriptorSetLayout() {
            {
                VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.create(MemSysm.malloc3(VkDescriptorSetLayoutBinding.SIZEOF*2L), 2);
               /*  long[] bindings = {
                         stack.stack().ncalloc(VkDescriptorSetLayoutBinding.ALIGNOF, 1, VkDescriptorSetLayoutBinding.SIZEOF),
                         stack.stack().ncalloc(VkDescriptorSetLayoutBinding.ALIGNOF, 1, VkDescriptorSetLayoutBinding.SIZEOF)
                 };

                 //uboLayoutBinding
                 VkDescriptorSetLayoutBinding.nbinding(bindings[0], 0);
                 VkDescriptorSetLayoutBinding.ndescriptorCount(bindings[0], 1);
                 VkDescriptorSetLayoutBinding.ndescriptorType(bindings[0], VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                 //                         .pImmutableSamplers(null)
                 VkDescriptorSetLayoutBinding.nstageFlags(bindings[0], VK_SHADER_STAGE_VERTEX_BIT);
                  //samplerLayoutBinding
                 VkDescriptorSetLayoutBinding.nbinding(bindings[1], 1);
                 VkDescriptorSetLayoutBinding.ndescriptorCount(bindings[1], 1);
                 VkDescriptorSetLayoutBinding.ndescriptorType(bindings[1], VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                 //                         .pImmutableSamplers(null)
                 VkDescriptorSetLayoutBinding.nstageFlags(bindings[1], VK_SHADER_STAGE_FRAGMENT_BIT);*/

                bindings.get(0)
                        .binding(0)
                        .descriptorCount(1)
                        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
                //samplerLayoutBinding
                bindings.get(1)
                        .binding(1)
                        .descriptorCount(1)
                        .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                        //                         .pImmutableSamplers(null)
                        .stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);


                VkDescriptorSetLayoutCreateInfo a = VkDescriptorSetLayoutCreateInfo.create(MemSysm.malloc2(VkDescriptorSetLayoutCreateInfo.SIZEOF))
                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                        .pBindings(bindings);
                //                 memPutAddress(a + VkDescriptorSetLayoutCreateInfo.PBINDINGS, bindings[1]);
//                 VkDescriptorSetLayoutCreateInfo.nbindingCount(a.address(), bindings.remaining());
//                 Memsys2.free(bindings);
                //VkUtils2.MemSysm.free(a);
//                 nmemFree(a);
//                 memFree(bindings);
                MemSysm.Memsys2.doPointerAllocSafe2(a, device.getCapabilities().vkCreateDescriptorSetLayout, UniformBufferObject.descriptorSetLayout);
            }
        }

        static void createVertexBufferStaging() {


            //            nvkMapMemory(Queues.device, stagingBufferMemory, 0, value, 0, address);

            //            VkPushConstantRange.

            nvkMapMemory(device, stagingBufferMemory[0], 0, value, 0, MemSysm.address);
            {
                GLU2.theGLU.memcpy2(vertices, MemSysm.getHandle(), (vertices.length << 2));
                //                GLU2.theGLU.wrap()

            }
            vkUnmapMemory(device, stagingBufferMemory[0]);

            //            vertexBuffers = getSetBuff  er(PipeLine.currentBuffer);

            copyBuffer(vertexBuffer, value);


        }

        static void createIndexBuffer() {

            nvkMapMemory(device, stagingBufferMemory[0], 0, size, 0, MemSysm.address);
            {
//                memFloatBuffer(data.get(0), vertices.length).put(vertices);
                /*todo: might e posible to only undata.remap a specic aaspect of the mapped buffer memory if only a specici vbo, vertex is being updated instead of remappign teh entre buffer every/Single time
                 * e.g. only need to remap/modify.adjust a specific cube.block.vertex so will only rrmap the meory potion equal to the veretx offset+the eisze of the modified setcion/vertex of the buffer
                 * e..g so if mroe veretx ned to be mrodified/wirtten/removed the remapped manger range betwen teh offset anf the termatteitr amy need to be garter
                 * e.g. if Block 3 need sto be modifed, only remap the memeory equal to the offset(BlockVertex size*Block instance/ID.orderof buffer isnertion.precedense. and the blcok veretx size itself)
                 * e.g. if 3rc blcok ned to be remapedd, use offset 192*Blockprecedenceorder/id/ofsfet-1 and size 192 to onlys epcevely remap onlu the Block and not the Entrry of the Buffer
                 */

//                memShortBuffer(getHandle(), SIZEOFIn).put(indices);
                GLU2.theGLU.memcpy2(indices, MemSysm.getHandle(), indices.length << 2L);
//                GLU2.theGLU.wrap()

            }
            vkUnmapMemory(device, stagingBufferMemory[0]);

            copyBuffer(indexBuffer, size);

        }

        static void creanupBufferStaging() {
            vkDestroyBuffer(device, stagingBuffer[0], VkUtils2.MemSys.pAllocator());
            vkFreeMemory(device, stagingBufferMemory[0], VkUtils2.MemSys.pAllocator());
        }
    }

    static final class UniformBufferObject
    {

        //        private static final Matrix4f model = new Matrix4f().identity().determineProperties();
        //private static final Matrix4f proj = new Matrix4f().identity().determineProperties();
        private static final Matrix4f mvp;// = new Matrix4f().identity();
//        private static Matrix4f pers= new Matrix4f();
//        private static final Matrix4f proj4= new Matrix4f();
//        private static final float[] proj2=new float[16];
        static final long[] descriptorSetLayout = {0};
        //        private static LongBuffer pDescriptorSetLayout;
        private static final long[] uniformBuffers = new long[(VkUtils2.SwapChainSupportDetails.swapChainImages.length)];
        private static final long[] uniformBuffersMemory = new long[(VkUtils2.SwapChainSupportDetails.swapChainImages.length)];
        static final int capacity = 16 * Float.BYTES;
        private static final long[] descriptorPool ={0};
        static final long[] descriptorSets = new long[VkUtils2.SwapChainSupportDetails.swapChainImages.length];
//        private static final float h = (Math.tan(Math.toRadians(45) * 0.5f));
//        private static final double Half_Pi = java.lang.Math.atan(1) * 4D / 180D;

//        private static final float[] proj3= new float[16];
        static final long[] textureImageView = {0};
        static final long[] textureSampler = {0};

        private static final float zFar = 120.0f;

        private static final float zNear = 3.1f;
//        private static final int i=0;

        private static final Matrix4f trans = new Matrix4f().identity();

//        private static final Matrix4f view = new Matrix4f().determineProperties().setLookAt(2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);

        static {
            float h = (Math.tan(Math.toRadians(45) * 0.5f));
            mvp = new Matrix4f().determineProperties().m00(1.0f / (h * (VkUtils2.SwapChainSupportDetails.swapChainExtent.width() / (float) VkUtils2.SwapChainSupportDetails.swapChainExtent.height())))
                    .m11((1.0f / h) * -1)
                    .m22((zFar + zNear) / (zNear - zFar))
                    .m32((zFar + zFar) * zNear / (zNear - zFar))
                    .m23(-1.0f);
            mvp.mulPerspectiveAffine(new Matrix4f().determineProperties().setLookAt(2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f));
            mvp.translate(new Vector3f(-4, -4, -4));
//            proj.get(proj2);

//                proj2.put(proj3).re();

//            System.arraycopy(proj2, 0, proj3, 0, 16);

        }
        //todo: Important: atcuall have to 'reset; or revert teh matrixces bakc to baseline.Idneitfy in order to atcually update te Unform Buffer(s) properly e.g. .etc i.e.
//            UniformBufferObject.model.identity();
//            UniformBufferObject.view.identity();
//            UniformBufferObject.proj.identity();


        private static final double aFloat = Math.toRadians(9D);

        public static void createUniformBuffers()
        {
            for (int i = 0; i < VkUtils2.SwapChainSupportDetails.swapChainImages.length; i++) {

                uniformBuffers[i]=(((Buffers.setBuffer(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, capacity))));
                uniformBuffersMemory[i]= (Buffers.createBuffer(uniformBuffers[i]));
            }
        }

        static void updateUniformBuffer()
        {

            /*if(pImageIndex%2==0)
            {
                return;
            }*/

            double angle = (glfwGetTime()*aFloat);

            //            model.identity();
//            System.arraycopy(proj3, 0, proj2, 0, proj2.length);
            /*mulAffineL(
                    (float) Math.sin(angle),
                    (float) Math.cos(angle+Half_Pi)
            );*/

//            proj4.set(proj);
//            pers = new Matrix4f().setPerspective(h*-1,(VkUtils2.SwapChainSupportDetails.swapChainExtent.width() / (float) VkUtils2.SwapChainSupportDetails.swapChainExtent.height()),
//                    zFar, zNear);
//            mvp.mul(pers);
//            mvp.mulPerspectiveAffine(view);
//            trans.setTranslation(new Vector3f(-4, -4, -4));//.rotate((float) angle, 0, 0, 1, mvp);
//            mvp.mul(trans).getToAddress(Renderer2.aa);
//            trans.rotate((float) angle, 0, 0, 1, proj4);

//            proj4.determineProperties();
            mvp.rotate((float) angle, 0, 0, 1, trans).getToAddress(Renderer2.aa);
//                proj2.put(proj3).re();


        }

       /* private static void memcpy4()
        {
            for (int i=0;i<16;i++)
            GLU2.theUnSafe.UNSAFE.putFloat(null, Renderer2.aa+ (i << 2), proj2[i]);

        }*/

        /*private static void mulAffineL(
                float r01,
                float r11) {
            final float v = proj3[2]*r11;
            final float va = proj3[2]* -r01;
            final float a = proj3[7];
            final float a1 = proj3[0];
            proj2[0] = Math.fma(a1, r11, r01);
            proj2[1] = Math.fma(-a1, r11, r01);
            proj2[2] = v;
            proj2[3] = Math.fma(a, r01, v);
            proj2[4] = Math.fma(a1, -r01, r11);
            proj2[5] = Math.fma(-a1, -r01, r11);
            proj2[6] = va;
            proj2[7] = Math.fma(a, r11, va);
            proj2[8] = 0;
            proj2[12] = 0;
            proj2[13] = 0;

        }*/

        /*private static void memcpy(long handle)
        {
            proj4.getToAddress(handle);
//            memFloatBuffer(handle, proj2.length).put(proj2);
//            GLU2.theGLU.memcpy2(UniformBufferObject.proj2, handle, UniformBufferObject.proj2.length <<2);
        }*/

        static void createDescriptorPool() {
            {
                VkDescriptorPoolSize.Buffer poolSize = VkDescriptorPoolSize.create(MemSysm.calloc(2,VkDescriptorPoolSize.SIZEOF), 2);
//                        .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
//                        .descriptorCount(PipeLine.swapChainImages.length);
                VkDescriptorPoolSize uniformBufferPoolSize = poolSize.get(0)
                        .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(VkUtils2.SwapChainSupportDetails.swapChainImages.length);

                VkDescriptorPoolSize textureSamplerPoolSize = poolSize.get(1)
                        .type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                        .descriptorCount(VkUtils2.SwapChainSupportDetails.swapChainImages.length);
                VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.create(MemSysm.calloc(VkDescriptorPoolCreateInfo.SIZEOF)).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                        .pPoolSizes(poolSize)
                        .maxSets(VkUtils2.PipeLine.swapChainImages.length);
                MemSysm.Memsys2.doPointerAllocSafe2(poolCreateInfo, device.getCapabilities().vkCreateDescriptorPool, descriptorPool);
//               descriptorPool=aLong[0];
            }
        }

        static void createDescriptorSets() {
            {
                LongBuffer layouts = memLongBuffer(MemSysm.address, VkUtils2.SwapChainSupportDetails.swapChainImages.length);
                for(int i = 0;i < layouts.capacity();i++) {
                    layouts.put(i, descriptorSetLayout);
                }

                VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.create(MemSysm.calloc(VkDescriptorSetAllocateInfo.SIZEOF)).sType$Default();
//                allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
                allocInfo.descriptorPool(descriptorPool[0])
                        .pSetLayouts(layouts);

//                long[] pDescriptorSets = new long[(SwapChainSupportDetails.swapChainImages.length)];
//                nmemFree(allocInfo.address());
                vkAllocateDescriptorSets(device, allocInfo, descriptorSets);

                VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.create(MemSysm.malloc3(VkDescriptorBufferInfo.SIZEOF),1)
                        .offset(0)
                        .range(capacity);

                VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.create(MemSysm.malloc3(VkDescriptorImageInfo.SIZEOF),1)
                        .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                        .imageView(textureImageView[0])
                        .sampler(textureSampler[0]);

                VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.create(MemSysm.malloc3(VkWriteDescriptorSet.SIZEOF*2L),2);

                VkWriteDescriptorSet vkWriteDescriptorSet = descriptorWrites.get(0).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstBinding(0)
                        .dstArrayElement(0)
                        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(1)
                        .pBufferInfo(bufferInfo);

                VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(1).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstBinding(1)
                        .dstArrayElement(0)
                        .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                        .descriptorCount(1)
                        .pImageInfo(imageInfo);


                //nmemFree(bufferInfo);

                for(int i = 0;i < 1;i++) {


                    bufferInfo.buffer(uniformBuffers[i]);

                    vkWriteDescriptorSet.dstSet(descriptorSets[i]);
                    samplerDescriptorWrite.dstSet(descriptorSets[i]);

                    vkUpdateDescriptorSets(device, descriptorWrites , null);

//                    descriptorSets[i]=descriptorSet;

                }
                MemSysm.Memsys2.free(bufferInfo);
                MemSysm.Memsys2.free(descriptorWrites);
//                System.arraycopy(pDescriptorSets, 0, descriptorSets, 0, pDescriptorSets.length);
            }
        }


    }
}