package io.github.ocelot.space.datagen;

import io.github.ocelot.space.SpacePrototype;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagGen extends BlockTagsProvider
{
    public BlockTagGen(DataGenerator generatorIn, ExistingFileHelper existingFileHelper)
    {
        super(generatorIn, SpacePrototype.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
    }
}
