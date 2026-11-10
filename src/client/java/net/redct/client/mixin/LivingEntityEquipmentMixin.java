package net.redct.client.mixin;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.redct.client.data.Location;
import net.redct.client.data.PlayerHeadSkin;
import net.redct.client.utils.PlayerInfo;
import net.redct.client.utils.entity.EntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.redct.client.module.ModuleManager.isModuleEnabled;

@Mixin(LivingEntity.class)
public class LivingEntityEquipmentMixin {
    private final static EntityManager EM = EntityManager.getInstance();
    @Inject(
            method = "setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void onEquipmentUpdated(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getType() == EntityTypes.ARMOR_STAND) {
            EM.tryMatch(self);
        }
    }
}
