package io.github.ocelot.beyond.datagen;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EntityTypeTagGen extends EntityTypeTagsProvider
{
    public EntityTypeTagGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, Beyond.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
    }
}
