package vkutils;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.*;
import org.lwjgl.system.libc.LibCStdlib;
import org.lwjgl.vulkan.VkAllocationCallbacks;

import static org.lwjgl.system.JNI.callPPPPI;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_NOT_READY;
import static org.lwjgl.vulkan.VK10.VK_TIMEOUT;
import static vkutils.VkUtils2.Queues.device;

public record MemSys(MemoryStack stack, VkAllocationCallbacks pAllocator) implements MemoryAllocator {

//    protected static final VkAllocationCallbacks pAllocator = null;
    static final long address = GLU2.theGLU.alloc(Pointer.POINTER_SIZE);//nmemAllocChecked(Pointer.POINTER_SIZE);
    static final long[] pDummyPlacementPointerAlloc = {0};

    static void checkCall(int callPPPPI)
    {
        switch (callPPPPI)
        {
            case VK_NOT_READY -> throw new RuntimeException("Not ready!");
            case VK_TIMEOUT -> throw new RuntimeException("Bad TimeOut!");
            default -> {
            }
        }
    }

    static void doPointerAllocSafeExtrm2(long imageInfo, long vkCreateImage, long[] a)
    {
        checkCall(JNI.callPPPPI(VkUtils2.Queues.device.address(), imageInfo, NULL, a, vkCreateImage));

    }

    //todo: modfied form of getHandle specificaly Deisgned to handle Dierct access to UniformBuffer Modifictaion with Alternative CommandBuffer Execution, All memory Address Loctaions smust be aligned to 512 exactly to avoid Rendering Artifacts./nstablity
    static long getHandle(int address1)
    {
        //            System.out.println(l);
        return memGetLong(address)+(Integer.toUnsignedLong(address1*512));
    }

    static void doPointerAllocSafe2(@NotNull Struct allocateInfo, long vkCreateBuffer, long[] a) {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
        System.out.println("Attempting to Call: ->"+allocateInfo);
        Checks.check(allocateInfo.address());
        checkCall(callPPPPI(VkUtils2.Queues.device.address(), allocateInfo.address(), NULL, a, vkCreateBuffer));
    }

    static long doPointerAllocSafeExtrm(long allocateInfo, long vkCreateBuffer) {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator(), pVertexBufferMemory);
        System.out.println("Attempting to Call: ->"+ memGetLong(allocateInfo));
        Checks.check(allocateInfo);
        checkCall(callPPPPI(device.address(), allocateInfo, NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));

        return pDummyPlacementPointerAlloc[0];
    }

    //todo: generates randon Handles use with specific function.sAPI methdos that require it, avoiding teh need to use a pointerBuffer to do so instead
    static long getHandle()
    {
        return memGetLong(address);
    }

    /*todo: Java cannot handle/Wiriting>erading to references are not fully initlaised unliek C++ without wiring throiwng an NullPOinterException imemdiately, hense why this function was setup to make hanlding rfeernces less of a nuicience with javaa
        * the static inle Array acts as a menas of containg references when written to after an API call from Vulkan, which allow for reaidnga dn wiritng to references. dierctly without the need for intermediate structis such as Pointer/LongBuffers
        * and reducing pressure on the MemeoryStack SImulatbeously/In the Process
        * Unfortuately this lilely will never get as fast as the NCthe nativ eimpelation fo wiritng to refercnes as with posible with C++ as well aa the interent overead of function body calls and pasing paraaters
        * a fueld refercne is also used just in case dynamically allating with new is less effcienct that sinly wiritng to a static final Reference/memory adress instead
     */
    static long doPointerAllocSafe(@NotNull Struct allocateInfo, long vkCreateBuffer) {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator(), pVertexBufferMemory);
        System.out.println("Attempting to Call: ->"+allocateInfo);
        Checks.check(allocateInfo.address());
        checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));
        return pDummyPlacementPointerAlloc[0];
    }

    public long malloc(int a)
    {
        return stack.ncalloc(Pointer.POINTER_SHIFT, a,  Pointer.POINTER_SIZE);

    }
    static void freemalloc(int a)
    {

    }

    public long calloc(int size)
    {
        return MemoryStack.stackPush().nmalloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT);
//        return nmemCallocChecked(1, 8);
    }

    public long nmalloc(int alignof, int sizeof)
    {
        return stack.nmalloc(alignof, sizeof);
    }

    @Override
    public long getMalloc()
    {
        return 0;
    }

    @Override
    public long getCalloc()
    {
        return 0;
    }

    @Override
    public long getRealloc()
    {
        return 0;
    }

    @Override
    public long getFree()
    {
        return 0;
    }

    @Override
    public long getAlignedAlloc()
    {
        return 0;
    }

    @Override
    public long getAlignedFree()
    {
        return 0;
    }

    @Override
    public long malloc(long size)
    {
        return memAddress0(LibCStdlib.malloc(size));
    }

    @Override
    public long calloc(long num, long size)
    {
        return 0;
    }

    @Override
    public long realloc(long ptr, long size)
    {
        return 0;
    }

    @Override
    public void free(long ptr)
    {

    }

    @Override
    public long aligned_alloc(long alignment, long size)
    {
        return 0;
    }

    @Override
    public void aligned_free(long ptr)
    {

    }

}
