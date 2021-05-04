package io.github.ocelot.space.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.client.screen.component.SolarSystemWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.IScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.system.NativeResource;

/**
 * @author Ocelot
 */
public class SpaceTravelScreen extends Screen
{
    private final SolarSystemWidget solarSystemWidget;

    public SpaceTravelScreen()
    {
        super(new TranslationTextComponent("screen." + SpacePrototype.MOD_ID + ".space_travel"));
        this.addButton(this.solarSystemWidget = new SolarSystemWidget(this, 0, 0, this.width, this.height));
    }

    private void repositionWidgets()
    {
        this.solarSystemWidget.setWidth(this.width);
        this.solarSystemWidget.setHeight(this.height);
    }

    @Override
    public void init(Minecraft minecraft, int width, int height)
    {
        this.minecraft = minecraft;
        this.itemRenderer = minecraft.getItemRenderer();
        this.font = minecraft.font;
        this.width = width;
        this.height = height;
        this.repositionWidgets();
    }

    @Override
    public void tick()
    {
        for (IGuiEventListener listener : this.children)
            if (listener instanceof IScreen)
                ((IScreen) listener).tick();
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
        for (IGuiEventListener listener : this.children)
            if (listener instanceof NativeResource)
                ((NativeResource) listener).free();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        for (IGuiEventListener iguieventlistener : this.children())
        {
            if (iguieventlistener.mouseClicked(mouseX, mouseY, mouseButton))
            {
                this.setFocused(iguieventlistener);
                this.setDragging(true);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy)
    {
        return this.getFocused() != null && this.isDragging() && this.getFocused().mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
    }
}
