package io.github.ocelot.space.mixin.client;

import io.github.ocelot.space.client.screen.SpaceTravelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetHandler.class)
public class ClientPlayNetHandlerMixin
{
    @Shadow
    private boolean started;
    private boolean zooming;

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    public void handleMovePlayer(SPlayerPositionLookPacket packet, CallbackInfo ci)
    {
        if (this.zooming)
        {
            this.zooming = false;
            if (Minecraft.getInstance().screen instanceof SpaceTravelScreen)
                ((SpaceTravelScreen) Minecraft.getInstance().screen).transition();
        }
    }

    @Redirect(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 0))
    public void updateScreenAndTick(Minecraft minecraft, Screen screen)
    {
        if (!(minecraft.screen instanceof SpaceTravelScreen))
        {
            minecraft.setScreen(screen);
        }
        else
        {
            this.started = true;
            this.zooming = true;
        }
    }
}
