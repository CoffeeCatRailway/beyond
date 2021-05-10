package io.github.ocelot.beyond.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import org.lwjgl.system.NativeResource;

import java.util.Random;

/**
 * <p>Creates and renders the custom starry sky used in space.</p>
 *
 * @author Ocelot
 */
public class SpaceStarsRenderer implements NativeResource
{
    private VertexBuffer starsVBO;

    private void generateSky()
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
        if (this.starsVBO != null)
            this.starsVBO.close();

        this.starsVBO = new VertexBuffer(DefaultVertexFormats.POSITION_COLOR);
        Random random = new Random(10842L);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < 3000; ++i)
        {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d18 = (double) ((j & 2) - 1) * d3;
                    double d19 = (double) ((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    float blue = 0.7F + random.nextFloat() * 0.3F;
                    bufferbuilder.vertex(d5 + d25, d6 + d23, d7 + d26).color(blue, blue, 1.0F, random.nextFloat()).endVertex();
                }
            }
        }
        bufferbuilder.end();
        this.starsVBO.upload(bufferbuilder);
    }

    /**
     * Renders the stars to the screen with the provided transformation.
     *
     * @param poseStack The pose stack to get the transformation from
     */
    public void render(MatrixStack poseStack)
    {
        RenderSystem.disableTexture();
        if (this.starsVBO == null)
            this.generateSky();
        this.starsVBO.bind();
        DefaultVertexFormats.POSITION_COLOR.setupBufferState(0L);
        this.starsVBO.draw(poseStack.last().pose(), 7);
        VertexBuffer.unbind();
        DefaultVertexFormats.POSITION_COLOR.clearBufferState();
        RenderSystem.enableTexture();
    }

    @Override
    public void free()
    {
        if (this.starsVBO != null)
        {
            this.starsVBO.close();
            this.starsVBO = null;
        }
    }
}
