package io.github.ocelot.beyond.common.space.simulation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.ocelot.beyond.common.MagicMath;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

/**
 * <p>A single moving body in a {@link CelestialBodySimulation}.</p>
 *
 * @author Ocelot
 */
@OnlyIn(Dist.CLIENT)
public interface SimulatedBody
{
    /**
     * Steps through the simulation.
     */
    void tick();

    /**
     * @return The id of this body in the simulation
     */
    ResourceLocation getId();

    /**
     * @return The parent of this body
     */
    Optional<ResourceLocation> getParent();

    /**
     * @return The name of this body in the simulation
     */
    Component getDisplayName();

    /**
     * @return The tooltip text of this body in the simulation
     */
    Optional<Component> getDescription();

    /**
     * Casts a ray through this body to check for an intersection.
     *
     * @param start        The starting position of the ray
     * @param end          The ending position of the ray
     * @param partialTicks The percentage from last update and this update
     * @return The optional result of the ray trace
     */
    default Optional<Vec3> clip(Vec3 start, Vec3 end, float partialTicks)
    {
        float size = Math.max(this.getSize(), 1.0F) / 2F;
        float x = this.getX(partialTicks);
        float y = this.getY(partialTicks);
        float z = this.getZ(partialTicks);
        float rotationX = this.getRotationX(partialTicks);
        float rotationY = this.getRotationY(partialTicks);
        float rotationZ = this.getRotationZ(partialTicks);
        AABB box = new AABB(x - size, y - size, z - size, x + size, y + size, z + size);

        PoseStack stack = new PoseStack();
        stack.translate(x, y, z);
        stack.mulPose(Vector3f.XN.rotation(rotationX));
        stack.mulPose(Vector3f.YN.rotation(rotationY));
        stack.mulPose(Vector3f.ZN.rotation(rotationZ));
        stack.translate(-x, -y, -z);
        Matrix4f matrix = stack.last().pose();

        return box.clip(MagicMath.transform(start, matrix), MagicMath.transform(end, matrix)).map(p -> MagicMath.transform(p, MagicMath.invertGeneric(matrix)));
    }

    /**
     * @return The size of this body
     */
    float getSize();

    /**
     * @return The distance from this body and the body being orbited
     */
    float getDistanceFromParent();

    /**
     * Calculates the x position of this body.
     *
     * @param partialTicks The percentage from last tick and this tick
     * @return The x position of this body
     */
    float getX(float partialTicks);

    /**
     * Calculates the y position of this body.
     *
     * @param partialTicks The percentage from last tick and this tick
     * @return The y position of this body
     */
    float getY(float partialTicks);

    /**
     * Calculates the z position of this body.
     *
     * @param partialTicks The percentage from last tick and this tick
     * @return The z position of this body
     */
    float getZ(float partialTicks);

    /**
     * Calculates the rotation of this body.
     *
     * @param partialTicks The percentage from last tick and this tick
     * @return The x rotation of this body
     */
    float getRotationX(float partialTicks);

    /**
     * Calculates the rotation of this body.
     *
     * @param partialTicks The percentage from last tick and this tick
     * @return The y rotation of this body
     */
    float getRotationY(float partialTicks);

    /**
     * Calculates the rotation of this body.
     *
     * @param partialTicks The percentage from last tick and this tick
     * @return The z rotation of this body
     */
    float getRotationZ(float partialTicks);

    /**
     * @return Whether or not this body can be flown to by players
     */
    boolean canTeleportTo();

    /**
     * @return The dimension to teleport the player to if they fly here
     */
    Optional<ResourceLocation> getDimension();

    /**
     * @return The type of renderer to use
     */
    RenderType getRenderType();

    /**
     * <p>The types of bodies that can be rendered.</p>
     *
     * @author Ocelot
     */
    enum RenderType
    {
        CUBE, MODEL, PLAYER
    }
}
