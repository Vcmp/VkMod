package vkutils.setup;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.jemalloc.JEmalloc;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.setup.Queues.device;


public final class MemSysm /*implements MemoryAllocator*/ {

    //    protected static final VkAllocationCallbacks pAllocator = null;
    //int m = JEmalloc.je_mallctl(),
    public static final MemoryStack stack = MemoryStack.stackPush();
    static final VkAllocationCallbacks pAllocator = null;
    private static final long[] pDummyPlacementPointerAlloc = {0};
//    private static long sizeOf;
    private static long frame;
    static long stacks;
    static final HashMap<Long, Long> tracker = new HashMap<>();
    public static final long size = 512L;//0x1FF;
    public static final long address = JEmalloc.nje_malloc(size);//+VkMemoryRequirements.SIZEOF;//nmemAllocChecked(Pointer.POINTER_SIZE);
        static
        {
            System.out.println("BAse Address: "+address);
            JEmalloc.nje_posix_memalign(address, 64, sizeof(address));
            JEmalloc.nje_dallocx(address, JEmalloc.MALLOCX_ARENA(2));
            System.out.println("BAse Address: "+address);

        }

    public static long calloc(long size)
    {
        final long l = JEmalloc.nje_calloc(1, size);
        if(tracker.getOrDefault(l, 0L)!=0)
        {
            System.err.println("WARN:C: Is Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);
            return l;
        }
        System.out.println("AllocatingC: " + size + " With Capacity: " + 1 + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        stacks += size;
        tracker.put(l, size);
        return l;
        //return LibCStdlib.ncalloc(num, size);
    }
    public static long calloc(long num, long size)
    {
        final long l = JEmalloc.nje_calloc(num, size);
        if(tracker.getOrDefault(l, 0L)!=0)
        {
            System.err.println("WARN:C: Is Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);
            return l;
        }
        System.out.println("AllocatingC: " + size + " With Capacity: " + num + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        stacks += size;
        tracker.put(l, size);
        return l;
        //return LibCStdlib.ncalloc(num, size);
    }

    public static long malloc(long size)
    {
        final long l = JEmalloc.nje_malloc(size);
        if(tracker.getOrDefault(l, 0L)==size)
        {
            System.err.println("WARN:M: Is Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);
            return l;
        }
        System.out.println("AllocatingM: " + size + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        System.out.println("Allocated Size: " + sizeof(l));

        stacks += size;
        tracker.put(l, size);
        return l;//memAddress0(LibCStdlib.malloc(size));
//        return LibCStdlib.nmalloc(Integer.toUnsignedLong(size));//memAddress0(LibCStdlib.malloc(size));
    }//Exploit Java freeing;/Dealloctaing/freeing/removing from Heap obejsts intilaised in fuctions nor returned as an arument.paraamter.object to aovid teh need to explcitly free/rrset the Frame.pffset.Indicies/rlatvieoptiianing/mpelemnst/ocucurneces.//occupations/Ranges/wodtsh./offsets/Psootions/atcual Adresses/initialistaions/Atcua;istaions tracked alloctaions

    private static void getOffset()
    {
        System.out.println(JEmalloc.nje_malloc_usable_size(address+frame)+"-->"+frame);
    }

    public static long malloc3(long size)
    {
        if(frame>size)
        {
//            throw new UnsupportedOperationException("No Enough Memory: "+ Thread.currentThread().getStackTrace()[2]);
        }
        getOffset();
        frame += size;
         long l = address + (frame - size);
         System.out.println(l);
        l = alignAs(l);
        if(sizeof(l) > size)
        {
           System.err.println(sizeof(l)+"="+size);
        }
        System.out.println("AlloctaionMalloc3: "+l+"--->"+Thread.currentThread().getStackTrace()[2]);
        return l;
    }

    private static long alignAs(long l)
    {
        final long l1 = l % Pointer.POINTER_SIZE;
        if(l1 !=0)
        {
            System.err.println("Mem Alloctaion not Aligned!"+ l+ "Aligning As: --->"+(l-l1));
            l -= l1;
        }
        return l;
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
    static long doPointerAllocSafeA(@NotNull Pointer allocateInfo, long vkCreateBuffer) {
        System.out.println("Attempting to CallS: ->"+allocateInfo);
        Checks.check(allocateInfo.address());
        Memsys2.checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));
        return pDummyPlacementPointerAlloc[0];
    }
    static long doPointerAllocSafe(@NotNull Pointer allocateInfo, long vkCreateBuffer) {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator(), pVertexBufferMemory);
        Memsys2.free(allocateInfo);
        System.out.println("Attempting to CallS: ->"+allocateInfo+"-->"+Thread.currentThread().getStackTrace()[2]);
        Checks.check(allocateInfo.address());
//todo--->        Memsys2.checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, allocateInfo.address(), vkCreateBuffer));
        Memsys2.checkCall(callPPPI(device.address(), allocateInfo.address(), NULL, vkCreateBuffer));
        return memGetLong(allocateInfo.address());
    }

    static long doPointerAlloc5L(Pointer device, Pointer pipelineInfo)
    {
        Checks.check(pipelineInfo.address());
        Memsys2.checkCall(callPJPPPI(device.address(), VK_NULL_HANDLE, 1, pipelineInfo.address(), NULL, pipelineInfo.address(), Buffers.capabilities.vkCreateGraphicsPipelines));
        return memGetLong(pipelineInfo.address());
    }

    public static long sizeof(long address)
    {
        return JEmalloc.nje_sallocx(address, 0);
    }

    public static long malloc0()
    {
        System.out.println(JEmalloc.nje_malloc_usable_size(address));
//        JEmalloc.nje_sdallocx();
        return (address+frame);
    }

    public static long doPointerAllocSafe3(VkSemaphoreCreateInfo allocateInfo, long x)
    {
        Memsys2.free(allocateInfo);
        System.out.println("Attempting to CallS: ->"+allocateInfo+"-->"+Thread.currentThread().getStackTrace()[2]);
        Memsys2.checkCall(callPPPI(device.address(), allocateInfo.address(), NULL, x));
        return memGetLong(allocateInfo.address());
    }

    public static long mallocSafe(int sizeof)
    {
        return stack.nmalloc(Pointer.POINTER_SIZE, sizeof);
    }

    public static void resetFrame()
    {
        JEmalloc.nje_dallocx(address, 0);
        JEmalloc.nje_free(address);
        frame=0;
    }

    public static @NotNull IntBuffer ints(int... graphicsFamily)
    {
        final IntBuffer put = memByteBuffer(alignAs(address + (frame)), graphicsFamily.length * 4).asIntBuffer().put(graphicsFamily).flip();
        frame+=put.capacity();
        if(!put.isDirect())
        {
            throw new RuntimeException("Not Direct");
        }
        System.out.println("ByteBuf: "+memAddress0(put)+"+"+frame);
        return put;

    }

    public static @NotNull PointerBuffer longs(Pointer... graphicsFamily)
    {
        final PointerBuffer put = memPointerBuffer(address + (frame), graphicsFamily.length );
        for (Pointer addr: graphicsFamily)
        {
            put.put(memGetLong(addr.address()));
        }
        put.flip();
        frame+=put.capacity();
        System.out.println("ByteBuf: "+put.address0()+"+"+frame);
        return put;
    }public static PointerBuffer longs(long... graphicsFamily)
    {
        final PointerBuffer put = memPointerBuffer(address + (frame), graphicsFamily.length ).put(graphicsFamily).flip();
        frame+=put.capacity();
        System.out.println("ByteBuf: "+put.address0()+"+"+frame);
        return put;
    }

     public static final class Memsys2 {

//        private static final PointerBuffer removed = memPointerBuffer(address+0x7FF, 96);

         static void doPointerAllocSafe2(Pointer allocateInfo, long vkCreateBuffer, long[] a)
        {
            //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
            free(allocateInfo);
            System.out.println("Attempting to Call: ->" + allocateInfo);
            checkCall(callPPPPI(device.address(), Checks.check(allocateInfo.address()), NULL, a, vkCreateBuffer));
        }

         public static void free(@NotNull Pointer ptr)
        {
            final long orDefault = tracker.getOrDefault(ptr.address(), 0L);
            final long stacks = memGetLong(ptr.address());//+ orDefault;
            if (stacks == 0 || orDefault == 0) {
                System.err.println("Warn: Don't Need to free!: " + ptr + "" + ptr.address());
            }
            System.out.println("            Freeing: ->" + ptr + "Size: " + stacks + " Addr: " + ptr.address() + "Current allocations: " + MemSysm.stacks + "-->"+Thread.currentThread().getStackTrace()[2]);
            MemSysm.stacks -= stacks;
            JEmalloc.nje_free(ptr.address());
            tracker.remove(ptr.address());
//            removed.put(ptr.address());
        }

         public static void free8(long ptr)
        {
            final long orDefault = tracker.getOrDefault(ptr, 0L);
            final long stacks = memGetLong(ptr);
            if (orDefault == 0) {
//                throw new UnsupportedOperationException("Warn: Don't Need to free!: " + ptr);
            }
            if (stacks == 0) {
                System.err.println("WARN: Is not Allocated Object!");
            }
            System.out.println("FreeingL: " + ptr + "Size: " + stacks + "Current allocations: " + MemSysm.stacks + "-->" + Thread.currentThread().getStackTrace()[2]);
            MemSysm.stacks -= 8;
//            removed.put(ptr);
            JEmalloc.nje_free(ptr);

        }

         static VkCommandBuffer doPointerAllocAlt(long allocateInfo, long vkAllocateCommandBuffers) {
            //            vkAllocateMemory(device, allocateInfo, pAllocator(), pVertexBufferMemory);
            callPPPI(device.address(), allocateInfo, pDummyPlacementPointerAlloc, vkAllocateCommandBuffers);
            return new VkCommandBuffer(pDummyPlacementPointerAlloc[0], device);

        }

        //static final long MemMainAllC = JEmalloc.nje_mallocx(128, 0);
        private static void checkCall(int callPPPPI)
        {
            switch (callPPPPI)
            {
                case VK_SUCCESS -> System.out.println("OK!");
                case VK_NOT_READY -> throw new RuntimeException("Not ready!");
                case VK_TIMEOUT -> throw new RuntimeException("Bad TimeOut!");
                case VK_INCOMPLETE -> throw new RuntimeException("Incomplete!");
                case VK_ERROR_INITIALIZATION_FAILED -> throw new RuntimeException("Error: bad Initialisation!");
                case VK_ERROR_FRAGMENTED_POOL -> throw new RuntimeException("Error: bad Mem Alloc");
                case VK_ERROR_OUT_OF_HOST_MEMORY -> throw new RuntimeException("No Host Memory");
                case VK_ERROR_OUT_OF_DEVICE_MEMORY -> throw new RuntimeException("No Device Memory");
                default -> throw new RuntimeException("Unknown Error!");
            }
        }

         public static void free(IntBuffer ints)
         {
             JEmalloc.je_free(ints);
         }


         public static void doPointerAllocSafeX(Pointer createInfo, long vkCreateSwapchainKHR, long swapChain)
         {
             callPPPPI(device.address(), createInfo.address(), NULL, swapChain, vkCreateSwapchainKHR);
             //return  swapChain;
         }
     }
}
