package io.github.ocelot.space.client;

import io.github.ocelot.sonar.client.util.SonarSpriteUploader;
import io.github.ocelot.space.SpacePrototype;
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

/**
 * @author Ocelot
 */
@OnlyIn(Dist.CLIENT)
public class SpaceGuiSpriteManager
{
    public static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(SpacePrototype.MOD_ID, "textures/atlas/gui.png");

    private static SonarSpriteUploader spriteUploader;

    private SpaceGuiSpriteManager()
    {
    }

    private static void registerSprites(IResourceManager resourceManager, SonarSpriteUploader uploader)
    {
    }

    public static void init(IEventBus bus)
    {
        bus.addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, event ->
        {
            Minecraft minecraft = Minecraft.getInstance();
            SonarSpriteUploader spriteUploader = new SonarSpriteUploader(minecraft.textureManager, ATLAS_LOCATION, "gui");
            IResourceManager resourceManager = minecraft.getResourceManager();
            registerSprites(resourceManager, spriteUploader);
            if (resourceManager instanceof IReloadableResourceManager)
            {
                ((IReloadableResourceManager) resourceManager).registerReloadListener(spriteUploader);
            }
            SpaceGuiSpriteManager.spriteUploader = spriteUploader;
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
