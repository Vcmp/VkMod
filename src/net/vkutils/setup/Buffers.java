package vkutils.setup;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.*;


import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.setup.Queues.*;

public final class Buffers {
    public static final PointerBuffer commandBuffers = memPointerBuffer(MemSysm.malloc3(24), 3);
    //Manipulate indicies to use indexes more likley to be dulcitaed when.if cases of adjency occur to allow vereycoes to be shared between Blocks/Quads COmposits.Hybrids
    static final short[] indicesTemp = {

            0, 1, 2, 2, 3, 0,
            4, 5, 6, 6, 7, 4,
            8, 9, 10, 10, 11, 8,
            12, 13, 14, 14, 15, 12,
            16, 17, 18, 18, 19, 16,
            20, 21, 22, 22, 23, 20

            /*0, 1, 2, 2, 3, 0,
            0, 1, 5, 5, 4, 0,
            1, 2, 6, 6, 5, 1,
            4, 5, 6, 6, 7, 4,
            4, 7, 3, 3, 0, 4,
            7, 6, 2, 2, 3, 7,*/
/*
            0+8, 1+8, 2+8, 2+8, 3+8, 0+8,
            0+8, 1+8, 5+8, 5+8, 4+8, 0+8,
            1+8, 2+8, 6+8, 6+8, 5+8, 1+8,
            4+8, 5+8, 6+8, 6+8, 7+8, 4+8,
            4+8, 7+8, 3+8, 3+8, 0+8, 4+8,
            7+8, 6+8, 2+8, 2+8, 3+8, 7+8,

            0+16, 1+16, 2+16, 2+16, 3+16, 0+16,
            0+16, 1+16, 5+16, 5+16, 4+16, 0+16,
            1+16, 2+16, 6+16, 6+16, 5+16, 1+16,
            4+16, 5+16, 6+16, 6+16, 7+16, 4+16,
            4+16, 7+16, 3+16, 3+16, 0+16, 4+16,
            7+16, 6+16, 2+16, 2+16, 3+16, 7+16,*/
    };
    static final short[] indices = new short[indicesTemp.length * 64];
    static final int size = Short.BYTES * indices.length;
    static final VkOffset2D set = VkOffset2D.create(MemSysm.malloc3(16)).set(0, 0);
    static long commandPool = 0;//MemSysm.stack.mallocPointer(1);
    static final long vkLayout = (set.address() + set.sizeof());
     static final long vertexBufferMemory;
    static final long[] stagingBuffer = {0};
     static final long stagingBufferMemory;
     static final long indexBufferMemory;
    static final long vkAllocMemory = MemSysm.malloc3(8); //TODO:BUGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG!
    static final long depthImageView = MemSysm.malloc3(8);
    private static final long offsets = MemSysm.longs(0).address0();
    //        private static final int VERT_SIZE= (OFFSET_POS + OFFSETOF_COLOR + OFFSETOF_TEXTCOORDS) / Float.BYTES;
    static final int VERTICESSTRIDE = 32;//vertices.length*6/indices.length;
    static final VKCapabilitiesDevice capabilities;
    //        private static long stagingBuffer;
    static final long[] vertexBuffer = {0};
    static final long[] indexBuffer = {0};
    private static final int U = 0;
    private static final int V = 0;
    private static final float[] verticesTemp = { //TODO: Auto connetc when adding diitonal evrticies: e.g. if now Block.Cube added, rese preisiting vertocoes to avoid rudplciate/reudicnat faces/vetrcoe sbeign drawnbthat are not vosoable/occluded due to adjency, may be psob;e top use a Indicieis buffer to do thins inherently without the need to manually/systamtcially ascerain verticioe sduplciates manually /methodcially e.g.etc ie.e., preme;tively

            0, 0, 0, 1, 0, 0, U, V,
            1, 1, 0, 0, 1, 0, U + 1, V,
            1, 1, 0, 0, 0, 1, U + 1, V + 1,
            0, 1, 0, 1, 1, 1, U, V + 1,

            0, 0, 1, 1, 0, 0, U, V,
            1, 0, 1, 0, 1, 0, U + 1, V,
            1, 1, 1, 0, 0, 1, U + 1, V + 1,
            0, 1, 1, 1, 1, 1, U, V + 1,

            0, 0, 0, 1, 0, 0, U, V,
            1, 0, 0, 0, 1, 0, U + 1, V,
            1, 0, 1, 0, 0, 1, U + 1, V + 1,
            0, 0, 1, 1, 1, 1, U, V + 1,

            0, 1, 1, 1, 0, 0, U, V,
            1, 1, 1, 0, 1, 0, U + 1, V,
            1, 1, 0, 0, 0, 1, U + 1, V + 1,
            0, 1, 0, 1, 1, 1, U, V + 1,

            0, 0, 1, 1, 0, 0, U, V,
            0, 1, 1, 0, 1, 0, U + 1, V,
            0, 1, 0, 0, 0, 1, U + 1, V + 1,
            0, 0, 0, 1, 1, 1, U, V + 1,

            1, 0, 0, 1, 0, 0, U, V,
            1, 1, 0, 0, 1, 0, U + 1, V,
            1, 1, 1, 0, 0, 1, U + 1, V + 1,
            1, 0, 1, 1, 1, 1, U, V + 1,


    };
    private static int dest = -verticesTemp.length;
    private static final float[] vertices = new float[(verticesTemp.length) * 64];
    private static final int value = /*SIZEOF **/ vertices.length * Float.BYTES;

    private static final int VERT_SIZE = vertices.length / 8;


    static long graphicsPipeline;
//    private static VkCommandBufferBeginInfo beginInfo1;
//    private static VkRenderPassBeginInfo renderPassInfo;
    private static final VkSubmitInfo submitInfo1 = VkSubmitInfo.create(MemSysm.calloc(VkSubmitInfo.SIZEOF))//VkUtils2.MemSysm.nmalloc(VkSubmitInfo.ALIGNOF, VkSubmitInfo.SIZEOF);
            .sType$Default();

    static {
        capabilities = device.getCapabilities();

        doBufferAlloc();


        for (int i = -1; i < 1; i++)
            for (int ii = -8; ii < 8; ii++)
                for (int iii = -1; iii < 1; iii++)
                    doBufferAlloc2(i, ii, iii);
        //setup/doDepupeface/FaceCukking/
        //Still Likley better to reply in indicies buffer to inherent deuplcitaion/culling aslingas/if/based upon /which as  aconsequence of deplictaing same
        //may also be posibel to use offsets to negate x,y,andz parametsr for eahc rpesove block/veretx, alsmot a bit like  Identity Vertex reference, avoding tey need to check all vericies in the Buffer manually...

        //Dierctional face/Normal rletaion.imferemmt.orientation, used diesgnatiosnbabsed on the position of the new.propro.Old bLock to be inserted relative to adje ces fances, if palcong forward and can aplcoyatiosn rlativ eot curetnt blcok psoiton can togoleprpetivly falgs to outomaticlaly disocrd face.cull tiles. ters.Abs.DCOmpsotive.Ineteeracsosiint IQuead withotu teh need to carry out evalauations comapritive.euqalty.eqianelt operTIon .prooeebts.oidedundbf ,anuallye .g. etc i.e. .etc .Msic. ejegd

        //Do VBO/Poriton.chunk offset relatine to current.relative position instea dof need ing to verificy.ascertain Pos.Coords dierct


        setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT, value, vertexBuffer);
        vertexBufferMemory = createBuffer2(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, vertexBuffer[0]);
        setBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, value, stagingBuffer);
        stagingBufferMemory = createBuffer2(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
                VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer[0]);
        setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT, Short.BYTES * indices.length, indexBuffer);
        indexBufferMemory = createBuffer2(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, indexBuffer[0]);

    }

    static void createCommandBuffers()
    {
        VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.create(MemSysm.malloc3(VkCommandBufferAllocateInfo.SIZEOF))
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(commandPool)
                .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(SwapChainSupportDetails.swapChainFramebuffers.capacity());

        PointerBuffer descriptorSets = memPointerBuffer(allocateInfo.address(), allocateInfo.commandBufferCount());
//            MemSysm.Memsys2.free(allocateInfo);
        doPointerAllocS(allocateInfo, capabilities.vkAllocateCommandBuffers, descriptorSets);
        commandBuffers.put(descriptorSets);
        descriptorSets.free();

        VkCommandBufferBeginInfo beginInfo1 = VkCommandBufferBeginInfo.create(MemSysm.malloc3(VkCommandBufferBeginInfo.SIZEOF)).sType$Default()
                .flags(VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT);

//            VkRenderPassAttachmentBeginInfo vkRenderPassAttachmentBeginInfo1 = VkRenderPassAttachmentBeginInfo.create(MemSysm.malloc(VkRenderPassAttachmentBeginInfo .SIZEOF))
//                    .sType$Default();
        //.pAttachments(VkUtils2.MemSys.stack.longs(VkUtils2.SwapChainSupportDetails.swapChainImageViews));
        VkRect2D renderArea = VkRect2D.create(MemSysm.malloc3(VkRect2D.SIZEOF))
                .offset(set)
                .extent(SwapChainSupportDetails.swapChainExtent);

        //todo: multiple attachments with VK_ATTACHMENT_LOAD_OP_CLEAR: https://vulkan-tutorial.com/en/Depth_buffering#page_Clear-values

        VkClearValue.Buffer clearValues = VkClearValue.create(MemSysm.malloc3(VkClearValue.SIZEOF * 2L), 2);
        clearValues.get(0).color().float32(MemSysm.stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
        clearValues.get(1).depthStencil().set(1.0f, 0);


        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.create(MemSysm.malloc3(VkRenderPassBeginInfo.SIZEOF)).sType$Default()
//                    .pNext(vkRenderPassAttachmentBeginInfo1)
                .pClearValues(clearValues)
                .renderPass(memGetLong(SwapChainSupportDetails.renderPass))
                .renderArea(renderArea);


//            memPutLong(renderPassInfo.address() + VkRenderPassBeginInfo.PCLEARVALUES, clearValues.address0());
//            VkRenderPassBeginInfo.nclearValueCount(renderPassInfo.address(), clearValues.remaining());

        //todo: Mmeory addres sranegs for Uniform bufefrs are opened here propr to the vkCMD Command Buffer records in an attempt to potenetial. reuce overal
        /*todo: Performance Hack: allow Memepry to be premptively mapped prior to FUll Initialisation/Drawing/rendering/Setup/prior to VKCOmmand Buffer recording to allow to avoid overhead/adiitonal when attem0ng to modifey vertex.Buffer data when itilsied and Mapped to a specific memry range(s),
         * This has the darwback of requiring specific alignment when when Obtaining.Plaicng.reading/Writing references.memory Adresses, which at leas in the case of Command Buffers.Uniform buffers seems to be exactly 512, which may potnetial decrease stabilility due to lack of potentoa,s afty wth prevent.prpettcing. mana ging agaonst/with/for Acess Viplations/Segfaults e.g.

         */
//            nvkMapMemory(device, UniformBufferObject.uniformBuffersMemory[0], 0, UniformBufferObject.capacity, 0, MemSysm.address);
        nvkMapMemory(device, UniformBufferObject.uniformBuffersMemory.get(1), 0, UniformBufferObject.capacity, 0, MemSysm.address);

        for (int i = 0; i < commandBuffers.capacity(); i++) {
            extracted(beginInfo1, renderPassInfo, i);

        }

    }

    private static void extracted(VkCommandBufferBeginInfo beginInfo1, VkRenderPassBeginInfo renderPassInfo, int i)
    {
        long commandBuffer = commandBuffers.get(i);
        long __functionAddress = device.getCapabilities().vkBeginCommandBuffer;
        callPPI((commandBuffer), beginInfo1.address(), __functionAddress);
        VkDrawIndexedIndirectCommand indexedIndirectCommand = VkDrawIndexedIndirectCommand.create(MemSysm.malloc3(VkDrawIndexedIndirectCommand.SIZEOF))
                .indexCount(indices.length)
                .instanceCount(1)
                .firstIndex(0)
                .vertexOffset(0)
                .firstInstance(1);

        renderPassInfo.framebuffer(SwapChainSupportDetails.swapChainFramebuffers.get(i));

        callPPV((commandBuffer), renderPassInfo.address(), VK_SUBPASS_CONTENTS_INLINE, capabilities.vkCmdBeginRenderPass);
        {
            callPJV((commandBuffer), VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline, capabilities.vkCmdBindPipeline);
            //                    nvkCmdBindVertexBuffers(commandBuffer, 0, 1, (put), memAddress0(offsets));
            final PointerBuffer longs = MemSysm.longs(vertexBuffer);
            callPPPV((commandBuffer), 0, 1, longs.address0(), offsets, device.getCapabilities().vkCmdBindVertexBuffers);
            callPJJV((commandBuffer), indexBuffer[0], 0, VK_INDEX_TYPE_UINT16, capabilities.vkCmdBindIndexBuffer);
            UniformBufferObject.mvp.getToAddress(memGetLong(MemSysm.address) + 0x200);
//                long pointerBuffer = memPointerBuffer(UniformBufferObject.descriptorSets.address(), 1).address0();
            longs.free();
            // Convert to long to support addressing up to 2^31-1 elements, regardless of sizeof(element).
            // The unsigned conversion helps the JIT produce code that is as fast as if int was returned.
            callPJPPV((commandBuffer), VK_PIPELINE_BIND_POINT_GRAPHICS, memGetLong(vkLayout), 0, 1, (UniformBufferObject.descriptorSets.address()), 0, 0, capabilities.vkCmdBindDescriptorSets);
            callPV((commandBuffer), indices.length, 1, 0, 0, 0, capabilities.vkCmdDrawIndexed);
            UniformBufferObject.descriptorSets.free();

            longs.free();
            indexedIndirectCommand.free();
            renderPassInfo.free();
            beginInfo1.free();
        }
        callPV((commandBuffer), capabilities.vkCmdEndRenderPass);

        System.out.println("Using: " + VERTICESSTRIDE + " Vertices");
        System.out.println("Using: " + VERT_SIZE + " Vert SIze/ Tris");
        callPI((commandBuffer), capabilities.vkEndCommandBuffer);
    }

    public static void doPointerAllocS(Pointer.Default allocateInfo, long vkAllocateDescriptorSets, PointerBuffer descriptorSets)
    {

        callPPPI(device.address(), allocateInfo.address(), descriptorSets.address0(), vkAllocateDescriptorSets);
        MemSysm.Memsys2.free(allocateInfo);
//            return address;

    }

    static void setBuffer(int usage, int size, long[] a)
    {
        VkBufferCreateInfo allocateInfo = VkBufferCreateInfo.create(MemSysm.malloc(VkBufferCreateInfo.SIZEOF))
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .size(size)
                .usage(usage)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE);

        MemSysm.Memsys2.doPointerAllocSafe2(allocateInfo, capabilities.vkCreateBuffer, a);
        //nmemFree(allocateInfo);
    }

    static long setBuffer()
    {
        //long allocateInfo = VkUtils2.MemSysm.malloc(VkBufferCreateInfo.SIZEOF);
        VkBufferCreateInfo allocateInfo = VkBufferCreateInfo.create(MemSysm.calloc(VkBufferCreateInfo.SIZEOF))
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .size(UniformBufferObject.capacity)
                .usage(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
        //nmemFree(allocateInfo);
        MemSysm.Memsys2.free(allocateInfo);
        return MemSysm.doPointerAllocSafe(allocateInfo, capabilities.vkCreateBuffer);
    }

    static long createBuffer2(int properties, long currentBuffer)
    {

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

    }

    static void createBuffer(long[] currentBuffer, long[] vertexBufferMemory)
    {

        //long vkMemoryRequirements = VkUtils2.MemSysm.malloc(VkMemoryRequirements.SIZEOF);

        nvkGetBufferMemoryRequirements(device, currentBuffer[0], MemSysm.address);


        VkMemoryAllocateInfo allocateInfo1 = VkMemoryAllocateInfo.create(MemSysm.calloc(VkMemoryAllocateInfo.SIZEOF))
                .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .allocationSize(VkMemoryRequirements.nsize(MemSysm.address))
                .memoryTypeIndex(findMemoryType(VkMemoryRequirements.nmemoryTypeBits(MemSysm.address), 6));
//            nmemFree(vkMemoryRequirements);
        MemSysm.Memsys2.doPointerAllocSafe2(allocateInfo1, capabilities.vkAllocateMemory, vertexBufferMemory);
        long __functionAddress = device.getCapabilities().vkBindBufferMemory;
        callPJJJI(device.address(), currentBuffer[0], vertexBufferMemory[0], 0, __functionAddress);
        //memPutLong( device.address(), a);

    }

    static int findMemoryType(int typeFilter, int properties)
    {
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

    static long createBuffer(long currentBuffer)
    {

        nvkGetBufferMemoryRequirements(device, currentBuffer, MemSysm.address);

        long[] vertexBufferMemory = {0};
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
        for (int v = dest; v < dest + verticesTemp.length; v += 8) {
            vertices[v] += x;
            vertices[v + 1] += y;
            vertices[v + 2] += z;
        }
    }

    private static void doBufferAlloc()
    {
        int a = 2;
        int r = verticesTemp.length / 8;
        System.arraycopy(indicesTemp, 0, indices, 0, indicesTemp.length);
        for (int w = 36; w < indices.length; w += 36) {
            {
                System.arraycopy(indicesTemp, 0, indices, w, indicesTemp.length);
                for (int i = w; i < indicesTemp.length * a; i++) {
                    indices[i] += r;
                }
                r += verticesTemp.length / 8;

                a++;
            }
        }


    }

    static void copyBuffer(long dstBuffer, int size)
    {

        VkCommandBuffer commandBuffer = beginSingleTimeCommands();
        final VkBufferCopy vkBufferCopy = VkBufferCopy.create(MemSysm.address)
                .srcOffset(0)
                .dstOffset(0)
                .size(size);
//            nmemFree(vkBufferCopy);
        nvkCmdCopyBuffer(commandBuffer, stagingBuffer[0], dstBuffer, 1, vkBufferCopy.address());
        endSingleTimeCommands(commandBuffer);

    }

    static void copyBuffer(long[] dstBuffer, int size)
    {

        VkCommandBuffer commandBuffer = beginSingleTimeCommands();
        final VkBufferCopy vkBufferCopy = VkBufferCopy.create(MemSysm.address)
                .srcOffset(0)
                .dstOffset(0)
                .size(size);
        MemSysm.Memsys2.free(vkBufferCopy);
        nvkCmdCopyBuffer(commandBuffer, stagingBuffer[0], dstBuffer[0], 1, vkBufferCopy.address());
        endSingleTimeCommands(commandBuffer);

    }

    static @NotNull VkCommandBuffer beginSingleTimeCommands()
    {

        final long allocateInfo = MemSysm.malloc3(VkCommandBufferAllocateInfo.SIZEOF);
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

    static void endSingleTimeCommands(@NotNull Pointer commandBuffer)
    {
        callPI(commandBuffer.address(), ((VkCommandBuffer) commandBuffer).getCapabilities().vkEndCommandBuffer);

        final long pointers = (MemSysm.stack.pointers(commandBuffer.address()).address0());
        memPutLong(memGetLong(commandBuffer.address()), commandBuffer.address());
        memPutAddress(submitInfo1.address() + VkSubmitInfo.PCOMMANDBUFFERS, memGetLong(commandBuffer.address()));
        VkSubmitInfo.ncommandBufferCount(submitInfo1.address(), 1);

//            VkSubmitInfo.ncommandBufferCount(submitInfo1, 1);

        callPPJI(presentQueue.address(), 1, submitInfo1.address(), VK_NULL_HANDLE, presentQueue.getCapabilities().vkQueueSubmit);
        //            callPI(presentQueue.address(), presentQueue.getCapabilities().vkQueueWaitIdle);
        MemSysm.Memsys2.free8(pointers);
        nvkFreeCommandBuffers(device, memGetLong(commandBuffer.address()), 1, memGetLong(commandBuffer.address()));
    }

    static long createDescriptorSetLayout()
    {
        {
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.create(MemSysm.malloc3(VkDescriptorSetLayoutBinding.SIZEOF * 2L), 2);

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

    static void createVertexBufferStaging()
    {


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

    static void createIndexBuffer()
    {

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

    static void creanupBufferStaging()
    {
        vkDestroyBuffer(device, stagingBuffer[0], MemSysm.pAllocator);
        vkFreeMemory(device, stagingBufferMemory, MemSysm.pAllocator);
    }

    public static void createVkEvents()
    {

        VkEventCreateInfo vkEventCreateInfo = VkEventCreateInfo.create(MemSysm.malloc0()).sType$Default();
        MemSysm.Memsys2.free(vkEventCreateInfo);
        MemSysm.doPointerAllocSafe(vkEventCreateInfo, device.getCapabilities().vkCreateEvent);
    }

    static long createTextureSampler()
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
