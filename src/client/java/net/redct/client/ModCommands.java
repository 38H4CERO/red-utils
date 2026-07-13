package net.redct.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.world.phys.Vec3;
import net.redct.client.gui.config.ConfigScreen;
import net.redct.client.gui.hud.impl.HudEditorScreen;
import net.redct.client.utils.Utils;

public class ModCommands {

    public static void register() {


        // ru: Config open
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {dispatcher.register(
                    ClientCommands.literal("ru").executes(context -> {
                                var client = context.getSource().getClient();
                                client.execute(() -> {
                                    if (client.screen == null) {
                                        client.setScreen(new ConfigScreen());
                                    }
                                });
                                return 1;
                    }).then(ClientCommands.literal("hud").executes(context -> {
                        var client = context.getSource().getClient();
                        client.execute(() -> {
                            if (client.screen == null) {
                                client.setScreen(new HudEditorScreen());
                            }
                        });
                        return 1;
                    }))
            );
        });

        // ru: Config open
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {dispatcher.register(
                    ClientCommands.literal("test").executes(context -> {
                        //TracerRenderer.setTarget(new Vec3(8, -60, 8));
                        Utils.playLocalClientSound();
                                return 0;
                    }));
        });


    }
}