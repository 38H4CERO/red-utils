package net.redct.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.redct.client.utils.accessor.HiddenStateAccessor;
import net.redct.client.utils.entity.EntityManager;
import net.redct.client.utils.entity.EntitySettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandRenderer.class)
public abstract class ArmorStandRendererMixin {
    private final static EntityManager EM = EntityManager.getInstance();
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void redutils$flagHidden(ArmorStand entity, ArmorStandRenderState state, float partialTicks, CallbackInfo ci) {
        EntitySettings sett = EM.get(entity);
        ((HiddenStateAccessor) state).redutils$setHidden(sett != null && sett.style.hide);
    }

    // TODO: This can be optimized more
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void redutils$cancelHiddenSubmit(ArmorStandRenderState state, PoseStack poseStack,
                                             SubmitNodeCollector submitNodeCollector,
                                             CameraRenderState camera, CallbackInfo ci) {
        //ci.cancel();
        if (((HiddenStateAccessor) state).redutils$isHidden()) {
            ci.cancel();
        }
    }
}
