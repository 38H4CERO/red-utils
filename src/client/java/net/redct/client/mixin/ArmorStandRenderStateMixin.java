package net.redct.client.mixin;

import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.redct.client.utils.accessor.HiddenStateAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ArmorStandRenderState.class)
public class ArmorStandRenderStateMixin implements HiddenStateAccessor {

    @Unique
    private boolean redutils$hidden = false;

    @Override
    public boolean redutils$isHidden() {
        return redutils$hidden;
    }

    @Override
    public void redutils$setHidden(boolean hidden) {
        this.redutils$hidden = hidden;
    }
}
