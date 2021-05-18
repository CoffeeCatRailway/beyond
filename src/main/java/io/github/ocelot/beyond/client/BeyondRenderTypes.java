package io.github.ocelot.beyond.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.ocelot.beyond.Beyond;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.SortedMap;

@OnlyIn(Dist.CLIENT)
public final class BeyondRenderTypes extends RenderType
{
    private static final RenderType PLANET_SHADE = create(Beyond.MOD_ID + ":planet_shade", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType PLANET = create(Beyond.MOD_ID + ":planet", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType PLANET_ATMOSPHERE_SHADE = create(Beyond.MOD_ID + ":planet_atmosphere_shade", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType PLANET_ATMOSPHERE = create(Beyond.MOD_ID + ":planet_atmosphere", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(SpacePlanetSpriteManager.ATLAS_LOCATION, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true));
    private static final RenderType[] PLANETS = {PLANET, PLANET_SHADE, PLANET_ATMOSPHERE, PLANET_ATMOSPHERE_SHADE};
    private static final RenderType PLANET_SELECT = create(Beyond.MOD_ID + ":planet_select", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, RenderType.CompositeState.builder().setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(NO_DIFFUSE_LIGHTING).setLightmapState(NO_LIGHTMAP).setOverlayState(NO_OVERLAY).createCompositeState(false));
    private static final SortedMap<RenderType, BufferBuilder> FIXED_BUFFERS = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) ->
    {
        put(map, planet(true, false));
        put(map, BeyondRenderTypes.planet(false, false));
        put(map, RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
        put(map, BeyondRenderTypes.planet(true, true));
        put(map, BeyondRenderTypes.planet(false, true));
        put(map, BeyondRenderTypes.planetSelect());
    });
    private static final MultiBufferSource.BufferSource BUFFER = MultiBufferSource.immediateWithBuffers(FIXED_BUFFERS, new BufferBuilder(256));

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> p_228486_0_, RenderType p_228486_1_)
    {
        p_228486_0_.put(p_228486_1_, new BufferBuilder(p_228486_1_.bufferSize()));
    }

    private BeyondRenderTypes(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_)
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

    public static MultiBufferSource.BufferSource planetBuffer()
    {
        return BUFFER;
    }
}
