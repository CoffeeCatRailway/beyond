package io.github.ocelot.beyond.client;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import io.github.ocelot.beyond.common.MagicMath;
import net.minecraft.world.phys.Vec3;

/**
 * <p>Implementation of a mouse picker to create a ray out of a camera.</p>
 *
 * @author Ocelot
 */
public class MousePicker
{
    /**
     * Calculates the ray facing out of the screen.
     *
     * @param projectionMatrix The current projection matrix
     * @param viewMatrix       The current transformed view
     * @param normalizedMouseX The normalized x position. From -1.0 to 1.0 of the viewport
     * @param normalizedMouseY The normalized t position. From -1.0 to 1.0 of the viewport
     * @return A ray pointing out of the camera into the 3D world
     */
    public static Vec3 getRay(Matrix4f projectionMatrix, Matrix4f viewMatrix, float normalizedMouseX, float normalizedMouseY)
    {
        Vector4f clipCoords = new Vector4f(normalizedMouseX, -normalizedMouseY, -1.0F, 1.0F);
        Vector4f eyeSpace = toEyeCoords(projectionMatrix, clipCoords);
        return toWorldCoords(viewMatrix, eyeSpace);
    }

    private static Vector4f toEyeCoords(Matrix4f projectionMatrix, Vector4f clipCoords)
    {
        Matrix4f inverse = MagicMath.invertProjection(projectionMatrix);
        Vector4f result = new Vector4f(clipCoords.x(), clipCoords.y(), clipCoords.z(), clipCoords.w());
        result.transform(inverse);
        result.setZ(-1.0F);
        result.setW(0.0F);
        return result;
    }

    private static Vec3 toWorldCoords(Matrix4f viewMatrix, Vector4f eyeCoords)
    {
        Matrix4f inverse = MagicMath.invertGeneric(viewMatrix);
        Vector4f result = new Vector4f(eyeCoords.x(), eyeCoords.y(), eyeCoords.z(), eyeCoords.w());
        result.transform(inverse);
        return new Vec3(result.x(), result.y(), result.z()).normalize();
    }
}
