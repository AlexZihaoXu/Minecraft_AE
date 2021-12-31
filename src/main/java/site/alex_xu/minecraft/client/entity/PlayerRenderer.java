package site.alex_xu.minecraft.client.entity;

import site.alex_xu.minecraft.client.model.Mesh;
import site.alex_xu.minecraft.client.render.GameObjectRenderer;
import site.alex_xu.minecraft.client.screen.world.Camera;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class PlayerRenderer extends EntityRenderer {
    protected Mesh head, body, leftArm, rightArm, leftLeg, rightLeg;
    public float pitch = 0;
    public float yaw = (float) (-Math.PI / 2);
    protected float bodyYaw = yaw;
    protected Mesh[] parts;
    public float animationSpeed = 1.0f;

    public PlayerRenderer() {
        head = createBodyPart(
                8, 8, 8,
                rect(8, 0, 8, 8),
                rect(16, 0, 8, 8),
                rect(16, 8, 8, 8),
                rect(0, 8, 8, 8),
                rect(8, 8, 8, 8),
                rect(24, 8, 8, 8)
        );

        body = createBodyPart(
                8, 12, 4,
                rect(20, 16, 8, 4),
                rect(28, 16, 8, 4),
                rect(28, 20, 4, 12),
                rect(16, 20, 4, 12),
                rect(20, 20, 8, 12),
                rect(32, 20, 8, 12)
        );

        leftArm = createBodyPart(
                4, 12, 4,
                rect(44, 16, 4, 4),
                rect(48, 16, 4, 4),
                rect(40, 20, 4, 12),
                rect(48, 20, 4, 12),
                rect(44, 20, 4, 12),
                rect(52, 20, 4, 12)
        );

        rightArm = createBodyPart(
                4, 12, 4,
                rect(44 - 8, 16 + 32, 4, 4),
                rect(48 - 8, 16 + 32, 4, 4),
                rect(40 - 8, 20 + 32, 4, 12),
                rect(48 - 8, 20 + 32, 4, 12),
                rect(44 - 8, 20 + 32, 4, 12),
                rect(52 - 8, 20 + 32, 4, 12)
        );

        leftLeg = createBodyPart(
                4, 12, 4,
                rect(4, 16, 4, 4),
                rect(8, 16, 4, 4),
                rect(8, 20, 4, 12),
                rect(0, 20, 4, 12),
                rect(4, 20, 4, 12),
                rect(12, 20, 4, 12)
        );

        rightLeg = createBodyPart(
                4, 12, 4,
                rect(16 + 4, 32 + 16, 4, 4),
                rect(16 + 8, 32 + 16, 4, 4),
                rect(16 + 8, 32 + 20, 4, 12),
                rect(16 + 0, 32 + 20, 4, 12),
                rect(16 + 4, 32 + 20, 4, 12),
                rect(16 + 12, 32 + 20, 4, 12)
        );
        parts = new Mesh[]{
                head,
                leftArm, body, rightArm,
                leftLeg, rightLeg
        };
    }

    @Override
    public void render(Camera camera, GameObjectRenderer renderer, double vdt) {
        float now = (float) glfwGetTime();

        if (yaw - bodyYaw > 0.6) {
            float dt = (((yaw - 0.4f) - bodyYaw));

            if (dt > 3.14) {
                dt = (float) (dt - Math.PI * 2);
                bodyYaw += dt * vdt * 6;
                bodyYaw = (float) (bodyYaw - Math.floor(bodyYaw / (Math.PI * 2)) * Math.PI * 2);
            } else {
                bodyYaw += dt * vdt * 6;
            }

        }
        if (yaw - bodyYaw < -0.6) {
            float dt = ((yaw + 0.4f) - bodyYaw);

            if (dt < -3.14) {
                dt = (float) (dt + Math.PI * 2);
                bodyYaw += dt * vdt * 6;
                bodyYaw = (float) (bodyYaw - Math.floor(bodyYaw / (Math.PI * 2)) * Math.PI * 2);
            } else {

                bodyYaw += dt * vdt * 6;
            }
        }

        for (Mesh part : parts) {
            part.resetModelMatrix();
            part.getModelMatrix().translate(position());
            part.getModelMatrix().translate(0, units(-6), 0);
            if (part == head)
                part.getModelMatrix().rotateY((float) (-yaw + Math.PI / 2));
            else
                part.getModelMatrix().rotateY((float) (-bodyYaw + Math.PI / 2));
        }
        leftLeg.getModelMatrix().translate(units(-2), units(12), 0);
        rightLeg.getModelMatrix().translate(units(2), units(12), 0);
        body.getModelMatrix().translate(0, units(12 * 2), 0);
        leftArm.getModelMatrix().translate(units(-6), units(12 * 2), 0);
        rightArm.getModelMatrix().translate(units(6), units(12 * 2), 0);
        head.getModelMatrix().translate(0, units(34), 0);

        head.getModelMatrix().translate(0, units(-4), 0);
        head.getModelMatrix().rotateX(pitch);
        head.getModelMatrix().translate(0, units(4), 0);

        leftArm.getModelMatrix().translate(0, units(4), 0);
        leftArm.getModelMatrix().rotateZ(-0.06f + -0.03f * sin(now));
        leftArm.getModelMatrix().rotateX(cos(animationSpeed * now) * min(animationSpeed * 0.13f, 1.65f));
        leftArm.getModelMatrix().rotateY(cos(now * 0.9f) * 0.01f);
        leftArm.getModelMatrix().translate(0, units(-4), 0);

        rightArm.getModelMatrix().translate(0, units(4), 0);
        rightArm.getModelMatrix().rotateZ(0.06f + 0.03f * sin(now));
        rightArm.getModelMatrix().rotateX(-cos(animationSpeed * now) * min(animationSpeed * 0.13f, 1.65f));
        rightArm.getModelMatrix().rotateY(cos(now * 0.9f + 1.2f) * 0.01f);
        rightArm.getModelMatrix().translate(0, units(-4), 0);


        leftLeg.getModelMatrix().translate(0, units(6), 0);
        leftLeg.getModelMatrix().rotateX(-cos(animationSpeed * now) * min(animationSpeed * 0.13f, 1.65f));
        leftLeg.getModelMatrix().translate(0, units(-6), 0);

        rightLeg.getModelMatrix().translate(0, units(6), 0);
        rightLeg.getModelMatrix().rotateX(cos(animationSpeed * now) * min(animationSpeed * 0.13f, 1.65f));
        rightLeg.getModelMatrix().translate(0, units(-6), 0);

        renderer.render(camera, head, 0, getTexture());
        renderer.render(camera, body, 0, getTexture());
        renderer.render(camera, leftArm, 0, getTexture());
        renderer.render(camera, rightArm, 0, getTexture());
        renderer.render(camera, leftLeg, 0, getTexture());
        renderer.render(camera, rightLeg, 0, getTexture());


    }
}
