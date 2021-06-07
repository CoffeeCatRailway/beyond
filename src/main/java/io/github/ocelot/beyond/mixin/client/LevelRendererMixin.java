package io.github.ocelot.beyond.mixin.client;

import io.github.ocelot.beyond.event.ReloadRenderersEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin
{
    @Shadow
    private ClientLevel level;

    @Inject(method = "allChanged", at = @At("TAIL"))
    public void allChanged(CallbackInfo ci)
    {
        MinecraftForge.EVENT_BUS.post(new ReloadRenderersEvent(this.level));
    }
}
