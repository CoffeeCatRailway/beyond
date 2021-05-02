package io.github.ocelot.space.datagen;

import io.github.ocelot.space.SpacePrototype;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class FluidTagGen extends FluidTagsProvider
{
    public FluidTagGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, SpacePrototype.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
    }
}
