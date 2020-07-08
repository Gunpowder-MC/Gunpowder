package io.github.nyliummc.essentials.mixin.utilities;

import io.github.nyliummc.essentials.mixin.cast.SpeedSetter;
import net.minecraft.entity.player.PlayerAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerAbilities.class)
public abstract class PlayerAbilitiesAccessor implements SpeedSetter {
    @Shadow
    private float walkSpeed;

    @Shadow
    private float flySpeed;

    @Override
    public void setServerWalkSpeed(float speed) {
        this.walkSpeed = speed;
    }

    @Override
    public void setServerFlySpeed(float speed) {
        this.flySpeed = speed;
    }
}
