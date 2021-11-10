# VkMod

Highly experimental renderer or 'Toy renderer' heavily derived from these Tutorial resources:

    https://vulkan-tutorial.com/#page_E-book
    https://vkguide.dev

This was originally intended as a replacement for the OpenGL for Minecraft clients hence why it is written in Java, however if a convincing or motivating reason exists to do so, this can easy be ported to other languages such as C++.


Additional Details of Note 

	This project utilises the most recent alpha versions of LWJGL 3 due to enabling access to Vulkan 1.2 features, 
  including vkCmdDrawIndexedIndirect and VK_NV_ray_tracing , which are not uapported in the current stable version, likely primarily due to its age.


	Due to the rundimentary and limited nature of the renderer, stablity and perfokmance are not gurenteed and may lead to unpredicable behaviour 
	
	A minimum of Java 16 and Vulkan 1.2.192 is likely required to compile and/or run the Application properly
