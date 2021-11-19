package vkutils;

import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.Checks.check;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.VkUtils2.Queues.device;
import static vkutils.VkUtils2.Queues.surface;

public final class VkUtils2 {
    static final MemSys MemSys = new MemSys(MemoryStack.stackPush(), null);
    static final GLFWVidMode.Buffer videoModes;
    static final GLFWVidMode videoMode;
    static final long window;
    //        static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);
           static final Set<String> DEVICE_EXTENSIONS=new HashSet<>();
    static VkInstance vkInstance;
    static final long[] pDebugMessenger = new long[1];
    static long debugMessenger;
    //    X(),
    //    Y
    //    KEY(1)
        static final long monitor=0;
    static PointerBuffer glfwExtensions;
    static final boolean ENABLE_VALIDATION_LAYERS = false;
    static final Set<String> VALIDATION_LAYERS;
    static final boolean debug = true;

    static {

//            Set<String> set = new HashSet<>();
            VkUtils2.DEVICE_EXTENSIONS.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
//            DEVICE_EXTENSIONS = set;


        Configuration.DISABLE_CHECKS.set(!debug);
        Configuration.DISABLE_FUNCTION_CHECKS.set(!debug);
        Configuration.DEBUG.set(debug);
        Configuration.DEBUG_FUNCTIONS.set(debug);
        Configuration.DEBUG_STREAM.set(debug);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(debug);
        Configuration.DEBUG_STACK.set(debug);
        Configuration.STACK_SIZE.set(4);
        Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(debug);
//        Configuration.VULKAN_EXPLICIT_INIT.set(true);

        System.setProperty("joml.fastmath", "true");
        System.setProperty("joml.useMathFma", "true");
        System.setProperty("joml.sinLookup", "true");


        if (ENABLE_VALIDATION_LAYERS) {
            VALIDATION_LAYERS = new HashSet<>();
            VALIDATION_LAYERS.add("VK_LAYER_KHRONOS_validation");
        } else VALIDATION_LAYERS = null;
        if(!glfwInit()) throw new RuntimeException("Cannot initialize GLFW");

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_ROBUSTNESS, GLFW_NO_ROBUSTNESS);
        glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
        glfwWindowHint(GLFW_CONTEXT_RELEASE_BEHAVIOR , GLFW_RELEASE_BEHAVIOR_NONE);


        window = glfwCreateWindow(854, 480, " ", monitor, 0);


        if(window == NULL) throw new RuntimeException("Cannot create window");

        glfwSetWindowShouldClose(window, false);
        glfwMakeContextCurrent(window);

        videoModes=glfwGetVideoModes(window);
        videoMode=glfwGetVideoMode(window);
    }

    static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {

        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

        System.err.println("Validation layer: " + callbackData.pMessageString()+"Object Pointer: "+ callbackData.objectCount());

        return VK_FALSE;
    }

    private static int createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfo) {

        if(vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != NULL) {
            return vkCreateDebugUtilsMessengerEXT(instance, createInfo, MemSys.pAllocator(), VkUtils2.pDebugMessenger);
        }

        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    //Stolen From Here: https://github.com/Naitsirc98/Vulkan-Tutorial-Java/blob/master/src/main/java/javavulkantutorial/Ch02ValidationLayers.java
    private static boolean checkValidationLayerSupport() {

        {
            //todo:fix Intbuffer being inlined eincirertcly and malloc one Int.value eachat eahc method call/usage incorrectly, cauisng validtaion layerst be broken/which in tandme iwth teh 443.41 dirver bug where valditaion layers do nto function corertcly untill full farembuffer/Piplelinebuffer>Framebuffer is implemneted and others e.g. etc i.e.
            final IntBuffer ints = MemSys.stack().ints(0);
            VK10.vkEnumerateInstanceLayerProperties(ints, null);
            VkLayerProperties.Buffer availableLayers = VkLayerProperties.malloc(ints.get(0), MemSys.stack());
            VK10.vkEnumerateInstanceLayerProperties(ints, availableLayers);
            Set<String> availableLayerNames = availableLayers.stream()
                    .map(VkLayerProperties::layerNameString)
                    .collect(toSet());
            for (int i = 0; i < availableLayerNames.size(); i++) {
                System.out.println(Arrays.toString(availableLayerNames.iterator().next().getBytes(StandardCharsets.UTF_8)));
            }


            return availableLayerNames.containsAll(VALIDATION_LAYERS);
        }
    }

    static void createInstance()
    {
        System.out.println("Creating Instance");
        if (ENABLE_VALIDATION_LAYERS && !checkValidationLayerSupport())
        {
            System.out.println(MemSys.stack());
            throw new RuntimeException("Validation requested but not supported");
        }

        long vkApplInfo = vkutils.MemSys.Memsys2.calloc(1, VkApplicationInfo.SIZEOF);
        memSet(vkApplInfo, 0,VkApplicationInfo.SIZEOF);
        VkApplicationInfo.nsType(vkApplInfo, VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO);
        VkApplicationInfo.npApplicationName(vkApplInfo, MemSys.stack().UTF8Safe(" "));
        VkApplicationInfo.napplicationVersion(vkApplInfo, VK10.VK_MAKE_VERSION(1, 0, 0));
        VkApplicationInfo.npEngineName(vkApplInfo, MemSys.stack().UTF8Safe("No Engine"));
        VkApplicationInfo.nengineVersion(vkApplInfo, VK10.VK_MAKE_VERSION(1, 0, 0));
        VkApplicationInfo.napiVersion(vkApplInfo, VK10.VK_API_VERSION_1_0);
        //nmemFree(vkApplInfo);


        VkInstanceCreateInfo InstCreateInfo = VkInstanceCreateInfo.create(vkutils.MemSys.Memsys2.malloc(1, VkInstanceCreateInfo.SIZEOF)).sType$Default();
//                InstCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
        memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.PAPPLICATIONINFO, vkApplInfo);
        glfwExtensions = getRequiredExtensions();
                InstCreateInfo.ppEnabledExtensionNames(glfwExtensions);

        //nmemFree(InstCreateInfo.address());

        int enabledExtensionCount = InstCreateInfo.enabledExtensionCount();
        int enabledLayerCount = InstCreateInfo.enabledLayerCount();
        if(ENABLE_VALIDATION_LAYERS) {
            memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.PPENABLEDLAYERNAMES, memAddress(asPointerBuffer()));
            memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.ENABLEDLAYERCOUNT, VkUtils2.VALIDATION_LAYERS.size());
            VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.create(vkutils.MemSys.Memsys2.calloc(1, VkDebugUtilsMessengerCreateInfoEXT.SIZEOF)).sType$Default();
//            debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
            debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
            debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
            debugCreateInfo.pfnUserCallback(VkUtils2::debugCallback);
            InstCreateInfo.pNext(debugCreateInfo.address());
            //nmemFree(debugCreateInfo.address());
            vkutils.MemSys.Memsys2.free(debugCreateInfo.address());
            }
//            else InstCreateInfo.pNext(NULL);

        PointerBuffer instancePtr = memPointerBuffer(MemSys.stack().nmalloc(Pointer.POINTER_SIZE, 1 << Pointer.POINTER_SHIFT), 1);
                VK10.vkCreateInstance(InstCreateInfo, MemSys.pAllocator(), instancePtr);


        vkInstance = new VkInstance(instancePtr.get(0), InstCreateInfo);
    }

    static void setupDebugMessenger()
    {
        if(!ENABLE_VALIDATION_LAYERS) {
            return;
        }

        VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.create(vkutils.MemSys.Memsys2.malloc(1, VkDebugUtilsMessengerCreateInfoEXT.SIZEOF)).sType$Default();
//        createInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        createInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
        createInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
        createInfo.pfnUserCallback(VkUtils2::debugCallback);
        vkutils.MemSys.Memsys2.free(createInfo.address());//nmemFree(createInfo.address());

        if(createDebugUtilsMessengerEXT(vkInstance, createInfo) != VK_SUCCESS)
            throw new RuntimeException("Failed to set up debug messenger");
        debugMessenger = pDebugMessenger[0];
    }

    private static boolean isDeviceSuitable(VkPhysicalDevice device) {

        boolean extensionsSupported = checkDeviceExtensionSupport(device);
        boolean swapChainAdequate = false;
        if(extensionsSupported) {
            SwapChainSupportDetails.querySwapChainSupport(device);
            swapChainAdequate = SwapChainSupportDetails.formats.hasRemaining() && SwapChainSupportDetails.presentModes.hasRemaining();
        }

        Queues.findQueueFamilies(device);
        return Queues.isComplete() && extensionsSupported && swapChainAdequate;
    }

    static boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {
        IntBuffer extensionCount = MemSys.stack().ints(0);
        vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, null);
        VkExtensionProperties.Buffer availableExtensions= VkExtensionProperties.malloc(extensionCount.get(0), MemSys.stack());
        vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, availableExtensions);

        return availableExtensions.stream()
                .map(VkExtensionProperties::extensionNameString)
                .collect(toSet())
                .containsAll(DEVICE_EXTENSIONS);
    }

    private static PointerBuffer getRequiredExtensions() {

        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();
        if(ENABLE_VALIDATION_LAYERS)
        {
            int size = glfwExtensions.capacity() + 1;
            PointerBuffer extensions = memPointerBuffer(MemSys.malloc(size << Pointer.POINTER_SHIFT), size);
            extensions.put(glfwExtensions);
            extensions.put(MemSys.stack().UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
            // Rewind the buffer before returning it to reset its position back to 0
            return extensions.rewind();
        }

        return glfwExtensions;
    }

    private static void pickPhysicalDevice()
    {
        System.out.println("Picking Physical Device");


        IntBuffer deviceCount = MemSys.stack().ints(0);
        VK10.vkEnumeratePhysicalDevices(vkInstance, deviceCount, null);
        if(deviceCount.get(0) == 0) throw new RuntimeException("Failed to find GPUs with Vulkan support");
        int size = deviceCount.get(0);
        PointerBuffer ppPhysicalDevices = memPointerBuffer(vkutils.MemSys.Memsys2.calloc(1, size), size);
        VK10.vkEnumeratePhysicalDevices(vkInstance, deviceCount, ppPhysicalDevices);
        for(int i = 0;i < ppPhysicalDevices.capacity();i++)
        {
            VkPhysicalDevice device = new VkPhysicalDevice(ppPhysicalDevices.get(i), vkInstance);
            if(isDeviceSuitable(device)) {
                vkutils.MemSys.Memsys2.free(ppPhysicalDevices.address());
                Queues.physicalDevice = device;
                return;
            }
        }
        throw new RuntimeException("Failed to find a suitable GPU");
    }

    public static void createSurface()
    {
        System.out.println("Creating Surface");

        long createSurfaceInfo = vkutils.MemSys.Memsys2.malloc(1, VkWin32SurfaceCreateInfoKHR.SIZEOF);
        VkWin32SurfaceCreateInfoKHR.nsType(createSurfaceInfo, VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR);
        VkWin32SurfaceCreateInfoKHR.nhwnd(createSurfaceInfo, glfwGetWin32Window(window));
        VkWin32SurfaceCreateInfoKHR.nhinstance(createSurfaceInfo, vkInstance.address());
//            nmemFree(createSurfaceInfo);

        long[] surface_= {vkutils.MemSys.Memsys2.malloc(1, VK_NULL_HANDLE)};
        vkutils.MemSys.Memsys2.free(createSurfaceInfo);
        if (GLFWVulkan.glfwCreateWindowSurface(vkInstance, window, MemSys.pAllocator(), surface_) != VK_SUCCESS) throw new RuntimeException("failed to create window surface!");

        surface = surface_[0];

    }

    private static @NotNull PointerBuffer asPointerBuffer() {


        int size = VALIDATION_LAYERS.size();
        PointerBuffer buffer = memPointerBuffer(vkutils.MemSys.Memsys2.malloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT), size);

        for (String s : VALIDATION_LAYERS) {
            ByteBuffer byteBuffer = MemSys.stack().UTF8(s);
            buffer.put(byteBuffer);
        }

        return buffer.rewind();

    }

    static void extracted()
    {
        createInstance();
        setupDebugMessenger();
        createSurface();
        pickPhysicalDevice();
        VKUtilsSafe.createLogicalDevice();
        SwapChainSupportDetails.createSwapChain();
        SwapChainSupportDetails.createImageViews();
        PipeLine.createRenderPasses(true);
        renderer2.Buffers.createDescriptorSetLayout();
        PipeLine.createGraphicsPipelineLayout();

        PipeLine.createCommandPool();
        PipeLine.createDepthResources();
        SwapChainSupportDetails.createFramebuffers();
        PipeLine.createTextureImage();
        PipeLine.createTextureImageView();
        PipeLine.createTextureSampler();
        renderer2.Buffers.createVertexBufferStaging();
        renderer2.Buffers.createIndexBuffer();
        renderer2.Buffers.creanupBufferStaging();
        renderer2.UniformBufferObject.createUniformBuffers();
        renderer2.UniformBufferObject.createDescriptorPool();
        renderer2.UniformBufferObject.createDescriptorSets();
        renderer2.Buffers.createCommandBuffers();
        MemSys.stack().pop();
    }


    static final class SwapChainSupportDetails
    {

        static final long[]  swapChainFramebuffers = new long[2];
        static final long[] swapChainImages = new long[2];
        static final long[] swapChainImageViews = new long[2];
        static final long[] renderPass= {0};
        private static int swapChainImageFormat;
        static VkExtent2D swapChainExtent;
        private static final VkSurfaceCapabilitiesKHR   capabilities = VkSurfaceCapabilitiesKHR.malloc(MemSys.stack());
        private static  VkSurfaceFormatKHR.Buffer formats;
        private static IntBuffer presentModes;
        static long[] swapChain={0};
//        static boolean depthBuffer = true;
        //            swapChainImageViews = new ArrayList<>(swapChainImages.length);


        static @NotNull VkExtent2D chooseSwapExtent() {
            if (SwapChainSupportDetails.capabilities.currentExtent().width() != 0xFFFFFFFF) {
                return SwapChainSupportDetails.capabilities.currentExtent();
            }

            VkExtent2D actualExtent = VkExtent2D.malloc().set(854, 480);

            VkExtent2D minExtent = SwapChainSupportDetails.capabilities.minImageExtent();
            VkExtent2D maxExtent = SwapChainSupportDetails.capabilities.maxImageExtent();

            actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
            actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));


            return actualExtent;
        }

        private static int clamp(int min, int max, int value) {
            return Math.max(min, Math.min(max, value));
        }

        public static void createImageViews()
        {
            System.out.println("Creating Image Views");

            for (int i = 0; i < PipeLine.swapChainImages.length; i++) {

//                swapChainImageViews[i]=PipeLine.createImageView(swapChainImages[i], swapChainImageFormat);

                swapChainImageViews[i]= createImageView(PipeLine.swapChainImages[i], swapChainImageFormat);
            }


        }

        static void createImageView(long[] i, int swapChainImageFormat, int vkImageAspect, long[] a) {
            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.create(vkutils.MemSys.Memsys2.calloc(1, VkImageViewCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(i[0])
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(swapChainImageFormat);

            createInfo.subresourceRange()
                    .aspectMask(vkImageAspect)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);
                    vkutils.MemSys.Memsys2.free(createInfo.address());//nmemFree(createInfo.address());

            vkutils.MemSys.Memsys2.doPointerAllocSafe2(createInfo, renderer2.Buffers.capabilities.vkCreateImageView, a);
        }
        static long createImageView(long i, int swapChainImageFormat) {
            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.create(vkutils.MemSys.Memsys2.calloc(1, VkImageViewCreateInfo.SIZEOF)).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                    .image(i)
                    .viewType(VK_IMAGE_VIEW_TYPE_2D)
                    .format(swapChainImageFormat);

            createInfo.subresourceRange()
                    .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);
            vkutils.MemSys.Memsys2.free(createInfo.address());//nmemFree(createInfo.address());

            return vkutils.MemSys.doPointerAllocSafe(createInfo, renderer2.Buffers.capabilities.vkCreateImageView);
        }

        private static void querySwapChainSupport(VkPhysicalDevice device)
        {
//            SwapChainSupportDetails  details = new SwapChainSupportDetails();


            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, capabilities);

            IntBuffer count = memIntBuffer(MemSys.stack().getAddress(), 1);

            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);

            if(count.get(0) != 0) {
                formats = VkSurfaceFormatKHR.malloc(count.get(0), MemSys.stack());
                vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, formats);
            }

            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null);

            if(count.get(0) != 0) {
                presentModes = MemSys.stack().mallocInt(count.get(0));
                vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, presentModes);
            }

//            return details;
        }

       private static void createFramebuffers()
        {



           {

               LongBuffer attachments;
//               if(depthBuffer)
                   attachments = MemSys.stack().longs(VK_NULL_HANDLE, renderer2.Buffers.depthImageView[0]);
//               else
//                   attachments = stack.stack().longs(1);


               // Lets allocate the create info struct once and just update the pAttachments field each iteration
               //VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.createSafe(MemSys.malloc(1, VkFramebufferCreateInfo.SIZEOF)).sType$Default()
               VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.create(vkutils.MemSys.Memsys2.calloc(1, VkFramebufferCreateInfo.SIZEOF)).sType$Default()
//                       .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                       .renderPass(renderPass[0])
                       .width(swapChainExtent.width())
                       .height(swapChainExtent.height())
                       .layers(1);
//                       .pAttachments(attachments)
//                       .attachmentCount(attachments.capacity());

               //todo: Check only oneusbpass runing due to differing ColourDpetHFormats and isn;t coauong probelsm sude to Sumuetnous ComandBuffering nort requiinG fencing.Allowing for FenceSkip
               memPutLong(framebufferCreateInfo.address() + VkFramebufferCreateInfo.PATTACHMENTS, memAddress0(attachments));
//               memPutInt(framebufferCreateInfo.address() + VkFramebufferCreateInfo.ATTACHMENTCOUNT, 1);
               memPutInt(framebufferCreateInfo.address() + VkFramebufferCreateInfo.ATTACHMENTCOUNT, attachments.capacity());

                //nmemFree(framebufferCreateInfo.address());
               //TODO: warn Possible Fail!
               for (int i = 0; i < swapChainImageViews.length; i++) {

                   attachments.put(0, swapChainImageViews[i]);


                   swapChainFramebuffers[i] = vkutils.MemSys.doPointerAllocSafe(framebufferCreateInfo/*.pNext(NULL)*/, renderer2.Buffers.capabilities.vkCreateFramebuffer);
               }
            }
        }

        static void createSwapChain() {

           {

               querySwapChainSupport(Queues.physicalDevice);

                VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(formats);
                int presentMode = chooseSwapPresentMode(presentModes);
                VkExtent2D extent = chooseSwapExtent();

                int imageCount = (capabilities.minImageCount()/* + 1*/);
//                int[] imageCount = {(SwapChainSupportDetails.capabilities.minImageCount()/* + 1*/)};


                if(capabilities.maxImageCount() > 0 && imageCount > capabilities.maxImageCount()) {
                    imageCount= capabilities.maxImageCount();
                }

               VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.create(vkutils.MemSys.Memsys2.malloc(1, VkSwapchainCreateInfoKHR.SIZEOF))

                       .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
               .surface(surface)

               // Image settings
               .minImageCount(imageCount)
               .imageFormat(VkSurfaceFormatKHR.nformat(surfaceFormat.address()))
               .imageColorSpace(surfaceFormat.colorSpace())
               .imageExtent(extent)
               .imageArrayLayers(1)
               .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

               Queues.findQueueFamilies(Queues.physicalDevice);

                if(!(Queues.graphicsFamily ==Queues.presentFamily)) {
                    //VkSwapchainCreateInfoKHR.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                    createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                    createInfo.pQueueFamilyIndices(MemSys.stack().ints(Queues.graphicsFamily, Queues.presentFamily));
                } else {
                    createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
                }

               createInfo.preTransform(capabilities.currentTransform())
               .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
               .presentMode(presentMode)
               .clipped(true)

               .oldSwapchain(VK_NULL_HANDLE);

               vkutils.MemSys.Memsys2.doPointerAllocSafe2(createInfo, renderer2.Buffers.capabilities.vkCreateSwapchainKHR, swapChain);



//               callPJPPI(PipeLine.deviceAddress, swapChain, (imageCount), NULL, device.getCapabilities().vkGetSwapchainImagesKHR);

               long[] pSwapchainImages = new long[2];


               KHRSwapchain.vkGetSwapchainImagesKHR(device, swapChain[0], new int[]{(imageCount)}, pSwapchainImages);

               System.arraycopy(pSwapchainImages, 0, PipeLine.swapChainImages, 0, pSwapchainImages.length);

               swapChainImageFormat = VkSurfaceFormatKHR.nformat(surfaceFormat.address());
               swapChainExtent = VkExtent2D.create().set(extent);
            }
        }

        private static VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.@NotNull Buffer availableFormats) {
            return availableFormats.stream()
                    .filter(availableFormat -> VkSurfaceFormatKHR.nformat(availableFormat.address()) == VK_FORMAT_B8G8R8_UNORM && availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                    .findAny()
                    .orElse(availableFormats.get(0));
        }

        private static int chooseSwapPresentMode(@NotNull IntBuffer availablePresentModes) {

            for(int i = 0;i < availablePresentModes.capacity();i++) {
                if(availablePresentModes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR) {
                    return availablePresentModes.get(i);
                }
            }

            return VK_PRESENT_MODE_FIFO_KHR;
        }

        /*private static void querySwapChainSupport(VkPhysicalDevice device) {

//            SwapChainSupportDetails details = new SwapChainSupportDetails();

//            details.capabilities = VkSurfaceCapabilitiesKHR.malloc()(VKUtilsSafe.stack.stack());
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, capabilities);

            IntBuffer count = stack.stack().ints(0);

            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);

            if(count.get(0) != 0) {
                formats = VkSurfaceFormatKHR.malloc(count.get(0), stack.stack());
                KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, formats);
            }

            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null);

            if(count.get(0) != 0) {
                presentModes = stack.stack().mallocInt(count.get(0));
                KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, presentModes);
            }

//            return details;
        }*/

    }

    public static final class PipeLine {


        //Manipulate indicies to use indexes more likley to be dulcitaed when.if cases of adjency occur to allow vereycoes to be shared between Blocks/Quads COmposits.Hybrids
        static final short[] indicesTemp ={

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
        static final long[] swapChainImages = new long[2];
        private static final int OFFSETOF_COLOR = 3 * Float.BYTES;
        private static final int OFFSET_POS = 0;

        private static final int OFFSETOF_TEXTCOORDS = (3+3) * Float.BYTES;

//            nvkMapMemory(Queues.device, stagingBufferMemory, 0, SIZEOFIn, 0, address);

        //        static final int SIZEOFIn = Short.BYTES * renderer.Buffers.indices.length;

        //            vertexBuffers = stack.stack().longs(PipeLine.vertexBuffer);
//            stagingBuffer = setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);


        public static void createGraphicsPipelineLayout() {
            System.out.println("Setting up PipeLine");

            ShaderSPIRVUtils.SPIRV vertShaderSPIRV = ShaderSPIRVUtils.compileShaderFile("shaders/21_shader_ubo.vert", ShaderSPIRVUtils.ShaderKind.VERTEX_SHADER);
            ShaderSPIRVUtils.SPIRV fragShaderSPIRV = ShaderSPIRVUtils.compileShaderFile("shaders/21_shader_ubo.frag", ShaderSPIRVUtils.ShaderKind.FRAGMENT_SHADER);

            long vertShaderModule = createShaderModule(vertShaderSPIRV.bytecode());
            long fragShaderModule = createShaderModule(fragShaderSPIRV.bytecode());

            ByteBuffer entryPoint = MemSys.stack().UTF8("main");

            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2).sType$Default();

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


            VkPipelineVertexInputStateCreateInfo vkPipelineVertexInputStateCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                    .pVertexBindingDescriptions(getVertexInputBindingDescription())
                            .pVertexAttributeDescriptions(getAttributeDescriptions());
//            memPutLong(vkPipelineVertexInputStateCreateInfo.address() + VkPipelineVertexInputStateCreateInfo.PVERTEXATTRIBUTEDESCRIPTIONS, getAttributeDescriptions());
            //VkPipelineVertexInputStateCreateInfo.nvertexAttributeDescriptionCount(vkPipelineVertexInputStateCreateInfo.address(), 3);
            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                    .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                    .primitiveRestartEnable(false);
            /*todo: Fixed Viewport COnstruction/Initilaistaion?Configration: ([Had use wrong Function?method Veowpprt/Stagong function Calls/cfongurations e.g.])
             *(had also used vkViewport instead of VkViewport of Type Buffer which is the atcual correct Obejct/Stage/Steup.veiwport conponnat.consituent
             *
             * (CorretcioN: had actually also used viewportBuffer and not vkViewport(Of type VkViewport.Bufferand not VkViewPort....) in VkPipelineViewportStateCreateInfo as well)
             */
            VkViewport.Buffer vkViewport = VkViewport.malloc(1)
                    .x(0.0F)
                    .y(0.0F)
                    .width(SwapChainSupportDetails.swapChainExtent.width())
                    .height(SwapChainSupportDetails.swapChainExtent.height())
                    .minDepth(0.0F)
                    .maxDepth(1.0F);

            VkRect2D.Buffer scissor = VkRect2D.malloc(1)
//                    .offset(vkOffset2D ->vkViewport.y()) //todo: not sure if correct Offset
                    .offset(renderer2.Buffers.set)
                    .extent(SwapChainSupportDetails.swapChainExtent);

            VkPipelineViewportStateCreateInfo vkViewPortState = VkPipelineViewportStateCreateInfo.calloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                    .pViewports(vkViewport)
//                    .pScissors(vkrect2DBuffer);
                    .pScissors(scissor);


            VkPipelineRasterizationStateCreateInfo VkPipeLineRasterization = VkPipelineRasterizationStateCreateInfo.calloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                    .depthClampEnable(false)
                    .rasterizerDiscardEnable(false)
                    .polygonMode(VK_POLYGON_MODE_FILL)
                    .lineWidth(1.0f)
//                    .cullMode(VK_CULL_MODE_BACK_BIT)
//                    .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
                    .depthBiasEnable(false);

            //todo: actuall need multismapling to Compleet.Initialsie.Construct.Substanciate the renderPipeline corretcly even if Antialsing /AF/MMs are not neeeded......
            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                    .sampleShadingEnable(false)
                    .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
//                    .alphaToOneEnable(false)
//                    .alphaToCoverageEnable(false);


            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                    .depthTestEnable(true)
                    .depthWriteEnable(true)
                    .depthCompareOp(VK_COMPARE_OP_LESS)
                    .depthBoundsTestEnable(false)
//                    .minDepthBounds(0) //Optional
//                    .maxDepthBounds(1) //Optional
                    .stencilTestEnable(false);


            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.malloc(1)
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

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.malloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                    .logicOpEnable(false)
                    .logicOp(VK_LOGIC_OP_COPY)
                    .pAttachments(colorBlendAttachment)
                    .blendConstants(MemSys.stack().floats(0.0f, 0.0f, 0.0f, 0.0f));
            memFree(colorBlendAttachment);
            memFree(vkViewport);
            memFree(scissor);
            VkPushConstantRange.Buffer vkPushConstantRange = VkPushConstantRange.calloc(1, VkUtils2.MemSys.stack())
                    .offset(0)
                    .size(16*Float.BYTES)
                    .stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            VkPipelineLayoutCreateInfo vkPipelineLayoutCreateInfo1 = VkPipelineLayoutCreateInfo.malloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                    .pPushConstantRanges(vkPushConstantRange)
                    .pSetLayouts(MemSys.stack().longs(renderer2.UniformBufferObject.descriptorSetLayout));


            System.out.println("using pipeLine with Length: " + SwapChainSupportDetails.swapChainImages.length);
            nmemFree(vkPipelineLayoutCreateInfo1.address());
            vkutils.MemSys.Memsys2.doPointerAllocSafe2(vkPipelineLayoutCreateInfo1, renderer2.Buffers.capabilities.vkCreatePipelineLayout, renderer2.Buffers.vkLayout);


            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.create(vkutils.MemSys.Memsys2.malloc(1, VkGraphicsPipelineCreateInfo.SIZEOF),1).sType$Default()
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
                    .layout(renderer2.Buffers.vkLayout[0])
                    .renderPass(SwapChainSupportDetails.renderPass[0])
                    .subpass(0)
//                    .basePipelineHandle(VK_NULL_HANDLE)
                    .basePipelineIndex(-1);
            memFree(shaderStages);
            nmemFree(colorBlending.address());
            //memFree(pipelineInfo);
            nmemFree(vkPipelineVertexInputStateCreateInfo.address());
            nmemFree(inputAssembly.address());

            nmemFree(vkViewPortState.address());
            nmemFree(VkPipeLineRasterization.address());
            nmemFree(multisampling.address());
            nmemFree(depthStencil.address());

            vkutils.MemSys.Memsys2.free(entryPoint);
            renderer2.Buffers.graphicsPipeline =doPointerAlloc5L(device, pipelineInfo);
            vkutils.MemSys.Memsys2.free(pipelineInfo.address0());

            vkDestroyShaderModule(device, vertShaderModule, MemSys.pAllocator());
            vkDestroyShaderModule(device, fragShaderModule, MemSys.pAllocator());

            vertShaderSPIRV.free();
            fragShaderSPIRV.free();


        }

        private static long doPointerAlloc5L(VkDevice device, VkGraphicsPipelineCreateInfo.Buffer pipelineInfo)
        {
            Checks.check(pipelineInfo.address0());
            vkutils.MemSys.checkCall(callPJPPPI(device.address(), VK10.VK_NULL_HANDLE, pipelineInfo.remaining(), pipelineInfo.address0(), NULL, vkutils.MemSys.pDummyPlacementPointerAlloc, renderer2.Buffers.capabilities.vkCreateGraphicsPipelines));
            return vkutils.MemSys.pDummyPlacementPointerAlloc[0];
        }

        private static long createShaderModule(ByteBuffer spirvCode) {

            {

                return vkutils.MemSys.doPointerAllocSafe(VkShaderModuleCreateInfo.calloc(MemSys.stack()).sType$Default().pCode(spirvCode), renderer2.Buffers.capabilities.vkCreateShaderModule);
            }
        }

        public static void createRenderPasses(boolean depthEnabled) {
            int capacity = 2;
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.create(vkutils.MemSys.Memsys2.calloc(capacity, VkAttachmentDescription.SIZEOF), capacity);
            VkAttachmentReference.Buffer attachmentsRefs =VkAttachmentReference.create(vkutils.MemSys.Memsys2.calloc(capacity, VkAttachmentReference.SIZEOF), capacity);


            VkAttachmentReference colourAttachmentRef = attachmentsRefs.get(0)
                    .attachment(0)
                    .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
//                    .set()


            VkAttachmentDescription colourAttachment = attachments.get(0)
                    .format(SwapChainSupportDetails.swapChainImageFormat)
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
//                    .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
//                    .stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
            //TODO: WARn Possible FAIL!
            VkSubpassDescription vkSubpassDescriptions = VkSubpassDescription.create(vkutils.MemSys.Memsys2.calloc(2, VkSubpassDescription.SIZEOF))
                    .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            vkSubpassDescriptions.colorAttachmentCount(1);
            memPutLong(vkSubpassDescriptions.address() + VkSubpassDescription.PCOLORATTACHMENTS, colourAttachmentRef.address());
            if(depthEnabled) {
                VkAttachmentDescription depthAttachment = attachments.get(1)
                        .format(findDepthFormat())
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
            }





            VkSubpassDependency dependency = VkSubpassDependency.create(vkutils.MemSys.Memsys2.calloc(1, VkSubpassDependency.SIZEOF))
                    .srcSubpass(VK_SUBPASS_EXTERNAL)
                    .dstSubpass(0)
                    .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .srcAccessMask(0)
                    .dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);


            VkRenderPassCreateInfo vkRenderPassCreateInfo1 = VkRenderPassCreateInfo.create(vkutils.MemSys.Memsys2.calloc(1, VkRenderPassCreateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            memPutAddress(vkRenderPassCreateInfo1.address() + VkRenderPassCreateInfo.PATTACHMENTS, attachments.address0());
            VkRenderPassCreateInfo.nattachmentCount(vkRenderPassCreateInfo1.address(), capacity);
            memPutLong(vkRenderPassCreateInfo1.address() + VkRenderPassCreateInfo.PSUBPASSES, vkSubpassDescriptions.address());
            VkRenderPassCreateInfo.nsubpassCount(vkRenderPassCreateInfo1.address(), 1);
            memPutLong(vkRenderPassCreateInfo1.address() + VkRenderPassCreateInfo.PDEPENDENCIES, dependency.address());
            memPutInt(vkRenderPassCreateInfo1.address() + VkRenderPassCreateInfo.DEPENDENCYCOUNT, 1);


            vkutils.MemSys.Memsys2.doPointerAllocSafe2(vkRenderPassCreateInfo1, renderer2.Buffers.capabilities.vkCreateRenderPass, SwapChainSupportDetails.renderPass);
//            memFree(attachments);
//            memFree(attachmentsRefs);


        }

        public static void createCommandPool() {
            Queues.findQueueFamilies(Queues.physicalDevice);

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.create(vkutils.MemSys.Memsys2.calloc(1, VkCommandPoolCreateInfo.SIZEOF)).sType$Default()
                    .queueFamilyIndex(Queues.graphicsFamily)
                    .flags(0);
            vkutils.MemSys.Memsys2.free(poolInfo.address());

            vkutils.MemSys.Memsys2.doPointerAllocSafe2(poolInfo, renderer2.Buffers.capabilities.vkCreateCommandPool, renderer2.Buffers.commandPool);

        }


        private static VkVertexInputBindingDescription.@NotNull Buffer getVertexInputBindingDescription() {
            return VkVertexInputBindingDescription.create(vkutils.MemSys.Memsys2.calloc(1, VkVertexInputAttributeDescription.SIZEOF),1)
                    .binding(0)
//                    .stride(vertices.length/2)
//                    .stride(vertices.length/VERT_SIZE+1)
                    .stride(renderer2.Buffers.VERTICESSTRIDE)
                    .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
        }

        public static void createTextureImage() {
                String a = (Paths.get("").toAbsolutePath() +("/shaders/terrain.png"));
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
                renderer2.Buffers.setBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, imageSize, stagingBufferImg);
                long[] stagingBufferMemoryImg = {0};
                renderer2.Buffers.createBuffer(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferImg, stagingBufferMemoryImg);


                nvkMapMemory(device, stagingBufferMemoryImg[0], 0, imageSize, 0, vkutils.MemSys.address);
                {
//                        memByteBuffer(getHandle(), imageSize).put(pixels);
                    GLU2.theGLU.memcpy(memAddress0(pixels), vkutils.MemSys.getHandle(), imageSize);
                }
                vkUnmapMemory(device, stagingBufferMemoryImg[0]);
                STBImage.stbi_image_free(pixels);

                createImage(pWidth[0], pHeight[0],
                        VK_FORMAT_R8G8B8A8_SRGB,
                        VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                        renderer2.Buffers.vkImage, renderer2.Buffers.vkAllocMemory
                );


//            beginSingleTimeCommands();
                transitionImageLayout(renderer2.Buffers.vkImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
                copyBufferToImage(stagingBufferImg, renderer2.Buffers.vkImage, pWidth[0], pHeight[0]);
                transitionImageLayout(renderer2.Buffers.vkImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

            }

        private static void copyBufferToImage(long[] buffer, long[] image, int width, int height)
        {
            VkCommandBuffer commandBuffer = renderer2.Buffers.beginSingleTimeCommands();
            long region = nmemCallocChecked(1, VkBufferImageCopy.SIZEOF);
            VkBufferImageCopy.nbufferOffset(region, 0);
            VkBufferImageCopy.nbufferRowLength(region, 0);
            VkBufferImageCopy.nbufferImageHeight(region, 0);
            VkBufferImageCopy.nimageSubresource(region).aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(0)
                    .baseArrayLayer(0)
                    .layerCount(1);
            VkBufferImageCopy.nimageOffset(region).set(0,0,0);
            VkBufferImageCopy.nimageExtent(region, VkExtent3D.calloc(MemSys.stack()).set(width, height, 1));
            nvkCmdCopyBufferToImage(
                    commandBuffer,
                    buffer[0],
                    image[0],
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    1,
                    region
            );
            nmemFree(region);
            renderer2.Buffers.endSingleTimeCommands(commandBuffer);

        }

        private static void transitionImageLayout(long[] image, int format, int oldLayout, int newLayout) {
            VkCommandBuffer commandBuffer = renderer2.Buffers.beginSingleTimeCommands();

            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.create(vkutils.MemSys.Memsys2.malloc(1, VkImageMemoryBarrier.SIZEOF), 1).sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                    .oldLayout(oldLayout)
                    .newLayout(newLayout)
                    .srcQueueFamilyIndex(VK_IMAGE_LAYOUT_UNDEFINED)
                    .dstQueueFamilyIndex(VK_IMAGE_LAYOUT_UNDEFINED)
                    .image(image[0]);
            barrier.subresourceRange()
                    .aspectMask(format)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);

            if(newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {


                if(hasStencilComponent(format)) {
                    barrier.subresourceRange().aspectMask(
                            barrier.subresourceRange().aspectMask() | VK_IMAGE_ASPECT_STENCIL_BIT);
                }

            }

            final int sourceStage;
            final int destinationStage;
            switch (oldLayout) {
                case VK_IMAGE_LAYOUT_UNDEFINED -> barrier.srcAccessMask(0);
                case VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL -> barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                default -> throw new IllegalArgumentException("Unsupported layout transition");
            }
            switch (newLayout) {
                case VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL -> {
                    barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                    sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                    destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                    barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

                }
                case VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL -> {
                    barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);

                    barrier.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
                    sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                    destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
                }
                case VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL -> {
                    barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

                    barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);
                    sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                    destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
                }
                default -> throw new IllegalArgumentException("Unsupported layout transition");
            }
            vkutils.MemSys.Memsys2.free(barrier.address0());
            vkCmdPipelineBarrier(
                    commandBuffer,
                    sourceStage /* TODO */, destinationStage /* TODO */,
                    0,
                    null,
                    null,
                    barrier);
            renderer2.Buffers.endSingleTimeCommands(commandBuffer);


        }

        private static void createImage(int width, int height, int format, int usage, long[] pTextureImage, long[] pTextureImageMemory) {
            VkImageCreateInfo imageInfo = VkImageCreateInfo.create(vkutils.MemSys.Memsys2.calloc(1, VkImageCreateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(VK_IMAGE_TYPE_2D);
            imageInfo.extent().width(width)
                        .height(height)
                        .depth(1);
            imageInfo.mipLevels(1)
                    .arrayLayers(1)
                    .format(format)
                    .tiling(VK10.VK_IMAGE_TILING_OPTIMAL)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .usage(usage)
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            vkutils.MemSys.Memsys2.doPointerAllocSafe2(imageInfo, renderer2.Buffers.capabilities.vkCreateImage, pTextureImage);

            VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(MemSys.stack());
            vkGetImageMemoryRequirements(device, pTextureImage[0], memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.create(vkutils.MemSys.Memsys2.calloc(1, VkMemoryAllocateInfo.SIZEOF))
                    .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                    .allocationSize(memRequirements.size())
                    .memoryTypeIndex(renderer2.Buffers.findMemoryType(memRequirements.memoryTypeBits(), VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));

            vkutils.MemSys.Memsys2.doPointerAllocSafe2(allocInfo, renderer2.Buffers.capabilities.vkAllocateMemory, pTextureImageMemory);

            vkBindImageMemory(device, pTextureImage[0],pTextureImageMemory[0], 0);

        }

        public static void createTextureImageView() {
            SwapChainSupportDetails.createImageView(renderer2.Buffers.vkImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT, renderer2.UniformBufferObject.textureImageView);
        }

        public static void createTextureSampler()
        {
            //                    System.out.println(properties.limits());
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.malloc().sType$Default()
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
            vkutils.MemSys.Memsys2.doPointerAllocSafe2(samplerInfo, renderer2.Buffers.capabilities.vkCreateSampler, renderer2.UniformBufferObject.textureSampler);
            nmemFree(samplerInfo.address());
        }

        private static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.create(vkutils.MemSys.Memsys2.calloc(3, VkVertexInputAttributeDescription.SIZEOF),3);

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

            return attributeDescriptions;
        }

        public static void createDepthResources()
        {
            //            hasStencilComponent();
            int depthFormat = findDepthFormat();
            long[] depthImageBuffer = {0};
            long []depthImageBufferMemory = {0};
            createImage(SwapChainSupportDetails.swapChainExtent.width(), SwapChainSupportDetails.swapChainExtent.height(),
                    depthFormat,
                    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                    depthImageBuffer,
                    depthImageBufferMemory);


            SwapChainSupportDetails.createImageView(depthImageBuffer, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, renderer2.Buffers.depthImageView);
            transitionImageLayout(depthImageBuffer, depthFormat,
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        }

        private static int findDepthFormat()
        {
            return findSupportedFormat(
                    MemSys.stack().ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT)
            );
        }


        private static boolean hasStencilComponent(int format)
        {
            return format == VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK_FORMAT_D24_UNORM_S8_UINT;
        }


        private static int findSupportedFormat(IntBuffer formatCandidates)
        {
            //TODO: WARN Possible FAIL!
            VkFormatProperties props = VkFormatProperties.calloc(MemSys.stack());

            for(int i = 0; i < formatCandidates.capacity(); ++i) {

                int format = formatCandidates.get(i);

                vkGetPhysicalDeviceFormatProperties(Queues.physicalDevice, format, props);

                /*final int i1 = props.linearTilingFeatures() & VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT;
                if (i1 == VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT && VK10.VK_IMAGE_TILING_OPTIMAL == VK_IMAGE_TILING_LINEAR) {
                    return format;
                }*/
                final int i2 = props.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT;
                if (i2 == VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT/* && VK10.VK_IMAGE_TILING_OPTIMAL == VK_IMAGE_TILING_OPTIMAL*/) {
                    return format;
                }
            }

                throw new RuntimeException("failed to find supported format!");
        }


    }

    static class VKUtilsSafe
    {

        private static final int[] uniqueQueueFamilies = IntStream.of(Queues.graphicsFamily, Queues.presentFamily).distinct().toArray();

//        private static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);

        private static final PointerBuffer pQueue;

        static {
            PointerBuffer pointerBuffer = MemSys.stack().mallocPointer(1);

            // Convert to long to support addressing up to 2^31-1 elements, regardless of sizeof(element).
            // The unsigned conversion helps the JIT produce code that is as fast as if int was returned.
            memPutAddress(pointerBuffer.address0(), VK_NULL_HANDLE);
            pQueue = pointerBuffer;
        }


        private static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger) {

            if(vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
                vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, MemSys.pAllocator());
            }

        }

        private static void createLogicalDevice() {

           {

                Queues.findQueueFamilies(Queues.physicalDevice);

               VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueQueueFamilies.length, MemSys.stack());

                for(int i = 0; i < uniqueQueueFamilies.length; i++) {
                    VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i).sType$Default();
//                    queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                    queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
                    queueCreateInfo.pQueuePriorities(MemSys.stack().floats(1.0f));
                }

                VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(MemSys.stack())
                        .fillModeNonSolid(true) //dneeded to adres valditaion errors when using VK_POLIGYON_MODE_LINE or POINT
                        .robustBufferAccess(true);
//                        .geometryShader(true);
//                        .pipelineStatisticsQuery(true)
//                        .alphaToOne(false);

               VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.create(vkutils.MemSys.Memsys2.calloc(1, VkDeviceCreateInfo.SIZEOF)).sType$Default();

//                createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
               vkutils.MemSys.Memsys2.free(queueCreateInfos.address0());
                createInfo.pQueueCreateInfos(queueCreateInfos);
                // queueCreateInfoCount is automatically set

               memPutLong(createInfo.address() + VkDeviceCreateInfo.PENABLEDFEATURES, deviceFeatures.address());

               PointerBuffer value = asPointerBuffer(DEVICE_EXTENSIONS);
               memPutLong(createInfo.address() + VkDeviceCreateInfo.PPENABLEDEXTENSIONNAMES, value.address0());
               memPutInt(createInfo.address() + VkDeviceCreateInfo.ENABLEDEXTENSIONCOUNT, value.remaining());

               if(ENABLE_VALIDATION_LAYERS) {
                    createInfo.ppEnabledLayerNames(asPointerBuffer(VALIDATION_LAYERS));
                }
                vkutils.MemSys.Memsys2.free(createInfo.address());
//                PointerBuffer pDevice = stack.stack().pointers(VK_NULL_HANDLE);
               device = new VkDevice(doPointerAlloc(createInfo), Queues.physicalDevice, createInfo);


               setupQueues();
           }
        }

        private static void setupQueues()
        {

            nvkGetDeviceQueue(device, Queues.graphicsFamily, 0, pQueue.address0());
            Queues.graphicsQueue = new VkQueue(pQueue.get(0), device);

            nvkGetDeviceQueue(device, Queues.presentFamily, 0, pQueue.address0());
            Queues.presentQueue = new VkQueue(pQueue.get(0), device);
        }

        private static long doPointerAlloc(@NotNull Struct allocateInfo)
        {
            //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator(), pVertexBufferMemory);
            long[] pDummyPlacementPointerAlloc = {0};
            JNI.callPPPPI(Queues.physicalDevice.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, Queues.physicalDevice.getCapabilities().vkCreateDevice);
            return pDummyPlacementPointerAlloc[0];
        }

        private static @NotNull PointerBuffer asPointerBuffer(@NotNull Collection<String> collection) {

//            MemoryStack stack = stackGet();

            int size = collection.size();
            PointerBuffer buffer = memPointerBuffer(MemSys.stack().nmalloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT), size);

            for (String s : collection) {
                ByteBuffer byteBuffer = MemSys.stack().UTF8(s);
                buffer.put(byteBuffer);
            }

            return buffer.rewind();
        }

    }

    static final class Queues {

        //        public static long pImageIndex;
       static VkQueue graphicsQueue;
       static VkQueue presentQueue;



       static VkDevice device;
//        static final long deviceAddress = device.address();
        static VkPhysicalDevice physicalDevice;
       static long surface;
       // We use Integer to use null as the empty value
       static Integer graphicsFamily;
       static Integer presentFamily;
       static int a =0;

        /*static void findQueueFamilies(VkPhysicalDevice device) {

           {

               IntBuffer queueFamilyCount = MemSys.stack().ints(0);


               nvkGetPhysicalDeviceQueueFamilyProperties(device, memAddress0(queueFamilyCount), 0);

               VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), MemSys.stack());


               nvkGetPhysicalDeviceQueueFamilyProperties(device, memAddress0(queueFamilyCount), memAddressSafe(queueFamilies));

               IntBuffer presentSupport = MemSys.stack().ints(VK_FALSE);

              int bound = queueFamilies.capacity();
              for (int i = 0; i < bound; i++) {
                  if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                      graphicsFamily=i;

                  }
                  vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);

                  if(presentSupport.get(0) == VK_TRUE) {
                      presentFamily = i;
                  }
              }

//               return new QueueFamilyIndices();
           }
       }*/

        static boolean isComplete() {return graphicsFamily != null && presentFamily != null;}


        private static void findQueueFamilies(VkPhysicalDevice device) {

            {


                PointerBuffer queueFamilyCount = memPointerBuffer(MemSys.stack().getAddress(), 1).put(VK_NULL_HANDLE);//long[] queueFamilyCount = MemSys.calloc(1);// stack.stack().ints(0);


                nvkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount.address0(), 0);
                VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.create(vkutils.MemSys.Memsys2.malloc(queueFamilyCount.limit(), VkQueueFamilyProperties.SIZEOF), queueFamilyCount.limit());

                nvkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount.address0(), queueFamilies.address0());

                IntBuffer presentSupport = MemSys.stack().ints(VK_FALSE);

                int i = 0;
                while (i < queueFamilyCount.limit() /*&& !isComplete()*/) {

                    if((queueFamilies.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                        graphicsFamily = i;
                    }

                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);

                    if(presentSupport.get(0) == VK_TRUE) {
                        presentFamily = i;
                    }
                    i++;
                }
                System.out.println(a+++"Graphics Family: "+graphicsFamily+" Present family: "+presentFamily);
                vkutils.MemSys.Memsys2.free(queueFamilies.address());
                vkutils.MemSys.Memsys2.free(queueFamilyCount.address0());

//                return new QueueFamilyIndices();
            }
        }
    }

}
