package vkutils;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.Checks;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.jemalloc.JEmalloc;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.system.JNI.callPJPPPI;
import static org.lwjgl.system.JNI.callPPPPI;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_NOT_READY;
import static org.lwjgl.vulkan.VK10.VK_TIMEOUT;
import static vkutils.VkUtils2.Queues.device;

final record MemSysm(MemoryStack stack, VkAllocationCallbacks pAllocator) /*implements MemoryAllocator*/ {

//    protected static final VkAllocationCallbacks pAllocator = null;
    //int m = JEmalloc.je_mallctl()
    static final long[] pDummyPlacementPointerAlloc = {0};
    static long stacks;
    static final HashMap<Long, Long> tracker= new HashMap<>();
    static  long address = JEmalloc.nje_mallocx(1, 0);//nmemAllocChecked(Pointer.POINTER_SIZE);

    //static final long MemMainAllC = JEmalloc.nje_mallocx(128, 0);
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

    public static long mallocM(long num, long size)
    {
        System.err.println(VkMemoryRequirements.SIZEOF);
        //final long l = JEmalloc.nje_malloc(MemMainAllC+size);
        return address+=size;
    }

    public static long calloc(long num, long size)
    {
        final long l = JEmalloc.nje_calloc(num, size);
        if(tracker.getOrDefault(l, 0L)!=0)
        {
            System.err.println("WARN:C: Is Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);
            return l;
        }
        if(contains(l))
        {
            System.err.println("WARN:M: Is/Was Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);

        }
        System.out.println("AllocatingC: " + size + " With Capacity: " + num + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        stacks += size;
        tracker.put(l, size);
        return l;
        //return LibCStdlib.ncalloc(num, size);
    }

    private static boolean contains(long l)
    {
        //removed.rewind();
        for (int i = 0; i< Memsys2.removed.limit(); i++)
        {
            if(Memsys2.removed.get(i)==l)
                return true;
        }
        return false;
    }

    public static ByteBuffer mallocB(int i, int size)
    {
        final ByteBuffer l = JEmalloc.je_calloc(i, size);
        if (l==null)
        {
            throw new NullPointerException("Buff NULL!");
        }
       /* if(tracker.getOrDefault(l, 0L)==size)
        {
            System.err.println("WARN:M: Is Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);
            return l;
        }*/
        System.out.println("AllocatingM: " + size + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        stacks += size;
        //tracker.put(l, size);
        return l.rewind();//memPointerBuffer(memAddress(l), l.limit()).put(l);//memAddress0(LibCStdlib.malloc(size));
//        return LibCStdlib.nmalloc(Integer.toUnsignedLong(size));//memAddress0(LibCStdlib.malloc(size));
    }
    public static long malloc(long size)
    {
        final long l = JEmalloc.nje_malloc(size);
        if(tracker.getOrDefault(l, 0L)==size)
        {
            System.err.println("WARN:M: Is Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);
            return l;
        }
        if(contains(l))
        {
            System.err.println("WARN:M: Is/Was Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);

        }
        System.out.println("AllocatingM: " + size + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        stacks += size;
        tracker.put(l, size);
        return l;//memAddress0(LibCStdlib.malloc(size));
//        return LibCStdlib.nmalloc(Integer.toUnsignedLong(size));//memAddress0(LibCStdlib.malloc(size));
    }

    public static long mallocLongPtr(long descriptorSets)
    {
        System.out.println("AllocatingL: "+descriptorSets+" Total Allocations : "+stacks+"  "+Thread.currentThread().getStackTrace()[2]);
        long s = JEmalloc.nje_malloc(1);
        tracker.put(s, descriptorSets);
        stacks+=8;
        System.out.println("Allocated: "+s);
        memPutLong(s, descriptorSets);
        return s;

    }

    public static long mallocLongPtr(Pointer commandBuffer)
    {
        System.out.println("AllocatingL: "+commandBuffer+"Addr: "+commandBuffer.address()+" Total Allocations : "+stacks);
        long s = JEmalloc.nje_malloc(8);
        tracker.put(s, commandBuffer.address());
        stacks+=8;
        memPutLong(s, commandBuffer.address());
        return s;
    }

    static void doPointerAllocSafeExtrm2(long imageInfo, long vkCreateImage, long[] a)
    {
        Memsys2.free8(imageInfo);
        checkCall(JNI.callPPPPI(VkUtils2.Queues.device.address(), imageInfo, NULL, a, vkCreateImage));
    }

    //todo: modfied form of getHandle specificaly Deisgned to handle Dierct access to UniformBuffer Modifictaion with Alternative CommandBuffer Execution, All memory Address Loctaions smust be aligned to 512 exactly to avoid Rendering Artifacts./nstablity
    static long getHandle(int address1)
    {
        //            System.out.println(l);
        return memGetLong(address)+((address1 * 512L) & 0xffffffffL);
    }

    static long doPointerAllocSafeExtrm(long allocateInfo, long vkCreateBuffer) {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator(), pVertexBufferMemory);
//        Memsys2.free(allocateInfo);
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
    static long doPointerAllocSafeA(@NotNull Pointer allocateInfo, long vkCreateBuffer) {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator(), pVertexBufferMemory);
//        Memsys2.free(allocateInfo);
        System.out.println("Attempting to CallS: ->"+allocateInfo);
        Checks.check(allocateInfo.address());
        checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));
        return pDummyPlacementPointerAlloc[0];
    }
    static long doPointerAllocSafe(@NotNull Pointer allocateInfo, long vkCreateBuffer) {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator(), pVertexBufferMemory);
        //Memsys2.free(allocateInfo);
        System.out.println("Attempting to CallS: ->"+allocateInfo);
        Checks.check(allocateInfo.address());
        checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));
        return pDummyPlacementPointerAlloc[0];
    }

    static long doPointerAlloc5L(VkDevice device, VkGraphicsPipelineCreateInfo.Buffer pipelineInfo)
    {
        Checks.check(pipelineInfo.address0());
        checkCall(callPJPPPI(device.address(), VK10.VK_NULL_HANDLE, pipelineInfo.remaining(), pipelineInfo.address0(), NULL, pDummyPlacementPointerAlloc, renderer2.Buffers.capabilities.vkCreateGraphicsPipelines));
        return pDummyPlacementPointerAlloc[0];
    }

    public long nmalloc(int a)
    {
        return stack.nmalloc(Pointer.POINTER_SHIFT, a);   //stack.ncalloc(Pointer.POINTER_SHIFT, a,  Pointer.POINTER_SIZE);

    }
    static void freemalloc(int a)
    {

    }

    public long ncalloc(int size)
    {
        return stack.ncalloc(Pointer.POINTER_SHIFT, size,  Pointer.POINTER_SIZE);//return MemoryStack.stackPush().nmalloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT);
//        return nmemCallocChecked(1, 8);
    }

     static final class Memsys2 {

         private static final LongBuffer removed = memLongBuffer(address+64L, 64);

         static void doPointerAllocSafe2(Pointer allocateInfo, long vkCreateBuffer, long[] a)
        {
            //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
            free(allocateInfo);
            System.out.println("Attempting to Call: ->" + allocateInfo);
            Checks.check(allocateInfo.address());
            checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, a, vkCreateBuffer));
        }

        static void free(@NotNull Pointer ptr)
        {
            final long orDefault = tracker.getOrDefault(ptr.address(), 0L);
            final long stacks = memGetLong(ptr.address());//+ orDefault;
            if (stacks == 0 || orDefault == 0) {
                throw new UnsupportedOperationException("Warn: Don't Need to free!: " + ptr + "" + ptr.address());
            }
            System.out.println("            Freeing: ->" + ptr + "Size: " + stacks + " Addr: " + ptr.address() + "Current allocations: " + MemSysm.stacks);
            MemSysm.stacks -= stacks;
            JEmalloc.nje_free(ptr.address());
            tracker.remove(ptr.address());
            removed.put(ptr.address());
        }

        public static void free(long ptr)
        {
            final long orDefault = tracker.getOrDefault(ptr, 0L);
            final long stacks = memGetLong(ptr);
            if (orDefault == 0) {
                throw new UnsupportedOperationException("Warn: Don't Need to free!: " + ptr);
            }
            if (stacks == 0) {
                System.err.println("WARN: Is not Allocated Object!");
            }
            System.out.println("Freeing: " + ptr + "Size: " + stacks + "Current allocations: " + MemSysm.stacks);
            MemSysm.stacks -= stacks;
            JEmalloc.nje_free(ptr);
            tracker.remove(ptr);
            removed.put(ptr);
        }

        public static void free8(long ptr)
        {
            final long stacks = tracker.getOrDefault(memGetLong(ptr), 0L);
            System.out.println("FreeingL: " + ptr + "Size: " + stacks + "Current allocations: " + MemSysm.stacks);
            MemSysm.stacks -= 8;
            removed.put(ptr);
            JEmalloc.nje_free(ptr);
        }

        public static void free(ByteBuffer ptr)
        {
            final long orDefault = tracker.getOrDefault(memAddress0(ptr), 0L);
            final long stacks = memGetLong(memAddress0(ptr));//+ orDefault;

            if (stacks == 0 || orDefault == 0) {
                throw new UnsupportedOperationException("Warn: Don't Need to free!: " + ptr + "" + memAddress0(ptr));
            }
            JEmalloc.je_free(ptr);
        }

       /* @Override
        public boolean equals(Object obj)
        {
            return obj == this || obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode()
        {
            return 1;
        }

        @Override
        public String toString()
        {
            return "Memsys2[]";
        }*/


    }
}
