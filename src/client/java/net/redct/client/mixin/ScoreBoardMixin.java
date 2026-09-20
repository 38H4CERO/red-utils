package net.redct.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.redct.client.data.Location;
import net.redct.client.module.ModuleManager;
import net.redct.client.utils.PlayerInfo;
import net.redct.client.utils.ScoreboardUtils;
import net.redct.client.utils.Utils;
import net.redct.client.utils.dungeon.DungeonUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ScoreBoardMixin {
    @Inject(method = "handleSetScore", at = @At("TAIL"))
    private void onUpdate(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if(!Utils.inHypixel) return;

        /* TODO: Maybe only check when world is un/loaded
        *   https://wiki.fabricmc.net/tutorial:event_index
        * */
        ScoreboardUtils.getScoreboard();

        if(ModuleManager.isModuleEnabled("dungeonClearAlert") && PlayerInfo.INSTANCE.getCurrentLocation() == Location.THE_CATACOMBS){
            DungeonUtils.checkDungeon();
        }
    }
}
