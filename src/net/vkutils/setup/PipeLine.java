package vkutils.setup;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.setup.Queues.device;

public final class PipeLine {


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


    //prepAlloc
    //        private static final long[] vkRenderPass = new long[SwapChainSupportDetails.imageIndex];
    static final long[] swapChainImages = new long[3];
    private static final int OFFSETOF_COLOR = 3 * Float.BYTES;
    private static final int OFFSET_POS = 0;

    private static final int OFFSETOF_TEXTCOORDS = (3 + 3) * Float.BYTES;

//            nvkMapMemory(Queues.device, stagingBufferMemory, 0, SIZEOFIn, 0, address);

    //        static final int SIZEOFIn = Short.BYTES * renderer.Buffers.indices.length;

    //            vertexBuffers = stack.stack.longs(PipeLine.vertexBuffer);
//            stagingBuffer = setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);


    static void createGraphicsPipelineLayout()
    {
        System.out.println("Setting up PipeLine");

        ShaderSPIRVUtils.SPIRV vertShaderSPIRV = ShaderSPIRVUtils.compileShaderFile("shaders/21_shader_ubo.vert", ShaderSPIRVUtils.ShaderKind.VERTEX_SHADER);
        ShaderSPIRVUtils.SPIRV fragShaderSPIRV = ShaderSPIRVUtils.compileShaderFile("shaders/21_shader_ubo.frag", ShaderSPIRVUtils.ShaderKind.FRAGMENT_SHADER);

        final long vertShaderModule = ShaderSPIRVUtils.createShaderModule(vertShaderSPIRV.bytecode());
        final long fragShaderModule = ShaderSPIRVUtils.createShaderModule(fragShaderSPIRV.bytecode());

        final ByteBuffer entryPoint = MemSysm.stack.UTF8("main");

        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.create(MemSysm.malloc(VkPipelineShaderStageCreateInfo.SIZEOF * 2L), 2).sType$Default();

        shaderStages.get(0).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .stage(VK_SHADER_STAGE_VERTEX_BIT)
                .module(vertShaderModule)
                .pName(entryPoint);

        shaderStages.get(1).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
                .module(fragShaderModule)
                .pName(entryPoint);


        VkPipelineVertexInputStateCreateInfo vkPipelineVertexInputStateCreateInfo = VkPipelineVertexInputStateCreateInfo.create(MemSysm.malloc(VkPipelineVertexInputStateCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                .pVertexBindingDescriptions(getVertexInputBindingDescription());
        //                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
        memPutAddress(vkPipelineVertexInputStateCreateInfo.address() + VkPipelineVertexInputStateCreateInfo.PVERTEXATTRIBUTEDESCRIPTIONS, (getAttributeDescriptions()));
        VkPipelineVertexInputStateCreateInfo.nvertexAttributeDescriptionCount(vkPipelineVertexInputStateCreateInfo.address(), 3);
        VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                .primitiveRestartEnable(false);
        /*todo: Fixed Viewport COnstruction/Initilaistaion?Configration: ([Had use wrong Function?method Veowpprt/Stagong function Calls/cfongurations e.g.])
         *(had also used vkViewport instead of VkViewport of Type Buffer which is the atcual correct Obejct/Stage/Steup.veiwport conponnat.consituent
         *
         * (CorretcioN: had actually also used viewportBuffer and not vkViewport(Of type VkViewport.Bufferand not VkViewPort....) in VkPipelineViewportStateCreateInfo as well)
         */
        VkViewport.Buffer vkViewport = VkViewport.create(MemSysm.malloc(VkViewport.SIZEOF), 1)
                .x(0.0F)
                .y(0.0F)
                .width(SwapChainSupportDetails.swapChainExtent.width())
                .height(SwapChainSupportDetails.swapChainExtent.height())
                .minDepth(0.0F)
                .maxDepth(1.0F);

        VkRect2D.Buffer scissor = VkRect2D.create(MemSysm.malloc(VkRect2D.SIZEOF), 1)
//                    .offset(vkOffset2D ->vkViewport.y()) //todo: not sure if correct Offset
                .offset(Buffers.set)
                .extent(SwapChainSupportDetails.swapChainExtent);

        VkPipelineViewportStateCreateInfo vkViewPortState = VkPipelineViewportStateCreateInfo.create(MemSysm.malloc3(VkPipelineViewportStateCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                .pViewports(vkViewport)
//                    .pScissors(vkrect2DBuffer);
                .pScissors(scissor);


        VkPipelineRasterizationStateCreateInfo VkPipeLineRasterization = VkPipelineRasterizationStateCreateInfo.create(MemSysm.malloc3(VkPipelineRasterizationStateCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                .depthClampEnable(false)
                .rasterizerDiscardEnable(false)
                .polygonMode(VK_POLYGON_MODE_FILL)
                .lineWidth(1.0f)
//                    .cullMode(VK_CULL_MODE_BACK_BIT)
//                    .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
                .depthBiasEnable(false);

        //todo: actuall need multismapling to Compleet.Initialsie.Construct.Substanciate the renderPipeline corretcly even if Antialsing /AF/MMs are not neeeded......
        VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.create(MemSysm.malloc3(VkPipelineMultisampleStateCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                .sampleShadingEnable(false)
                .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
//                    .alphaToOneEnable(false)
//                    .alphaToCoverageEnable(false);


        VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.create(MemSysm.mallocSafe(VkPipelineDepthStencilStateCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                .depthTestEnable(true)
                .depthWriteEnable(true)
                .depthCompareOp(VK_COMPARE_OP_LESS)
                .depthBoundsTestEnable(false)
//                    .minDepthBounds(0) //Optional
//                    .maxDepthBounds(1) //Optional
                .stencilTestEnable(false);


        VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.create(MemSysm.malloc(VkPipelineColorBlendStateCreateInfo.SIZEOF), 1)
                .colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT)
                //(Actually)Add blending?transparency to be suproted
                .blendEnable(true)
                .srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
//                    .dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
                .dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
//                    .colorBlendOp(VK_BLEND_OP_MAX)

//                    .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
//                    .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
//                    .alphaBlendOp(VK_BLEND_OP_ADD);

        VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.create(MemSysm.malloc3(VkPipelineColorBlendStateCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                .logicOpEnable(false)
                .logicOp(VK_LOGIC_OP_COPY)
                .pAttachments(colorBlendAttachment)
                .blendConstants(MemSysm.stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
//            memFree(colorBlendAttachment);

        VkPushConstantRange.Buffer vkPushConstantRange = VkPushConstantRange.create(MemSysm.malloc3(VkPipelineColorBlendStateCreateInfo.SIZEOF), 1)
                .offset(0)
                .size(16 * Float.BYTES)
                .stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
        VkPipelineLayoutCreateInfo vkPipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.create(MemSysm.malloc3(VkPipelineLayoutCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                .pPushConstantRanges(vkPushConstantRange);
        //                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
        PointerBuffer value = MemSysm.longs(UniformBufferObject.descriptorSetLayout);
        memPutAddress(vkPipelineLayoutCreateInfo.address() + VkPipelineLayoutCreateInfo.PSETLAYOUTS, value.address0());
        VkPipelineLayoutCreateInfo.nsetLayoutCount(vkPipelineLayoutCreateInfo.address(), value.remaining());


        System.out.println("using pipeLine with Length: " + SwapChainSupportDetails.swapChainImages.capacity());
        //nmemFree(vkPipelineLayoutCreateInfo1.address());
        MemSysm.Memsys2.doPointerAllocSafeX(vkPipelineLayoutCreateInfo, Buffers.capabilities.vkCreatePipelineLayout, Buffers.vkLayout);


        VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.create(MemSysm.malloc(VkGraphicsPipelineCreateInfo.SIZEOF), 1).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                .pStages(shaderStages)
                .pVertexInputState(vkPipelineVertexInputStateCreateInfo)
                .pInputAssemblyState(inputAssembly)
                .pViewportState(vkViewPortState)
                .pRasterizationState(VkPipeLineRasterization)
                .pMultisampleState(multisampling)
                .pDepthStencilState(depthStencil)
                .pColorBlendState(colorBlending)
//                    .pDynamicState(null)
                .layout(memGetLong(Buffers.vkLayout))
                .renderPass(memGetLong(SwapChainSupportDetails.renderPass))
                .subpass(0)
//                    .basePipelineHandle(VK_NULL_HANDLE)
                .basePipelineIndex(-1);
        shaderStages.free();
        colorBlending.free();
        pipelineInfo.free();
        vkPipelineVertexInputStateCreateInfo.free();
        inputAssembly.free();
//            MemSysm.Memsys2.free(vkViewPortState);
//            MemSysm.Memsys2.free(VkPipeLineRasterization);
//            MemSysm.Memsys2.free(multisampling);
        depthStencil.free();

        //Memsys2.free(entryPoint);
        Buffers.graphicsPipeline = MemSysm.doPointerAlloc5L(device, pipelineInfo);

        vkDestroyShaderModule(device, vertShaderModule, MemSysm.pAllocator);
        vkDestroyShaderModule(device, fragShaderModule, MemSysm.pAllocator);

        vertShaderSPIRV.free();
        fragShaderSPIRV.free();


    }

    static void createRenderPasses(boolean depthEnabled)
    {
        int capacity = 2;
        VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.create(MemSysm.address, capacity);
        VkAttachmentReference.Buffer attachmentsRefs = VkAttachmentReference.create(MemSysm.address, capacity);
        int abs;
//            if (!depthEnabled)
//            {
//                abs=VK_SUBPASS_EXTERNAL;
//            }
        //else
        abs = VK_SUBPASS_CONTENTS_INLINE;

        attachmentsRefs.get(0)
                .attachment(0)
                .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);


        attachments.get(0)
                .format(SwapChainSupportDetails.swapChainImageFormat)
                .samples(VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
//                    .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
//                    .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                .finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
        //TODO: WARn Possible FAIL!
        VkSubpassDescription.Buffer vkSubpassDescriptions = VkSubpassDescription.create(MemSysm.malloc(VkSubpassDescription.SIZEOF), 1)
                .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(1)
                .pColorAttachments(attachmentsRefs);
        VkAttachmentDescription depthAttachment = attachments.get(1)
                .format(Texture.findDepthFormat())
                .samples(VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                .finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);


        VkAttachmentReference depthAttachmentRef = attachmentsRefs.get(1)
                .attachment(1)
                .layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        memPutLong(vkSubpassDescriptions.address() + VkSubpassDescription.PDEPTHSTENCILATTACHMENT, memAddressSafe(depthAttachmentRef));


        VkSubpassDependency dependency = VkSubpassDependency.create(MemSysm.malloc(MemSysm.sizeof(depthAttachment.address())))
                .srcSubpass(abs)
                .dstSubpass(0)
                .srcStageMask(VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT)
                .srcAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT)
                .dstStageMask(VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT)
                .dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT)
                .dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);


        VkRenderPassCreateInfo vkRenderPassCreateInfo1 = VkRenderPassCreateInfo.create(MemSysm.calloc(VkRenderPassCreateInfo.SIZEOF)).sType$Default()
                .pAttachments(attachments)
                .pSubpasses(vkSubpassDescriptions);
        memPutLong(vkRenderPassCreateInfo1.address() + VkRenderPassCreateInfo.PDEPENDENCIES, dependency.address());
        memPutInt(vkRenderPassCreateInfo1.address() + VkRenderPassCreateInfo.DEPENDENCYCOUNT, 1);


        MemSysm.Memsys2.doPointerAllocSafeX(vkRenderPassCreateInfo1, Buffers.capabilities.vkCreateRenderPass, SwapChainSupportDetails.renderPass);
        MemSysm.Memsys2.free(dependency);
        MemSysm.Memsys2.free(vkRenderPassCreateInfo1);

    }

    static void createCommandPool()
    {
//            Queues.findQueueFamilies(Queues.physicalDevice);

        VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.create(MemSysm.calloc(VkCommandPoolCreateInfo.SIZEOF)).sType$Default()
                .queueFamilyIndex(Queues.graphicsFamily)
                .flags(0);
        //Memsys2.free(poolInfo);

        Buffers.commandPool = MemSysm.doPointerAllocSafe(poolInfo, Buffers.capabilities.vkCreateCommandPool);

    }


    private static VkVertexInputBindingDescription.@NotNull Buffer getVertexInputBindingDescription()
    {
        return VkVertexInputBindingDescription.create(MemSysm.malloc0(), 1)
                .binding(0)
//                    .stride(vertices.length/2)
//                    .stride(vertices.length/VERT_SIZE+1)
                .stride(Buffers.VERTICESSTRIDE)
                .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
    }

    private static long getAttributeDescriptions()
    {

        VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                VkVertexInputAttributeDescription.create(MemSysm.calloc(3, VkVertexInputAttributeDescription.SIZEOF), 3);

        // Position
        VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
        posDescription.binding(0);
        posDescription.location(0);
        posDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        posDescription.offset(OFFSET_POS);

        // Color
        VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
        colorDescription.binding(0);
        colorDescription.location(1);
        colorDescription.format(VK_FORMAT_R32G32B32_SFLOAT);
        colorDescription.offset(OFFSETOF_COLOR);

        // Texture coordinates
        VkVertexInputAttributeDescription texCoordsDescription = attributeDescriptions.get(2);
        texCoordsDescription.binding(0);
        texCoordsDescription.location(2);
        texCoordsDescription.format(VK_FORMAT_R32G32_SFLOAT);
        texCoordsDescription.offset(OFFSETOF_TEXTCOORDS);

//            memFree(attributeDescriptions);

        return attributeDescriptions.address0();
    }


}
