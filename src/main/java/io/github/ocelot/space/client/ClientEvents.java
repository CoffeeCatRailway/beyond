package io.github.ocelot.space.client;

import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.client.screen.SpaceTravelScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = SpacePrototype.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
    @SubscribeEvent
    public static void onEvent(InputEvent.KeyInputEvent event)
    {
        if (event.getKey() == GLFW.GLFW_KEY_L && Minecraft.getInstance().screen == null)
        {
            Minecraft.getInstance().setScreen(new SpaceTravelScreen());
        }
    }
}
