package io.github.ocelot.beyond.client;

import io.github.ocelot.beyond.Beyond;
import io.github.ocelot.beyond.client.screen.SpaceTravelScreen;
import io.github.ocelot.beyond.common.init.BeyondMessages;
import io.github.ocelot.beyond.common.network.play.message.CTemporaryOpenSpaceTravelMessage;
import io.github.ocelot.beyond.common.space.PlayerRocket;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Beyond.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    @SubscribeEvent
    public static void onEvent(InputEvent.KeyInputEvent event)
    {
        if (event.getKey() == GLFW.GLFW_KEY_L && Minecraft.getInstance().screen == null && Minecraft.getInstance().player != null)
        {
            BeyondMessages.PLAY.sendToServer(new CTemporaryOpenSpaceTravelMessage());
        }
    }
}
