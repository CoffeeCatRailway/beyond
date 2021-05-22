package io.github.ocelot.beyond.mixin.client;

import io.github.ocelot.beyond.client.screen.SpaceTravelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
    @Shadow
    @Nullable
    public Screen screen;
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

    @ModifyArg(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"), index = 2)
    public boolean render(boolean renderLevel)
    {
        return renderLevel && !(this.screen instanceof SpaceTravelScreen);
    }
}
