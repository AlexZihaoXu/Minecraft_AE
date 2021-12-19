#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 projMat;
uniform mat4 modelMat;

out vec4 vColor;
out vec2 texCoord;

void main() {
    vColor = aColor;
    texCoord = aTexCoord;
    gl_Position = projMat * modelMat * vec4(aPos, 1.0);
}