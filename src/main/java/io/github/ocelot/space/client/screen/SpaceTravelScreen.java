package io.github.ocelot.space.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.ocelot.space.SpacePrototype;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Random;

/**
 * @author Ocelot
 */
public class SpaceTravelScreen extends Screen
{
    private static final VertexFormat starFormat = DefaultVertexFormats.POSITION;

    private VertexBuffer starBuffer;

    public SpaceTravelScreen()
    {
        super(new TranslationTextComponent("screen." + SpacePrototype.MOD_ID + ".space_travel"));
    }

    private void destroyStars()
    {
        if (this.starBuffer != null)
        {
            this.starBuffer.close();
            this.starBuffer = null;
        }
    }

    private void createStars()
    {
        this.destroyStars();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        this.starBuffer = new VertexBuffer(starFormat);
        this.drawStars(bufferbuilder);
        bufferbuilder.end();
        this.starBuffer.upload(bufferbuilder);
    }

    private void drawStars(BufferBuilder builder)
    {
        Random random = new Random(10842L);
        builder.begin(7, starFormat);

        for (int i = 0; i < 1500; ++i)
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
                    builder.vertex(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }

    }

    @Override
    protected void init()
    {
        super.init();
        this.createStars();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        if (this.starBuffer == null)
            this.createStars();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.starBuffer.bind();
        starFormat.setupBufferState(0L);
        this.starBuffer.draw(poseStack.last().pose(), 7);
        VertexBuffer.unbind();
        starFormat.clearBufferState();

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed()
    {
        this.destroyStars();
    }
}
