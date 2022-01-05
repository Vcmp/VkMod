package vkutils.setup;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;


import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static vkutils.setup.Queues.device;

public final class UniformBufferObject {

     public static final Matrix4f mvp;// = new Matrix4f().identity();
    public static final long descriptorSetLayout;
    //        private static LongBuffer pDescriptorSetLayout;
     public static final PointerBuffer uniformBuffers = memPointerBuffer(MemSysm.malloc3(16), 3);
     static final PointerBuffer uniformBuffersMemory = memPointerBuffer(MemSysm.malloc3(16), 3);
    static final int capacity = 16 * Float.BYTES;
    public static long descriptorPool = 0;
    static final PointerBuffer descriptorSets = memPointerBuffer(MemSysm.malloc3(16), 3);

    static final long textureImageView = MemSysm.malloc(0);

    private static final float zFar = 120.0f;

    private static final float zNear = 1.1f;

    public static final Matrix4f trans = new Matrix4f().identity();

    public static final double aFloat = Math.toRadians(90D);
    //        private final static float a2;

    static {

        descriptorSetLayout = Buffers.createDescriptorSetLayout();
//            textureSampler = renderer2.Buffers.createTextureSampler();


        float h = (Math.tan(Math.toRadians(90) * 0.5f));
        mvp = new Matrix4f().perspective(h * -1, (SwapChainSupportDetails.swapChainExtent.width() / (float) SwapChainSupportDetails.swapChainExtent.height()), zNear, zFar, false);

        mvp.mulPerspectiveAffine(new Matrix4f().determineProperties().setLookAt(0.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f));
        mvp.translate(new Vector3f(0, 0, -4));

        //            a2 = mvp.m03();

    }
    //todo: Important: atcuall have to 'reset; or revert teh matrixces bakc to baseline.Idneitfy in order to atcually update te Unform Buffer(s) properly e.g. .etc i.e.
//            UniformBufferObject.model.identity();
//            UniformBufferObject.view.identity();
//            UniformBufferObject.proj.identity();


    //private static final double aFloat = Math.toRadians(90D);

    public static void createUniformBuffers()
    {
        for (int i = 0; i < SwapChainSupportDetails.swapChainImages.capacity(); i++) {

            uniformBuffers.put((((Buffers.setBuffer()))));
            uniformBuffersMemory.put((Buffers.createBuffer(uniformBuffers.get(i))));
        }
    }

    static void createDescriptorPool()
    {
        {
            VkDescriptorPoolSize.Buffer poolSize = VkDescriptorPoolSize.create(MemSysm.calloc(2, VkDescriptorPoolSize.SIZEOF), 2);
//                        .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
//                        .descriptorCount(PipeLine.swapChainImages.length);
            VkDescriptorPoolSize uniformBufferPoolSize = poolSize.get(0)
                    .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                    .descriptorCount(SwapChainSupportDetails.swapChainImages.capacity());

            VkDescriptorPoolSize textureSamplerPoolSize = poolSize.get(1)
                    .type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(SwapChainSupportDetails.swapChainImages.capacity());
            VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.create(MemSysm.calloc(VkDescriptorPoolCreateInfo.SIZEOF)).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                    .pPoolSizes(poolSize)
                    .maxSets(SwapChainSupportDetails.swapChainImages.capacity());
            descriptorPool = MemSysm.doPointerAllocSafe(poolCreateInfo, device.getCapabilities().vkCreateDescriptorPool);
//               descriptorPool=aLong[0];
        }
    }

    static void createDescriptorSets()
    {
        {
            LongBuffer layouts = memLongBuffer(MemSysm.address, SwapChainSupportDetails.swapChainImages.capacity());
            for (int i = 0; i < layouts.capacity(); i++) {
                layouts.put(i, descriptorSetLayout);
            }

            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.create(MemSysm.calloc(VkDescriptorSetAllocateInfo.SIZEOF)).sType$Default();
//                allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocInfo.descriptorPool((descriptorPool))
                    .pSetLayouts(layouts);

            nvkAllocateDescriptorSets(device, allocInfo.address(), descriptorSets.address0());

            VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.create(MemSysm.malloc3(VkDescriptorBufferInfo.SIZEOF), 1)
                    .offset(0)
                    .range(capacity);

            VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.create(MemSysm.malloc3(VkDescriptorImageInfo.SIZEOF), 1)
                    .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                    .imageView(memGetLong(textureImageView))
                    .sampler(Buffers.createTextureSampler());

            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.create(MemSysm.malloc3(VkWriteDescriptorSet.SIZEOF * 2L), 2);

            VkWriteDescriptorSet vkWriteDescriptorSet = descriptorWrites.get(0).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(0)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                    .descriptorCount(1)
                    .pBufferInfo(bufferInfo);

            VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(1).sType$Default()
//                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                    .dstBinding(1)
                    .dstArrayElement(0)
                    .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .pImageInfo(imageInfo);


            //nmemFree(bufferInfo);

            for (int i = 0; i < 1; i++) {


                bufferInfo.buffer(uniformBuffers.get(i));

                vkWriteDescriptorSet.dstSet(descriptorSets.get(i));
                samplerDescriptorWrite.dstSet(descriptorSets.get(i));

                vkUpdateDescriptorSets(device, descriptorWrites, null);

//                    descriptorSets[i]=descriptorSet;

            }
            MemSysm.Memsys2.free(bufferInfo);
            MemSysm.Memsys2.free(descriptorWrites);
//                System.arraycopy(pDescriptorSets, 0, descriptorSets, 0, pDescriptorSets.length);
        }
    }
}
