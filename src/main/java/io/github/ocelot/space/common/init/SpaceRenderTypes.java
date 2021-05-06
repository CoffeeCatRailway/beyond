package io.github.ocelot.space.common.init;

import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.client.SpacePlanetSpriteManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.SortedMap;

@OnlyIn(Dist.CLIENT)
public final class SpaceRenderTypes extends RenderType
{
    private static final RenderType PLANET_SHADE = create(SpacePrototype.MOD_ID + ":planet_shade", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, RenderType.State.builder().setTextureState(new RenderState.TextureState(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType PLANET = create(SpacePrototype.MOD_ID + ":planet", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, false, RenderType.State.builder().setTextureState(new RenderState.TextureState(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType PLANET_ATMOSPHERE_SHADE = create(SpacePrototype.MOD_ID + ":planet_atmosphere_shade", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, RenderType.State.builder().setTextureState(new RenderState.TextureState(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType PLANET_ATMOSPHERE = create(SpacePrototype.MOD_ID + ":planet_atmosphere", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, RenderType.State.builder().setTextureState(new RenderState.TextureState(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType[] PLANETS = {PLANET, PLANET_SHADE, PLANET_ATMOSPHERE, PLANET_ATMOSPHERE_SHADE};
    private static final RenderType PLANET_SELECT = create(SpacePrototype.MOD_ID + ":planet_select", DefaultVertexFormats.NEW_ENTITY, 7, 256, true, true, RenderType.State.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).createCompositeState(false));
    private static final SortedMap<RenderType, BufferBuilder> FIXED_BUFFERS = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) ->
    {
        for (RenderType planet : PLANETS)
            put(map, planet);
        put(map, PLANET_SELECT);
        put(map, RenderType.entityCutout(PlayerContainer.BLOCK_ATLAS));
    });
    private static final IRenderTypeBuffer.Impl BUFFER = IRenderTypeBuffer.immediateWithBuffers(FIXED_BUFFERS, new BufferBuilder(256));

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> p_228486_0_, RenderType p_228486_1_)
    {
        p_228486_0_.put(p_228486_1_, new BufferBuilder(p_228486_1_.bufferSize()));
    }

    private SpaceRenderTypes(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_)
    {
        super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
    }

    public static RenderType planet(boolean shade, boolean atmosphere)
    {
        return PLANETS[(shade ? 1 : 0) + (atmosphere ? 2 : 0)];
    }

    public static RenderType planetSelect()
    {
        return PLANET_SELECT;
    }

    public static IRenderTypeBuffer.Impl planetBuffer()
    {
        return BUFFER;
    }
}
