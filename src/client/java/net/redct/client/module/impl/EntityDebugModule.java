package net.redct.client.module.impl;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.AABB;
import net.redct.client.module.Category;
import net.redct.client.module.Module;
import net.redct.client.utils.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.redct.client.utils.entity.EntityUtils.getArmorStandPlayerHeadSkin;

public class EntityDebugModule extends Module {
    private final Set<UUID> logged = new HashSet<>();

    public EntityDebugModule() {
        super("entityDebug", "Entity Debug", Category.DEBUG);
    }

    @Override
    public void onDisable() {
        logged.clear();
    }

    // TODO: ClientEntityEvents.ENTITY_LOAD
    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        AABB box = mc.player.getBoundingBox().inflate(3.0);

        for (Entity e : mc.level.getEntities(mc.player, box)) {
            if (logged.add(e.getUUID())) {
                /*
                e.getId()
                Armor stand tiene +1 de su mob correspondiente, creo
                 */
                float health = -1;
                float maxHealth = -1;
                if (e instanceof LivingEntity living) {
                    health = living.getHealth();
                    maxHealth = living.getMaxHealth();

                }
                //if (!EntityUtils.isMob(e.getName().getString())) return;

                //GlowRegistry.setGlowing(e, true);
                if (e instanceof ArmorStand armorStand){
                    ItemStack head = armorStand.getItemBySlot(EquipmentSlot.HEAD);
                    ResolvableProfile profile = head.get(DataComponents.PROFILE);
                    String temp = "null";
                    if (profile != null) {
                        String textureBase64 = profile.partialProfile().properties().get("textures").iterator().next().value();
                        System.out.println("## "+ textureBase64);

                        /*
                        Property textureProperty = profile.partialProfile().properties().get("textures").stream().findFirst().orElse(null);
                        if (textureProperty != null) {
                            String base64Texture = textureProperty.value(); // base64-encoded JSON containing the skin URL
                            temp = base64Texture;
                        }

                         */
                    }
                    Logger.log("ARMOR_STAND", "name=%s | pos=[%.1f,%.1f,%.1f] | Head=%s | comp=%s",
                            armorStand.getName().getString(),
                            armorStand.getX(), armorStand.getY(), armorStand.getZ(),
                            armorStand.getItemBySlot(EquipmentSlot.HEAD).toString(),
                            getArmorStandPlayerHeadSkin(e)


                    );
                    return;
                }
                Logger.log("ENTITY", "type=%s | name=%s | customName=%s | team=%s | id=%s | pos=[%.1f,%.1f,%.1f]",
                        e.getType().toShortString(),
                        e.getName().getString(),
                        e.getCustomName() != null ? e.getCustomName().getString() : "null",
                        e.getTeam()!= null ? e.getTeam().getNameTagVisibility() : "null",
                        e.getId(),
                        e.getX(), e.getY(), e.getZ()
                );


            }
        }
    }
}