#version 330 core

in vec4 vColor;
in vec2 texCoord;

uniform sampler2D texture0;
uniform int mode;
uniform float texWidth;
uniform float texHeight;
uniform float time;

out vec4 FragColor;

void main() {
    float tx = texCoord.x;
    float ty = texCoord.y;
    if (mode == 1) { // Water
        tx /= 64;
        tx += float(mod(int(time * 15), 64)) * (16.0F / texWidth);
    }
    vec4 color = texture(texture0, vec2(tx, ty));
    if (mode == 0 && color.a == 0) {
        discard;
    }

    FragColor = color * vColor;
}
