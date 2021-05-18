package io.github.ocelot.beyond.mixin.client;

import io.github.ocelot.beyond.client.screen.SpaceTravelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Unique
    private boolean respawning;

    @Inject(method = "setLevel", at = @At("HEAD"))
    public void setLevel(ClientLevel world, CallbackInfo ci)
    {
        this.respawning = true;
    }

    @Redirect(method = "updateScreenAndTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    public void updateScreenAndTick(Minecraft minecraft, Screen screen)
    {
        if (!this.respawning || !(minecraft.screen instanceof SpaceTravelScreen))
            minecraft.setScreen(screen);
    }
}
