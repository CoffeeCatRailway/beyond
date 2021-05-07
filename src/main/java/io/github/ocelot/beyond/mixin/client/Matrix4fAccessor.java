package io.github.ocelot.beyond.mixin.client;

import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Matrix4f.class)
public interface Matrix4fAccessor
{
    @Accessor("m00")
    float getm00();

    @Accessor("m01")
    float getm01();

    @Accessor("m02")
    float getm02();

    @Accessor("m03")
    float getm03();

    @Accessor("m10")
    float getm10();

    @Accessor("m11")
    float getm11();

    @Accessor("m12")
    float getm12();

    @Accessor("m13")
    float getm13();

    @Accessor("m20")
    float getm20();

    @Accessor("m21")
    float getm21();

    @Accessor("m22")
    float getm22();

    @Accessor("m23")
    float getm23();

    @Accessor("m30")
    float getm30();

    @Accessor("m31")
    float getm31();

    @Accessor("m32")
    float getm32();

    @Accessor("m33")
    float getm33();

    @Accessor("m00")
    void setm00(float value);

    @Accessor("m01")
    void setm01(float value);

    @Accessor("m02")
    void setm02(float value);

    @Accessor("m03")
    void setm03(float value);

    @Accessor("m10")
    void setm10(float value);

    @Accessor("m11")
    void setm11(float value);

    @Accessor("m12")
    void setm12(float value);

    @Accessor("m13")
    void setm13(float value);

    @Accessor("m20")
    void setm20(float value);

    @Accessor("m21")
    void setm21(float value);

    @Accessor("m22")
    void setm22(float value);

    @Accessor("m23")
    void setm23(float value);

    @Accessor("m30")
    void setm30(float value);

    @Accessor("m31")
    void setm31(float value);

    @Accessor("m32")
    void setm32(float value);

    @Accessor("m33")
    void setm33(float value);
}
