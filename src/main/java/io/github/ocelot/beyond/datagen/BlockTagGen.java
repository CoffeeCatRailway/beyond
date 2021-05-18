package io.github.ocelot.beyond.datagen;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.common.init.BeyondBlocks;
import io.github.ocelot.beyond.common.init.BeyondTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
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
        this.tag(BeyondTags.INFINIBURN_MOON).addTag(BlockTags.INFINIBURN_OVERWORLD);
        this.tag(BeyondTags.BASE_STONE_MOON).add(BeyondBlocks.MOON_ROCK.get());
    }
}
