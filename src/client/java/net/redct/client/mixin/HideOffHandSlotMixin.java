package net.redct.client.mixin;

import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.redct.client.mixin.accessor.SlotAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.redct.client.module.ModuleManager.isModuleEnabled;

@Mixin(InventoryMenu.class)
public abstract class HideOffHandSlotMixin {
    @Unique private static final int OFF_HAND_SLOT = 40;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void redutils$hideOffhandSlot(CallbackInfo ci) {
        // Changes apply in lobby change, I don't care enough to fix it
        if(isModuleEnabled("clean_inventory")) {
            for (Slot slot : ((InventoryMenu) (Object) this).slots) {
                if (slot.getContainerSlot() == OFF_HAND_SLOT) {
                    SlotAccessor acc = (SlotAccessor) slot;
                    acc.redutils$setX(-1000);
                    acc.redutils$setY(-1000);
                    break;
                }
            }
        }
    }
}


// This works but its a little bit more expensive per tick
/*
@Mixin(Slot.class)
public abstract class HideOffHandSlotMixin {
    @Unique
    private static final int OFF_HAND_SLOT = 40;

    @Shadow @Final
    public Container container;
    @Shadow
    public abstract int getContainerSlot();

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void redutils$hideOffhand(CallbackInfoReturnable<Boolean> cir) {
        if (this.getContainerSlot() == OFF_HAND_SLOT && this.container instanceof Inventory) {
            cir.setReturnValue(false);
        }
    }
}
*/