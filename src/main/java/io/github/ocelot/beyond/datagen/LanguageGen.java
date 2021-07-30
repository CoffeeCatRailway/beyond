package io.github.ocelot.beyond.datagen;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondBiomes;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import io.github.ocelot.beyond.common.init.BeyondEntities;
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
        this.addBlock(BeyondBlocks.LANDING_PAD, "Landing Pad");
        this.addBlock(BeyondBlocks.ROCKET_CONTROLLER, "Rocket Controller");
        this.addBlock(BeyondBlocks.ROCKET_THRUSTER, "Rocket Thruster");
        this.addBlock(BeyondBlocks.CREATIVE_ROCKET_THRUSTER, "Creative Rocket Thruster");
        this.addEntityType(BeyondEntities.ROCKET, "Rocket");
        this.add("multiplayer." + Beyond.MOD_ID + ".disconnect.invalid_space_travel", "Invalid Space Travel");
        this.add("screen." + Beyond.MOD_ID + ".space_travel", "Space Travel");
        this.add("gui." + Beyond.MOD_ID + ".launch", "Launch!");
        this.add("gui." + Beyond.MOD_ID + ".cannot_launch", "Cannot Launch");
        this.add("gui." + Beyond.MOD_ID + ".already_there", "Already There");
        this.add("itemGroup." + Beyond.MOD_ID, "Beyond");
        this.add("block." + Beyond.MOD_ID + ".rocket_controller.scanning", "Already scanning");
        this.add("block." + Beyond.MOD_ID + ".rocket_controller.large", "Rocket is too large");
        this.add("block." + Beyond.MOD_ID + ".rocket_controller.too_many_controllers", "Too many Rocket Controllers. Found %s, expected 1");
        this.add("block." + Beyond.MOD_ID + ".rocket_controller.not_enough_thrust", "Not enough Thrust. At least %s is required, has %s");
        this.addAdvancement("root", "Beyond", "To Infinity, and Beyond!");
        this.addAdvancement("launch_rocket", "We Have Liftoff!", "Launch a rocket into space");
        this.addAdvancement("atlas", "Atlas", "Tried to move the earth");
        this.addAdvancement("moon_travel", "One Small Step for a Steve...", "One giant leap for Steve-kind");
    }

    private void addBiome(ResourceLocation id, String value)
    {
        this.add("biome." + id.getNamespace() + "." + id.getPath(), value);
    }

    private void addError(ResourceLocation id, String error, String value)
    {
        this.add("error." + id.getNamespace() + "." + id.getPath() + ".error", value);
    }

    private void addAdvancement(String name, String title, String description)
    {
        this.add("advancements." + Beyond.MOD_ID + "." + name + ".title", title);
        this.add("advancements." + Beyond.MOD_ID + "." + name + ".description", description);
    }
}
