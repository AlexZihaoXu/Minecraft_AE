#version 330 core

in vec4 vColor;
in vec2 texCoord;
in float depth;

uniform sampler2D texture0;
uniform int mode;
uniform float texWidth;
uniform float texHeight;
uniform float animationTime;
uniform float time;
uniform float lightLevel;

out vec4 FragColor;

float mip_map_level(vec2 texture_coordinate)// in texel units
{
    vec2  dx_vtc        = dFdx(texture_coordinate);
    vec2  dy_vtc        = dFdy(texture_coordinate);
    float delta_max_sqr = max(dot(dx_vtc, dx_vtc), dot(dy_vtc, dy_vtc));
    float mml = 0.5 * log2(delta_max_sqr);
    return max(0, mml);// Thanks @Nims
}

void main() {
    float shadow = vColor.x;
    float blockLight = vColor.y;
    float envLight = vColor.z;

    float tx = texCoord.x;
    float ty = texCoord.y;

    if (mode == 1) { // Water
        tx /= 64;
        tx += float(mod(int(animationTime * 15), 64)) * (16.0F / texWidth);
    }
    float level = mip_map_level(texCoord * textureSize(texture0, 0));
    vec4 color = textureLod(texture0, vec2(tx, ty), min(4, level));

    if (level < 0.8) {
        if (color.a < 0.5) {
            discard;
        }
    } else {
        if (mode != 1)
        color.a = 1;
    }


    float envLightMultiplier = max(0.0, pow(sin(mod(time + 0.06, 1) * 0.8 * 3.14159265 * 2), 0.12));

    vec3 nightLight = vec3(0.17, 0.17, 0.28);
    vec3 dayLight = vec3(1, 1, 1);

    vec3 timeLight = mix(nightLight, dayLight, envLightMultiplier);

    vec3 finalColor = vec3(
    max(blockLight, envLight * envLightMultiplier * timeLight.r) * shadow * lightLevel * 0.6 + 0.4,
    max(blockLight, envLight * envLightMultiplier * timeLight.g) * shadow * lightLevel * 0.6 + 0.4,
    max(blockLight, envLight * envLightMultiplier * timeLight.b) * shadow * lightLevel * 0.6 + 0.4
    );

    vec4 lightColor = vec4(
    finalColor.r,
    finalColor.g,
    finalColor.b,
    1
    );

    FragColor = color * lightColor;
}
