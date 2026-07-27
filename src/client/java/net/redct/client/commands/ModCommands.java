package net.redct.client.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import net.redct.client.gui.config.ConfigScreen;
import net.redct.client.gui.hud.impl.HudEditorScreen;
import net.redct.client.utils.entity.EntityManager;
import net.redct.client.utils.entity.EntityUtils;
import net.redct.client.utils.entity.GlowRegistry;
import net.redct.client.utils.render.Tracer;
import net.redct.client.utils.render.Tracer.Anchor;

import java.util.List;

public class ModCommands {

    public static void register() {

        // ru: Config open
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("ru").executes(context -> {
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
            })));
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("track").then(ClientCommands.literal("add").then(ClientCommands.argument("name", StringArgumentType.string()).executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                EntityManager.addMobTypeGlowing(name);
                                EntityManager.clearProcessedMobs();
                                EntityUtils.rescanLoadedArmorStands();
                                context.getSource().sendFeedback(Component.literal("Added " + name));
                                return 1;
                            })))

                            .then(ClientCommands.literal("remove").then(ClientCommands.argument("name", StringArgumentType.string()).suggests(new EntityTrackRemoveSuggestionProvider()).executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                EntityManager.removeMobTypeGlowing(name);
                                GlowRegistry.clearGlowRegistry();
                                context.getSource().sendFeedback(Component.literal("Removed " + name));
                                return 1;
                            })))

                            .then(ClientCommands.literal("list").executes(context -> {
                                List<String> names = EntityManager.getTrackedNames();
                                if (names.isEmpty()) {
                                    context.getSource().sendFeedback(Component.literal("No mobs are currently tracked."));
                                } else {
                                    context.getSource().sendFeedback(Component.literal("Tracked mobs: " + String.join(", ", names)));
                                }
                                return 1;
                            }))

            );
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("tracer").executes(context -> {
                Tracer.removeLine("command");
                return 1;
            }).then(ClientCommands.argument("x", FloatArgumentType.floatArg()).then(ClientCommands.argument("y", FloatArgumentType.floatArg()).then(ClientCommands.argument("z", FloatArgumentType.floatArg()).executes(context -> {
                float x = FloatArgumentType.getFloat(context, "x");
                float y = FloatArgumentType.getFloat(context, "y");
                float z = FloatArgumentType.getFloat(context, "z");
                Tracer.setLine("command", Anchor.player(), Anchor.fixed(new Vec3(x, y, z)), 3f, ARGB.white(255));
                return 1;
            })))).then(ClientCommands.literal("clear").executes(context -> {
                Tracer.clearLines();
                return 1;
            })));
        });
    }
}