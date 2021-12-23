#version 330 core

layout (location = 0) in vec3 aPos;

uniform mat4 projMat;
uniform mat4 modelMat;

void main() {
    gl_Position = projMat * modelMat * vec4(aPos, 1);
}