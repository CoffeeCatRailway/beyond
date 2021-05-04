package io.github.ocelot.space.client;

import io.github.ocelot.space.mixin.client.Matrix4fAccessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

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
    public static Vector3d getRay(Matrix4f projectionMatrix, Matrix4f viewMatrix, float normalizedMouseX, float normalizedMouseY)
    {
        Vector4f clipCoords = new Vector4f(normalizedMouseX, -normalizedMouseY, -1.0F, 1.0F);
        Vector4f eyeSpace = toEyeCoords(projectionMatrix, clipCoords);
        return toWorldCoords(viewMatrix, eyeSpace);
    }

    private static Vector4f toEyeCoords(Matrix4f projectionMatrix, Vector4f clipCoords)
    {
        Matrix4f inverse = invertProjection(projectionMatrix);
        Vector4f result = new Vector4f(clipCoords.x(), clipCoords.y(), clipCoords.z(), clipCoords.w());
        result.transform(inverse);
        result.setZ(-1.0F);
        result.setW(0.0F);
        return result;
    }

    private static Vector3d toWorldCoords(Matrix4f viewMatrix, Vector4f eyeCoords)
    {
        Matrix4f inverse = invertGeneric(viewMatrix);
        Vector4f result = new Vector4f(eyeCoords.x(), eyeCoords.y(), eyeCoords.z(), eyeCoords.w());
        result.transform(inverse);
        return new Vector3d(result.x(), result.y(), result.z()).normalize();
    }

    /**
     * <p>The code was changed to use a mixin accessor instead of directly modifying variables to fit with Minecraft. Taken from <a href="https://github.com/JOML-CI/JOML/blob/main/src/org/joml/Matrix4f.java">JOML</a></p>
     */
    private static Matrix4f invertProjection(Matrix4f matrix4f)
    {
        Matrix4f result = matrix4f.copy();
        Matrix4fAccessor inputAccessor = (Matrix4fAccessor) (Object) matrix4f;
        Matrix4fAccessor resultAccessor = (Matrix4fAccessor) (Object) result;
        float a = 1.0f / (inputAccessor.getm00() * inputAccessor.getm11());
        float l = -1.0f / (inputAccessor.getm23() * inputAccessor.getm32());
        resultAccessor.setm00(inputAccessor.getm11() * a);
        resultAccessor.setm01(0);
        resultAccessor.setm02(0);
        resultAccessor.setm03(0);
        resultAccessor.setm10(0);
        resultAccessor.setm11(inputAccessor.getm00() * a);
        resultAccessor.setm12(0);
        resultAccessor.setm13(0);
        resultAccessor.setm20(0);
        resultAccessor.setm21(0);
        resultAccessor.setm22(0);
        resultAccessor.setm23(-inputAccessor.getm23() * l);
        resultAccessor.setm20(0);
        resultAccessor.setm21(0);
        resultAccessor.setm22(-inputAccessor.getm32() * l);
        resultAccessor.setm23(inputAccessor.getm22() * l);
        return result;
    }

    /**
     * <p>The code was changed to use a mixin accessor instead of directly modifying variables to fit with Minecraft. Taken from <a href="https://github.com/JOML-CI/JOML/blob/main/src/org/joml/Matrix4f.java">JOML</a></p>
     */
    private static Matrix4f invertGeneric(Matrix4f matrix4f)
    {
        Matrix4f result = matrix4f.copy();
        Matrix4fAccessor inputAccessor = (Matrix4fAccessor) (Object) matrix4f;
        Matrix4fAccessor resultAccessor = (Matrix4fAccessor) (Object) result;
        float a = inputAccessor.getm00() * inputAccessor.getm11() - inputAccessor.getm01() * inputAccessor.getm10();
        float b = inputAccessor.getm00() * inputAccessor.getm12() - inputAccessor.getm02() * inputAccessor.getm10();
        float c = inputAccessor.getm00() * inputAccessor.getm13() - inputAccessor.getm03() * inputAccessor.getm10();
        float d = inputAccessor.getm01() * inputAccessor.getm12() - inputAccessor.getm02() * inputAccessor.getm11();
        float e = inputAccessor.getm01() * inputAccessor.getm13() - inputAccessor.getm03() * inputAccessor.getm11();
        float f = inputAccessor.getm02() * inputAccessor.getm13() - inputAccessor.getm03() * inputAccessor.getm12();
        float g = inputAccessor.getm20() * inputAccessor.getm31() - inputAccessor.getm21() * inputAccessor.getm30();
        float h = inputAccessor.getm20() * inputAccessor.getm32() - inputAccessor.getm22() * inputAccessor.getm30();
        float i = inputAccessor.getm20() * inputAccessor.getm33() - inputAccessor.getm23() * inputAccessor.getm30();
        float j = inputAccessor.getm21() * inputAccessor.getm32() - inputAccessor.getm22() * inputAccessor.getm31();
        float k = inputAccessor.getm21() * inputAccessor.getm33() - inputAccessor.getm23() * inputAccessor.getm31();
        float l = inputAccessor.getm22() * inputAccessor.getm33() - inputAccessor.getm23() * inputAccessor.getm32();
        float det = a * l - b * k + c * j + d * i - e * h + f * g;
        det = 1.0f / det;
        resultAccessor.setm00(fma(inputAccessor.getm11(), l, fma(-inputAccessor.getm12(), k, inputAccessor.getm13() * j)) * det);
        resultAccessor.setm01(fma(-inputAccessor.getm01(), l, fma(inputAccessor.getm02(), k, -inputAccessor.getm03() * j)) * det);
        resultAccessor.setm02(fma(inputAccessor.getm31(), f, fma(-inputAccessor.getm32(), e, inputAccessor.getm33() * d)) * det);
        resultAccessor.setm03(fma(-inputAccessor.getm21(), f, fma(inputAccessor.getm22(), e, -inputAccessor.getm23() * d)) * det);
        resultAccessor.setm10(fma(-inputAccessor.getm10(), l, fma(inputAccessor.getm12(), i, -inputAccessor.getm13() * h)) * det);
        resultAccessor.setm11(fma(inputAccessor.getm00(), l, fma(-inputAccessor.getm02(), i, inputAccessor.getm03() * h)) * det);
        resultAccessor.setm12(fma(-inputAccessor.getm30(), f, fma(inputAccessor.getm32(), c, -inputAccessor.getm33() * b)) * det);
        resultAccessor.setm13(fma(inputAccessor.getm20(), f, fma(-inputAccessor.getm22(), c, inputAccessor.getm23() * b)) * det);
        resultAccessor.setm20(fma(inputAccessor.getm10(), k, fma(-inputAccessor.getm11(), i, inputAccessor.getm13() * g)) * det);
        resultAccessor.setm21(fma(-inputAccessor.getm00(), k, fma(inputAccessor.getm01(), i, -inputAccessor.getm03() * g)) * det);
        resultAccessor.setm22(fma(inputAccessor.getm30(), e, fma(-inputAccessor.getm31(), c, inputAccessor.getm33() * a)) * det);
        resultAccessor.setm23(fma(-inputAccessor.getm20(), e, fma(inputAccessor.getm21(), c, -inputAccessor.getm23() * a)) * det);
        resultAccessor.setm30(fma(-inputAccessor.getm10(), j, fma(inputAccessor.getm11(), h, -inputAccessor.getm12() * g)) * det);
        resultAccessor.setm31(fma(inputAccessor.getm00(), j, fma(-inputAccessor.getm01(), h, inputAccessor.getm02() * g)) * det);
        resultAccessor.setm32(fma(-inputAccessor.getm30(), d, fma(inputAccessor.getm31(), b, -inputAccessor.getm32() * a)) * det);
        resultAccessor.setm33(fma(inputAccessor.getm20(), d, fma(-inputAccessor.getm21(), b, inputAccessor.getm22() * a)) * det);
        return result;
    }

    private static float fma(float a, float b, float c)
    {
        return a * b + c;
    }
}
