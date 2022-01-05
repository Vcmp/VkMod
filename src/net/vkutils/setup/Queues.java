package vkutils.setup;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.memGetInt;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.*;

public final class Queues {

    //        public static long pImageIndex;
    public static VkQueue graphicsQueue;
    public static VkQueue presentQueue;


    public static VkDevice device;
    //        static final long deviceAddress = device.address();
    static VkPhysicalDevice physicalDevice;
    public static final long[] surface = {0};
    // is Boxed Integer to allow it to be initialised/Detected as instances of null instead of 0
    static int graphicsFamily;
    static int presentFamily;
    static int a = 0;


    //static boolean isComplete() {return graphicsFamily != null && presentFamily != null;}

    /*TODO: Qierd isue wheer teh GPU Core laod becomes Unstable(Inconsitent-Fluctuating) at much higher vertcie slevels, VBOs/FBos.UBOs, unsure of the exatc ersion currently soutside og architecural/Utilsitaion/Thrputpu/efefctivnely/untilsitaion issues/Ineffciencies
     * Update: Was actual found to be due to a culling issue where the Framerate varies based on which Position or angle the vertex Buffer/VBO is viewed from, which due to the poorly optimised Intilaistaion/placemtn of verticies are only visib;e from certain diretcions, hense/causing.deriving the Framerate Differnce/Fluctuation/Inconsistency
     * This issue should be easily fixbale with a properr implemtaion of a Index Buffer with vertex DeDuplication the reduent
     */

    //This Method also has alignment problems hence why some graphical Distortions and Artifacts occur in some instances
    static void enumerateDetermineQueueFamilies(VkPhysicalDevice device)
    {

        {
            nvkGetPhysicalDeviceQueueFamilyProperties(device, MemSysm.address, 0);
//                qi=(GLU2.theUnSafe.UNSAFE.getInt(MemSysm.address+4, 0));

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.create(MemSysm.address, memGetInt(MemSysm.address));

            nvkGetPhysicalDeviceQueueFamilyProperties(device, MemSysm.address, queueFamilies.address0());

            IntBuffer presentSupport = MemSysm.ints(VK_FALSE);
            int i = 0;
            for (VkQueueFamilyProperties a : queueFamilies) {
                System.out.println(a.queueCount());
                if ((a.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    graphicsFamily = i;
                }
                vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface[0], presentSupport);

                if (/*(a.queueFlags() & VK_QUEUE_GRAPHICS_BIT) == 0 && */presentSupport.get(0) == VK_TRUE) {
                    presentFamily = 0;
                }
                i++;
            }

//
//                while (i <queueFamilyCount.limit() /*&& !isComplete()*/) {

            System.out.println(a++ + "Graphics Family: " + graphicsFamily + " Present family: " + presentFamily);

        }
    }
}
