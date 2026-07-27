package net.redct.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.redct.client.module.Module;
import net.redct.client.module.ModuleManager;
import net.redct.client.utils.entity.EntityManager;
import net.redct.client.utils.entity.GlowRegistry;
import net.redct.client.utils.render.Tracer;
import net.redct.client.utils.dungeon.DungeonSession;
import net.redct.client.utils.Utils;

public class EventSubscriber {

    public static void registerToEvents(){
        onServerConnectEVENT();
        onServerDisconnectEVENT();
        onTickEVENT();
        onLevelRenderEVENT();
        onEntityLoadEVENT();
        onEntityUnloadEVENT();
    }

    // TODO: This runs each time you change island
    private static void onServerConnectEVENT() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            Utils.isOnHypixel();
            Tracer.clearLines();
            GlowRegistry.clearGlowRegistry();
            EntityManager.clearProcessedMobs();

        });
    }

    private static void onServerDisconnectEVENT() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Utils.inHypixel = false;
            DungeonSession.end();
            Tracer.clearLines();
            GlowRegistry.clearGlowRegistry();
            EntityManager.clearProcessedMobs();
        });
    }

    private static void onTickEVENT(){
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (Module module : ModuleManager.getModules()) {
                if (module.isEnabled()) {
                    module.onTick();
                }
            }
        });
    }

    private static void onLevelRenderEVENT(){
        Tracer tracer = Tracer.getInstance();
        LevelRenderEvents.END_EXTRACTION.register(context -> {
            tracer.extractLine(context);
        });
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(context -> {
            tracer.renderAndDrawLines(context);
        });

    }


    private static void onEntityLoadEVENT(){

        ClientEntityEvents.ENTITY_LOAD.register( (entity, level) -> {
            //if (entity.getType().toShortString().contains("armor_stand")) return;
            // TODO: Some players are loaded before they get the tag
            // Some mobs are players
            switch (entity.getType().toShortString()){
                case "player":
                    if (entity.getTeam()!= null ? entity.getTeam().getNameTagVisibility().toString().equals("ALWAYS") : false){
                        // TODO: shows own player
                        //Tracer.setLine(entity.getStringUUID() ,Anchor.player(), Anchor.entity(entity), 3f, ARGB.white(255));
                    }
                    break;
                case "armor_stand":
                    if (entity.hasCustomName()){
                        // TODO: No estoy del todo seguro si esto es mejor o peor
                        EntityManager.onNameResolved(entity);
                    }
                    break;
            }

        });
    }

    private static void onEntityUnloadEVENT(){
        ClientEntityEvents.ENTITY_UNLOAD.register( (entity, level) -> {
            //if (entity.getType().toShortString().contains("armor_stand")) return;
            // TODO: Some players are loaded before they get the tag
            // Some mobs are players
            switch (entity.getType().toShortString()){
                case "player":
                    if (entity.getTeam()!= null ? entity.getTeam().getNameTagVisibility().toString().equals("ALWAYS") : false){
                        Tracer.removeLine(entity.getStringUUID());
                    }
                    break;
                case "armor_stand":
                    break;
                default:
                    break;

            }

            GlowRegistry.setGlowing(entity, false);
            EntityManager.removeProcessedMob(entity.getUUID()); // Aqui solo llegan armor stands creo (a la lista)
        });
    }

}
