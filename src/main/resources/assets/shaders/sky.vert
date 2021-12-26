#version 330 core

layout (location = 0) in vec2 aPos;

out vec2 vPos;

uniform mat4 projMat;
uniform mat4 modelMat;
uniform float yOffset;

void main() {
    vPos = aPos;
    float scale = 512;
    gl_Position = projMat * modelMat * vec4(aPos.x * scale, yOffset, aPos.y * scale, 1.0);
}
