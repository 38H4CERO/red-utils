package net.redct.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.redct.client.module.ModuleManager.isModuleEnabled;

@Mixin(AbstractRecipeBookScreen.class)
public class HideRecipeBookMixin {

    @Inject(method = "initButton", at = @At("HEAD"), cancellable = true)
    private void initRecipeBook(CallbackInfo ci) {
        if(isModuleEnabled("clean_inventory")) {
            ci.cancel();
        };
    }
}

