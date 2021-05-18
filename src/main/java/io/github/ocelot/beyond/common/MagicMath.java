package io.github.ocelot.beyond.common;

import com.mojang.math.Matrix4f;
import io.github.ocelot.beyond.mixin.client.Matrix4fAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * <p>Contains math I barely understand but it just works so cool bro.</p>
 *
 * @author Ocelot
 */
public class MagicMath
{
    /**
     * Modifies a linear line to allow a configurable steepness using bias.
     */
    public static float bias(float x, float bias)
    {
        float k = (float) Math.pow(1.0F - bias, 3);
        return (x * k) / (x * k - x + 1.0F);
    }

    /**
     * Blends a and b together based on the amount of smoothing using k.
     */
    public static float smoothMin(float a, float b, float k)
    {
        float h = Mth.clamp((b - a + k) / (2.0F * k), 0.0F, 1.0F);
        return a * h + b * (1.0F - h) - k * h * (1.0F - h);
    }

    /**
     * Uses a sin wave to ease from 0.0 to 1.0
     *
     * @param x The input value expected to be 0.0 to 1.0
     * @return The result of the function
     */
    public static float ease(float x)
    {
        return -(Mth.cos((float) (Math.PI * x)) - 1F) / 2F;
    }

    /**
     * Transforms the specified vector by the matrix. *W is assumed to be 1*.
     *
     * @param pos   The position to transform
     * @param input The input matrix
     * @return A transformed matrix
     */
    public static Vec3 transform(Vec3 pos, Matrix4f input)
    {
        Matrix4fAccessor inputAccessor = (Matrix4fAccessor) (Object) input;
        double f = pos.x;
        double f1 = pos.y;
        double f2 = pos.z;
        double x = inputAccessor.getm00() * f + inputAccessor.getm01() * f1 + inputAccessor.getm02() * f2 + inputAccessor.getm03();
        double y = inputAccessor.getm10() * f + inputAccessor.getm11() * f1 + inputAccessor.getm12() * f2 + inputAccessor.getm13();
        double z = inputAccessor.getm20() * f + inputAccessor.getm21() * f1 + inputAccessor.getm22() * f2 + inputAccessor.getm23();
        return new Vec3(x, y, z);
    }

    /**
     * <p>The code was changed to use a mixin accessor instead of directly modifying variables to fit with Minecraft. Taken from <a href="https://github.com/JOML-CI/JOML/blob/main/src/org/joml/Matrix4f.java">JOML</a></p>
     */
    public static Matrix4f invertProjection(Matrix4f matrix4f)
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
    public static Matrix4f invertGeneric(Matrix4f matrix4f)
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
