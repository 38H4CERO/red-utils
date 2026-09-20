package net.redct.client.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.redct.client.data.Location;
import net.redct.client.data.PlayerHeadSkin;
import net.redct.client.utils.PlayerInfo;
import net.redct.client.utils.entity.HiddenArmorStands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.redct.client.module.ModuleManager.isModuleEnabled;
import static net.redct.client.utils.entity.EntityUtils.getArmorStandPlayerHeadSkin;

@Mixin(LivingEntity.class)
public class LivingEntityEquipmentMixin {

    @Inject(
            method = "setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void onEquipmentUpdated(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (slot == EquipmentSlot.HEAD && self instanceof ArmorStand armorStand) {
            if (PlayerInfo.INSTANCE.getCurrentLocation() == Location.GARDEN){
                if (isModuleEnabled("all_in_alloe_tracker")){
                    if (HiddenArmorStands.INSTANCE.isProcessed(armorStand.getUUID())) return;
                    HiddenArmorStands.INSTANCE.setProcessed(armorStand.getUUID());
                    if(PlayerHeadSkin.MAGIC_JELLYBEAM.getId().equals(getArmorStandPlayerHeadSkin(armorStand))){
                        HiddenArmorStands.INSTANCE.hide(armorStand.getUUID());
                    }
                }
            }
        }
    }
}
