package vkutils;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.jemalloc.JEmalloc;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_NOT_READY;
import static org.lwjgl.vulkan.VK10.VK_TIMEOUT;
import static vkutils.VkUtils2.Queues.device;


final record MemSysm(MemoryStack stack, VkAllocationCallbacks pAllocator) /*implements MemoryAllocator*/ {

    //    protected static final VkAllocationCallbacks pAllocator = null;
    //int m = JEmalloc.je_mallctl()
    private static final long[] pDummyPlacementPointerAlloc = {0};
    private static long frame=0;
    static long stacks;
    static final HashMap<Long, Long> tracker = new HashMap<>();
    static final long size = 1023;
    static final long address = JEmalloc.nje_mallocx(size, JEmalloc.MALLOCX_ARENA(2))+VkMemoryRequirements.SIZEOF;//nmemAllocChecked(Pointer.POINTER_SIZE);

    public static long mallocM(long num, long size)
    {
        System.err.println(VkMemoryRequirements.SIZEOF);
        //final long l = JEmalloc.nje_malloc(MemMainAllC+size);
        return address;//+=size;
    }

    public static long calloc(long size)
    {
        final long l = JEmalloc.nje_calloc(1, size);
        if(tracker.getOrDefault(l, 0L)!=0)
        {
            System.err.println("WARN:C: Is Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);
            return l;
        }
        if(Memsys2.contains(l))
        {
            System.err.println("WARN:C: Is/Was Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);

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
        if(Memsys2.contains(l))
        {
            System.err.println("WARN:C: Is/Was Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);

        }
        System.out.println("AllocatingC: " + size + " With Capacity: " + num + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        stacks += size;
        tracker.put(l, size);
        return l;
        //return LibCStdlib.ncalloc(num, size);
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
        if(Memsys2.contains(l))
        {
            System.err.println("WARN:M: Is/Was Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);

        }
        System.out.println("AllocatingM: " + size + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        stacks += size;
        tracker.put(l, size);
        return l;//memAddress0(LibCStdlib.malloc(size));
//        return LibCStdlib.nmalloc(Integer.toUnsignedLong(size));//memAddress0(LibCStdlib.malloc(size));
    }//Exploit Java freeing;/Dealloctaing/freeing/removing from Heap obejsts intilaised in fuctions nor returned as an arument.paraamter.object to aovid teh need to explcitly free/rrset the Frame.pffset.Indicies/rlatvieoptiianing/mpelemnst/ocucurneces.//occupations/Ranges/wodtsh./offsets/Psootions/atcual Adresses/initialistaions/Atcua;istaions tracked alloctaions
    public static long malloc2(long size)
    {
//        JEmalloc.nje_sdallocx();
        System.out.println(JEmalloc.nje_malloc_usable_size(address));
        frame += size;
        return address+frame;
        /*final long l = JEmalloc.nje_malloc(1);
        if(tracker.getOrDefault(l, 0L)==size)
        {
            System.err.println("WARN:M: Is Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);
            return l;
        }
        if(Memsys2.contains(l))
        {
            System.err.println("WARN:M: Is/Was Already Allocated! "+l+" "+Thread.currentThread().getStackTrace()[2]);

        }
        System.out.println("AllocatingM: " + size + "Addr: " + l+" Total Allocations : "+stacks+" "+Thread.currentThread().getStackTrace()[2]);
        stacks += size;
        tracker.put(l, size);
        return l;//memAddress0(LibCStdlib.malloc(size));*/
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
    public static long callocLongPtr(Pointer commandBuffer)
    {
        System.out.println("AllocatingL: "+commandBuffer+"Addr: "+commandBuffer.address()+" Total Allocations : "+stacks);
        long s = JEmalloc.nje_calloc(1, 8);
        tracker.put(s, commandBuffer.address());
        stacks+=8;
        memPutLong(s, commandBuffer.address());
        return s;
    }

    static void doPointerAllocSafeExtrm2(long imageInfo, long vkCreateImage, long[] a)
    {
//        Memsys2.free8(imageInfo);
        Memsys2.checkCall(callPPPPI(device.address(), imageInfo, NULL, a, vkCreateImage));
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
        Memsys2.checkCall(callPPPPI(device.address(), allocateInfo, NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));

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
        Memsys2.checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));
        return pDummyPlacementPointerAlloc[0];
    }
    static long doPointerAllocSafe(@NotNull Pointer allocateInfo, long vkCreateBuffer) {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator(), pVertexBufferMemory);
        //Memsys2.free(allocateInfo);
        System.out.println("Attempting to CallS: ->"+allocateInfo+"-->"+Thread.currentThread().getStackTrace()[2]);
        Checks.check(allocateInfo.address());
        Memsys2.checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, pDummyPlacementPointerAlloc, vkCreateBuffer));
        return pDummyPlacementPointerAlloc[0];
    }

    static long doPointerAlloc5L(Pointer device, Pointer pipelineInfo)
    {
        Checks.check(pipelineInfo.address());
        Memsys2.checkCall(callPJPPPI(device.address(), VK10.VK_NULL_HANDLE, 1, pipelineInfo.address(), NULL, pDummyPlacementPointerAlloc, renderer2.Buffers.capabilities.vkCreateGraphicsPipelines));
        return pDummyPlacementPointerAlloc[0];
    }

    public static IntBuffer callocM(int sizeNum)
    {
        final ByteBuffer byteBuffer = JEmalloc.je_malloc(sizeNum);
        final IntBuffer a = byteBuffer.asIntBuffer();
        System.out.println("Allocating ByteBuf:"+memAddress0(byteBuffer)+   "As IntBuffer: "+memAddress0(a)+"Size: "+ sizeNum);
        return a;
    }

    public static long sizeof(long address)
    {
        return JEmalloc.nje_sallocx(address, 0);
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
        static void doPointerAllocSafe3(Struct allocateInfo, long vkCreateBuffer, long a)
        {
            //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
            free(allocateInfo);
            System.out.println("Attempting to Call: ->" + allocateInfo);
            Checks.check(allocateInfo.address());
            checkCall(callPPPPI(device.address(), allocateInfo.address(), NULL, a, vkCreateBuffer));
//            malloc2(allocateInfo.sizeof())
        }

        private static void free2(Struct allocateInfo)
        {
            frame-=allocateInfo.sizeof();
        }

        static void free(@NotNull Pointer ptr)
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
            removed.put(ptr.address());
        }

        public static void free(long ptr)
        {
            System.out.print("Attempting to free: "+ ptr + "-->" + Thread.currentThread().getStackTrace()[2] + "-->");
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
            final long orDefault = tracker.getOrDefault(ptr, 0L);
            final long stacks = memGetLong(ptr);
            if (orDefault == 0) {
                throw new UnsupportedOperationException("Warn: Don't Need to free!: " + ptr);
            }
            if (stacks == 0) {
                System.err.println("WARN: Is not Allocated Object!");
            }
            System.out.println("FreeingL: " + ptr + "Size: " + stacks + "Current allocations: " + MemSysm.stacks + "-->" + Thread.currentThread().getStackTrace()[2]);
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
                case VK_NOT_READY -> throw new RuntimeException("Not ready!");
                case VK_TIMEOUT -> throw new RuntimeException("Bad TimeOut!");
                default -> {
                }
            }
        }

        private static boolean contains(long l)
        {
            //removed.rewind();
            for (int i = 0; i< removed.limit(); i++)
            {
                if(removed.get(i)==l)
                    return true;
            }
            return false;
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
