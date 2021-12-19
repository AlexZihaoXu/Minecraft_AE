package site.alex_xu.minecraft.client.screen.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import site.alex_xu.minecraft.client.utils.BindableContext;
import site.alex_xu.minecraft.client.utils.RenderContext;
import site.alex_xu.minecraft.core.MinecraftAECore;

public class Camera extends MinecraftAECore {
    public Vector3f position = new Vector3f(0, 0, 0);
    public double pitch = 0;
    public double yaw = 0;
    public double fov = Math.PI / 180 * 80;
    public double far = 1500;
    public double near = 0.01f;

    public Vector3f getFront() {
        return new Vector3f(
                (float) (Math.cos(yaw) * Math.cos(pitch)),
                (float) Math.sin(pitch),
                (float) (Math.sin(yaw) * Math.cos(pitch))
        );
    }

    public Matrix4f getMatrix(BindableContext context) {
        return new Matrix4f().perspective((float) fov, (float) context.getWidth() / (float) context.getHeight(), (float) near, (float) far)
                .mul(new Matrix4f().lookAt(position, new Vector3f(position).add(getFront()), new Vector3f(0, 1, 0)));
    }
}
