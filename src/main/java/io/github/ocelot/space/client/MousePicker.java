package io.github.ocelot.space.client;

import io.github.ocelot.space.mixin.client.Matrix4fAccessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

public class MousePicker
{
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
        Matrix4f inverse = invertView(viewMatrix);

        Vector4f result = new Vector4f(eyeCoords.x(), eyeCoords.y(), eyeCoords.z(), eyeCoords.w());
        result.transform(inverse);
        return new Vector3d(result.x(), result.y(), result.z()).normalize();
    }

    private static Matrix4f invertProjection(Matrix4f matrix4f)
    {
        Matrix4f result = matrix4f.copy();
        Matrix4fAccessor accessor = (Matrix4fAccessor) (Object) result;
        float a = 1.0f / (accessor.getm00() * accessor.getm11());
        float l = -1.0f / (accessor.getm23() * accessor.getm32());
//        return dest
//                .set(m11 * a, 0, 0, 0,
//                        0, m00 * a, 0, 0,
//                        0, 0, 0, -m23 * l,
//                        0, 0, -m32 * l, m22 * l);
        accessor.setm00(accessor.getm11() * a);
        accessor.setm01(0);
        accessor.setm02(0);
        accessor.setm03(0);
        accessor.setm10(0);
        accessor.setm11(accessor.getm00() * a);
        accessor.setm12(0);
        accessor.setm13(0);
        accessor.setm20(0);
        accessor.setm21(0);
        accessor.setm22(0);
        accessor.setm23(-accessor.getm23() * l);
        accessor.setm20(0);
        accessor.setm21(0);
        accessor.setm22(-accessor.getm32() * l);
        accessor.setm23(accessor.getm22() * l);
        return result;
    }

    private static Matrix4f invertView(Matrix4f matrix4f)
    {
        Matrix4f result = matrix4f.copy();
        Matrix4fAccessor accessor = (Matrix4fAccessor) (Object) result;
        float a = accessor.getm00() * accessor.getm11() - accessor.getm01() * accessor.getm10();
        float b = accessor.getm00() * accessor.getm12() - accessor.getm02() * accessor.getm10();
        float c = accessor.getm00() * accessor.getm13() - accessor.getm03() * accessor.getm10();
        float d = accessor.getm01() * accessor.getm12() - accessor.getm02() * accessor.getm11();
        float e = accessor.getm01() * accessor.getm13() - accessor.getm03() * accessor.getm11();
        float f = accessor.getm02() * accessor.getm13() - accessor.getm03() * accessor.getm12();
        float g = accessor.getm20() * accessor.getm31() - accessor.getm21() * accessor.getm30();
        float h = accessor.getm20() * accessor.getm32() - accessor.getm22() * accessor.getm30();
        float i = accessor.getm20() * accessor.getm33() - accessor.getm23() * accessor.getm30();
        float j = accessor.getm21() * accessor.getm32() - accessor.getm22() * accessor.getm31();
        float k = accessor.getm21() * accessor.getm33() - accessor.getm23() * accessor.getm31();
        float l = accessor.getm22() * accessor.getm33() - accessor.getm23() * accessor.getm32();
        float det = a * l - b * k + c * j + d * i - e * h + f * g;
        det = 1.0f / det;
        accessor.setm00(fma(accessor.getm11(), l, fma(-accessor.getm12(), k, accessor.getm13() * j)) * det);
        accessor.setm01(fma(-accessor.getm01(), l, fma(accessor.getm02(), k, -accessor.getm03() * j)) * det);
        accessor.setm02(fma(accessor.getm31(), f, fma(-accessor.getm32(), e, accessor.getm33() * d)) * det);
        accessor.setm03(fma(-accessor.getm21(), f, fma(accessor.getm22(), e, -accessor.getm23() * d)) * det);
        accessor.setm10(fma(-accessor.getm10(), l, fma(accessor.getm12(), i, -accessor.getm13() * h)) * det);
        accessor.setm11(fma(accessor.getm00(), l, fma(-accessor.getm02(), i, accessor.getm03() * h)) * det);
        accessor.setm12(fma(-accessor.getm30(), f, fma(accessor.getm32(), c, -accessor.getm33() * b)) * det);
        accessor.setm13(fma(accessor.getm20(), f, fma(-accessor.getm22(), c, accessor.getm23() * b)) * det);
        accessor.setm20(fma(accessor.getm10(), k, fma(-accessor.getm11(), i, accessor.getm13() * g)) * det);
        accessor.setm21(fma(-accessor.getm00(), k, fma(accessor.getm01(), i, -accessor.getm03() * g)) * det);
        accessor.setm22(fma(accessor.getm30(), e, fma(-accessor.getm31(), c, accessor.getm33() * a)) * det);
        accessor.setm23(fma(-accessor.getm20(), e, fma(accessor.getm21(), c, -accessor.getm23() * a)) * det);
        accessor.setm30(fma(-accessor.getm10(), j, fma(accessor.getm11(), h, -accessor.getm12() * g)) * det);
        accessor.setm31(fma(accessor.getm00(), j, fma(-accessor.getm01(), h, accessor.getm02() * g)) * det);
        accessor.setm32(fma(-accessor.getm30(), d, fma(accessor.getm31(), b, -accessor.getm32() * a)) * det);
        accessor.setm33(fma(accessor.getm20(), d, fma(-accessor.getm21(), b, accessor.getm22() * a)) * det);
        return result;
    }

    private static float fma(float a, float b, float c)
    {
        return a * b + c;
    }
}
