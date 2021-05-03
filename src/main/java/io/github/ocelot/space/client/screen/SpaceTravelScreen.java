package io.github.ocelot.space.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.client.screen.component.SolarSystemWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.system.NativeResource;

/**
 * @author Ocelot
 */
public class SpaceTravelScreen extends Screen
{
    private SolarSystemWidget solarSystemWidget;

    public SpaceTravelScreen()
    {
        super(new TranslationTextComponent("screen." + SpacePrototype.MOD_ID + ".space_travel"));
    }

    @Override
    protected void init()
    {
        if (this.solarSystemWidget != null)
            this.solarSystemWidget.free();
        this.addButton(this.solarSystemWidget = new SolarSystemWidget(0, 0, this.width, this.height));
    }

    @Override
    public void tick()
    {
        for (Widget widget : this.buttons)
            if (widget instanceof IScreen)
                ((IScreen) widget).tick();
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        partialTicks = Minecraft.getInstance().getFrameTime();
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed()
    {
        for (Widget widget : this.buttons)
            if (widget instanceof NativeResource)
                ((NativeResource) widget).free();
    }
}
