package io.github.ocelot.beyond.client;

import io.github.ocelot.sonar.client.util.SonarSpriteUploader;
import io.github.ocelot.beyond.Beyond;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.stream.Collectors;

/**
 * @author Ocelot
 */
@OnlyIn(Dist.CLIENT)
public class SpacePlanetSpriteManager
{
    public static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(Beyond.MOD_ID, "textures/atlas/planets.png");

    private static SonarSpriteUploader spriteUploader;

    private SpacePlanetSpriteManager()
    {
    }

    private static void registerSprites(IResourceManager resourceManager, SonarSpriteUploader uploader)
    {
        uploader.registerSpriteSupplier(() -> resourceManager.listResources("textures/planet", s -> s.endsWith(".png")).stream().map(resourceLocation -> new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath().substring("textures/planet/".length(), resourceLocation.getPath().length() - 4))).collect(Collectors.toSet()));
    }

    public static void init(IEventBus bus)
    {
        bus.addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, event ->
        {
            Minecraft minecraft = Minecraft.getInstance();
            SonarSpriteUploader spriteUploader = new SonarSpriteUploader(minecraft.textureManager, ATLAS_LOCATION, "planet");
            spriteUploader.setMipmapLevels(3); // 8 is 2^3
            IResourceManager resourceManager = minecraft.getResourceManager();
            registerSprites(resourceManager, spriteUploader);
            if (resourceManager instanceof IReloadableResourceManager)
            {
                ((IReloadableResourceManager) resourceManager).registerReloadListener(spriteUploader);
            }
            SpacePlanetSpriteManager.spriteUploader = spriteUploader;
        });
    }

    /**
     * Checks the texture map for the specified icon.
     *
     * @param icon The key of the icon
     * @return The sprite for the specified key
     */
    public static TextureAtlasSprite getSprite(ResourceLocation icon)
    {
        return spriteUploader.getSprite(icon);
    }
}
