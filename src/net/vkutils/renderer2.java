package vkutils;

import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.*;
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

        vkDestroyDevice(device, MemSysm.pAllocator);
        KHRSurface.vkDestroySurfaceKHR(VkUtils2.vkInstance, surface[0], MemSysm.pAllocator);
        vkDestroyInstance(VkUtils2.vkInstance, MemSysm.pAllocator);
        /*glfwDestroyWindow(window);*/


//            vkDestroyInstance(vkInstance, pAllocator);


        glfwDestroyWindow(VkUtils2.window);

        glfwTerminate();
    }

    private static void _mainDeletionQueue()
    {
        doDestroyFreeAlloc((Buffers.vkImage), Buffers.capabilities.vkDestroyImage);
        doDestroyFreeAlloc((Buffers.vkAllocMemory), Buffers.capabilities.vkFreeMemory);
//        doDestroyFreeAlloc(UniformBufferObject.textureSampler, Buffers.capabilities.vkDestroySampler);
        doDestroyFreeAlloc((UniformBufferObject.textureImageView), Buffers.capabilities.vkDestroyImageView);

        {
            vkDestroySemaphore(device, Renderer2.AvailableSemaphore, MemSysm.pAllocator);
        }
        vkDestroyDescriptorSetLayout(device, (UniformBufferObject.descriptorSetLayout), MemSysm.pAllocator);
        vkDestroyDescriptorPool(device, (UniformBufferObject.descriptorPool), MemSysm.pAllocator);

        //vkDestroyCommandPool(device, memGetLong(Buffers.commandPool), MemSysm.pAllocator);
        for (int i = 0; i < 3; i++)
        {
            vkDestroySwapchainKHR(device, VkUtils2.SwapChainSupportDetails.swapChainImages[i], MemSysm.pAllocator);
            vkDestroyImageView(device, VkUtils2.SwapChainSupportDetails.swapChainImageViews.get(i), MemSysm.pAllocator);
//            vkFreeCommandBuffers(device, Buffers.commandPool, Buffers.commandBuffers[i]);
            vkDestroyFramebuffer(device, VkUtils2.SwapChainSupportDetails.swapChainFramebuffers[i], MemSysm.pAllocator);
//             vkFreeMemory(device, UniformBufferObject.uniformBuffersMemory.get(i), MemSysm.pAllocator);
            vkDestroyBuffer(device, UniformBufferObject.uniformBuffers.get(i), MemSysm.pAllocator);

        }
        {

        }




//            vkFreeMemory(Queues.device, PipeLine.stagingBufferMemory, pAllocator);
        vkFreeMemory(device, (Buffers.vertexBufferMemory), MemSysm.pAllocator);
        vkFreeMemory(device, (Buffers.indexBufferMemory), MemSysm.pAllocator);
        vkDestroyBuffer(device, Buffers.vertexBuffer[0], MemSysm.pAllocator);
//            vkDestroyBuffer(Queues.device, PipeLine.stagingBuffer, pAllocator);
        vkDestroyBuffer(device, Buffers.indexBuffer[0], MemSysm.pAllocator);
        vkDestroyCommandPool(device, Buffers.commandPool, MemSysm.pAllocator);

        vkDestroyPipeline(device, Buffers.graphicsPipeline, MemSysm.pAllocator);

        vkDestroyPipelineLayout(device, memGetLong(Buffers.vkLayout), MemSysm.pAllocator);

        vkDestroyRenderPass(device, memGetLong(VkUtils2.SwapChainSupportDetails.renderPass), MemSysm.pAllocator);


    }

    private static void doDestroyFreeAlloc(long textureImageView, long vkDestroyImageView)
    {
        System.out.println("Destroying:+ " + textureImageView);
        callPJPV(device.address(), memGetLong(textureImageView), NULL, vkDestroyImageView);
    }

    static final class Renderer2 {
        private static final long AvailableSemaphore;
        private static final int MAX_FRAMES_IN_FLIGHT = 3;


        private static final long FinishedSemaphore;


        private static final long address2;
        private static final long TmUt = 1000000000;

        private static final long VkPresentInfoKHR1;
        private static final long swapchain = memGetLong(VkUtils2.SwapChainSupportDetails.swapChain);
        public static long frps;
        public static final long aa = memGetLong(MemSysm.address)+0x200;
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
                    AvailableSemaphore= MemSysm.doPointerAllocSafe3(vkSemaphoreCreateInfo, device.getCapabilities().vkCreateSemaphore);
                    FinishedSemaphore= MemSysm.doPointerAllocSafe3(vkSemaphoreCreateInfo, device.getCapabilities().vkCreateSemaphore);


                    VkSubmitInfo info = VkSubmitInfo.create(MemSysm.malloc3(VkSubmitInfo.SIZEOF))
                    		.set(
                    		VK_STRUCTURE_TYPE_SUBMIT_INFO,
                    		NULL,
                    		1, 
                    		MemSysm.stack.longs(AvailableSemaphore),
                    		MemSysm.ints(VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT), MemSysm.longs(renderer2.Buffers.commandBuffers),
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
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PSWAPCHAINS, VkUtils2.SwapChainSupportDetails.swapChain);
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

                nvkAcquireNextImageKHR(device, swapchain, TmUt, AvailableSemaphore, VK_NULL_HANDLE, MemSysm.address);


                /*mulAffineL(
                    (float) Math.sin(angle),
                    (float) Math.cos(angle+Half_Pi)
            );*/

                renderer2.UniformBufferObject.updateUniformBuffer();
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


                GLU2.theUnSafe.UNSAFE.putLong(value, Buffers.commandBuffers[currentFrame].address());


                nvkQueueSubmit(graphicsQueue, 1, address2, VK_NULL_HANDLE);

                GLU2.theUnSafe.UNSAFE.putLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore));


                nvkQueuePresentKHR(presentQueue, VkPresentInfoKHR1);
                //                   i+= vkResetFences(Queues.device, vkFenceA);


                currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
//                 System.out.println(stack.stack.getPointer());
                frps++;

            }
        }

    }


     static final record Buffers() {
        public static final VkCommandBuffer[] commandBuffers = new VkCommandBuffer[3];
        static final short[] indices = new short[VkUtils2.PipeLine.indicesTemp.length*64];
        static final int size = Short.BYTES * indices.length;
        static final VkOffset2D set = VkOffset2D.create(MemSysm.malloc3(16)).set(0, 0);
        static long commandPool =0;//MemSysm.stack.mallocPointer(1);
        static final long vkLayout = (set.address()+set.sizeof());
        private static final long vertexBufferMemory;
        static final long[] stagingBuffer = {0};
        private static final long stagingBufferMemory;
        private static final long indexBufferMemory;
        static final long vkAllocMemory = MemSysm.malloc3(8); //TODO:BUGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG!
        static final long vkImage = (MemSysm.malloc3(8));
        static final long depthImageView =MemSysm.malloc3(8);
        private static final long offsets = MemSysm.longs(0).address0();
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


            for (int i =-1; i< 1; i++)
                for (int ii = -1; ii < 1; ii++)
                    for (int iii = -1; iii < 1; iii++)
                        doBufferAlloc2(i, ii, iii);
            //setup/doDepupeface/FaceCukking/
            //Still Likley better to reply in indicies buffer to inherent deuplcitaion/culling aslingas/if/based upon /which as  aconsequence of deplictaing same
            //may also be posibel to use offsets to negate x,y,andz parametsr for eahc rpesove block/veretx, alsmot a bit like  Identity Vertex reference, avoding tey need to check all vericies in the Buffer manually...

            //Dierctional face/Normal rletaion.imferemmt.orientation, used diesgnatiosnbabsed on the position of the new.propro.Old bLock to be inserted relative to adje ces fances, if palcong forward and can aplcoyatiosn rlativ eot curetnt blcok psoiton can togoleprpetivly falgs to outomaticlaly disocrd face.cull tiles. ters.Abs.DCOmpsotive.Ineteeracsosiint IQuead withotu teh need to carry out evalauations comapritive.euqalty.eqianelt operTIon .prooeebts.oidedundbf ,anuallye .g. etc i.e. .etc .Msic. ejegd

            //Do VBO/Poriton.chunk offset relatine to current.relative position instea dof need ing to verificy.ascertain Pos.Coords dierct


            setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT, value, vertexBuffer, VK_SHARING_MODE_EXCLUSIVE);
            vertexBufferMemory=createBuffer2(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, vertexBuffer[0]);
            setBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, value, stagingBuffer, VK_SHARING_MODE_EXCLUSIVE);
            stagingBufferMemory=createBuffer2(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
                    VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer[0]);
            setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT, Short.BYTES * indices.length, indexBuffer, VK_SHARING_MODE_EXCLUSIVE);
            indexBufferMemory = createBuffer2(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, indexBuffer[0]);

        }

        static void createCommandBuffers() {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.create(MemSysm.malloc3(VkCommandBufferAllocateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(VkUtils2.SwapChainSupportDetails.swapChainFramebuffers.length);

            PointerBuffer descriptorSets = memPointerBuffer(allocateInfo.address(), allocateInfo.commandBufferCount());
//            MemSysm.Memsys2.free(allocateInfo);
            doPointerAllocS(allocateInfo, capabilities.vkAllocateCommandBuffers, descriptorSets);
            commandBuffers[0]=new VkCommandBuffer(descriptorSets.get(0), device);
            commandBuffers[1]=new VkCommandBuffer(descriptorSets.get(1), device);
            commandBuffers[2]=new VkCommandBuffer(descriptorSets.get(2), device);


            beginInfo1 = VkCommandBufferBeginInfo.create(MemSysm.malloc3(VkCommandBufferBeginInfo.SIZEOF)).sType$Default()
                    .flags(VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT);

//            VkRenderPassAttachmentBeginInfo vkRenderPassAttachmentBeginInfo1 = VkRenderPassAttachmentBeginInfo.create(MemSysm.malloc(VkRenderPassAttachmentBeginInfo .SIZEOF))
//                    .sType$Default();
            //.pAttachments(VkUtils2.MemSys.stack.longs(VkUtils2.SwapChainSupportDetails.swapChainImageViews));
            VkRect2D renderArea = VkRect2D.create(MemSysm.malloc3(VkRect2D.SIZEOF))
                    .offset(set)
                    .extent(VkUtils2.SwapChainSupportDetails.swapChainExtent);

            //todo: multiple attachments with VK_ATTACHMENT_LOAD_OP_CLEAR: https://vulkan-tutorial.com/en/Depth_buffering#page_Clear-values

            VkClearValue.Buffer clearValues = VkClearValue.create(MemSysm.malloc3(VkClearValue.SIZEOF*2L), 2);
            clearValues.get(0).color().float32(MemSysm.stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);


            renderPassInfo = VkRenderPassBeginInfo.create(MemSysm.malloc3(VkRenderPassBeginInfo.SIZEOF)).sType$Default()
//                    .pNext(vkRenderPassAttachmentBeginInfo1)
                    .pClearValues(clearValues)
                    .renderPass(memGetLong(VkUtils2.SwapChainSupportDetails.renderPass))
                    .renderArea(renderArea);



//            memPutLong(renderPassInfo.address() + VkRenderPassBeginInfo.PCLEARVALUES, clearValues.address0());
//            VkRenderPassBeginInfo.nclearValueCount(renderPassInfo.address(), clearValues.remaining());

            //todo: Mmeory addres sranegs for Uniform bufefrs are opened here propr to the vkCMD Command Buffer records in an attempt to potenetial. reuce overal
            /*todo: Performance Hack: allow Memepry to be premptively mapped prior to FUll Initialisation/Drawing/rendering/Setup/prior to VKCOmmand Buffer recording to allow to avoid overhead/adiitonal when attem0ng to modifey vertex.Buffer data when itilsied and Mapped to a specific memry range(s),
             * This has the darwback of requiring specific alignment when when Obtaining.Plaicng.reading/Writing references.memory Adresses, which at leas in the case of Command Buffers.Uniform buffers seems to be exactly 512, which may potnetial decrease stabilility due to lack of potentoa,s afty wth prevent.prpettcing. mana ging agaonst/with/for Acess Viplations/Segfaults e.g.

             */
//            nvkMapMemory(device, UniformBufferObject.uniformBuffersMemory[0], 0, UniformBufferObject.capacity, 0, MemSysm.address);
            nvkMapMemory(device, UniformBufferObject.uniformBuffersMemory.get(1), 0, UniformBufferObject.capacity, 0, MemSysm.address);

            for (int i = 0; i < commandBuffers.length; i++) {
                extracted(i);

            }

        }

        private static void extracted(int i)
        {
            VkCommandBuffer commandBuffer = commandBuffers[i];
            vkBeginCommandBuffer(commandBuffer, beginInfo1);
            VkDrawIndexedIndirectCommand indexedIndirectCommand = VkDrawIndexedIndirectCommand.create(MemSysm.malloc3(VkDrawIndexedIndirectCommand.SIZEOF))
                            .indexCount(indices.length)
                                    .instanceCount(1)
                                            .firstIndex(0)
                                                    .vertexOffset(0)
                                                            .firstInstance(1);

            renderPassInfo.framebuffer(VkUtils2.SwapChainSupportDetails.swapChainFramebuffers[i]);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
//                    nvkCmdBindVertexBuffers(commandBuffer, 0, 1, (put), memAddress0(offsets));
                nvkCmdBindVertexBuffers(commandBuffer, 0, 1,MemSysm.longs(vertexBuffer).address0(), offsets);
                vkCmdBindIndexBuffer(commandBuffer, indexBuffer[0], 0, VK_INDEX_TYPE_UINT16);
                UniformBufferObject.mvp.getToAddress(memGetLong(MemSysm.address)+0x200);
                long pointerBuffer = MemSysm.stack.pointers((UniformBufferObject.descriptorSets.get(0))).address0();

                // Convert to long to support addressing up to 2^31-1 elements, regardless of sizeof(element).
                // The unsigned conversion helps the JIT produce code that is as fast as if int was returned.
                nvkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, memGetLong(vkLayout), 0, 1, (pointerBuffer), 0, 0);
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
            return memGetLong(commandPool/*.address()*/);
        }

        public static void doPointerAllocS(Pointer.Default allocateInfo, long vkAllocateDescriptorSets, PointerBuffer descriptorSets) {

            callPPPI(device.address(), allocateInfo.address(), descriptorSets.address0(), vkAllocateDescriptorSets);
            MemSysm.Memsys2.free(allocateInfo);
//            return address;

        }

        //todo: need to do thsi seperately from createBuffer as Java cannot handle references/pointrs by default and the buffre must be a field due to the bindVertexBuffers call needing acces to teh same VertexBuffer that is cerated, and a methdo/upu cannto return multiple variables/refercnes/valeus at teh same time
        static long setBuffer2(int usage, int size, int sharingMode) {
            VkBufferCreateInfo allocateInfo = VkBufferCreateInfo.create(MemSysm.malloc(VkBufferCreateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(size)
                    .usage(usage)
                    .sharingMode(sharingMode);

            return MemSysm.doPointerAllocSafe(allocateInfo, capabilities.vkCreateBuffer);
            //nmemFree(allocateInfo);
        }static void setBuffer(int usage, int size, long[] a, int sharingMode) {
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

        static long createBuffer2(int properties, long currentBuffer) {

            //long vkMemoryRequirements = VkUtils2.MemSysm.malloc(VkMemoryRequirements.SIZEOF);

            nvkGetBufferMemoryRequirements(device, currentBuffer, MemSysm.address);


            VkMemoryAllocateInfo allocateInfo1 = VkMemoryAllocateInfo.create(MemSysm.calloc(VkMemoryAllocateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(VkMemoryRequirements.nsize(MemSysm.address))
                    .memoryTypeIndex(findMemoryType(VkMemoryRequirements.nmemoryTypeBits(MemSysm.address), properties));
//            nmemFree(vkMemoryRequirements);
            long vertexBufferMemory = MemSysm.doPointerAllocSafe(allocateInfo1, capabilities.vkAllocateMemory);
            long __functionAddress = device.getCapabilities().vkBindBufferMemory;
            callPJJJI(device.address(), currentBuffer, vertexBufferMemory, 0, __functionAddress);
            //memPutLong( device.address(), a);
            return vertexBufferMemory;

        }static void createBuffer(int properties, long[] currentBuffer, long[] vertexBufferMemory) {

            //long vkMemoryRequirements = VkUtils2.MemSysm.malloc(VkMemoryRequirements.SIZEOF);

            nvkGetBufferMemoryRequirements(device, currentBuffer[0], MemSysm.address);


            VkMemoryAllocateInfo allocateInfo1 = VkMemoryAllocateInfo.create(MemSysm.calloc(VkMemoryAllocateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(VkMemoryRequirements.nsize(MemSysm.address))
                    .memoryTypeIndex(findMemoryType(VkMemoryRequirements.nmemoryTypeBits(MemSysm.address), properties));
//            nmemFree(vkMemoryRequirements);
            MemSysm.Memsys2.doPointerAllocSafe2(allocateInfo1, capabilities.vkAllocateMemory, vertexBufferMemory);
             long __functionAddress = device.getCapabilities().vkBindBufferMemory;
            callPJJJI(device.address(), currentBuffer[0], vertexBufferMemory[0], 0, __functionAddress);
            //memPutLong( device.address(), a);

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

            long __functionAddress = device.getCapabilities().vkBindBufferMemory;
            callPJJJI(device.address(), currentBuffer, vertexBufferMemory[0], 0, __functionAddress);

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

        static void copyBuffer(long dstBuffer, int size) {

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();
            final VkBufferCopy vkBufferCopy = VkBufferCopy.create(MemSysm.address)
                    .srcOffset(0)
            .dstOffset(0)
                    .size(size);
//            nmemFree(vkBufferCopy);
            nvkCmdCopyBuffer(commandBuffer, stagingBuffer[0], dstBuffer, 1, vkBufferCopy.address());
            endSingleTimeCommands(commandBuffer);

        }static void copyBuffer(long[] dstBuffer, int size) {

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();
            final VkBufferCopy vkBufferCopy = VkBufferCopy.create(MemSysm.address)
                    .srcOffset(0)
            .dstOffset(0)
                    .size(size);
            MemSysm.Memsys2.free(vkBufferCopy);
            nvkCmdCopyBuffer(commandBuffer, stagingBuffer[0], dstBuffer[0], 1, vkBufferCopy.address());
            endSingleTimeCommands(commandBuffer);

        }

        static @NotNull VkCommandBuffer beginSingleTimeCommands() {

            final long allocateInfo = MemSysm.calloc(VkCommandBufferAllocateInfo.SIZEOF);
            VkCommandBufferAllocateInfo.nsType(allocateInfo, VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            VkCommandBufferAllocateInfo.nlevel(allocateInfo, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            VkCommandBufferAllocateInfo.ncommandPool(allocateInfo, commandPool);
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

            final long pointers = (MemSysm.stack.pointers(commandBuffer.address()).address0());
            memPutAddress(submitInfo1.address() + VkSubmitInfo.PCOMMANDBUFFERS, pointers);
            VkSubmitInfo.ncommandBufferCount(submitInfo1.address(), 1);

//            VkSubmitInfo.ncommandBufferCount(submitInfo1, 1);

            callPPJI(presentQueue.address(), 1, submitInfo1.address(), VK_NULL_HANDLE, presentQueue.getCapabilities().vkQueueSubmit);
            //            callPI(presentQueue.address(), presentQueue.getCapabilities().vkQueueWaitIdle);
            MemSysm.Memsys2.free8(pointers);
            nvkFreeCommandBuffers(device, memGetLong(commandBuffer.address()), 1, pointers);
        }

        private static long createDescriptorSetLayout() {
            {
                VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.create(MemSysm.malloc3(VkDescriptorSetLayoutBinding.SIZEOF*2L), 2);

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


                VkDescriptorSetLayoutCreateInfo a = VkDescriptorSetLayoutCreateInfo.create(MemSysm.malloc3(VkDescriptorSetLayoutCreateInfo.SIZEOF))
                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                        .pBindings(bindings);
                return MemSysm.doPointerAllocSafe(a, device.getCapabilities().vkCreateDescriptorSetLayout);
            }
        }

        static void createVertexBufferStaging() {


            //            nvkMapMemory(Queues.device, stagingBufferMemory, 0, value, 0, address);

            //            VkPushConstantRange.

            nvkMapMemory(device, stagingBufferMemory, 0, value, 0, MemSysm.address);
            {
                GLU2.theGLU.memcpy2(vertices, MemSysm.getHandle(), (vertices.length << 2));
                //                GLU2.theGLU.wrap()

            }
            vkUnmapMemory(device, stagingBufferMemory);

            //            vertexBuffers = getSetBuff  er(PipeLine.currentBuffer);

            copyBuffer(vertexBuffer, value);


        }

        static void createIndexBuffer() {

            nvkMapMemory(device, stagingBufferMemory, 0, size, 0, MemSysm.address);
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
            vkUnmapMemory(device, stagingBufferMemory);

            copyBuffer(indexBuffer, size);

        }

        static void creanupBufferStaging() {
            vkDestroyBuffer(device, stagingBuffer[0], MemSysm.pAllocator);
            vkFreeMemory(device, stagingBufferMemory, MemSysm.pAllocator);
        }

         public static void createVkEvents()
         {

             VkEventCreateInfo vkEventCreateInfo = VkEventCreateInfo.create(MemSysm.malloc0()).sType$Default()
                     ;
             MemSysm.Memsys2.free(vkEventCreateInfo);
             MemSysm.doPointerAllocSafe(vkEventCreateInfo, device.getCapabilities().vkCreateEvent);
         }

         private static long createTextureSampler()
         {
             //                    System.out.println(properties.limits());
             VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.create(MemSysm.malloc(VkSamplerCreateInfo.SIZEOF)).sType$Default()
                     //                    .sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                     .magFilter(VK_FILTER_NEAREST)
                     .minFilter(VK_FILTER_NEAREST)
                     .addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                     .addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                     .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT)
                     .anisotropyEnable(false)
                     //                    .maxAnisotropy(properties.limits().maxSamplerAnisotropy())
                     .borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK)
                     .unnormalizedCoordinates(false)
                     .compareEnable(false)
                     .compareOp(VK_COMPARE_OP_ALWAYS)
                     .mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST)
                     .mipLodBias(0)
                     .minLod(0)
                     .maxLod(0);
             MemSysm.Memsys2.free(samplerInfo);
             return MemSysm.doPointerAllocSafe(samplerInfo, capabilities.vkCreateSampler);
             //nmemFree(samplerInfo.address());
         }
     }

     static final class UniformBufferObject
    {

        private static final Matrix4f mvp;// = new Matrix4f().identity();
        static final long descriptorSetLayout;
        //        private static LongBuffer pDescriptorSetLayout;
        private static final PointerBuffer uniformBuffers =  memPointerBuffer(MemSysm.malloc3(16),3);
        private static final PointerBuffer uniformBuffersMemory = memPointerBuffer(MemSysm.malloc3(16),3);
        static final int capacity = 16 * Float.BYTES;
        private static long descriptorPool = 0;
        static final PointerBuffer descriptorSets = memPointerBuffer(MemSysm.malloc3(16),3);

        static final long textureImageView = MemSysm.malloc(0);

        private static final float zFar = 120.0f;

        private static final float zNear = 3.1f;

        private static final Matrix4f trans = new Matrix4f().identity();

        private final static float v1;
        private final static float a;
        private final static float a1;
//        private final static float a2;

        static {

            descriptorSetLayout = renderer2.Buffers.createDescriptorSetLayout();
//            textureSampler = renderer2.Buffers.createTextureSampler();


            float h = (Math.tan(Math.toRadians(45) * 0.5f));
            mvp = new Matrix4f().determineProperties().m00(1.0f / (h * (VkUtils2.SwapChainSupportDetails.swapChainExtent.width() / (float) VkUtils2.SwapChainSupportDetails.swapChainExtent.height())))
                    .m11((1.0f / h) * -1)
                    .m22((zFar + zNear) / (zNear - zFar))
                    .m32((zFar + zFar) * zNear / (zNear - zFar))
                    .m23(-1.0f);
            mvp.mulPerspectiveAffine(new Matrix4f().determineProperties().setLookAt(2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f));
            mvp.translate(new Vector3f(-4, -4, -4));

            v1 = mvp.m13();
            a = mvp.m00();
            a1 = mvp.m01();
//            a2 = mvp.m03();

        }
        //todo: Important: atcuall have to 'reset; or revert teh matrixces bakc to baseline.Idneitfy in order to atcually update te Unform Buffer(s) properly e.g. .etc i.e.
//            UniformBufferObject.model.identity();
//            UniformBufferObject.view.identity();
//            UniformBufferObject.proj.identity();


        //private static final double aFloat = Math.toRadians(90D);

        public static void createUniformBuffers()
        {
            for (int i = 0; i < VkUtils2.SwapChainSupportDetails.swapChainImages.length; i++) {

                uniformBuffers.put((((Buffers.setBuffer(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, capacity)))));
                uniformBuffersMemory.put((Buffers.createBuffer(uniformBuffers.get(i))));
            }
        }

        //@Benchmark
        static void updateUniformBuffer()
        {
           // final double v = cursorPos.x-VkUtils2.SwapChainSupportDetails.swapChainExtent.height()/-cursorPos.y- VkUtils2.SwapChainSupportDetails.swapChainExtent.width();//(invokeD(Functions.GetTime) * aFloat);
            double dirX = Math.sin(cursorPos.x/100);
            double dirY = -Math.cosFromSin(dirX, cursorPos.y/100);
            //rotateTowardsXY2();//.getToAddress(Renderer2.aa);
            mvp.rotateTowardsXY((float) dirX, (float) dirY, trans).getToAddress(Renderer2.aa);
//            trans.transform((float) (-cursorPos.y / -cursorPos.x)).getToAddress(Renderer2.aa);
//            memcpy2();

        }

        private static void rotateTowardsXY2()
        {
            final double v = cursorPos.x-VkUtils2.SwapChainSupportDetails.swapChainExtent.height()/-cursorPos.y- VkUtils2.SwapChainSupportDetails.swapChainExtent.width();//(invokeD(Functions.GetTime) * aFloat);
            double dirX = Math.sin(cursorPos.x/100);
            double dirY = -Math.cosFromSin(dirX, cursorPos.y/100);

            trans
                 .m00((float) Math.fma(a, dirY, dirX))
                 .m01((float) Math.fma(a1, dirY, -dirY))
                 .m02((float) Math.fma(v1, dirX, 0))
                 //.m03((float) Math.fma(a2, dirY, v1 * dirX))
                 .m10((float) Math.fma(a, -dirX, dirY))
                 .m11((float) Math.fma(a1, -dirX, dirY))
                 .m12((float) Math.fma(v1, dirY, 0));
                 //.m13((float) Math.fma(a2, -dirX, v1 * dirY));
//                    .m20(mvp.m20())
            //                    .m21(mvp.m21())
            //                    .m22(mvp.m22())
            //                    .m23(mvp.m23())
//                    .m30(mvp.m30())
            //                    .m31(mvp.m31())
            //                    .m32(mvp.m32())
            //                    .m33(mvp.m33());
//                    .properties(properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION));
        }

        private static void memcpy2()
        {
            GLU2.theUnSafe.UNSAFE.putLong(Renderer2.aa, GLU2.theUnSafe.UNSAFE.getLong(trans, 0xC));
            GLU2.theUnSafe.UNSAFE.putLong(Renderer2.aa + 0x8, GLU2.theUnSafe.UNSAFE.getLong(trans, 0x14));
            GLU2.theUnSafe.UNSAFE.putLong(Renderer2.aa + 0x10, GLU2.theUnSafe.UNSAFE.getLong(trans, 0x1C));
            GLU2.theUnSafe.UNSAFE.putLong(Renderer2.aa + 0x18, GLU2.theUnSafe.UNSAFE.getLong(trans, 0x24));
        }

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
                descriptorPool = MemSysm.doPointerAllocSafe(poolCreateInfo, device.getCapabilities().vkCreateDescriptorPool);
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
                allocInfo.descriptorPool((descriptorPool))
                        .pSetLayouts(layouts);

                nvkAllocateDescriptorSets(device, allocInfo.address(), descriptorSets.address0());

                VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.create(MemSysm.malloc3(VkDescriptorBufferInfo.SIZEOF),1)
                        .offset(0)
                        .range(capacity);

                VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.create(MemSysm.malloc3(VkDescriptorImageInfo.SIZEOF),1)
                        .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                        .imageView(memGetLong(textureImageView))
                        .sampler(renderer2.Buffers.createTextureSampler());

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


                    bufferInfo.buffer(uniformBuffers.get(i));

                    vkWriteDescriptorSet.dstSet(descriptorSets.get(i));
                    samplerDescriptorWrite.dstSet(descriptorSets.get(i));

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