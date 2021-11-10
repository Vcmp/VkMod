import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VKCapabilitiesDevice;
import org.lwjgl.vulkan.VkDevice;


public class VkUnstableCommandBuffer implements Pointer {

    private final long anInt;
    private final VKCapabilitiesDevice capabilities;

    public VkUnstableCommandBuffer(long handle, long pAllocateInfo,  VkDevice d)
    {
        anInt=handle;
        capabilities=d.getCapabilities();
        VK10.nvkAllocateCommandBuffers(d, pAllocateInfo, handle);

    }

    @Override
    public long address()
    {
        return anInt;
    }

    public VKCapabilitiesDevice getCapabilities()
    {
        return capabilities;
    }
}
