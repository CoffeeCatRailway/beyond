package io.github.ocelot.space.datagen;

import io.github.ocelot.space.SpacePrototype;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.EntityTypeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EntityTypeTagGen extends EntityTypeTagsProvider
{
    public EntityTypeTagGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, SpacePrototype.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
    }
}
