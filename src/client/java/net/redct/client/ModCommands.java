package net.redct.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import net.redct.client.gui.config.ConfigScreen;
import net.redct.client.gui.hud.impl.HudEditorScreen;
import net.redct.client.utils.entity.EntityManager;
import net.redct.client.utils.render.Tracer;
import net.redct.client.utils.render.Tracer.Anchor;

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
                        EntityManager.addMobTypeGlowing("Graveyard Zombie");
                        return 1;
                    }));
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("tracer")
                    .executes(context -> {
                        Tracer.removeLine("command");
                        return 1;
                    })
                    .then(ClientCommands.argument("x", FloatArgumentType.floatArg())
                            .then(ClientCommands.argument("y", FloatArgumentType.floatArg())
                                    .then(ClientCommands.argument("z", FloatArgumentType.floatArg())
                                            .executes(context -> {
                                                float x = FloatArgumentType.getFloat(context, "x");
                                                float y = FloatArgumentType.getFloat(context, "y");
                                                float z = FloatArgumentType.getFloat(context, "z");
                                                Tracer.setLine("command", Anchor.player(), Anchor.fixed(new Vec3(x,y,z)), 3f, ARGB.white(255));
                                                return 1;
                                            }))))
                    .then(ClientCommands.literal("clear")
                            .executes(context -> {
                                Tracer.clearLines();
                                return 1;
                            }))
            );
        });


    }
}