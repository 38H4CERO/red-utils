package net.redct.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.redct.client.utils.entity.EntityManager;
import net.redct.client.utils.entity.EntitySettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class EntityGlowingMixin {
    private EntitySettings sett;
    private static EntityManager em = EntityManager.getInstance();

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void shouldEntityGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        sett = em.get(entity);
        if (sett != null && sett.style.glow) cir.setReturnValue(true);
        /*
        switch (entity.getType().toShortString()){

            case "armor_stand":
                break;


            case "player":
                if (entity.getTeam() != null && entity.getTeam().getNameTagVisibility().toString().equals("ALWAYS")){
                    //cir.setReturnValue(true);
                } else {
                    // Puede se goblins... etc y npc
                    if (GlowRegistry.shouldGlow(entity))
                        cir.setReturnValue(true);
                }
                break;
            default:
                if (GlowRegistry.shouldGlow(entity))
                    cir.setReturnValue(true);
        }
        */
    }

}
