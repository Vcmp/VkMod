package vkutils.setup;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.stb.STBImage;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryUtil.memAddress0;
import static org.lwjgl.system.MemoryUtil.memGetLong;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.setup.Queues.device;

final class Texture {

    static void createTextureImage()
    {
        final String a = (Paths.get("").toAbsolutePath() + ("/shaders/terrain.png"));
        System.out.println(a);
        String filename = Paths.get(a).toString();
        System.out.println(filename);
        int[] pWidth = {0};
        int[] pHeight = {0};
        int[] pChannels = {0};
        ByteBuffer pixels = STBImage.stbi_load(filename, pWidth, pHeight, pChannels, STBImage.STBI_rgb_alpha);


        int imageSize = pWidth[0] * pHeight[0] * pChannels[0];

        if (pixels == null) {
            throw new RuntimeException("No Image!");
        }

        long[] stagingBufferImg = {0};
        Buffers.setBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, imageSize, stagingBufferImg, VK_SHARING_MODE_EXCLUSIVE);
        long[] stagingBufferMemoryImg = {0};
        Buffers.createBuffer(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferImg, stagingBufferMemoryImg);


        nvkMapMemory(device, stagingBufferMemoryImg[0], 0, imageSize, 0, MemSysm.address);
        {
            //                        memByteBuffer(getHandle(), imageSize).put(pixels);
            GLU2.theGLU.memcpy(memAddress0(pixels), MemSysm.getHandle(), imageSize);
        }
        vkUnmapMemory(device, stagingBufferMemoryImg[0]);
        STBImage.stbi_image_free(pixels);

        createImage(pWidth[0], pHeight[0],
                VK_FORMAT_R8G8B8A8_SRGB,
                VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                Buffers.vkImage
        );


        copyBufferToImage(stagingBufferImg, Buffers.vkImage, pWidth[0], pHeight[0]);
        transitionImageLayout(Buffers.vkImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

    }

    private static void copyBufferToImage(long @NotNull [] buffer, long image, int width, int height)
    {
        VkCommandBuffer commandBuffer = Buffers.beginSingleTimeCommands();
        VkBufferImageCopy region = VkBufferImageCopy.create(MemSysm.malloc3(VkBufferImageCopy.SIZEOF))
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0);
        region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .mipLevel(0)
                .baseArrayLayer(0)
                .layerCount(1);
        region.imageOffset().set(0, 0, 0);
        region.imageExtent(VkExtent3D.create(MemSysm.address).set(width, height, 1));
        nvkCmdCopyBufferToImage(
                commandBuffer,
                buffer[0],
                memGetLong(image),
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                1,
                region.address()
        );
        Buffers.endSingleTimeCommands(commandBuffer);

    }

    static void transitionImageLayout(long image, int format, int oldLayout, int newLayout)
    {
        VkCommandBuffer commandBuffer = Buffers.beginSingleTimeCommands();

        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.create(MemSysm.malloc(VkImageMemoryBarrier.SIZEOF), 1).sType$Default()
                //                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .oldLayout(oldLayout)
                .newLayout(newLayout)
                .srcQueueFamilyIndex(VK_IMAGE_LAYOUT_UNDEFINED)
                .dstQueueFamilyIndex(VK_IMAGE_LAYOUT_UNDEFINED)
                .image(memGetLong(image));
        barrier.subresourceRange()
                .aspectMask(format)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);
//            membarrier=barrier.address0();
        if (newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {


            if (hasStencilComponent(format)) {
                barrier.subresourceRange().aspectMask(
                        barrier.subresourceRange().aspectMask() | VK_IMAGE_ASPECT_STENCIL_BIT);
            }

        }

        final int sourceStage;
        final int destinationStage;
        switch (oldLayout) {
            case VK_IMAGE_LAYOUT_UNDEFINED -> barrier.srcAccessMask(KHRSynchronization2.VK_ACCESS_NONE_KHR);
            case VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL -> barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
            default -> throw new IllegalArgumentException("Unsupported layout transition");
        }
        switch (newLayout) {
            case VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL -> {
                barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);

                barrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT;
            }
            case VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL -> {
                barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
            }
            default -> throw new IllegalArgumentException("Unsupported layout transition");
        }
        MemSysm.Memsys2.free(barrier);
        vkCmdPipelineBarrier(
                commandBuffer,
                sourceStage /* TODO */, destinationStage /* TODO */,
                0,
                null,
                null,
                barrier);
        Buffers.endSingleTimeCommands(commandBuffer);


    }

    private static void createImage(int width, int height, int format, int usage, long pTextureImage)
    {
        VkImageCreateInfo imageInfo = VkImageCreateInfo.create(MemSysm.malloc(VkImageCreateInfo.SIZEOF))
                .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                .imageType(VK_IMAGE_TYPE_2D);
        imageInfo.extent().width(width)
                .height(height)
                .depth(1);
        imageInfo.mipLevels(1)
                .arrayLayers(1)
                .format(format)
                .tiling(VK_IMAGE_TILING_OPTIMAL)
                .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                .usage(usage)
                .samples(VK_SAMPLE_COUNT_1_BIT)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
        MemSysm.Memsys2.free(imageInfo);
        MemSysm.Memsys2.doPointerAllocSafeX(imageInfo, Buffers.capabilities.vkCreateImage, pTextureImage);
        VkMemoryDedicatedRequirementsKHR img2 = VkMemoryDedicatedRequirementsKHR.create(MemSysm.address).sType$Default();

        VkMemoryRequirements2 memRequirements = VkMemoryRequirements2.create(MemSysm.address).sType$Default();
        vkGetImageMemoryRequirements(device, memGetLong(pTextureImage), memRequirements.memoryRequirements());

        VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.create(MemSysm.malloc(VkMemoryAllocateInfo.SIZEOF))
                .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
//                    .pNext(vkMemoryDedicatedAllocateInfoKHR)
                .allocationSize(memRequirements.memoryRequirements().size())
                .memoryTypeIndex(Buffers.findMemoryType(memRequirements.memoryRequirements().memoryTypeBits(), VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));

        if (img2.prefersDedicatedAllocation() || img2.requiresDedicatedAllocation()) {
            System.out.println("Using Dedicated Memory Allocation");
            VkMemoryDedicatedAllocateInfo dedicatedAllocateInfoKHR = VkMemoryDedicatedAllocateInfo.create(MemSysm.malloc3(VkMemoryDedicatedAllocateInfo.SIZEOF)).sType$Default()
                    .image(memGetLong(pTextureImage));

            allocInfo.pNext(dedicatedAllocateInfoKHR);
        }

        MemSysm.Memsys2.free(allocInfo);
        MemSysm.Memsys2.doPointerAllocSafeX(allocInfo, Buffers.capabilities.vkAllocateMemory, Buffers.vkAllocMemory);

        vkBindImageMemory(device, memGetLong(pTextureImage), memGetLong(Buffers.vkAllocMemory), 0);

    }

    private static boolean hasStencilComponent(int format)
    {
        return format == VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK_FORMAT_D24_UNORM_S8_UINT;
    }

    static void createTextureImageView()
    {
        createImageView(Buffers.vkImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT, UniformBufferObject.textureImageView);
    }

    static void createDepthResources()
    {
        //            hasStencilComponent();
        int depthFormat = findDepthFormat();
        createImage(SwapChainSupportDetails.swapChainExtent.width(), SwapChainSupportDetails.swapChainExtent.height(),
                depthFormat,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                Buffers.vkImage
        );


        createImageView(Buffers.vkImage, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, Buffers.depthImageView);
        transitionImageLayout(Buffers.vkImage, depthFormat,
                VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
    }

    static int findDepthFormat()
    {
        return findSupportedFormat(
                MemSysm.ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT)
        );
    }

    private static int findSupportedFormat(@NotNull IntBuffer formatCandidates)
    {
        //TODO: WARN Possible FAIL!
        VkFormatProperties props = VkFormatProperties.create(MemSysm.address + 8);

        for (int i = 0; i < formatCandidates.capacity(); ++i) {

            int format = formatCandidates.get(i);

            vkGetPhysicalDeviceFormatProperties(Queues.physicalDevice, format, props);

            final int i2 = props.optimalTilingFeatures() & VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT;
            if (i2 == VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT/* && VK10.VK_IMAGE_TILING_OPTIMAL == VK_IMAGE_TILING_OPTIMAL*/) {
                return format;
            }
        }

        throw new RuntimeException("failed to find supported format!");
    }

    private static void createImageView(long i, int swapChainImageFormat, int vkImageAspect, long a)
    {
        VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.create(MemSysm.calloc(VkImageViewCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .image(memGetLong(i))
                .viewType(VK_IMAGE_VIEW_TYPE_2D)
                .format(swapChainImageFormat);

        createInfo.subresourceRange()
                .aspectMask(vkImageAspect)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);
//                    Memsys2.free(createInfo);//nmemFree(createInfo.address());

        MemSysm.Memsys2.doPointerAllocSafeX(createInfo, Buffers.capabilities.vkCreateImageView, a);
    }
}
