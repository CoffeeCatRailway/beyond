package io.github.ocelot.space.common.simulation.body;

import com.mojang.authlib.GameProfile;
import io.github.ocelot.space.SpacePrototype;
import io.github.ocelot.space.common.simulation.CelestialBodySimulation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Optional;

/**
 * <p>The player inside the simulation.</p>
 *
 * @author Ocelot
 */
public class PlayerRocket extends AbstractSimulatedBody
{
    private static final float TRANSITION_SPEED = 0.025F;

    private final GameProfile player;
    private final ITextComponent displayName;
    private ResourceLocation parent;
    private ResourceLocation newParent;
    private float newDistanceFromParent;
    private float lastTransition;
    private float transition;

    public PlayerRocket(CelestialBodySimulation simulation, ResourceLocation parent, GameProfile player)
    {
        super(simulation, new ResourceLocation(SpacePrototype.MOD_ID, DigestUtils.md5Hex(player.getName())));
        this.setDistanceFromParent(simulation.getBody(parent).getSize() + 4.0F);
        this.parent = parent;
        this.player = player;
        this.displayName = new StringTextComponent(player.getName());
    }

    private float getTransition(float partialTicks)
    {
        return MathHelper.lerp(partialTicks, this.lastTransition, this.transition);
    }

    @Override
    public void tick()
    {
        super.tick();
        this.lastTransition = this.transition;
        if (this.newParent != null && this.transition < 1.0F)
        {
            this.transition += TRANSITION_SPEED;
            if (this.transition >= 1.0F)
            {
                this.transition = 1.0F;
                this.parent = this.newParent;
                this.newParent = null;
                this.setDistanceFromParent(this.newDistanceFromParent);
                // TODO notify gui of transition completion
            }
        }
    }

    private float getNewHorizontalDistance(float partialTicks)
    {
        return this.newDistanceFromParent * MathHelper.cos(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    private float getNewVerticalDistance(float partialTicks)
    {
        return this.newDistanceFromParent * MathHelper.sin(MathHelper.lerp(partialTicks, this.lastYaw, this.yaw));
    }

    @Override
    public float getX(float partialTicks)
    {
        float x = super.getX(partialTicks);
        if (this.newParent == null)
            return x;
        float newX = this.getNewParent().map(this.simulation::getBody).map(simulatedBody -> simulatedBody.getX(partialTicks) + this.getNewHorizontalDistance(partialTicks)).orElse(0F);
        return MathHelper.lerp(this.getTransition(partialTicks), x, newX);
    }

    @Override
    public float getZ(float partialTicks)
    {
        float z = super.getZ(partialTicks);
        if (this.newParent == null)
            return z;
        float newZ = this.getNewParent().map(this.simulation::getBody).map(simulatedBody -> simulatedBody.getZ(partialTicks) + this.getNewVerticalDistance(partialTicks)).orElse(0F);
        return MathHelper.lerp(this.getTransition(partialTicks), z, newZ);
    }

    @Override
    public Optional<ResourceLocation> getParent()
    {
        return Optional.of(this.parent);
    }

    public Optional<ResourceLocation> getNewParent()
    {
        return Optional.of(this.newParent);
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    @Override
    public float getSize()
    {
        return 1.0F;
    }

    @Override
    public boolean canTeleportTo()
    {
        return false;
    }

    @Override
    public Optional<ResourceLocation> getDimension()
    {
        return Optional.empty();
    }

    @Override
    public RenderType getRenderType()
    {
        return RenderType.PLAYER;
    }

    /**
     * @return The player represented by this rocket
     */
    public GameProfile getPlayer()
    {
        return player;
    }

    /**
     * Sets the body the player rocket should travel to.
     *
     * @param body The body to transition towards
     */
    public void travelTo(ResourceLocation body)
    {
        this.lastTransition = 0.0F;
        this.transition = 0.0F;
        this.newParent = body;
        this.newDistanceFromParent = this.simulation.getBody(body).getSize() + 4.0F;
    }
}
