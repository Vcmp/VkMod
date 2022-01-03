#version 450
layout(binding = 0) uniform UniformBufferObject {
    mat4 model;
//    mat4 proj;
//    mat4 view;
} ubo;

layout(location = 0) in vec3 inPosition;
//layout(location = 1) in vec3 inColor;
layout(location = 2) in vec2 inTexCoord;

//layout(location = 0) out vec3 fragColor;
layout(location = 1) out vec2 fragTexCoord;

void main() {
//    vec2 aa =inPosition.yz*ubo.model;
//    vec2 aaa =inPosition.xy*ubo.model;
    //gl_Position = vec4(inPosition, aa*aaa);
    gl_Position = ubo.model * vec4(inPosition, 1.0);
    //fragColor = inColor;
    fragTexCoord = inTexCoord;
}
