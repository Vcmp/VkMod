package vkutils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NativeResource;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

final class ShaderSPIRVUtils {

    static SPIRV compileShaderFile(String shaderFile, ShaderKind shaderKind) {
        return compileShaderAbsoluteFile(shaderFile, shaderKind);
    }

    private static @Nullable SPIRV compileShaderAbsoluteFile(String shaderFile, ShaderKind shaderKind) {
        try {
            System.out.println(shaderFile);
            String source = new String(Files.readAllBytes(Paths.get((shaderFile))));
//            System.out.println(source);
            return compileShader(shaderFile, source, shaderKind);
        } catch (IOException e) {
            e.printStackTrace();
            e.getCause();
        }
        return null;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull SPIRV compileShader(String filename, String source, ShaderKind shaderKind) {

        long compiler = shaderc_compiler_initialize();

        if(compiler == NULL) throw new RuntimeException("Failed to create shader compiler");

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", NULL);

        if(result == NULL) throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");

        if(shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success)
            throw new RuntimeException("Failed to compile shader " + filename + "into SPIR-V:\n " + shaderc_result_get_error_message(result));

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    static long createShaderModule(ByteBuffer spirvCode) {

        {

            return MemSysm.doPointerAllocSafeA(VkShaderModuleCreateInfo.calloc(VkUtils2.MemSys.stack()).sType$Default().pCode(spirvCode), renderer2.Buffers.capabilities.vkCreateShaderModule);
        }
    }

    enum ShaderKind {

        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader);

        private final int kind;

        ShaderKind(int kind) {
            this.kind = kind;
        }
    }

    static final record SPIRV(long handle, ByteBuffer bytecode) implements NativeResource {






        @Override
        public void free() {
            shaderc_result_release(handle);
            //bytecode = null; // Help the GC
        }
    }

}
