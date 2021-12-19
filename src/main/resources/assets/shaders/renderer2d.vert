#version 330 core

layout (location = 0) in vec2 aPos;
uniform mat4 transform;
uniform vec4 rect;

out vec2 vPos;

void main() {
    vPos = aPos;

    float x = aPos.x > 0.5F ? rect.x + rect.z : rect.x;
    float y = aPos.y > 0.5F ? rect.y + rect.w : rect.y;

    gl_Position = transform * vec4(x, y, 0.0F, 1.0F);
}
