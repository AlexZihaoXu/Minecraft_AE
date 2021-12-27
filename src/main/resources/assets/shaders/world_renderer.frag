#version 330 core

in vec4 vColor;
in vec2 texCoord;
in float zIndex;

uniform sampler2D texture0;
uniform int mode;
uniform float texWidth;
uniform float texHeight;
uniform float animationTime;
uniform float time;

out vec4 FragColor;

void main() {
    float tx = texCoord.x;
    float ty = texCoord.y;
    if (mode == 1) { // Water
        tx /= 64;
        tx += float(mod(int(animationTime * 15), 64)) * (16.0F / texWidth);
    }
    vec4 color = texture(texture0, vec2(tx, ty));
    if (mode == 0 && color.a < 0.7) {
        discard;
    }

    float dayLight = max(0.15, pow(sin(time * 3.14159265 * 2), 0.2));

    vec4 light = vec4(
        max(vColor.r, dayLight),
        max(vColor.g, dayLight),
        max(vColor.b, dayLight),
        max(vColor.a, dayLight)
    );

    FragColor = color * light;
}
