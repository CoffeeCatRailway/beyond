package io.github.ocelot.space.client.screen;

import io.github.ocelot.space.SpacePrototype;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class SpaceTravelScreen extends Screen
{
    public SpaceTravelScreen()
    {
        super(new TranslationTextComponent("screen." + SpacePrototype.MOD_ID + ".space_travel"));
    }
}
