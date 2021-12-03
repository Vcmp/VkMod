# VkMod

Highly experimental renderer and Tech Demo heavily derived from these Tutorial resources:

    https://vulkan-tutorial.com/#page_E-book
    https://vkguide.dev

This was originally intended as a replacement for the OpenGL calls and functionality for Minecraft clients hence why it is written in Java, however if a convincing or motivating reason exists to do so, this can easy be ported to other languages such as C++.


Additional Details of Note 

	This project utilities the most recent alpha versions of LWJGL 3 due to enabling access to Vulkan 1.2 features, 
	including vkCmdDrawIndexedIndirect and VK_NV_ray_tracing , which are not supported in the current stable version, likely primarily due to its age.


	Due to the rudimentary and limited nature of the renderer as well as the fact that this is effectively a Tech Demo, stability and performance are not guaranteed and may lead to unpredictable behavior 
	
	Expanding on the prior point Several Performance hacks and bypasses are used to work around some of the inherent limitations of Java to allow for better interoperability with LWJGL and the Underlaying Vulkan API:
	
		This primary consists of Pointer/Reference access and Memory manipulations not normally possible with Java such as the use of the Unsafe API, JEMalloc for dedicated Heap Allocation/Deallocation, Function Pointer/reference access e.g.
		
			As a result: Performance, stability is not gurenteed and due to the highly experimental and untested nature of these modification may prove to be counterintuitive, harmful or determinable to system stability and may not be safe or stable to use on all system configurations
	
	A minimum of Java 17 and Vulkan 1.2.192 is required to compile and/or run the Application properly
