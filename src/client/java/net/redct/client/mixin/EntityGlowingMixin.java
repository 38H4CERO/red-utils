package net.redct.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.redct.client.utils.entity.GlowRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class EntityGlowingMixin {
    private boolean glow;
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void shouldEntityGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        switch (entity.getType().toShortString()){
            case "armor_stand":
                break;
            case "player":
                if (entity.getTeam()!= null ? entity.getTeam().getNameTagVisibility().toString().equals("ALWAYS") : false){
                    //cir.setReturnValue(true);
                } else {
                    // Puede se goblins... etc y npc
                }
            default:
                glow = GlowRegistry.shouldGlow(entity);
                cir.setReturnValue(glow);
        }
    }
}
