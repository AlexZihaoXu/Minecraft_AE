#version 330 core

in vec2 vPos;// current x, y
out vec4 FragColor;

uniform vec4 color;
uniform int drawMode;
uniform sampler2D texture0;
uniform vec4 rect;
uniform vec4 srcRect;
uniform float texWidth;
uniform float texHeight;
uniform int shadow;

vec2 getCoord() {
    return vec2(
    (srcRect.r + srcRect.b * vPos.x) / texWidth,
    (srcRect.g + srcRect.a * vPos.y) / texHeight
    );
}

void main() {
    vec4 destRect = rect;

    if (drawMode == 0) {
        FragColor = color;
    } else if (drawMode == 1){
        vec2 pos = getCoord();
        FragColor = texture(texture0, vec2(pos.x, 1.0F - pos.y));
    } else {
        vec2 pos = getCoord();
        float alpha = texture(texture0, vec2(pos.x, 1.0F - pos.y)).r;
        vec4 c = vec4(color.r, color.g, color.b, alpha);

        if (shadow == 1 && alpha < 0.1F) {
            vec4 c2 = texture(texture0, vec2(pos.x - 1.0F / texWidth * 3.0F, 1.0F - pos.y + 1.0F / texHeight * 3.0F));
            if (c2.a > 0.1F) {
                c = vec4(0.23F * c2.r, 0.23F * c2.g, 0.23F * c2.b, 0.6F * c2.a);
            }
        }
        FragColor = c;
    }
}