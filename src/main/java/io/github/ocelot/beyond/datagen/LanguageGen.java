package io.github.ocelot.beyond.datagen;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class LanguageGen extends LanguageProvider
{
    public LanguageGen(DataGenerator gen)
    {
        super(gen, Beyond.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        this.add("screen." + Beyond.MOD_ID + ".space_travel", "Space Travel");
        this.add("gui." + Beyond.MOD_ID + ".launch", "Launch!");
        this.add("gui." + Beyond.MOD_ID + ".cannot_launch", "Cannot Launch");
        this.add("gui." + Beyond.MOD_ID + ".already_there", "Already There");
    }
}
