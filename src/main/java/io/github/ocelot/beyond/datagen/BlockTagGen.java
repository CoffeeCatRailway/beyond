package io.github.ocelot.beyond.datagen;

import io.github.ocelot.beyond.Beyond;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagGen extends BlockTagsProvider
{
    public BlockTagGen(DataGenerator generatorIn, ExistingFileHelper existingFileHelper)
    {
        super(generatorIn, Beyond.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
    }
}
