package vkutils;

import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.joml.Math;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.*;

import java.net.URI;
import java.net.URISyntaxException;
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

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.stream.Collectors.toSet;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.VkUtils2.Queues.*;
import static vkutils.VkUtils2.SwapChainSupportDetails.renderPass;
import static vkutils.VkUtils2.SwapChainSupportDetails.swapChainExtent;

public class VkUtils2 {
    static final vkutils.stack stack = new stack(MemoryStack.stackPush());
    static final GLFWVidMode.Buffer videoModes;
    static final GLFWVidMode videoMode;
    static final long window;
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
    private static final VkAllocationCallbacks pAllocator = null;

    static {

        Configuration.DISABLE_CHECKS.set(false);
        Configuration.DISABLE_FUNCTION_CHECKS.set(false);
        Configuration.DEBUG.set(true);
        Configuration.DEBUG_FUNCTIONS.set(true);
        Configuration.DEBUG_STREAM.set(true);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
        Configuration.DEBUG_STACK.set(true);
        Configuration.STACK_SIZE.set(4);
        Configuration.DEBUG_MEMORY_ALLOCATOR_INTERNAL.set(true);
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
            return vkCreateDebugUtilsMessengerEXT(instance, createInfo, pAllocator, VkUtils2.pDebugMessenger);
        }

        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    //Stolen From Here: https://github.com/Naitsirc98/Vulkan-Tutorial-Java/blob/master/src/main/java/javavulkantutorial/Ch02ValidationLayers.java
    private static boolean checkValidationLayerSupport() {

        {
            //todo:fix Intbuffer being inlined eincirertcly and malloc one Int.value eachat eahc method call/usage incorrectly, cauisng validtaion layerst be broken/which in tandme iwth teh 443.41 dirver bug where valditaion layers do nto function corertcly untill full farembuffer/Piplelinebuffer>Framebuffer is implemneted and others e.g. etc i.e.
            final IntBuffer ints = stack.stack().ints(0);
            VK10.vkEnumerateInstanceLayerProperties(ints, null);
            VkLayerProperties.Buffer availableLayers = VkLayerProperties.malloc(ints.get(0), stack.stack());
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
            System.out.println(stack.stack());
            throw new RuntimeException("Validation requested but not supported");
        }

        long vkApplInfo = nmemCallocChecked(1, VkApplicationInfo.SIZEOF);
        memSet(vkApplInfo, 0,VkApplicationInfo.SIZEOF);
        VkApplicationInfo.nsType(vkApplInfo, VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO);
        VkApplicationInfo.npApplicationName(vkApplInfo, stack.stack().UTF8Safe(" "));
        VkApplicationInfo.napplicationVersion(vkApplInfo, VK10.VK_MAKE_VERSION(1, 0, 0));
        VkApplicationInfo.npEngineName(vkApplInfo, stack.stack().UTF8Safe("No Engine"));
        VkApplicationInfo.nengineVersion(vkApplInfo, VK10.VK_MAKE_VERSION(1, 0, 0));
        VkApplicationInfo.napiVersion(vkApplInfo, VK10.VK_API_VERSION_1_0);
        nmemFree(vkApplInfo);


        VkInstanceCreateInfo InstCreateInfo = VkInstanceCreateInfo.malloc().sType$Default();
//                InstCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
        memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.PAPPLICATIONINFO, vkApplInfo);
        glfwExtensions = getRequiredExtensions();
                InstCreateInfo.ppEnabledExtensionNames(glfwExtensions);

        nmemFree(InstCreateInfo.address());

        int enabledExtensionCount = InstCreateInfo.enabledExtensionCount();
        int enabledLayerCount = InstCreateInfo.enabledLayerCount();
        if(ENABLE_VALIDATION_LAYERS) {
            memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.PPENABLEDLAYERNAMES, memAddress(Queues.asPointerBuffer()));
            memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.ENABLEDLAYERCOUNT, VkUtils2.VALIDATION_LAYERS.size());
            VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.malloc().sType$Default();
//            debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
            debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
            debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
            debugCreateInfo.pfnUserCallback(VkUtils2::debugCallback);
            InstCreateInfo.pNext(debugCreateInfo.address());
            nmemFree(debugCreateInfo.address());
            }
//            else InstCreateInfo.pNext(NULL);

        PointerBuffer instancePtr = memPointerBuffer(stack.stack().nmalloc(Pointer.POINTER_SIZE, 1 << Pointer.POINTER_SHIFT), 1);
                VK10.vkCreateInstance(InstCreateInfo, pAllocator, instancePtr);


        vkInstance = new VkInstance(instancePtr.get(0), InstCreateInfo);
    }

    static void setupDebugMessenger()
    {
        if(!ENABLE_VALIDATION_LAYERS) {
            return;
        }

        VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.malloc().sType$Default();
//        createInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        createInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
        createInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
        createInfo.pfnUserCallback(VkUtils2::debugCallback);
        nmemFree(createInfo.address());

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
        IntBuffer extensionCount = stack.stack().ints(0);
        vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, null);
        VkExtensionProperties.Buffer availableExtensions= VkExtensionProperties.malloc(extensionCount.get(0), stack.stack());
        vkEnumerateDeviceExtensionProperties(device, (String)null, extensionCount, availableExtensions);

        return availableExtensions.stream()
                .map(VkExtensionProperties::extensionNameString)
                .collect(toSet())
                .containsAll(Queues.DEVICE_EXTENSIONS);
    }

    private static PointerBuffer getRequiredExtensions() {

        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();
        if(ENABLE_VALIDATION_LAYERS)
        {
            int size = glfwExtensions.capacity() + 1;
            PointerBuffer extensions = memPointerBuffer(stack.stack().nmalloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT), size);
            extensions.put(glfwExtensions);
            extensions.put(stack.stack().UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
            // Rewind the buffer before returning it to reset its position back to 0
            return extensions.rewind();
        }

        return glfwExtensions;
    }

    public static void handleInputs()
    {
        //todo: need to thritile/manage to fixed branchless Tockrate to avoid excess Polling.Buffering./Overehad
        glfwPollEvents();

    }


     static final class Queues {

//        static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);
        private static final Set<String> DEVICE_EXTENSIONS=new HashSet<>();
//        public static long pImageIndex;
        static VkQueue graphicsQueue;
        static VkQueue presentQueue;

        static {
//            Set<String> set = new HashSet<>();
            DEVICE_EXTENSIONS.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
//            DEVICE_EXTENSIONS = set;
        }

        static VkDevice device;
        static VkPhysicalDevice physicalDevice;
        static long surface;
        // We use Integer to use null as the empty value
        static Integer graphicsFamily;
        static Integer presentFamily;

        private static @NotNull PointerBuffer asPointerBuffer() {


            int size = VkUtils2.VALIDATION_LAYERS.size();
            PointerBuffer buffer = memPointerBuffer(stack.stack().nmalloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT), size);

            for (String s : VkUtils2.VALIDATION_LAYERS) {
                ByteBuffer byteBuffer = stack.stack().UTF8(s);
                buffer.put(byteBuffer);
            }

            return buffer.rewind();

        }

        static void findQueueFamilies(VkPhysicalDevice device) {

            {

                IntBuffer queueFamilyCount = stack.stack().ints(0);


                nvkGetPhysicalDeviceQueueFamilyProperties(device, memAddress0(queueFamilyCount), 0);

                VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack.stack());


                nvkGetPhysicalDeviceQueueFamilyProperties(device, memAddress0(queueFamilyCount), memAddressSafe(queueFamilies));

                IntBuffer presentSupport = stack.stack().ints(VK_FALSE);

               int bound = queueFamilies.capacity();
               for (int i = 0; i < bound; i++) {
                   if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                       graphicsFamily = i;

                   }
                   vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);

                   if(presentSupport.get(0) == VK_TRUE) {
                       presentFamily = i;
                   }
               }

//               return new QueueFamilyIndices();
            }
        }

        public static void createSurface()
        {
            System.out.println("Creating Surface");

            long createSurfaceInfo = stack.calloc(1);
            VkWin32SurfaceCreateInfoKHR.nsType(createSurfaceInfo, VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR);
            VkWin32SurfaceCreateInfoKHR.nhwnd(createSurfaceInfo, glfwGetWin32Window(window));
            VkWin32SurfaceCreateInfoKHR.nhinstance(createSurfaceInfo, vkInstance.address());
//            nmemFree(createSurfaceInfo);

            LongBuffer surface_= stack.stack().longs(VK_NULL_HANDLE);

            if (GLFWVulkan.nglfwCreateWindowSurface(vkInstance.address(), window, memAddressSafe(pAllocator), memAddress0(surface_)) != VK_SUCCESS) throw new RuntimeException("failed to create window surface!");

            surface = surface_.get(0);

        }

        static void pickPhysicalDevice()
        {
            System.out.println("Picking Physical Device");


            IntBuffer deviceCount = stack.stack().ints(0);
            vkEnumeratePhysicalDevices(vkInstance, deviceCount, null);
            if(deviceCount.get(0) == 0) throw new RuntimeException("Failed to find GPUs with Vulkan support");
            int size = deviceCount.get(0);
            PointerBuffer ppPhysicalDevices = memPointerBuffer(stack.calloc(size), size);
            vkEnumeratePhysicalDevices(vkInstance, deviceCount, ppPhysicalDevices);
            for(int i = 0;i < ppPhysicalDevices.capacity();i++)
            {
                VkPhysicalDevice device = new VkPhysicalDevice(ppPhysicalDevices.get(i), vkInstance);
                if(isDeviceSuitable(device)) {
                    physicalDevice = device;
                    return;
                }
            }
            throw new RuntimeException("Failed to find a suitable GPU");
        }

        static boolean isComplete() {
            return graphicsFamily != null && presentFamily != null;
        }

    }

    static final class SwapChainSupportDetails
    {

        static final long[]  swapChainFramebuffers = new long[2];
        static final long[] swapChainImages = new long[2];
        private static final long[] swapChainImageViews = new long[2];
        static final long[] renderPass= {0};
        private static int swapChainImageFormat;
        static VkExtent2D swapChainExtent;
        private static final VkSurfaceCapabilitiesKHR   capabilities = VkSurfaceCapabilitiesKHR.malloc(stack.stack());
        private static  VkSurfaceFormatKHR.Buffer formats;
        private static IntBuffer presentModes;
        static long swapChain;
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
            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc().sType$Default()
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
            nmemFree(createInfo.address());

            PipeLine.doPointerAllocSafe2(createInfo, PipeLine.capabilities.vkCreateImageView, a);
        }
        static long createImageView(long i, int swapChainImageFormat) {
            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc().sType$Default()
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
            nmemFree(createInfo.address());

            return PipeLine.doPointerAllocSafe(createInfo, PipeLine.capabilities.vkCreateImageView);
        }

        private static void querySwapChainSupport(VkPhysicalDevice device)
        {
//            SwapChainSupportDetails  details = new SwapChainSupportDetails();


            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, capabilities);

            IntBuffer count = memIntBuffer(stack.stack().getAddress(), 1);

            vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);

            if(count.get(0) != 0) {
                formats = VkSurfaceFormatKHR.malloc(count.get(0), stack.stack());
                vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, formats);
            }

            vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null);

            if(count.get(0) != 0) {
                presentModes = stack.stack().mallocInt(count.get(0));
                vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, presentModes);
            }

//            return details;
        }

        static void createFramebuffers()
        {



           {

               LongBuffer attachments;
//               if(depthBuffer)
                   attachments = stack.stack().longs(VK_NULL_HANDLE, PipeLine.depthImageView[0]);
//               else
//                   attachments = stack.stack().longs(1);


               // Lets allocate the create info struct once and just update the pAttachments field each iteration
               VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.malloc().sType$Default()
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

                nmemFree(framebufferCreateInfo.address());
               //TODO: warn Possible Fail!
               for (int i = 0; i < swapChainImageViews.length; i++) {

                   attachments.put(0, swapChainImageViews[i]);


                   swapChainFramebuffers[i] = PipeLine.doPointerAllocSafe(framebufferCreateInfo.pNext(NULL), PipeLine.capabilities.vkCreateFramebuffer);
               }
            }
        }
    }

    public static final class PipeLine {


        private static final long address = GLU2.theGLU.alloc(Pointer.POINTER_SIZE);//nmemAllocChecked(Pointer.POINTER_SIZE);

        private static final int U = 0;
        private static final int V = 0;
        private static final float[] verticesTemp = { //TODO: Auto connetc when adding diitonal evrticies: e.g. if now Block.Cube added, rese preisiting vertocoes to avoid rudplciate/reudicnat faces/vetrcoe sbeign drawnbthat are not vosoable/occluded due to adjency, may be psob;e top use a Indicieis buffer to do thins inherently without the need to manually/systamtcially ascerain verticioe sduplciates manually /methodcially e.g.etc ie.e., preme;tively

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
        private static final float[] vertices = new float[(verticesTemp.length)*8];

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
        static final short[] indices = new short[indicesTemp.length*8];



        //prepAlloc
        //        private static final long[] vkRenderPass = new long[SwapChainSupportDetails.imageIndex];
        private static final long[] swapChainImages = new long[2];
        private static final VkOffset2D set = VkOffset2D.calloc(stack.stack()).set(0, 0);
private static final int OFFSETOF_COLOR = 3 * Float.BYTES;
        private static final int OFFSET_POS = 0;
        public static long frps;

        static long graphicsPipeline;
        static final long[] commandPool ={VK_NULL_HANDLE};
        private static final VkCommandBuffer[] commandBuffers = new VkCommandBuffer[SwapChainSupportDetails.swapChainFramebuffers.length];
        static int currentFrame;
        private static final long[] vkLayout ={0};
        private static final LongBuffer offsets = stack.stack().longs(0);
        private static final int value = /*SIZEOF **/ vertices.length* Float.BYTES;
        private static final int OFFSETOF_TEXTCOORDS = (3+3) * Float.BYTES;
        private static final int SIZEOFIn = Short.BYTES * indices.length;
        private static final int VERT_SIZE= vertices.length/ 8;
//        private static final int VERT_SIZE= (OFFSET_POS + OFFSETOF_COLOR + OFFSETOF_TEXTCOORDS) / Float.BYTES;
        private static final int VERTICESSTRIDE = 32;//vertices.length*6/indices.length;
        private static final long[] pDummyPlacementPointerAlloc = {0};
        private static final long deviceAddress = device.address();
        private static final VKCapabilitiesDevice capabilities;

        //            vertexBuffers = stack.stack().longs(PipeLine.vertexBuffer);
//            stagingBuffer = setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);

        //        private static long stagingBuffer;
        private static final long[] vertexBuffer ={0};
        private static final long[] vertexBufferMemory = {0};
        private static final long[] stagingBuffer = {0};
        private static final long[] stagingBufferMemory ={0};
        private static final long[] indexBuffer ={0};
        private static final long[] indexBufferMemory ={0};
        private static final long[] vkAllocMemory = {0};
        private static final long[] vkImage ={0};
        private static final long[] textureImageView = {0};
        private static final long[] textureSampler = {0};
        private static final long[] depthImageView ={0};


        private static int dest= -verticesTemp.length;

        static {


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


            capabilities = device.getCapabilities();
            setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, value, vertexBuffer);
            createBuffer(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, vertexBuffer, vertexBufferMemory);
            setBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, value, stagingBuffer);
            createBuffer(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
                    VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer, stagingBufferMemory);
            setBuffer(VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT, SIZEOFIn, indexBuffer);
            createBuffer(VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, indexBuffer, indexBufferMemory);


        }

        private static void doBufferAlloc2(float x, float y, float z)
        {
            System.arraycopy(verticesTemp, 0, vertices, dest+=verticesTemp.length, verticesTemp.length);
            for(int v = dest; v< dest +verticesTemp.length; v+=8)
            {
                vertices[v]+=x;
                vertices[v+1]+=y;
                vertices[v+2]+=z;
            }
        }

        private static void doBufferAlloc()
        {
            int a=2;
            int r=verticesTemp.length/8;
            System.arraycopy(indicesTemp, 0, indices, 0, indicesTemp.length);
            for (int w=36; w<indices.length;w+=36) {
                {
                    System.arraycopy(indicesTemp, 0, indices, w, indicesTemp.length);
                    for(int i=w;i<indicesTemp.length*a; i++)
                   {
                       indices[i]+=r;
                   }
                   r+=verticesTemp.length/8;

                   a++;
               }
            }


        }

        public static void createGraphicsPipelineLayout() {
            System.out.println("Setting up PipeLine");

            ShaderSPIRVUtils.SPIRV vertShaderSPIRV = ShaderSPIRVUtils.compileShaderFile("shaders/21_shader_ubo.vert", ShaderSPIRVUtils.ShaderKind.VERTEX_SHADER);
            ShaderSPIRVUtils.SPIRV fragShaderSPIRV = ShaderSPIRVUtils.compileShaderFile("shaders/21_shader_ubo.frag", ShaderSPIRVUtils.ShaderKind.FRAGMENT_SHADER);

            long vertShaderModule = createShaderModule(vertShaderSPIRV.bytecode());
            long fragShaderModule = createShaderModule(fragShaderSPIRV.bytecode());

            ByteBuffer entryPoint = stack.stack().UTF8("main");

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
                            .pVertexAttributeDescriptions(getAttributeDescriptions())
                                    .pNext(NULL);
//            memPutLong(vkPipelineVertexInputStateCreateInfo.address() + VkPipelineVertexInputStateCreateInfo.PVERTEXATTRIBUTEDESCRIPTIONS, getAttributeDescriptions());
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
            VkViewport.Buffer vkViewport = VkViewport.malloc(1)
                    .x(0.0F)
                    .y(0.0F)
                    .width(swapChainExtent.width())
                    .height(swapChainExtent.height())
                    .minDepth(0.0F)
                    .maxDepth(1.0F);

            VkRect2D.Buffer scissor = VkRect2D.malloc(1)
//                    .offset(vkOffset2D ->vkViewport.y()) //todo: not sure if correct Offset
                    .offset(set)
                    .extent(swapChainExtent);

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
                    .blendConstants(stack.stack().floats(0.0f, 0.0f, 0.0f, 0.0f));
            memFree(colorBlendAttachment);
            memFree(vkViewport);
            memFree(scissor);
            VkPipelineLayoutCreateInfo vkPipelineLayoutCreateInfo1 = VkPipelineLayoutCreateInfo.malloc().sType$Default()
//                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
//                    .pPushConstantRanges(null)
                    .pSetLayouts(stack.stack().longs(UniformBufferObject.descriptorSetLayout));


            System.out.println("using pipeLine with Length: " + SwapChainSupportDetails.swapChainImages.length);
            nmemFree(vkPipelineLayoutCreateInfo1.address());
            doPointerAllocSafe2(vkPipelineLayoutCreateInfo1, capabilities.vkCreatePipelineLayout, vkLayout);


            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1).sType$Default()
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
                    .layout(vkLayout[0])
                    .renderPass(SwapChainSupportDetails.renderPass[0])
                    .subpass(0)
//                    .basePipelineHandle(VK_NULL_HANDLE)
                    .basePipelineIndex(-1);
            memFree(shaderStages);
            nmemFree(colorBlending.address());
            memFree(pipelineInfo);
            nmemFree(vkPipelineVertexInputStateCreateInfo.address());
            nmemFree(inputAssembly.address());

            nmemFree(vkViewPortState.address());
            nmemFree(VkPipeLineRasterization.address());
            nmemFree(multisampling.address());
            nmemFree(depthStencil.address());


            graphicsPipeline =doPointerAlloc5L(device, pipelineInfo);

            vkDestroyShaderModule(device, vertShaderModule, pAllocator);
            vkDestroyShaderModule(device, fragShaderModule, pAllocator);

            vertShaderSPIRV.free();
            fragShaderSPIRV.free();


        }

        private static long doPointerAlloc5L(VkDevice device, StructBuffer<VkGraphicsPipelineCreateInfo, VkGraphicsPipelineCreateInfo.Buffer> pipelineInfo)
        {
            Checks.check(pipelineInfo.address0());
            checkCall(callPJPPPI(device.address(), VK10.VK_NULL_HANDLE, pipelineInfo.remaining(), pipelineInfo.address0(), NULL, PipeLine.pDummyPlacementPointerAlloc,capabilities.vkCreateGraphicsPipelines));
            return pDummyPlacementPointerAlloc[0];
        }

        private static long createShaderModule(ByteBuffer spirvCode) {

            {

                return doPointerAllocSafe(VkShaderModuleCreateInfo.calloc(stack.stack()).sType$Default().pCode(spirvCode), capabilities.vkCreateShaderModule);
            }
        }

        public static void createRenderPasses(boolean depthEnabled) {
            int capacity = 2;
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(capacity);
            VkAttachmentReference.Buffer attachmentsRefs =VkAttachmentReference.calloc(capacity);


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
            VkSubpassDescription vkSubpassDescriptions = VkSubpassDescription.calloc(stack.stack())
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





            VkSubpassDependency dependency = VkSubpassDependency.calloc(stack.stack())
                    .srcSubpass(VK_SUBPASS_EXTERNAL)
                    .dstSubpass(0)
                    .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .srcAccessMask(0)
                    .dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                    .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);


            long vkRenderPassCreateInfo1 = stack.stack().ncalloc(VkRenderPassCreateInfo.ALIGNOF, 1, VkRenderPassCreateInfo.SIZEOF);
            VkRenderPassCreateInfo.nsType(vkRenderPassCreateInfo1, VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            memPutAddress(vkRenderPassCreateInfo1 + VkRenderPassCreateInfo.PATTACHMENTS, attachments.address0());
            VkRenderPassCreateInfo.nattachmentCount(vkRenderPassCreateInfo1, capacity);
            memPutLong(vkRenderPassCreateInfo1 + VkRenderPassCreateInfo.PSUBPASSES, vkSubpassDescriptions.address());
            VkRenderPassCreateInfo.nsubpassCount(vkRenderPassCreateInfo1, 1);
            memPutLong(vkRenderPassCreateInfo1 + VkRenderPassCreateInfo.PDEPENDENCIES, dependency.address());
            memPutInt(vkRenderPassCreateInfo1 + VkRenderPassCreateInfo.DEPENDENCYCOUNT, 1);


            doPointerAllocSafeExtrm2(vkRenderPassCreateInfo1, capabilities.vkCreateRenderPass, renderPass);
            memFree(attachments);
            memFree(attachmentsRefs);


        }

        public static void createCommandPool() {
            Queues.findQueueFamilies(physicalDevice);

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc().sType$Default()
                    .queueFamilyIndex(Queues.graphicsFamily)
                    .flags(0);
            nmemFree(poolInfo.address());

            doPointerAllocSafe2(poolInfo, capabilities.vkCreateCommandPool, commandPool);

        }

        public static void createCommandBuffers() {
            long allocateInfo = nmemCallocChecked(1, VkCommandBufferAllocateInfo.SIZEOF);
            VkCommandBufferAllocateInfo.nsType(allocateInfo, VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            VkCommandBufferAllocateInfo.ncommandPool(allocateInfo, getRef());
            VkCommandBufferAllocateInfo.nlevel(allocateInfo, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            VkCommandBufferAllocateInfo.ncommandBufferCount(allocateInfo, SwapChainSupportDetails.swapChainFramebuffers.length);

//            PointerBuffer pCommandBuffers = stack.stack().mallocPointer(SwapChainSupportDetails.swapChainFramebuffers.length);

            nmemFree(allocateInfo);
            long[] descriptorSets = new long[VkCommandBufferAllocateInfo.ncommandBufferCount(allocateInfo)];
            doPointerAllocS(allocateInfo, capabilities.vkAllocateCommandBuffers, descriptorSets);
            commandBuffers[0]=new VkCommandBuffer(descriptorSets[0], device);
            commandBuffers[1]=new VkCommandBuffer(descriptorSets[1], device);
//            commandBuffers[2]=new VkCommandBuffer(descriptorSets[1], device);


            long beginInfo1 =stack.stack().nmalloc(VkCommandBufferBeginInfo.ALIGNOF, VkCommandBufferBeginInfo.SIZEOF);
            VkCommandBufferBeginInfo.nsType(beginInfo1, VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            VkCommandBufferBeginInfo.nflags(beginInfo1, VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack.stack()).sType$Default();
            renderPassInfo
//                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                    .renderPass(SwapChainSupportDetails.renderPass[0]);
            VkRect2D renderArea = VkRect2D.calloc(stack.stack())
                    .offset(set)
                    .extent(swapChainExtent);
            renderPassInfo.renderArea(renderArea);

            //todo: multiple attachments with VK_ATTACHMENT_LOAD_OP_CLEAR: https://vulkan-tutorial.com/en/Depth_buffering#page_Clear-values

            VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack.stack());
            clearValues.get(0).color().float32(stack.stack().floats(0.0f, 0.0f, 0.0f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);
            memPutLong(renderPassInfo.address() + VkRenderPassBeginInfo.PCLEARVALUES, clearValues.address0());
            VkRenderPassBeginInfo.nclearValueCount(renderPassInfo.address(), clearValues.remaining());

            //todo: Mmeory addres sranegs for Uniform bufefrs are opened here propr to the vkCMD Command Buffer records in an attempt to potenetial. reuce overal
            /*todo: Performance Hack: allow Memepry to be premptively mapped prior to FUll Initialisation/Drawing/rendering/Setup/prior to VKCOmmand Buffer recording to allow to avoid overhead/adiitonal when attem0ng to modifey vertex.Buffer data when itilsied and Mapped to a specific memry range(s),
                * This has the darwback of requiring specific alignment when when Obtaining.Plaicng.reading/Writing references.memory Adresses, which at leas in the case of Command Buffers.Uniform buffers seems to be exactly 512, which may potnetial decrease stabilility due to lack of potentoa,s afty wth prevent.prpettcing. mana ging agaonst/with/for Acess Viplations/Segfaults e.g.

             */
            nvkMapMemory(device, UniformBufferObject.uniformBuffersMemory[0], 0, UniformBufferObject.capacity, 0, PipeLine.address);
            nvkMapMemory(device, UniformBufferObject.uniformBuffersMemory[1], 0, UniformBufferObject.capacity, 0, PipeLine.address);

            for (int i = 0; i < commandBuffers.length; i++) {
                VkCommandBuffer commandBuffer = commandBuffers[i];
                nvkBeginCommandBuffer(commandBuffer, beginInfo1);

                renderPassInfo.framebuffer(SwapChainSupportDetails.swapChainFramebuffers[i]);

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
//                    nvkCmdBindVertexBuffers(commandBuffer, 0, 1, (put), memAddress0(offsets));
                    vkCmdBindVertexBuffers(commandBuffer, 0, stack.stack().longs(PipeLine.vertexBuffer), offsets);
                    vkCmdBindIndexBuffer(commandBuffer, indexBuffer[0], 0, VK_INDEX_TYPE_UINT16);

                    vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, vkLayout[0], 0, stack.stack().longs(UniformBufferObject.descriptorSets[(i)]), null);
                    vkCmdDrawIndexed(commandBuffer, indices.length, 1, 0, 0, 0);


                }
                vkCmdEndRenderPass(commandBuffer);

                System.out.println("Using: " + VERTICESSTRIDE + " Vertices");
                System.out.println("Using: " + VERT_SIZE + " Vert SIze/ Tris");
                vkEndCommandBuffer(commandBuffer);

            }

        }

        private static long getRef()
        {
            return PipeLine.commandPool[0];
        }


        public static void createVertexBufferStaging() {


//            nvkMapMemory(Queues.device, stagingBufferMemory, 0, value, 0, address);

//            VkPushConstantRange.

            nvkMapMemory(device, stagingBufferMemory[0], 0, value, 0, address);
            {
                GLU2.theGLU.memcpy2(vertices, getHandle(), Integer.toUnsignedLong(vertices.length << 2));
                //                GLU2.theGLU.wrap()

            }
            vkUnmapMemory(device, stagingBufferMemory[0]);

            //            vertexBuffers = getSetBuff  er(PipeLine.currentBuffer);

            copyBuffer(vertexBuffer, value);


        }

        private static void copyBuffer(long[] dstBuffer, int size) {

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();

            long vkBufferCopy = nmemCallocChecked(1, VkBufferCopy.SIZEOF);
            VkBufferCopy.nsrcOffset(vkBufferCopy, 0);
            VkBufferCopy.ndstOffset(vkBufferCopy, 0);
            VkBufferCopy.nsize(vkBufferCopy, size);
            nmemFree(vkBufferCopy);
            callPJJPV(commandBuffer.address(), PipeLine.stagingBuffer[0], dstBuffer[0], 1, vkBufferCopy, capabilities.vkCmdCopyBuffer);
            endSingleTimeCommands(commandBuffer);

        }

        private static long createBuffer(long currentBuffer) {

            long vkMemoryRequirements = stack.stack().ncalloc(VkMemoryRequirements.ALIGNOF, 1, VkMemoryRequirements.SIZEOF);

            nvkGetBufferMemoryRequirements(device, currentBuffer, vkMemoryRequirements);


            long vertexBufferMemory;
            long allocateInfo1 = stack.stack().ncalloc(VkMemoryAllocateInfo.ALIGNOF, 1, VkMemoryAllocateInfo.SIZEOF);
            VkMemoryAllocateInfo.nsType(allocateInfo1, VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            VkMemoryAllocateInfo.nallocationSize(allocateInfo1, VkMemoryRequirements.nsize(vkMemoryRequirements));
            VkMemoryAllocateInfo.nmemoryTypeIndex(allocateInfo1, findMemoryType(VkMemoryRequirements.nmemoryTypeBits(vkMemoryRequirements), 6));
            vertexBufferMemory=doPointerAllocSafeExtrm(allocateInfo1, capabilities.vkAllocateMemory);

            vkBindBufferMemory(device, currentBuffer, vertexBufferMemory, 0);

            //        private static final long[] vertexBufferMemory=new long [1];

            return vertexBufferMemory;

        } private static void createBuffer(int properties, long[] currentBuffer, long[] vertexBufferMemory) {

            long vkMemoryRequirements = nmemCalloc(1, VkMemoryRequirements.SIZEOF);

            nvkGetBufferMemoryRequirements(device, currentBuffer[0], vkMemoryRequirements);


            long allocateInfo1 = nmemCalloc(1, VkMemoryAllocateInfo.SIZEOF);
            VkMemoryAllocateInfo.nsType(allocateInfo1, VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            VkMemoryAllocateInfo.nallocationSize(allocateInfo1, VkMemoryRequirements.nsize(vkMemoryRequirements));
            VkMemoryAllocateInfo.nmemoryTypeIndex(allocateInfo1, findMemoryType(VkMemoryRequirements.nmemoryTypeBits(vkMemoryRequirements), properties));
            nmemFree(vkMemoryRequirements);
            doPointerAllocSafeExtrm2(allocateInfo1, capabilities.vkAllocateMemory, vertexBufferMemory);
            nmemFree(allocateInfo1);

            vkBindBufferMemory(device, currentBuffer[0], vertexBufferMemory[0], 0);

        }

        //todo: need to do thsi seperately from createBuffer as Java cannot handle references/pointrs by default and the buffre must be a field due to the bindVertexBuffers call needing acces to teh same VertexBuffer that is cerated, and a methdo/upu cannto return multiple variables/refercnes/valeus at teh same time
        private static void setBuffer(int usage, int size, long[] a) {
            long allocateInfo = nmemCalloc(1, VkBufferCreateInfo.SIZEOF);
            VkBufferCreateInfo.nsType(allocateInfo, VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            VkBufferCreateInfo.nsize(allocateInfo, size);
            VkBufferCreateInfo.nusage(allocateInfo, usage);
            VkBufferCreateInfo.nsharingMode(allocateInfo, VK_SHARING_MODE_EXCLUSIVE);

            doPointerAllocSafeExtrm2(allocateInfo, capabilities.vkCreateBuffer, a);
            nmemFree(allocateInfo);
        }
        private static long setBuffer(int usage, int size) {
            long allocateInfo = nmemCalloc(1, VkBufferCreateInfo.SIZEOF);
            VkBufferCreateInfo.nsType(allocateInfo, VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            VkBufferCreateInfo.nsize(allocateInfo, size);
            VkBufferCreateInfo.nusage(allocateInfo, usage);
            VkBufferCreateInfo.nsharingMode(allocateInfo, VK_SHARING_MODE_EXCLUSIVE);
            nmemFree(allocateInfo);

            return doPointerAllocSafeExtrm(allocateInfo, capabilities.vkCreateBuffer);
        }


        private static long doPointerAllocSafeExtrm(long allocateInfo, long vkCreateBuffer) {
            //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
            System.out.println("Attempting to Call: ->"+ memGetLong(allocateInfo));
            Checks.check(allocateInfo);
            checkCall(callPPPPI(deviceAddress, allocateInfo, NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));

            return pDummyPlacementPointerAlloc[0];
        }

        /*todo: Java cannot handle/Wiriting>erading to references are not fully initlaised unliek C++ without wiring throiwng an NullPOinterException imemdiately, hense why this function was setup to make hanlding rfeernces less of a nuicience with javaa
            * the static inle Array acts as a menas of containg references when written to after an API call from Vulkan, which allow for reaidnga dn wiritng to references. dierctly without the need for intermediate structis such as Pointer/LongBuffers
            * and reducing pressure on the MemeoryStack SImulatbeously/In the Process
            * Unfortuately this lilely will never get as fast as the NCthe nativ eimpelation fo wiritng to refercnes as with posible with C++ as well aa the interent overead of function body calls and pasing paraaters
            * a fueld refercne is also used just in case dynamically allating with new is less effcienct that sinly wiritng to a static final Reference/memory adress instead
         */
        private static long doPointerAllocSafe(@NotNull Struct allocateInfo, long vkCreateBuffer) {
            //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
            System.out.println("Attempting to Call: ->"+allocateInfo);
            Checks.check(allocateInfo.address());
            checkCall(callPPPPI(deviceAddress, allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));
            return pDummyPlacementPointerAlloc[0];
        }
        private static void doPointerAllocSafe2(@NotNull Struct allocateInfo, long vkCreateBuffer, long[] a) {
            //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
            System.out.println("Attempting to Call: ->"+allocateInfo);
            Checks.check(allocateInfo.address());
            checkCall(callPPPPI(deviceAddress, allocateInfo.address(), NULL, a, vkCreateBuffer));
        }

        private static void checkCall(int callPPPPI)
        {
            switch (callPPPPI)
            {
                case VK_NOT_READY -> throw new RuntimeException("Not ready!");
                case VK_TIMEOUT -> throw new RuntimeException("Bad TimeOut!");
                default -> {
                }
            }
        }

        private static VkCommandBuffer doPointerAllocAlt(@NotNull long allocateInfo, long vkAllocateCommandBuffers) {
            //            vkAllocateMemory(device, allocateInfo, pAllocator, pVertexBufferMemory);
            callPPPI(deviceAddress, allocateInfo, pDummyPlacementPointerAlloc, vkAllocateCommandBuffers);
            return new VkCommandBuffer(pDummyPlacementPointerAlloc[0], device);

        }


        private static VkVertexInputBindingDescription.@NotNull Buffer getVertexInputBindingDescription() {
            return VkVertexInputBindingDescription.calloc(1, stack.stack())
                    .binding(0)
//                    .stride(vertices.length/2)
//                    .stride(vertices.length/VERT_SIZE+1)
                    .stride(VERTICESSTRIDE)
                    .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
        }

        static int findMemoryType(int typeFilter, int properties) {
            VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.calloc(stack.stack());
            vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProperties);
            for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
                if ((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                    return i;
                }
            }

            throw new RuntimeException("Failed to find suitable memory type");
        }

        public static void createIndexBuffer() {

//            nvkMapMemory(Queues.device, stagingBufferMemory, 0, SIZEOFIn, 0, address);

            nvkMapMemory(device, stagingBufferMemory[0], 0, SIZEOFIn, 0, address);
            {
//                memFloatBuffer(data.get(0), vertices.length).put(vertices);
                /*todo: might e posible to only undata.remap a specic aaspect of the mapped buffer memory if only a specici vbo, vertex is being updated instead of remappign teh entre buffer every/Single time
                * e.g. only need to remap/modify.adjust a specific cube.block.vertex so will only rrmap the meory potion equal to the veretx offset+the eisze of the modified setcion/vertex of the buffer
                * e..g so if mroe veretx ned to be mrodified/wirtten/removed the remapped manger range betwen teh offset anf the termatteitr amy need to be garter
                * e.g. if Block 3 need sto be modifed, only remap the memeory equal to the offset(BlockVertex size*Block instance/ID.orderof buffer isnertion.precedense. and the blcok veretx size itself)
                * e.g. if 3rc blcok ned to be remapedd, use offset 192*Blockprecedenceorder/id/ofsfet-1 and size 192 to onlys epcevely remap onlu the Block and not the Entrry of the Buffer
                */

//                memShortBuffer(getHandle(), SIZEOFIn).put(indices);
                GLU2.theGLU.memcpy2(indices, getHandle(), Integer.toUnsignedLong(indices.length <<2));
//                GLU2.theGLU.wrap()

            }
            vkUnmapMemory(device, stagingBufferMemory[0]);

            copyBuffer(indexBuffer, SIZEOFIn);

        }

        public static void creanupBufferStaging() {
            vkDestroyBuffer(device, stagingBuffer[0], pAllocator);
            vkFreeMemory(device, stagingBufferMemory[0], pAllocator);
        }

        public static void doPointerAllocS(long allocateInfo, long vkAllocateDescriptorSets, long[] descriptorSets) {

            callPPPI(deviceAddress, allocateInfo, descriptorSets, vkAllocateDescriptorSets);
//            return address;

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

                long stagingBufferImg[] = {0};
                setBuffer(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, imageSize, stagingBufferImg);
                long[] stagingBufferMemoryImg = {0};
                createBuffer(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferImg, stagingBufferMemoryImg);


                nvkMapMemory(device, stagingBufferMemoryImg[0], 0, imageSize, 0, address);
                {
//                        memByteBuffer(getHandle(), imageSize).put(pixels);
                    GLU2.theGLU.memcpy(memAddress0(pixels), getHandle(), imageSize);
                }
                vkUnmapMemory(device, stagingBufferMemoryImg[0]);
                STBImage.stbi_image_free(pixels);

                createImage(pWidth[0], pHeight[0],
                        VK_FORMAT_R8G8B8A8_SRGB,
                        VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                        vkImage, vkAllocMemory
                );


//            beginSingleTimeCommands();
                transitionImageLayout(vkImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
                copyBufferToImage(stagingBufferImg, vkImage, pWidth[0], pHeight[0]);
                transitionImageLayout(vkImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

            }

        private static void copyBufferToImage(long[] buffer, long[] image, int width, int height)
        {
            VkCommandBuffer commandBuffer = beginSingleTimeCommands();
            long region = nmemCallocChecked(1, VkBufferImageCopy.SIZEOF);
            VkBufferImageCopy.nbufferOffset(region, 0);
            VkBufferImageCopy.nbufferRowLength(region, 0);
            VkBufferImageCopy.nbufferImageHeight(region, 0);
            VkBufferImageCopy.nimageSubresource(region).aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(0)
                    .baseArrayLayer(0)
                    .layerCount(1);
            VkBufferImageCopy.nimageOffset(region).set(0,0,0);
            VkBufferImageCopy.nimageExtent(region, VkExtent3D.calloc(stack.stack()).set(width, height, 1));
            nvkCmdCopyBufferToImage(
                    commandBuffer,
                    buffer[0],
                    image[0],
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    1,
                    region
            );
            nmemFree(region);
            endSingleTimeCommands(commandBuffer);

        }

        private static void transitionImageLayout(long[] image, int format, int oldLayout, int newLayout) {
            VkCommandBuffer commandBuffer = beginSingleTimeCommands();

            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack.stack()).sType$Default()
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

            vkCmdPipelineBarrier(
                    commandBuffer,
                    sourceStage /* TODO */, destinationStage /* TODO */,
                    0,
                    null,
                    null,
                    barrier);
            endSingleTimeCommands(commandBuffer);


        }

        private static @NotNull VkCommandBuffer beginSingleTimeCommands() {

            final long allocateInfo = nmemCallocChecked(1, VkCommandBufferAllocateInfo.SIZEOF);
            VkCommandBufferAllocateInfo.nsType(allocateInfo, VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            VkCommandBufferAllocateInfo.nlevel(allocateInfo, VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            VkCommandBufferAllocateInfo.ncommandPool(allocateInfo, commandPool[0]);
            VkCommandBufferAllocateInfo.ncommandBufferCount(allocateInfo, 1);
            nmemFree(allocateInfo);
            VkCommandBuffer commandBuffer = doPointerAllocAlt(allocateInfo, capabilities.vkAllocateCommandBuffers);
            long vkCommandBufferBeginInfo = stack.stack().ncalloc(VkCommandBufferBeginInfo.ALIGNOF, 1, VkCommandBufferBeginInfo.SIZEOF);
            VkCommandBufferBeginInfo.nsType(vkCommandBufferBeginInfo, VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            VkCommandBufferBeginInfo.nflags(vkCommandBufferBeginInfo, VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            nvkBeginCommandBuffer(commandBuffer, vkCommandBufferBeginInfo);
            return commandBuffer;
        }
        //todo: modfied form of getHandle specificaly Deisgned to handle Dierct access to UniformBuffer Modifictaion with Alternative CommandBuffer Execution, All memory Address Loctaions smust be aligned to 512 exactly to avoid Rendering Artifacts./nstablity
        private static long getHandle(int address1)
        {
            //            System.out.println(l);
            return memGetLong(address)+(Integer.toUnsignedLong(address1*512));
        }
        //todo: generates randon Handles use with specific function.sAPI methdos that require it, avoiding teh need to use a pointerBuffer to do so instead
        private static long getHandle()
        {
            return memGetLong(address);
        }

        private static void endSingleTimeCommands(VkCommandBuffer commandBuffer) {
            vkEndCommandBuffer(commandBuffer);

            long submitInfo1 = stack.stack().ncalloc(VkSubmitInfo.ALIGNOF, 1, VkSubmitInfo.SIZEOF);
            memPutInt(submitInfo1 + VkSubmitInfo.STYPE, VK_STRUCTURE_TYPE_SUBMIT_INFO);
            VkSubmitInfo.npCommandBuffers(submitInfo1, stack.stack().pointers(commandBuffer));

            long __functionAddress = graphicsQueue.getCapabilities().vkQueueSubmit;

            JNI.callPPJI(graphicsQueue.address(), 1, submitInfo1, VK_NULL_HANDLE, __functionAddress);
            JNI.callPI(graphicsQueue.address(), graphicsQueue.getCapabilities().vkQueueWaitIdle);

            VK10.vkFreeCommandBuffers(device, commandPool[0], commandBuffer);
        }

        private static void createImage(int width, int height, int format, int usage, long[] pTextureImage, long[] pTextureImageMemory) {
            long imageInfo = nmemCallocChecked(1, VkImageCreateInfo.SIZEOF);
            VkImageCreateInfo.nsType(imageInfo, VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
            VkImageCreateInfo.nimageType(imageInfo, VK_IMAGE_TYPE_2D);
            VkImageCreateInfo.nextent(imageInfo).width(width);
            VkImageCreateInfo.nextent(imageInfo).height(height);
            VkImageCreateInfo.nextent(imageInfo).depth(1);
            VkImageCreateInfo.nmipLevels(imageInfo, 1);
            VkImageCreateInfo.narrayLayers(imageInfo, 1);
            VkImageCreateInfo.nformat(imageInfo, format);
            VkImageCreateInfo.ntiling(imageInfo, VK10.VK_IMAGE_TILING_OPTIMAL);
            VkImageCreateInfo.ninitialLayout(imageInfo, VK_IMAGE_LAYOUT_UNDEFINED);
            VkImageCreateInfo.nusage(imageInfo, usage);
            VkImageCreateInfo.nsamples(imageInfo, VK_SAMPLE_COUNT_1_BIT);
            VkImageCreateInfo.nsharingMode(imageInfo, VK_SHARING_MODE_EXCLUSIVE);
            doPointerAllocSafeExtrm2(imageInfo, capabilities.vkCreateImage, pTextureImage);
            nmemFree(imageInfo);

            VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack.stack());
            vkGetImageMemoryRequirements(device, pTextureImage[0], memRequirements);

            long allocInfo = nmemCallocChecked(1, VkMemoryAllocateInfo.SIZEOF);
            VkMemoryAllocateInfo.nsType(allocInfo, VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            VkMemoryAllocateInfo.nallocationSize(allocInfo, memRequirements.size());
            VkMemoryAllocateInfo.nmemoryTypeIndex(allocInfo, findMemoryType(memRequirements.memoryTypeBits(), VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT));
            nmemFree(allocInfo);

            doPointerAllocSafeExtrm2(allocInfo, capabilities.vkAllocateMemory, pTextureImageMemory);

            vkBindImageMemory(device, pTextureImage[0],pTextureImageMemory[0], 0);

        }

        private static void doPointerAllocSafeExtrm2(long imageInfo, long vkCreateImage, long[] a)
        {
            checkCall(callPPPPI(deviceAddress, imageInfo, NULL, a, vkCreateImage));

        }

        public static void createTextureImageView() {
            SwapChainSupportDetails.createImageView(vkImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_ASPECT_COLOR_BIT, textureImageView);
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
            doPointerAllocSafe2(samplerInfo, capabilities.vkCreateSampler, textureSampler);
            nmemFree(samplerInfo.address());
        }

        private static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.calloc(3, stack.stack());

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
            createImage(swapChainExtent.width(), swapChainExtent.height(),
                    depthFormat,
                    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                    depthImageBuffer,
                    depthImageBufferMemory);


            SwapChainSupportDetails.createImageView(depthImageBuffer, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, depthImageView);
            transitionImageLayout(depthImageBuffer, depthFormat,
                    VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        }

        private static int findDepthFormat()
        {
            return findSupportedFormat(
                    stack.stack().ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT)
            );
        }


        private static boolean hasStencilComponent(int format)
        {
            return format == VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK_FORMAT_D24_UNORM_S8_UINT;
        }


        private static int findSupportedFormat(IntBuffer formatCandidates)
        {
            //TODO: WARN Possible FAIL!
            VkFormatProperties props = VkFormatProperties.calloc(stack.stack());

            for(int i = 0; i < formatCandidates.capacity(); ++i) {

                int format = formatCandidates.get(i);

                vkGetPhysicalDeviceFormatProperties(physicalDevice, format, props);

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

        private static final int[] uniqueQueueFamilies = IntStream.of(graphicsFamily, presentFamily).distinct().toArray();

//        private static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);

        private static final PointerBuffer pQueue = stack.stack().pointers(VK_NULL_HANDLE);


        private static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger) {

            if(vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
                vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, pAllocator);
            }

        }

        static void cleanup() {


//            for (int i =0; i< Renderer.MAX_FRAMES_IN_FLIGHT;i++)
            vkDeviceWaitIdle(device);
//            vkWaitForFences(Queues.device, Renderer.vkFence, true, 1000000000);

            _mainDeletionQueue();

            vkDestroyDevice(device, pAllocator);
            vkDestroySurfaceKHR(vkInstance, surface, pAllocator);
            vkDestroyInstance(vkInstance, pAllocator);
            /*glfwDestroyWindow(window);*/


//            vkDestroyInstance(vkInstance, pAllocator);


            glfwDestroyWindow(window);

            glfwTerminate();
        }

        private static void _mainDeletionQueue()
        {
            doDestroyFreeAlloc(PipeLine.vkImage[0], PipeLine.capabilities.vkDestroyImage);
            doDestroyFreeAlloc(PipeLine.vkAllocMemory[0], PipeLine.capabilities.vkFreeMemory);
            doDestroyFreeAlloc(PipeLine.textureSampler[0], PipeLine.capabilities.vkDestroySampler);
            doDestroyFreeAlloc(PipeLine.textureImageView[0], PipeLine.capabilities.vkDestroyImageView);

            {
                vkDestroySemaphore(device, VkUtils2.Renderer.AvailableSemaphore[0], pAllocator);
//                vkDestroySemaphore(device, VkUtils2.Renderer.FinishedSemaphore[0], pAllocator);
                vkDestroyFence(device, VkUtils2.Renderer.vkFence[0], pAllocator);
            }
                vkDestroyDescriptorSetLayout(device, UniformBufferObject.descriptorSetLayout[0], pAllocator);
                vkDestroyDescriptorPool(device, UniformBufferObject.descriptorPool[0], pAllocator);
            for (int i = 0; i < SwapChainSupportDetails.swapChainFramebuffers.length; i++)
            {
                vkDestroySwapchainKHR(device, SwapChainSupportDetails.swapChainImages[i], pAllocator);
                vkDestroyImageView(device, SwapChainSupportDetails.swapChainImageViews[i], pAllocator);
                VK10.vkFreeCommandBuffers(device, PipeLine.commandPool[0], PipeLine.commandBuffers[i]);
                vkDestroyFramebuffer(device, SwapChainSupportDetails.swapChainFramebuffers[i], pAllocator);
                vkFreeMemory(device, UniformBufferObject.uniformBuffersMemory[i], pAllocator);
                vkDestroyBuffer(device, UniformBufferObject.uniformBuffers[i], pAllocator);

            }
            {

            }




//            vkFreeMemory(Queues.device, PipeLine.stagingBufferMemory, pAllocator);
            vkFreeMemory(device, PipeLine.vertexBufferMemory[0], pAllocator);
            vkFreeMemory(device, PipeLine.indexBufferMemory[0], pAllocator);
            vkDestroyBuffer(device, PipeLine.vertexBuffer[0], pAllocator);
//            vkDestroyBuffer(Queues.device, PipeLine.stagingBuffer, pAllocator);
            vkDestroyBuffer(device, PipeLine.indexBuffer[0], pAllocator);
            vkDestroyCommandPool(device, PipeLine.commandPool[0], pAllocator);

            vkDestroyPipeline(device, PipeLine.graphicsPipeline, pAllocator);

            vkDestroyPipelineLayout(device, PipeLine.vkLayout[0], pAllocator);

            vkDestroyRenderPass(device, SwapChainSupportDetails.renderPass[0], pAllocator);


        }

        private static void doDestroyFreeAlloc(long textureImageView, long vkDestroyImageView)
        {
            System.out.println("Destroying:+ " + textureImageView);
            JNI.callPJPV(device.address(), textureImageView, NULL, vkDestroyImageView);
        }

        static void createLogicalDevice() {

           {

                findQueueFamilies(physicalDevice);

               VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueQueueFamilies.length, stack.stack());

                for(int i = 0; i < uniqueQueueFamilies.length; i++) {
                    VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i).sType$Default();
//                    queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
                    queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
                    queueCreateInfo.pQueuePriorities(stack.stack().floats(1.0f));
                }

                VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack.stack())
                        .fillModeNonSolid(true) //dneeded to adres valditaion errors when using VK_POLIGYON_MODE_LINE or POINT
                        .robustBufferAccess(true);
//                        .geometryShader(true);
//                        .pipelineStatisticsQuery(true)
//                        .alphaToOne(false);

               VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc().sType$Default();

//                createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
                createInfo.pQueueCreateInfos(queueCreateInfos);
                // queueCreateInfoCount is automatically set

               memPutLong(createInfo.address() + VkDeviceCreateInfo.PENABLEDFEATURES, deviceFeatures.address());

               PointerBuffer value = asPointerBuffer(Queues.DEVICE_EXTENSIONS);
               memPutLong(createInfo.address() + VkDeviceCreateInfo.PPENABLEDEXTENSIONNAMES, value.address0());
               memPutInt(createInfo.address() + VkDeviceCreateInfo.ENABLEDEXTENSIONCOUNT, value.remaining());

               if(ENABLE_VALIDATION_LAYERS) {
                    createInfo.ppEnabledLayerNames(asPointerBuffer(VALIDATION_LAYERS));
                }
                nmemFree(createInfo.address());
//                PointerBuffer pDevice = stack.stack().pointers(VK_NULL_HANDLE);
               device = new VkDevice(doPointerAlloc(createInfo), physicalDevice, createInfo);


               setupQueues();
           }
        }

        private static void setupQueues()
        {

            nvkGetDeviceQueue(device, graphicsFamily, 0, pQueue.address());
            graphicsQueue = new VkQueue(pQueue.get(0), device);

            nvkGetDeviceQueue(device, presentFamily, 0, pQueue.address());
            presentQueue = new VkQueue(pQueue.get(0), device);
        }

        private static long doPointerAlloc(@NotNull Struct allocateInfo)
        {
            //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
            long[] pDummyPlacementPointerAlloc = {0};
            JNI.callPPPPI(physicalDevice.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc,physicalDevice.getCapabilities().vkCreateDevice);
            return pDummyPlacementPointerAlloc[0];
        }

        static void createSwapChain() {

           {

               querySwapChainSupport(physicalDevice);

                VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(SwapChainSupportDetails.formats);
                int presentMode = chooseSwapPresentMode(SwapChainSupportDetails.presentModes);
                VkExtent2D extent = SwapChainSupportDetails.chooseSwapExtent();

                int imageCount = (SwapChainSupportDetails.capabilities.minImageCount()/* + 1*/);
//                int[] imageCount = {(SwapChainSupportDetails.capabilities.minImageCount()/* + 1*/)};


                if(SwapChainSupportDetails.capabilities.maxImageCount() > 0 && imageCount > SwapChainSupportDetails.capabilities.maxImageCount()) {
                    imageCount=SwapChainSupportDetails.capabilities.maxImageCount();
                }

               long createInfo = stack.stack().ncalloc(VkSwapchainCreateInfoKHR.ALIGNOF, 1, VkSwapchainCreateInfoKHR.SIZEOF);

               VkSwapchainCreateInfoKHR.nsType(createInfo, VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
               VkSwapchainCreateInfoKHR.nsurface(createInfo, surface);

               // Image settings
               VkSwapchainCreateInfoKHR.nminImageCount(createInfo, imageCount);
               VkSwapchainCreateInfoKHR.nimageFormat(createInfo, VkSurfaceFormatKHR.nformat(surfaceFormat.address()));
               VkSwapchainCreateInfoKHR.nimageColorSpace(createInfo, surfaceFormat.colorSpace());
               VkSwapchainCreateInfoKHR.nimageExtent(createInfo, extent);
               VkSwapchainCreateInfoKHR.nimageArrayLayers(createInfo, 1);
               VkSwapchainCreateInfoKHR.nimageUsage(createInfo, VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

               findQueueFamilies(physicalDevice);

                if(!graphicsFamily.equals(presentFamily)) {
                    //VkSwapchainCreateInfoKHR.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                    VkSwapchainCreateInfoKHR.nimageSharingMode(createInfo, VK_SHARING_MODE_CONCURRENT);
                    VkSwapchainCreateInfoKHR.npQueueFamilyIndices(createInfo, stack.stack().ints(graphicsFamily, presentFamily));
                } else {
                    VkSwapchainCreateInfoKHR.nimageSharingMode(createInfo, VK_SHARING_MODE_EXCLUSIVE);
                }

               VkSwapchainCreateInfoKHR.npreTransform(createInfo, SwapChainSupportDetails.capabilities.currentTransform());
               VkSwapchainCreateInfoKHR.ncompositeAlpha(createInfo, VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
               VkSwapchainCreateInfoKHR.npresentMode(createInfo, presentMode);
               VkSwapchainCreateInfoKHR.nclipped(createInfo, 1);

               VkSwapchainCreateInfoKHR.noldSwapchain(createInfo, VK_NULL_HANDLE);

               SwapChainSupportDetails.swapChain = PipeLine.doPointerAllocSafeExtrm(createInfo, PipeLine.capabilities.vkCreateSwapchainKHR);



//               callPJPPI(PipeLine.deviceAddress, swapChain, (imageCount), NULL, device.getCapabilities().vkGetSwapchainImagesKHR);

               long[] pSwapchainImages = new long[2];


               KHRSwapchain.vkGetSwapchainImagesKHR(device, SwapChainSupportDetails.swapChain, new int[]{(imageCount)}, pSwapchainImages);

               System.arraycopy(pSwapchainImages, 0, PipeLine.swapChainImages, 0, pSwapchainImages.length);

               SwapChainSupportDetails.swapChainImageFormat = VkSurfaceFormatKHR.nformat(surfaceFormat.address());
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

        private static void querySwapChainSupport(VkPhysicalDevice device) {

//            SwapChainSupportDetails details = new SwapChainSupportDetails();

//            details.capabilities = VkSurfaceCapabilitiesKHR.malloc()(VKUtilsSafe.stack.stack());
            vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, SwapChainSupportDetails.capabilities);

            IntBuffer count = stack.stack().ints(0);

            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);

            if(count.get(0) != 0) {
                SwapChainSupportDetails.formats = VkSurfaceFormatKHR.malloc(count.get(0), stack.stack());
                KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, SwapChainSupportDetails.formats);
            }

            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null);

            if(count.get(0) != 0) {
                SwapChainSupportDetails.presentModes = stack.stack().mallocInt(count.get(0));
                KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, SwapChainSupportDetails.presentModes);
            }

//            return details;
        }

        private static void findQueueFamilies(VkPhysicalDevice device) {

            {

                IntBuffer queueFamilyCount = memIntBuffer(stack.stack().getAddress(), 1).put(0);// stack.stack().ints(0);


                nvkGetPhysicalDeviceQueueFamilyProperties(device, memAddress0(queueFamilyCount), 0);

                VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack.stack());

                nvkGetPhysicalDeviceQueueFamilyProperties(device, memAddress0(queueFamilyCount), memAddressSafe(queueFamilies));

                IntBuffer presentSupport = stack.stack().ints(VK_FALSE);

                for(int i = 0; i < queueFamilies.capacity() || !Queues.isComplete(); i++) {

                    if((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                        graphicsFamily = i;
                    }

                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);

                    if(presentSupport.get(0) == VK_TRUE) {
                        presentFamily = i;
                    }
                }

//                return new QueueFamilyIndices();
            }
        }

        private static @NotNull PointerBuffer asPointerBuffer(@NotNull Collection<String> collection) {

//            MemoryStack stack = stackGet();

            int size = collection.size();
            PointerBuffer buffer = memPointerBuffer(stack.stack().nmalloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT), size);

            for (String s : collection) {
                ByteBuffer byteBuffer = stack.stack().UTF8(s);
                buffer.put(byteBuffer);
            }

            return buffer.rewind();
        }

    }

    public static class UniformBufferObject
    {

//        private static final Matrix4f model = new Matrix4f().identity().determineProperties();
        private static final Matrix4f proj = new Matrix4f().identity().determineProperties();
        private static final float[] proj2;
        private static final long[] descriptorSetLayout = {0};
//        private static LongBuffer pDescriptorSetLayout;
        private static final long[] uniformBuffers = new long[(SwapChainSupportDetails.swapChainImages.length)];
        private static final long[] uniformBuffersMemory = new long[(SwapChainSupportDetails.swapChainImages.length)];
        private static final int capacity = 16 * Float.BYTES;
        private static final long[] descriptorPool ={0};
        private static final long[] descriptorSets = new long[SwapChainSupportDetails.swapChainImages.length];
        private static final float h = (Math.tan(Math.toRadians(45) * 0.5f));
        private static final double Half_Pi = java.lang.Math.atan(1) * 4D / 180D;

        private static final float zFar = 20.0f;

        private static final float zNear = 0.8f;

        private static final float aFloat1 = 1.0f;

        private static final float[] proj3= new float[16];

        static {
            proj.identity().m00(aFloat1 / (h * (swapChainExtent.width() / (float) swapChainExtent.height())))
                    .m11((aFloat1 / h) * -1)
                    .m22((zFar + zNear) / (zNear - zFar))
                    .m32((zFar + zFar) * zNear / (zNear - zFar))
                    .m23(-aFloat1);
            Matrix4f  view=new Matrix4f().identity().determineProperties().setLookAt(2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, aFloat1);
            proj.mulPerspectiveAffine(view);
                proj2 = new float[]{proj.m00(),proj.m01(),proj.m02(),proj.m03(),
                                    proj.m10(),proj.m11(),proj.m12(),proj.m13(),
                                    proj.m20(),proj.m21(),proj.m22(),proj.m23(),
                                    proj.m30(),proj.m31(),proj.m32(),proj.m33(),};
                System.arraycopy(proj2, 0, proj3, 0, proj2.length);

        }
        //todo: Important: atcuall have to 'reset; or revert teh matrixces bakc to baseline.Idneitfy in order to atcually update te Unform Buffer(s) properly e.g. .etc i.e.
//            UniformBufferObject.model.identity();
//            UniformBufferObject.view.identity();
//            UniformBufferObject.proj.identity();


        private static final double aFloat = Math.toRadians(90D);

        static void createDescriptorSetLayout() {
             {
                 VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(2);
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


                 long a = nmemCallocChecked(1, VkDescriptorSetLayoutCreateInfo.SIZEOF);
                 VkDescriptorSetLayoutCreateInfo.nsType(a, VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
                 memPutAddress(a + VkDescriptorSetLayoutCreateInfo.PBINDINGS, bindings.address0());
//                 memPutAddress(a + VkDescriptorSetLayoutCreateInfo.PBINDINGS, bindings[1]);
                 VkDescriptorSetLayoutCreateInfo.nbindingCount(a, bindings.remaining());
                 nmemFree(a);
                 memFree(bindings);
                 PipeLine.doPointerAllocSafeExtrm2(a, PipeLine.capabilities.vkCreateDescriptorSetLayout, descriptorSetLayout);
            }
        }

        public static void createUniformBuffers()
        {
            for (int i = 0; i < SwapChainSupportDetails.swapChainImages.length; i++) {

                uniformBuffers[i]=(((PipeLine.setBuffer(VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, capacity))));
                uniformBuffersMemory[i]= (PipeLine.createBuffer(uniformBuffers[i]));
            }
        }

        private static void updateUniformBuffer(int pImageIndex)
        {

            double angle = (glfwGetTime()*aFloat);

            //            model.identity();
//            System.arraycopy(proj3, 0, proj2, 0, proj2.length);
            mulAffineL(
                    (float) Math.sin(angle),
                    (float) Math.cos(angle+Half_Pi)
            );
            //._properties(Madtrix4fc.PROPERTY_AFFINE | Matrix4fc.PROPERTY_ORTHONORMAL);
//            model.m00(cos).m01(sin).m10(-sin).m11(cos);//.mulOrthoAffine(proj);//._properties(Matrix4fc.PROPERTY_AFFINE | Matrix4fc.PROPERTY_ORTHONORMAL);
//            abs.identity().mul(proj);
//            abs.determineProperties();
//            proj.determineProperties();
//            abs.set(proj);
            //.add(model);

            //                    .properties(Matrix4fc.PROPERTY_PERSPECTIVE);

            {
                memcpy(PipeLine.getHandle(pImageIndex));
            }

        }
        private static void mulAffineL(
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

        }

        private static void memcpy(long handle)
        {
//            abs.getToAddress(handle);
            GLU2.theGLU.memcpy2(UniformBufferObject.proj2, handle, (long) UniformBufferObject.proj2.length <<2);
        }

        static void createDescriptorPool() {
            {
                VkDescriptorPoolSize.Buffer poolSize = VkDescriptorPoolSize.calloc(2, stack.stack());
//                        .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
//                        .descriptorCount(PipeLine.swapChainImages.length);
                VkDescriptorPoolSize uniformBufferPoolSize = poolSize.get(0)
                        .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(SwapChainSupportDetails.swapChainImages.length);

                VkDescriptorPoolSize textureSamplerPoolSize = poolSize.get(1)
//                        .type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                        .descriptorCount(SwapChainSupportDetails.swapChainImages.length);
                VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.calloc(stack.stack()).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                        .pPoolSizes(poolSize)
                        .maxSets(PipeLine.swapChainImages.length);
                PipeLine.doPointerAllocSafe2(poolCreateInfo, PipeLine.capabilities.vkCreateDescriptorPool, descriptorPool);
//               descriptorPool=aLong[0];
            }
        }

        static void createDescriptorSets() {
            {
                LongBuffer layouts = memLongBuffer(stack.stack().getAddress(), SwapChainSupportDetails.swapChainImages.length);
                for(int i = 0;i < layouts.capacity();i++) {
                    layouts.put(i, descriptorSetLayout);
                }

                VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc().sType$Default();
//                allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
                allocInfo.descriptorPool(descriptorPool[0]);
                memPutLong(allocInfo.address() + VkDescriptorSetAllocateInfo.PSETLAYOUTS, memAddressSafe(layouts));
                memPutInt(allocInfo.address() + VkDescriptorSetAllocateInfo.DESCRIPTORSETCOUNT, layouts.remaining());

//                long[] pDescriptorSets = new long[(SwapChainSupportDetails.swapChainImages.length)];
                nmemFree(allocInfo.address());
                VK10.vkAllocateDescriptorSets(device, allocInfo, descriptorSets);

                long bufferInfo = nmemCallocChecked(1, VkDescriptorBufferInfo.SIZEOF);
                VkDescriptorBufferInfo.noffset(bufferInfo, 0);
                VkDescriptorBufferInfo.nrange(bufferInfo, UniformBufferObject.capacity);

                VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack.stack())
                        .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                        .imageView(PipeLine.textureImageView[0])
                        .sampler(PipeLine.textureSampler[0]);

                VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(2, stack.stack());

                VkWriteDescriptorSet vkWriteDescriptorSet = descriptorWrites.get(0).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstBinding(0)
                        .dstArrayElement(0)
                        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(1);
                memPutAddress(vkWriteDescriptorSet.address() + VkWriteDescriptorSet.PBUFFERINFO, bufferInfo);

                VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(1).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .dstBinding(1)
                        .dstArrayElement(0)
                        .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                        .descriptorCount(1)
                        .pImageInfo(imageInfo);


                nmemFree(bufferInfo);

                for(int i = 0;i < descriptorSets.length;i++) {


                    memPutLong(bufferInfo + VkDescriptorBufferInfo.BUFFER, uniformBuffers[i]);

                    memPutLong(vkWriteDescriptorSet.address() + VkWriteDescriptorSet.DSTSET, descriptorSets[i]);
                    memPutLong(samplerDescriptorWrite.address() + VkWriteDescriptorSet.DSTSET, descriptorSets[i]);

                    vkUpdateDescriptorSets(device, descriptorWrites , null);

//                    descriptorSets[i]=descriptorSet;

                }
//                System.arraycopy(pDescriptorSets, 0, descriptorSets, 0, pDescriptorSets.length);
            }
        }


    }

    static final class Renderer
    {
        private static final int MAX_FRAMES_IN_FLIGHT = 2;
//        private static final int[] imagesInFlight = new int[MAX_FRAMES_IN_FLIGHT];
        private static final long[] AvailableSemaphore ={0};


        private static final long[] FinishedSemaphore = {0};
//        private static final long[] vkFenceA;
        private static final long[] vkFence = {0};


        private static final long address2;
        private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
        private static final long TmUt = 1000000000;
        private static final long pointerBuffer = (stack.stack().getAddress() + stack.stack().getSize());
//        private static final VKCapabilitiesDevice capabilities1 = Queues.graphicsQueue.getCapabilities();
//        private static final long vkAcquireNextImageKHR = PipeLine.capabilities.vkAcquireNextImageKHR;
        //                 vkWaitForFences(Queues.device, longs, true, UINT64_MAX);

        // Align address to the specified alignment
        private static final long address3 = stack.stack().getAddress() + stack.stack().getPointer() - 4 & -4L;
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


        private static final long VkPresentInfoKHR1;
        static int i;
        static int FenceStat;

        static {
            {

                //                FinishedSemaphore = PipeLine.doPointerAllocSafe(semaphoreInfo, PipeLine.capabilities.vkCreateSemaphore);

                /* for(int i = 0;i < MAX_FRAMES_IN_FLIGHT;i++)*/
                {
//                    long vkSemaphoreCreateInfo = doAbsCalloc2(VkSemaphoreCreateInfo.SIZEOF,VkSemaphoreCreateInfo.ALIGNOF)-4L;
                    long vkSemaphoreCreateInfo = nmemCallocChecked(1, VkSemaphoreCreateInfo.SIZEOF);
                    VkSemaphoreCreateInfo.nsType(vkSemaphoreCreateInfo, VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
                    PipeLine.doPointerAllocSafeExtrm2(vkSemaphoreCreateInfo, PipeLine.capabilities.vkCreateSemaphore, AvailableSemaphore);
                    PipeLine.doPointerAllocSafeExtrm2(vkSemaphoreCreateInfo, PipeLine.capabilities.vkCreateSemaphore, FinishedSemaphore);
                    nmemFree(vkSemaphoreCreateInfo);

//                    long vkFenceCreateInfo = doAbsCalloc(VkFenceCreateInfo.SIZEOF,VkFenceCreateInfo.ALIGNOF);
                    long vkFenceCreateInfo = nmemCallocChecked(1,VkFenceCreateInfo.SIZEOF);
                    VkFenceCreateInfo.nsType(vkFenceCreateInfo, VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
                    VkFenceCreateInfo.nflags(vkFenceCreateInfo, VK_FENCE_CREATE_SIGNALED_BIT);
                    PipeLine.doPointerAllocSafeExtrm2(vkFenceCreateInfo, PipeLine.capabilities.vkCreateFence, vkFence);
//                    vkFence[1] = PipeLine.doPointerAllocSafeExtrm(vkFenceCreateInfo, PipeLine.capabilities.vkCreateFence);
//                    vkFence[2] = PipeLine.doPointerAllocSafeExtrm(vkFenceCreateInfo, PipeLine.capabilities.vkCreateFence);
                    nmemFree(vkFenceCreateInfo);
//                    vkFenceA = PipeLine.doPointerAllocSafeExtrm(vkFenceCreateInfo, PipeLine.capabilities.vkCreateFence);


//

//                    longs2 = stack.stack().longs(vkFence);
//                    pFences = memAddress0(longs2);
                    // Align address to the specified alignment
//                    long address = (stack.stack().getAddress() + stack.stack().getPointer() - 8) & -8L;
                    long address21 = stack.stack().ncalloc(VkSubmitInfo.ALIGNOF, 1, VkSubmitInfo.SIZEOF);//(ncalloc());
                    VkSubmitInfo.nsType(address21, VK_STRUCTURE_TYPE_SUBMIT_INFO);
                    VkSubmitInfo.nwaitSemaphoreCount(address21, 1);
                    VkSubmitInfo.npNext(address21, NULL);

                    stack.stack().setPointer((stack.stack().getPointer() - 8));

                    memPutLong(address21 + VkSubmitInfo.PWAITSEMAPHORES, memAddress0(memLongBuffer((stack.stack().getPointerAddress()), 1).put(0, AvailableSemaphore)));
                    memPutLong(address21 + VkSubmitInfo.PWAITDSTSTAGEMASK, memAddress0(stack.stack().ints(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT)));
                    memPutLong(address21 + VkSubmitInfo.PSIGNALSEMAPHORES, GLU2.theGLU.getPointer(FinishedSemaphore,8));
                    address2= address21+VkSubmitInfo.STYPE;
                    // Align address to the specified alignment


                    VkPresentInfoKHR1= nmemCallocChecked(1, VkPresentInfoKHR.SIZEOF);
                    memSet(VkPresentInfoKHR1, 0, VkPresentInfoKHR.SIZEOF);
                    VkPresentInfoKHR.nsType(VkPresentInfoKHR1, VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore[0]));
                    memPutInt(VkPresentInfoKHR1 + VkPresentInfoKHR.SWAPCHAINCOUNT, 1);
                    memPutLong(address21 + VkSubmitInfo.PCOMMANDBUFFERS, (pointerBuffer));
                    VkSubmitInfo.ncommandBufferCount(address21, 1);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PIMAGEINDICES, address3);
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PSWAPCHAINS, memAddress0(memLongBuffer(stack.stack().getAddress(), 1).put(0, SwapChainSupportDetails.swapChain)));
                    nmemFree(VkPresentInfoKHR1);
//                    VkPresentInfoKHR.nsType(VkPresentInfoKHR1, VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
//                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore));
//                    memPutInt(VkPresentInfoKHR1 + VkPresentInfoKHR.SWAPCHAINCOUNT, 1);
                    // Align address to the specified alignment
                    VkSubmitInfo.validate(address21);
                    VkPresentInfoKHR.validate(VkPresentInfoKHR1);
                }

            }

        }

        private static long ncalloc()
        {
            int  bytes   = VkSubmitInfo.ALIGNOF * VkSubmitInfo.SIZEOF;
            // Align address to the specified alignment


            long address = (stack.stack().getAddress() + stack.stack().getPointer() - VkSubmitInfo.SIZEOF) & ~Integer.toUnsignedLong(VkSubmitInfo.ALIGNOF - 1);
            stack.stack().setPointer((int)(address - stack.stack().getAddress()));



            memSet(address, 0, bytes);
            return address;
        }

        private static long doAbsCalloc(int sizeof, int alignof)
        {
            return (stack.stack().getAddress() + stack.stack().getPointer() - sizeof) & -alignof;
        }

        /* todo: Possible me issue: uusing msiafterburner sems yo dratically and perminantly improve the Frarate even after it is closed:
            pehaes a mem sync effect introduced by the OSD overlay which is helping to synchronise/talise the queue/Pipeline/CommandBufferSubmissions e.g. .etc i.e.
            [might be afence/Blocking.Brarier problem as intially had WaiFencesand rdetfencescalls removed intially
        */
        static void drawFrame() {


                {
                    //Must Push andPop/Pop/Push at the Exact Currect Intervals
                    i += JNI.callPPI(device.address(), vkFence.length, vkFence, PipeLine.capabilities.vkResetFences);
//                    i+= vkWaitForFences(device, vkFence, false, TmUt);

                    i+=nvkAcquireNextImageKHR(device, SwapChainSupportDetails.swapChain, TmUt, AvailableSemaphore[0], VK_NULL_HANDLE, address3);


                    UniformBufferObject.updateUniformBuffer(PipeLine.currentFrame);





//                 PipeLine.imagesInFlight.get(PipeLine.currentFrame);
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



                    memPutLong(pointerBuffer, PipeLine.commandBuffers[PipeLine.currentFrame].address());

                    i += nvkQueueSubmit(Queues.graphicsQueue, 1, address2, vkFence[0]);

                    i += VK10.vkWaitForFences(device, vkFence, false, TmUt); //Don;t know why a Waiti sneeded here: lil;ey for Vailable/Finished Sempahore as Queue present needs both fences and Semphors to all be Synchronised prior to SHowing teh Farmebuffer/Swa[Chain
//                    FenceStat += vkGetFenceStatus(device, vkFence[0]); //For some reson callinf fence Status seem to help prevebt exeution erros/valdiatiomne rrors the  reserting Siad fences at the begging of the next DrawLoopCall?iteration/CallLoop/Invokation FrameAquisition e.g. e.g.
                    memPutLong(VkPresentInfoKHR1 + VkPresentInfoKHR.PWAITSEMAPHORES, (AvailableSemaphore[0]));


                    i+= nvkQueuePresentKHR(Queues.presentQueue, VkPresentInfoKHR1);
//                   i+= vkResetFences(Queues.device, vkFenceA);


                    PipeLine.currentFrame = (PipeLine.currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
//                 System.out.println(stack.stack().getPointer());
                    PipeLine.frps++;

                }
        }


    }

}

