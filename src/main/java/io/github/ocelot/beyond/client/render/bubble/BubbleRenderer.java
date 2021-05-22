package io.github.ocelot.beyond.client.render.bubble;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;

import javax.annotation.Nullable;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

/**
 * <p>Renders bubbles from specific points.</p>
 *
 * @author Ocelot
 */
public class BubbleRenderer
{
    public static final int PADDING = 8;
    public static final int LINE_WIDTH = 5;
    public static final int LINE_SHEERING = 20;

    public static final float LINE_RED = 38F / 255F;
    public static final float LINE_GREEN = 30F / 255F;
    public static final float LINE_BLUE = 36F / 255F;
    public static final float LINE_ALPHA = 0.8F;
    public static final float BOX_RED = 38F / 255F;
    public static final float BOX_GREEN = 30F / 255F;
    public static final float BOX_BLUE = 36F / 255F;
    public static final float BOX_ALPHA = 0.7F;

    // TODO make fit in screen
    /**
     * Draws a bubble at the specified x, y position.
     *
     * @param poseStack       The stack of transformations
     * @param x               The x position to place the point
     * @param y               The y position to place the point
     * @param width           The width of the box excluding padding
     * @param height          The height of the box excluding padding
     * @param contentRenderer The renderer for the content or <code>null</code> to only show the box
     */
    public static void render(PoseStack poseStack, int x, int y, int width, int height, @Nullable ContentRenderer contentRenderer)
    {
        int boxWidth = width + PADDING * 2;
        int boxHeight = height + PADDING * 2;
        int length = boxHeight + 40;

        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(GL_TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        // Line
        builder.vertex(matrix4f, 0, 0, 0).color(LINE_RED, LINE_GREEN, LINE_BLUE, LINE_ALPHA).endVertex();
        builder.vertex(matrix4f, LINE_SHEERING + LINE_WIDTH, -length, 0).color(LINE_RED, LINE_GREEN, LINE_BLUE, LINE_ALPHA).endVertex();
        builder.vertex(matrix4f, LINE_SHEERING, -length, 0).color(LINE_RED, LINE_GREEN, LINE_BLUE, LINE_ALPHA).endVertex();
        builder.vertex(matrix4f, 0, 0, 0).color(LINE_RED, LINE_GREEN, LINE_BLUE, LINE_ALPHA).endVertex();
        builder.vertex(matrix4f, 1, 0, 0).color(LINE_RED, LINE_GREEN, LINE_BLUE, LINE_ALPHA).endVertex();
        builder.vertex(matrix4f, LINE_SHEERING + LINE_WIDTH, -length, 0).color(LINE_RED, LINE_GREEN, LINE_BLUE, LINE_ALPHA).endVertex();

        // Box
        builder.vertex(matrix4f, LINE_SHEERING + LINE_WIDTH - ((float) boxHeight / (float) length) * (LINE_SHEERING + LINE_WIDTH - 1), boxHeight - length, 0).color(BOX_RED, BOX_GREEN, BOX_BLUE, BOX_ALPHA).endVertex();
        builder.vertex(matrix4f, LINE_SHEERING + LINE_WIDTH + boxWidth, boxHeight - length, 0).color(BOX_RED, BOX_GREEN, BOX_BLUE, BOX_ALPHA).endVertex();
        builder.vertex(matrix4f, LINE_SHEERING + LINE_WIDTH, -length, 0).color(BOX_RED, BOX_GREEN, BOX_BLUE, BOX_ALPHA).endVertex();
        builder.vertex(matrix4f, LINE_SHEERING + LINE_WIDTH + boxWidth, boxHeight - length, 0).color(BOX_RED, BOX_GREEN, BOX_BLUE, BOX_ALPHA).endVertex();
        builder.vertex(matrix4f, LINE_SHEERING + LINE_WIDTH + boxWidth, -length, 0).color(BOX_RED, BOX_GREEN, BOX_BLUE, BOX_ALPHA).endVertex();
        builder.vertex(matrix4f, LINE_SHEERING + LINE_WIDTH, -length, 0).color(BOX_RED, BOX_GREEN, BOX_BLUE, BOX_ALPHA).endVertex();

        Tesselator.getInstance().end();

        if (contentRenderer != null)
        {
            poseStack.translate(LINE_WIDTH + LINE_SHEERING, -length, 0.0F);
            contentRenderer.render(x + LINE_WIDTH + LINE_SHEERING, y - length, boxWidth, boxHeight);
        }
        poseStack.popPose();
    }

    /**
     * <p>Draws the content of bubbles.</p>
     *
     * @author Ocelot
     */
    @FunctionalInterface
    public interface ContentRenderer
    {
        /**
         * Draws the content of the bubble.
         *
         * @param boxX      The starting x position of the box
         * @param boxY      The starting y position of the box
         * @param boxWidth  The x size of the box including padding
         * @param boxHeight The y size of the box including padding
         */
        void render(int boxX, int boxY, int boxWidth, int boxHeight);
    }
}
