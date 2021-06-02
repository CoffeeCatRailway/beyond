package io.github.ocelot.beyond.datagen;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondBiomes;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
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
        this.addBiome(BeyondBiomes.MOON_LOCATION.location(), "Moon");
        this.addBlock(BeyondBlocks.MOON_ROCK, "Moon Rock");
        this.addBlock(BeyondBlocks.ROCKET_CONSTRUCTION_PLATFORM, "Rocket Construction Platform");
        this.addBlock(BeyondBlocks.ROCKET_CONTROLLER, "Rocket Controller");
        this.addBlock(BeyondBlocks.ROCKET_THRUSTER, "Rocket Thruster");
        this.add("multiplayer." + Beyond.MOD_ID + ".disconnect.invalid_space_travel", "Invalid Space Travel");
        this.add("screen." + Beyond.MOD_ID + ".space_travel", "Space Travel");
        this.add("gui." + Beyond.MOD_ID + ".launch", "Launch!");
        this.add("gui." + Beyond.MOD_ID + ".cannot_launch", "Cannot Launch");
        this.add("gui." + Beyond.MOD_ID + ".already_there", "Already There");
        this.add("itemGroup." + Beyond.MOD_ID, "Beyond");
        this.add("block." + Beyond.MOD_ID + ".rocket_controller.scanning", "Already scanning");
        this.add("block." + Beyond.MOD_ID + ".rocket_controller.large", "Rocket is too large");
        this.add("block." + Beyond.MOD_ID + ".rocket_controller.too_many_controllers", "Too many Rocket Controllers. Found %s, expected 1");
    }

    private void addBiome(ResourceLocation id, String value)
    {
        this.add("biome." + id.getNamespace() + "." + id.getPath(), value);
    }

    private void addError(ResourceLocation id, String error, String value)
    {
        this.add("error." + id.getNamespace() + "." + id.getPath() + ".error", value);
    }
}
