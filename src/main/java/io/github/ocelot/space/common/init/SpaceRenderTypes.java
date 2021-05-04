package io.github.ocelot.space.common.init;

import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.client.SpacePlanetSpriteManager;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SpaceRenderTypes extends RenderType
{
    private static final RenderType PLANET_SHADE = create(SpacePrototype.MOD_ID + ":planet_shade", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, RenderType.State.builder().setTextureState(new RenderState.TextureState(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType PLANET = create(SpacePrototype.MOD_ID + ":planet", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, RenderType.State.builder().setTextureState(new RenderState.TextureState(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType PLANET_SELECT = create(SpacePrototype.MOD_ID + ":planet_select", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, RenderType.State.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).createCompositeState(false));

    private SpaceRenderTypes(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_)
    {
        super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
    }

    public static RenderType planetShade()
    {
        return PLANET_SHADE;
    }

    public static RenderType planet()
    {
        return PLANET;
    }

    public static RenderType planetSelect()
    {
        return PLANET_SELECT;
    }
}
