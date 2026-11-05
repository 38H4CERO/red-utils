package net.redct.client.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.redct.client.utils.Logger;
import net.redct.client.utils.Utils;
import net.redct.client.utils.entity.EntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMaxHPMixin {
    private static EntityManager manager = EntityManager.getInstance();
    @Inject(method = "onAttributeUpdated", at = @At("TAIL"))
    private void onAttrUpdated(Holder<Attribute> attribute, CallbackInfo ci) {
        if (!Utils.inHypixel) return;
        LivingEntity self = (LivingEntity) (Object) this;


        if (attribute == Attributes.MAX_HEALTH) {
            if (!self.getType().toShortString().equals("armor_stand")){
                manager.tryMatch(self, self.getAttributeBaseValue(Attributes.MAX_HEALTH) );
                Logger.log("HP", "Name: %s| HP: %s", self.getName().getString() ,self.getAttributeBaseValue(Attributes.MAX_HEALTH));
            }
            //foo(self.getUUID(), self.getAttributeBaseValue(Attributes.MAX_HEALTH));
            //if (!self.getType().toShortString().equals("armor_stand")) Logger.log("Mixin", "%s %s",self.getType().toShortString(), self.getAttributeBaseValue(Attributes.MAX_HEALTH));
        }
    }
}
