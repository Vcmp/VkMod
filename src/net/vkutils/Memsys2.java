package vkutils;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.jemalloc.JEmalloc;

import java.nio.ByteBuffer;

import static org.lwjgl.system.JNI.callPPPPI;
import static org.lwjgl.system.MemoryUtil.*;

final record Memsys2() {

    static void doPointerAllocSafe2(@NotNull Pointer allocateInfo, long vkCreateBuffer, long[] a)
    {
        //            vkAllocateMemory(Queues.device, allocateInfo, pAllocator, pVertexBufferMemory);
        free(allocateInfo);
        System.out.println("Attempting to Call: ->" + allocateInfo);
        Checks.check(allocateInfo.address());
        MemSysm.checkCall(callPPPPI(VkUtils2.Queues.device.address(), allocateInfo.address(), NULL, a, vkCreateBuffer));
    }

    public static void free(Pointer ptr)
    {
        final long orDefault = MemSysm.tracker.getOrDefault(ptr.address(), 0L);
        final long stacks = memGetLong(ptr.address());//+ orDefault;
        if(stacks==0||orDefault==0)
        {
            throw new UnsupportedOperationException("Warn: Don't Need to free!: "+ptr+""+ptr.address());
        }
        System.out.println("            Freeing: ->" + ptr + "Size: " + stacks + " Addr: " + ptr.address() + "Current allocations: " + MemSysm.stacks);
        MemSysm.stacks -= stacks;
        JEmalloc.nje_free(ptr.address());
        MemSysm.tracker.remove(ptr.address());
        MemSysm.removed.put(ptr.address());
    }

    public static void free(long ptr)
    {
        final long orDefault = MemSysm.tracker.getOrDefault(ptr, 0L);
        final long stacks = memGetLong(ptr);
        if(orDefault==0)
        {
            throw new UnsupportedOperationException("Warn: Don't Need to free!: "+ptr);
        }
        if(stacks==0)
        {
            System.err.println("WARN: Is not Allocated Object!");
        }
        System.out.println("Freeing: " + ptr + "Size: " + stacks + "Current allocations: " + MemSysm.stacks);
        MemSysm.stacks -= stacks;
        JEmalloc.nje_free(ptr);
        MemSysm.tracker.remove(ptr);
        MemSysm.removed.put(ptr);
    }
    public static void free8(long ptr)
    {
        final long stacks = MemSysm.tracker.getOrDefault(memGetLong(ptr), 0L);
        System.out.println("FreeingL: " + ptr + "Size: " + stacks + "Current allocations: " + MemSysm.stacks);
        MemSysm.stacks -= 8;
        JEmalloc.nje_free(ptr);
    }

    public static void free(ByteBuffer ptr)
    {
        final long orDefault = MemSysm.tracker.getOrDefault(memAddress0(ptr), 0L);
        final long stacks = memGetLong(memAddress0(ptr));//+ orDefault;

        if(stacks==0||orDefault==0)
        {
            throw new UnsupportedOperationException("Warn: Don't Need to free!: "+ptr+""+memAddress0(ptr));
        }
        JEmalloc.je_free(ptr);
    }

}
