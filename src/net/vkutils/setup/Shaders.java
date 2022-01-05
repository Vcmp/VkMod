package vkutils.setup;

import org.lwjgl.vulkan.VkVertexInputAttributeDescription;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;

public class Shaders {
    private static final int OFFSETOF_COLOR = 3 * Float.BYTES;
    private static final int OFFSET_POS = 0;

    private static final int OFFSETOF_TEXTCOORDS = (3 + 3) * Float.BYTES;
    static long getAttributeDescriptions()
    {

        VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                VkVertexInputAttributeDescription.create(MemSysm.calloc(3, VkVertexInputAttributeDescription.SIZEOF), 3);

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
        attributeDescriptions.free();

//            memFree(attributeDescriptions);

        return attributeDescriptions.address0();
    }
}
