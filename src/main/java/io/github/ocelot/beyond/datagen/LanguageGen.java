package io.github.ocelot.beyond.datagen;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondBiomes;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.RegistryObject;

public class LanguageGen extends LanguageProvider
{
    public LanguageGen(DataGenerator gen)
    {
        super(gen, Beyond.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        this.addBiome(BeyondBiomes.MOON_LOCATION.getRegistryName(), "Moon");
        this.addBlock(BeyondBlocks.MOON_ROCK, "Moon Rock");
        this.add("screen." + Beyond.MOD_ID + ".space_travel", "Space Travel");
        this.add("gui." + Beyond.MOD_ID + ".launch", "Launch!");
        this.add("gui." + Beyond.MOD_ID + ".cannot_launch", "Cannot Launch");
        this.add("gui." + Beyond.MOD_ID + ".already_there", "Already There");
        this.add("itemGroup." + Beyond.MOD_ID, "Beyond");
    }

    private void addBiome(ResourceLocation id, String value)
    {
        this.add("biome." + id.getNamespace() + "." + id.getPath(), value);
    }
}
