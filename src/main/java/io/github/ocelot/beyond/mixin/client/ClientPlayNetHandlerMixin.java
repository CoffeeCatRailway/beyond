package io.github.ocelot.beyond.mixin.client;

import io.github.ocelot.beyond.client.screen.SpaceTravelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetHandlerMixin
{
    @Shadow
    private boolean started;

    @Unique
    private boolean zooming;

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    public void handleMovePlayer(ClientboundPlayerPositionPacket packet, CallbackInfo ci)
    {
        if (this.zooming)
        {
            this.zooming = false;
            if (Minecraft.getInstance().screen instanceof SpaceTravelScreen)
                ((SpaceTravelScreen) Minecraft.getInstance().screen).transition();
        }
    }

    @Redirect(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0))
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
