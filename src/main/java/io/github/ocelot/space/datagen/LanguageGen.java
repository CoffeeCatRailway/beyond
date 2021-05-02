package io.github.ocelot.space.datagen;

import io.github.ocelot.space.SpacePrototype;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class LanguageGen extends LanguageProvider
{
    public LanguageGen(DataGenerator gen)
    {
        super(gen, SpacePrototype.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
    }
}
