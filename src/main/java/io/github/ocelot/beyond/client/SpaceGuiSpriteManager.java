package io.github.ocelot.beyond.client;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.sonar.client.util.SonarSpriteUploader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
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
    public static final ResourceLocation ATLAS_LOCATION = new ResourceLocation(Beyond.MOD_ID, "textures/atlas/gui.png");

    private static SonarSpriteUploader spriteUploader;

    private SpaceGuiSpriteManager()
    {
    }

    private static void registerSprites(ResourceManager resourceManager, SonarSpriteUploader uploader)
    {
    }

    public static void init(IEventBus bus)
    {
        bus.addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, event ->
        {
            Minecraft minecraft = Minecraft.getInstance();
            SonarSpriteUploader spriteUploader = new SonarSpriteUploader(minecraft.textureManager, ATLAS_LOCATION, "gui");
            ResourceManager resourceManager = minecraft.getResourceManager();
            registerSprites(resourceManager, spriteUploader);
            if (resourceManager instanceof ReloadableResourceManager)
            {
                ((ReloadableResourceManager) resourceManager).registerReloadListener(spriteUploader);
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
