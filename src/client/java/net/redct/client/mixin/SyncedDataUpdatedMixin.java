package net.redct.client.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.redct.client.utils.Utils;
import net.redct.client.utils.entity.EntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class SyncedDataUpdatedMixin {

    @Inject(
            method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V",
            at = @At("TAIL")
    )
    private void onSyncedDataUpdated(EntityDataAccessor<?> accessor, CallbackInfo ci) {
        if (!Utils.inHypixel) return;

        if (accessor.equals(EntityAccessor.getCustomNameAccessor())) {
            Entity self = (Entity) (Object) this;
            EntityManager.onNameResolved(self, self.getCustomName());
        }
    }


}
