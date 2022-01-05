package vkutils.setup;

import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.setup.Queues.device;
import static vkutils.setup.Queues.surface;

public final class SwapChainSupportDetails {

    static final PointerBuffer swapChainFramebuffers = memPointerBuffer(MemSysm.malloc3(24), 3);
    public static final PointerBuffer swapChainImages = memPointerBuffer(MemSysm.malloc3(24), 3);
    public static final PointerBuffer swapChainImageViews = memPointerBuffer(MemSysm.malloc3(24), 3);
    static final long renderPass = MemSysm.malloc(0);
    static int swapChainImageFormat;
    public static VkExtent2D swapChainExtent;
    private static final VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.create(MemSysm.stack.getAddress());
    static VkSurfaceFormatKHR.Buffer formats;
    static IntBuffer presentModes;
    public static final long swapChain = MemSysm.malloc(0);


    static @NotNull VkExtent2D chooseSwapExtent()
    {
        if (capabilities.currentExtent().width() != 0xFFFFFFFF) {
            return capabilities.currentExtent();
        }

        VkExtent2D actualExtent = VkExtent2D.create(MemSysm.address).set(854, 480);

        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();

        actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));


        return actualExtent;
    }

    private static int clamp(int min, int max, int value)
    {
        return Math.max(min, Math.min(max, value));
    }

    static void createImageViews()
    {
        System.out.println("Creating Image Views");

        createImageView(swapChainImageFormat);


    }

    static void createImageView(int swapChainImageFormat)
    {

        VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.create(MemSysm.malloc3(VkSwapchainCreateInfoKHR.SIZEOF)).sType$Default()

                .viewType(VK_IMAGE_VIEW_TYPE_2D)
                .format(swapChainImageFormat);

        createInfo.subresourceRange()
                .aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1);
        for (int iu = 0; iu < PipeLine.swapChainImages.length; iu++) {

            swapChainImageViews.put(iu, MemSysm.doPointerAllocSafe(createInfo.image(PipeLine.swapChainImages[iu]), Buffers.capabilities.vkCreateImageView));
        }
    }

    static void querySwapChainSupport(VkPhysicalDevice device)
    {
        vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface[0], capabilities);

        IntBuffer count = memIntBuffer(MemSysm.address, 1);

        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface[0], count, null);

        if (count.get(0) != 0) {
            formats = VkSurfaceFormatKHR.malloc(count.get(0), MemSysm.stack);
            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface[0], count, formats);
        }

        vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface[0], count, null);

        if (count.get(0) != 0) {
            presentModes = MemSysm.stack.mallocInt(count.get(0));
            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface[0], count, presentModes);
        }

//            return details;
    }

    static void createFramebuffers()
    {


        {

            PointerBuffer attachments;
//               if(depthBuffer)
//               else
//                   attachments = stack.stack.longs(1);
            VkFramebufferAttachmentImageInfo.Buffer AttachmentImageInfo = VkFramebufferAttachmentImageInfo.create(MemSysm.calloc(VkFramebufferAttachmentImageInfo.SIZEOF * 2L), 2);
            AttachmentImageInfo.get(0).sType$Default()
                    .layerCount(1)
                    .width(swapChainExtent.width())
                    .height(swapChainExtent.height())
                    .usage(VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT)
                    .pViewFormats(MemSysm.ints(VK_FORMAT_R8G8B8A8_SRGB));
            AttachmentImageInfo.get(1).sType$Default()
                    .layerCount(1)
                    .width(swapChainExtent.width())
                    .height(swapChainExtent.height())
                    .usage(VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
                    .pViewFormats(MemSysm.ints(Texture.findDepthFormat()));

            VkFramebufferAttachmentsCreateInfo.create(MemSysm.calloc(VkFramebufferAttachmentsCreateInfo.SIZEOF)).sType$Default()
                    .pAttachmentImageInfos(AttachmentImageInfo);

            // Lets allocate the create info struct once and just update the pAttachments field each iteration
            //VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.createSafe(MemSysm.malloc(1, VkFramebufferCreateInfo.SIZEOF)).sType$Default()
            VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.create(MemSysm.malloc3(VkFramebufferCreateInfo.SIZEOF)).sType$Default()
//                      .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .renderPass(memGetLong(renderPass))
                    .width(swapChainExtent.width())
                    .height(swapChainExtent.height())
                    .layers(1);
            attachments = MemSysm.longs(swapChainImageFormat, memGetLong(Buffers.depthImageView));

            //                      .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
            memPutAddress(framebufferCreateInfo.address() + VkFramebufferCreateInfo.PATTACHMENTS, attachments.address());
            VkFramebufferCreateInfo.nattachmentCount(framebufferCreateInfo.address(), (attachments).remaining());
//                       .flags(VK12.VK_FRAMEBUFFER_CREATE_IMAGELESS_BIT)
//                        .pNext(vkFramebufferAttachmentsCreateInfo)

            //todo: Check only oneusbpass runing due to differing ColourDpetHFormats and isn;t coauong probelsm sude to Sumuetnous ComandBuffering nort requiinG fencing.Allowing for FenceSkip
            //memPutLong(framebufferCreateInfo.address() + VkFramebufferCreateInfo.PATTACHMENTS, memAddress0(attachments));
//               memPutInt(framebufferCreateInfo.address() + VkFramebufferCreateInfo.ATTACHMENTCOUNT, 1);
            //memPutInt(framebufferCreateInfo.address() + VkFramebufferCreateInfo.ATTACHMENTCOUNT, attachments.capacity());
            //Memsys2.free(framebufferCreateInfo);
            //nmemFree(framebufferCreateInfo.address());
            //TODO: warn Possible Fail!
            for (int i = 0; i < swapChainImageViews.capacity(); i++) {
                attachments.put(0, swapChainImageViews.get(i));


                swapChainFramebuffers.put(i, MemSysm.doPointerAllocSafe(framebufferCreateInfo/*.pNext(NULL)*/, Buffers.capabilities.vkCreateFramebuffer));
            }
        }
    }

    static void createSwapChain()
    {

        {

            querySwapChainSupport(Queues.physicalDevice);

            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(formats);
            int presentMode = chooseSwapPresentMode(presentModes);
            VkExtent2D extent = chooseSwapExtent();

            int imageCount = (capabilities.minImageCount() + 1);


            if (capabilities.maxImageCount() > 0 && imageCount > capabilities.maxImageCount()) {
                imageCount = capabilities.maxImageCount();
            }

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.create(MemSysm.malloc3(VkSwapchainCreateInfoKHR.SIZEOF))

                    .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface[0])

                    // Image settings
                    .minImageCount(imageCount)
                    .imageFormat(VkSurfaceFormatKHR.nformat(surfaceFormat.address()))
                    .imageColorSpace(surfaceFormat.colorSpace())
                    .imageExtent(extent)
                    .imageArrayLayers(1)
                    .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

//                Queues.findQueueFamilies(Queues.physicalDevice);

            if (!(Objects.equals(Queues.graphicsFamily, Queues.presentFamily))) {
                //VkSwapchainCreateInfoKHR.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
                createInfo.pQueueFamilyIndices(MemSysm.ints(Queues.graphicsFamily, Queues.presentFamily));
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(capabilities.currentTransform())
                    .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .presentMode(presentMode)
                    .clipped(true)

                    .oldSwapchain(VK_NULL_HANDLE);

            MemSysm.Memsys2.doPointerAllocSafeX(createInfo, Buffers.capabilities.vkCreateSwapchainKHR, swapChain);


//               callPJPPI(PipeLine.deviceAddress, swapChain, (imageCount), NULL, device.getCapabilities().vkGetSwapchainImagesKHR);

            long[] pSwapchainImages = new long[3];


            vkGetSwapchainImagesKHR(device, memGetLong(swapChain), new int[]{(imageCount)}, pSwapchainImages);

            System.arraycopy(pSwapchainImages, 0, PipeLine.swapChainImages, 0, pSwapchainImages.length);

            swapChainImageFormat = VkSurfaceFormatKHR.nformat(surfaceFormat.address());
            swapChainExtent = VkExtent2D.create().set(extent);
        }
    }

    private static VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.@NotNull Buffer availableFormats)
    {
        return availableFormats.stream()
                .filter(availableFormat -> VkSurfaceFormatKHR.nformat(availableFormat.address()) == VK_FORMAT_B8G8R8_UNORM && availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                .findAny()
                .orElse(availableFormats.get(0));
    }

    private static int chooseSwapPresentMode(@NotNull IntBuffer availablePresentModes)
    {

        for (int i = 0; i < availablePresentModes.capacity(); i++) {
            if (availablePresentModes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR) {
                return availablePresentModes.get(i);
            }
        }

        return VK_PRESENT_MODE_FIFO_KHR;
    }

}
