package io.github.ocelot.beyond.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

public class RecipeGen extends RecipeProvider
{
    public RecipeGen(DataGenerator generator)
    {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<FinishedRecipe> consumer)
    {
//        ShapedRecipeBuilder.shapedRecipe(ModItems.EXAMPLE.get())
//                .patternLine("DDD")
//                .patternLine("DDD")
//                .patternLine("III")
//                .key('D', Tags.Items.GEMS_DIAMOND)
//                .key('I', Tags.Items.INGOTS_IRON)
//                .addCriterion("has_diamond", hasItem(Tags.Items.GEMS_DIAMOND))
//                .addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON))
//                .build(consumer);
    }
}