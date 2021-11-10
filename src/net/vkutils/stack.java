package vkutils;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.VkWin32SurfaceCreateInfoKHR;

import static org.lwjgl.system.MemoryUtil.nmemCallocChecked;

public record stack(MemoryStack stack) {

    public long malloc(int a)
    {
        return stack.ncalloc(Pointer.POINTER_SHIFT, a,  Pointer.POINTER_SIZE);

    }
    static void freemalloc(int a)
    {

    }

    public long calloc(int size)
    {
        return stack.nmalloc(Pointer.POINTER_SIZE, size << Pointer.POINTER_SHIFT);
//        return nmemCallocChecked(1, 8);
    }
}
