package vkutils.setup;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.JNI;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.jemalloc.JEmalloc;
import org.lwjgl.vulkan.*;
import vkutils.renderer2;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeWin32.glfwGetWin32Window;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.JNI.callPJPV;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.setup.Queues.device;
import static vkutils.setup.Queues.surface;

public final class VkUtils2 {
    static final MemSysm MemSys = new MemSysm();
public static final long window;
    //        static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);
    private static final Set<String> DEVICE_EXTENSIONS=new HashSet<>();
    private static VkInstance vkInstance;
//    private static final long[] pDebugMessenger = new long[1];
    //    X(),
    //    Y
    //    KEY(1)
    private static final long monitor=0;
    private static final boolean ENABLE_VALIDATION_LAYERS = false;
    private static final Set<String> VALIDATION_LAYERS;
    private static final boolean debug = false;
    private static final boolean checks = true;

    static {

//            Set<String> set = new HashSet<>();
        DEVICE_EXTENSIONS.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
//            DEVICE_EXTENSIONS = set;


        Configuration.DISABLE_CHECKS.set(!checks);
        Configuration.DISABLE_FUNCTION_CHECKS.set(!checks);
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
//            VALIDATION_LAYERS.add("VK_LAYER_LUNARG_monitor");
        } else VALIDATION_LAYERS = null;
        if(!glfwInit()) throw new RuntimeException("Cannot initialize GLFW");

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_ROBUSTNESS, GLFW_NO_ROBUSTNESS);
        glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
        glfwWindowHint(GLFW_CONTEXT_RELEASE_BEHAVIOR , GLFW_RELEASE_BEHAVIOR_NONE);


        window = glfwCreateWindow(854, 480, "VKMod", monitor, 0);


        if(window == NULL) throw new RuntimeException("Cannot create window");

        glfwSetWindowShouldClose(window, false);
        glfwMakeContextCurrent(window);

    }

    //Stolen From Here: https://github.com/Naitsirc98/Vulkan-Tutorial-Java/blob/master/src/main/java/javavulkantutorial/Ch02ValidationLayers.java
    private static boolean checkValidationLayerSupport() {

        {
            //todo:fix Intbuffer being inlined eincirertcly and malloc one Int.value eachat eahc method call/usage incorrectly, cauisng validtaion layerst be broken/which in tandme iwth teh 443.41 dirver bug where valditaion layers do nto function corertcly untill full farembuffer/Piplelinebuffer>Framebuffer is implemneted and others e.g. etc i.e.
            final IntBuffer ints = MemSysm.ints(0);
            vkEnumerateInstanceLayerProperties(ints, null);
            VkLayerProperties.Buffer availableLayers = VkLayerProperties.create(MemSysm.malloc(VkLayerProperties.SIZEOF*ints.get(0)), ints.get(0));
            vkEnumerateInstanceLayerProperties(ints, availableLayers);
            Set<String> availableLayerNames = availableLayers.stream()
                    .map(VkLayerProperties::layerNameString)
                    .collect(toSet());
            for (int i = 0; i < availableLayerNames.size(); i++) {
                System.out.println(Arrays.toString(availableLayerNames.iterator().next().getBytes(StandardCharsets.UTF_8)));
            }
            MemSysm.Memsys2.free(ints);


            return availableLayerNames.containsAll(VALIDATION_LAYERS);
        }
    }

    private static void createInstance()
    {
        System.out.println("Creating Instance");
        if (ENABLE_VALIDATION_LAYERS && !checkValidationLayerSupport())
        {
            System.out.println(MemSysm.stack);
            throw new RuntimeException("Validation requested but not supported");
        }
        IntBuffer a = MemSysm.ints(EXTValidationFeatures.VK_VALIDATION_FEATURE_ENABLE_BEST_PRACTICES_EXT, EXTValidationFeatures.VK_VALIDATION_FEATURE_ENABLE_DEBUG_PRINTF_EXT, EXTValidationFeatures.VK_VALIDATION_FEATURE_ENABLE_GPU_ASSISTED_EXT, EXTValidationFeatures.VK_VALIDATION_FEATURE_ENABLE_GPU_ASSISTED_RESERVE_BINDING_SLOT_EXT);
        VkValidationFeaturesEXT extValidationFeatures = VkValidationFeaturesEXT.create(MemSysm.calloc(VkValidationFeaturesEXT.SIZEOF)).sType$Default()
                .pEnabledValidationFeatures(a);

        VkApplicationInfo vkApplInfo = VkApplicationInfo.create(MemSysm.malloc3(VkApplicationInfo.SIZEOF)).sType$Default()
                //memSet(vkApplInfo, 0,VkApplicationInfo.SIZEOF);
                .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(MemSysm.stack.UTF8Safe("VKMod"))
                .applicationVersion(VK_MAKE_VERSION(1, 0, 0))
                .pEngineName(MemSysm.stack.UTF8Safe("No Engine"))
                .engineVersion(VK_MAKE_VERSION(1, 0, 0))
                .apiVersion(VK12.VK_API_VERSION_1_2);

//        MemSysm.Memsys2.free(a);
        MemSysm.Memsys2.free(vkApplInfo);
        //nmemFree(vkApplInfo);


        VkInstanceCreateInfo InstCreateInfo = VkInstanceCreateInfo.create(MemSysm.malloc3(VkInstanceCreateInfo.SIZEOF)).sType$Default();
//                InstCreateInfo.sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
        memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.PAPPLICATIONINFO, vkApplInfo.address());
        PointerBuffer glfwExtensions = getRequiredExtensions();
        InstCreateInfo.ppEnabledExtensionNames(glfwExtensions);
        InstCreateInfo.pNext(extValidationFeatures.address());

        InstCreateInfo.enabledExtensionCount();
        InstCreateInfo.enabledLayerCount();
        if(ENABLE_VALIDATION_LAYERS) {
            memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.PPENABLEDLAYERNAMES, (asPointerBuffer()));
            memPutLong(InstCreateInfo.address() + VkInstanceCreateInfo.ENABLEDLAYERCOUNT, VALIDATION_LAYERS.size());
        }
//            else InstCreateInfo.pNext(NULL);

        PointerBuffer instancePtr = memPointerBuffer(MemSysm.address, 1);
        vkCreateInstance(InstCreateInfo, MemSysm.pAllocator, instancePtr);


        vkInstance = new VkInstance(instancePtr.get(0), InstCreateInfo);
        getVersion();
    }

    private static void getVersion()
    {
        //TODO: https://stackoverflow.com/questions/65382288/using-vkenumerateinstanceversion-to-get-exact-vulkan-api-version
        long FN_vkEnumerateInstanceVersion = vkGetInstanceProcAddr(vkInstance, "vkEnumerateInstanceVersion");
        int[] versionEnumeration = {0};
        VK12.vkEnumerateInstanceVersion(versionEnumeration);
        System.out.println(versionEnumeration[0]);
        int instanceVersion = versionEnumeration[0];
        if(FN_vkEnumerateInstanceVersion == 0)
            System.out.println(instanceVersion);
        else
        {

            System.out.println("Vulkan: "+VK_VERSION_MAJOR(instanceVersion)+"."+VK_VERSION_MINOR(instanceVersion)+"."+(VK_VERSION_PATCH(instanceVersion)));
        }
    }

    //TODO: BestPractices + Make sure/Fix alignment issues so the ValdiatioN layers.DepugMessnegder/DebugPutput hjas enough space on The Stack./COntingous memAllocator/memsym

    private static boolean isDeviceSuitable(VkPhysicalDevice device) {

        boolean extensionsSupported = checkDeviceExtensionSupport(device);
        boolean swapChainAdequate = false;
        if(extensionsSupported) {
            SwapChainSupportDetails.querySwapChainSupport(device);
            swapChainAdequate = SwapChainSupportDetails.formats.hasRemaining() && SwapChainSupportDetails.presentModes.hasRemaining();
        }

        Queues.enumerateDetermineQueueFamilies(device);
		return /* Queues.isComplete() && */ extensionsSupported && swapChainAdequate;
    }

    static boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {
        IntBuffer extensionCount = MemSysm.ints(0);
        nvkEnumerateDeviceExtensionProperties(device, NULL, memAddress0(extensionCount), NULL);
        VkExtensionProperties.Buffer availableExtensions= VkExtensionProperties.malloc(extensionCount.get(0), MemSysm.stack);
        nvkEnumerateDeviceExtensionProperties(device, NULL, memAddress0(extensionCount), availableExtensions.address0());
        availableExtensions.free();
        MemSysm.Memsys2.free(extensionCount);
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
            PointerBuffer extensions = memPointerBuffer(MemSysm.malloc(size), size);
            extensions.put(glfwExtensions);
            extensions.put(MemSysm.stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
            // Rewind the buffer before returning it to reset its position back to 0
            return extensions.rewind();
        }

        return glfwExtensions;
    }

    private static void pickPhysicalDevice()
    {
        System.out.println("Picking Physical Device");


        IntBuffer deviceCount = MemSysm.ints(1);
        vkEnumeratePhysicalDevices(vkInstance, deviceCount, null);
        if(deviceCount.get(0) == 0) throw new RuntimeException("Failed to find GPUs with Vulkan support");
        int size = deviceCount.get(0);
        PointerBuffer ppPhysicalDevices = memPointerBuffer(MemSysm.address, size);
        vkEnumeratePhysicalDevices(vkInstance, deviceCount, ppPhysicalDevices);
        for(int i = 0;i < ppPhysicalDevices.capacity();i++)
        {
            VkPhysicalDevice device = new VkPhysicalDevice(ppPhysicalDevices.get(i), vkInstance);
            if(isDeviceSuitable(device)) {
                MemSysm.Memsys2.free(ppPhysicalDevices);
                Queues.physicalDevice = device;
                return;
            }
        }
        throw new RuntimeException("Failed to find a suitable GPU");
    }

    private static void createSurface()
    {
        System.out.println("Creating Surface");

        long createSurfaceInfo = MemSysm.malloc3(VkWin32SurfaceCreateInfoKHR.SIZEOF);
        VkWin32SurfaceCreateInfoKHR.nsType(createSurfaceInfo, VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR);
        VkWin32SurfaceCreateInfoKHR.nhwnd(createSurfaceInfo, glfwGetWin32Window(window));
        VkWin32SurfaceCreateInfoKHR.nhinstance(createSurfaceInfo, vkInstance.address());

        if (GLFWVulkan.glfwCreateWindowSurface(vkInstance, window, MemSysm.pAllocator, surface) != VK_SUCCESS) throw new RuntimeException("failed to create window surface!");

        //surface = surface_[0];

    }

    private static long asPointerBuffer() {


        int size = VALIDATION_LAYERS.size();
        PointerBuffer buffer = MemSysm.stack.mallocPointer(size << Pointer.POINTER_SHIFT);//(vkutils.MemSysm.mallocB(size, size << Pointer.POINTER_SHIFT));

        for (String s : VALIDATION_LAYERS) {
            buffer.put(MemSysm.stack.UTF8(s));
        }
        MemSysm.Memsys2.free(buffer);
        return buffer.rewind().address0();

    }

    public static void extracted()
    {
        {
        createInstance();
//        setupDebugMessenger();
        createSurface();
        pickPhysicalDevice();
        createLogicalDevice();
        SwapChainSupportDetails.createSwapChain();
        SwapChainSupportDetails.createImageViews();
        PipeLine.createRenderPasses();
        //renderer2.Buffers.createDescriptorSetLayout();
        PipeLine.createGraphicsPipelineLayout();

        PipeLine.createCommandPool();
        Texture.createDepthResources();
        SwapChainSupportDetails.createFramebuffers();
        Texture.createTextureImage();
        Texture.createTextureImageView();
//        renderer2.Buffers.createTextureSampler();
            Buffers.createVertexBufferStaging();
            Buffers.createIndexBuffer();
            Buffers.creanupBufferStaging();
        UniformBufferObject.createUniformBuffers();
        UniformBufferObject.createDescriptorPool();
        UniformBufferObject.createDescriptorSets();
        Buffers.createCommandBuffers();
            Buffers.createVkEvents();
    }
        MemSysm.stack.pop();

        MemSysm.resetFrame();
        System.out.println("Real Alloc: "+JEmalloc.nje_sallocx(MemSysm.address,0));

    }

    public static void _mainDeletionQueue()
    {
        doDestroyFreeAlloc((Texture.vkImage), Buffers.capabilities.vkDestroyImage);
        doDestroyFreeAlloc((Buffers.vkAllocMemory), Buffers.capabilities.vkFreeMemory);
//        doDestroyFreeAlloc(UniformBufferObject.textureSampler, Buffers.capabilities.vkDestroySampler);
        doDestroyFreeAlloc((UniformBufferObject.textureImageView), Buffers.capabilities.vkDestroyImageView);

        {
            vkDestroySemaphore(device, renderer2.Renderer2.AvailableSemaphore, MemSysm.pAllocator);
        }
        vkDestroyDescriptorSetLayout(device, (UniformBufferObject.descriptorSetLayout), MemSysm.pAllocator);
        vkDestroyDescriptorPool(device, (UniformBufferObject.descriptorPool), MemSysm.pAllocator);

        //vkDestroyCommandPool(device, memGetLong(Buffers.commandPool), MemSysm.pAllocator);
           SwapChainSupportDetails.swapChainImages.free();
        for (int i = 0; i < 3; i++)
        {
            vkDestroyImageView(device, SwapChainSupportDetails.swapChainImageViews.get(i), MemSysm.pAllocator);
//            vkFreeCommandBuffers(device, Buffers.commandPool, Buffers.commandBuffers[i]);
            vkDestroyFramebuffer(device, SwapChainSupportDetails.swapChainFramebuffers.get(i), MemSysm.pAllocator);
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

        vkDestroyRenderPass(device, memGetLong(SwapChainSupportDetails.renderPass), MemSysm.pAllocator);


    }

    private static void doDestroyFreeAlloc(long textureImageView, long vkDestroyImageView)
    {
        System.out.println("Destroying:+ " + textureImageView);
        callPJPV(device.address(), memGetLong(textureImageView), NULL, vkDestroyImageView);
    }

    public static void cleanup() {


//            for (int i =0; i< Renderer.MAX_FRAMES_IN_FLIGHT;i++)
        glfwPostEmptyEvent();
        vkDeviceWaitIdle(device);
//            vkWaitForFences(Queues.device, Renderer.vkFence, true, 1000000000);

        _mainDeletionQueue();

        vkDestroyDevice(device, MemSysm.pAllocator);
        KHRSurface.vkDestroySurfaceKHR(vkInstance, surface[0], MemSysm.pAllocator);
        vkDestroyInstance(vkInstance, MemSysm.pAllocator);
        /*glfwDestroyWindow(window);*/


//            vkDestroyInstance(vkInstance, pAllocator);


        glfwDestroyWindow(window);

        glfwTerminate();
    }

    private static void createLogicalDevice() {

        {
            int[] uniqueQueueFamilies = IntStream.of(Queues.graphicsFamily, Queues.presentFamily).distinct().toArray();


//                Queues.findQueueFamilies(Queues.physicalDevice);
            //TODO: Fix bug with NULL/Missing.Invalid Queues
            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.create(MemSysm.malloc(VkDeviceQueueCreateInfo.SIZEOF), uniqueQueueFamilies.length).sType$Default();

            for(int i = 0; i < uniqueQueueFamilies.length; i++) {
                queueCreateInfos.queueFamilyIndex(uniqueQueueFamilies[i]);
                queueCreateInfos.pQueuePriorities(MemSysm.stack.floats(1.0f));
            }
            VkPhysicalDeviceVulkan12Features deviceVulkan12Features = VkPhysicalDeviceVulkan12Features.create(MemSysm.address).sType$Default()
                    .imagelessFramebuffer(true)
                    .descriptorBindingPartiallyBound(true);

            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.create(MemSysm.address);

            VkPhysicalDeviceFeatures2 deviceFeatures2 = VkPhysicalDeviceFeatures2.create(MemSysm.malloc3(VkPhysicalDeviceFeatures2.SIZEOF)).sType$Default()
                    .pNext(deviceVulkan12Features)
                    .features(deviceFeatures);



            //.fillModeNonSolid(true) //dneeded to adres valditaion errors when using VK_POLIGYON_MODE_LINE or POINT
            //.robustBufferAccess(true);
//                        .geometryShader(true);
//                        .pipelineStatisticsQuery(true)
//                        .alphaToOne(false);
            VK11.vkGetPhysicalDeviceFeatures2(Queues.physicalDevice, deviceFeatures2);
            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.create(MemSysm.malloc3(VkDeviceCreateInfo.SIZEOF)).sType$Default()
//                        .pNext(deviceVulkan12Features)
                    .pNext(deviceFeatures2);
//                                .pEnabledFeatures(deviceFeatures);

            createInfo.pQueueCreateInfos(queueCreateInfos);
            MemSysm.Memsys2.free(queueCreateInfos);
            // queueCreateInfoCount is automatically set

//                memPutLong(createInfo.address() + VkDeviceCreateInfo.PENABLEDFEATURES, deviceFeatures2.address());

            PointerBuffer value = asPointerBuffer(DEVICE_EXTENSIONS);
            memPutLong(createInfo.address() + VkDeviceCreateInfo.PPENABLEDEXTENSIONNAMES, value.address0());
            memPutInt(createInfo.address() + VkDeviceCreateInfo.ENABLEDEXTENSIONCOUNT, value.remaining());

            if(ENABLE_VALIDATION_LAYERS) {
                createInfo.ppEnabledLayerNames(asPointerBuffer(VALIDATION_LAYERS));
            }
            device = new VkDevice(doPointerAlloc(createInfo), Queues.physicalDevice, createInfo);


            setupQueues();
        }
    }

    private static void setupQueues()
    {

        nvkGetDeviceQueue(device, Queues.graphicsFamily, 0,MemSysm.address);
        Queues.graphicsQueue = new VkQueue(memGetLong(MemSysm.address), device);

        nvkGetDeviceQueue(device, Queues.presentFamily, 0,MemSysm.address);
        Queues.presentQueue = new VkQueue(memGetLong(MemSysm.address), device);

    }

    private static long doPointerAlloc(@NotNull Struct allocateInfo)
    {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
        long[] pDummyPlacementPointerAlloc = {0};
        JNI.callPPPPI(Queues.physicalDevice.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, Queues.physicalDevice.getCapabilities().vkCreateDevice);
        return pDummyPlacementPointerAlloc[0];
    }

    private static @NotNull PointerBuffer asPointerBuffer(@NotNull Collection<String> collection) {

//            MemoryStack stack = stackGet();

        int size = collection.size();
        PointerBuffer buffer = memPointerBuffer(MemSysm.stack.nmalloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT), size);

        for (String s : collection) {
            ByteBuffer byteBuffer = MemSysm.stack.UTF8(s);
            buffer.put(byteBuffer);
        }

        return buffer.rewind();
    }






//        private static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);


}