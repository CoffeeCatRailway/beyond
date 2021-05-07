package io.github.ocelot.beyond.datagen;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemTagGen extends ItemTagsProvider
{
    public ItemTagGen(DataGenerator generator, BlockTagsProvider blockTagsProvider, ExistingFileHelper existingFileHelper)
    {
        super(generator, blockTagsProvider, Beyond.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
    }
}
