package com.bg03.gptmc;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.bg03.gptmc.GPTMC.server;
import static com.bg03.gptmc.ModEventListeners.recentActions;

public class PlayerUtils {

    public static ServerPlayerEntity getPlayerByName(String playerName) {
        if (server != null) {
            return server.getPlayerManager().getPlayer(playerName);
        }
        return null;
    }

    public static RegistryEntry<StatusEffect> getEffectByName(String effectName) {
        switch (effectName) {
            case "speed":
                return StatusEffects.SPEED;
            case "slowness":
                return StatusEffects.SLOWNESS;
            case "haste":
                return StatusEffects.HASTE;
            case "mining_fatigue":
                return StatusEffects.MINING_FATIGUE;
            case "strength":
                return StatusEffects.STRENGTH;
            case "weakness":
                return StatusEffects.WEAKNESS;
            case "instant_health":
                return StatusEffects.INSTANT_HEALTH;
            case "instant_damage":
                return StatusEffects.INSTANT_DAMAGE;
            case "jump_boost":
                return StatusEffects.JUMP_BOOST;
            case "nausea":
                return StatusEffects.NAUSEA;
            case "regeneration":
                return StatusEffects.REGENERATION;
            case "resistance":
                return StatusEffects.RESISTANCE;
            case "fire_resistance":
                return StatusEffects.FIRE_RESISTANCE;
            case "water_breathing":
                return StatusEffects.WATER_BREATHING;
            case "invisibility":
                return StatusEffects.INVISIBILITY;
            case "blindness":
                return StatusEffects.BLINDNESS;
            case "night_vision":
                return StatusEffects.NIGHT_VISION;
            case "hunger":
                return StatusEffects.HUNGER;
            case "saturation":
                return StatusEffects.SATURATION;
            case "glowing":
                return StatusEffects.GLOWING;
            case "levitation":
                return StatusEffects.LEVITATION;
            case "luck":
                return StatusEffects.LUCK;
            case "unluck":
                return StatusEffects.UNLUCK;
            case "slow_falling":
                return StatusEffects.SLOW_FALLING;
            case "conduit_power":
                return StatusEffects.CONDUIT_POWER;
            case "dolphins_grace":
                return StatusEffects.DOLPHINS_GRACE;
            case "bad_omen":
                return StatusEffects.BAD_OMEN;
            case "hero_of_the_village":
                return StatusEffects.HERO_OF_THE_VILLAGE;
            case "darkness":
                return StatusEffects.DARKNESS;
            default:
                return StatusEffects.GLOWING;
        }
    }

//    public static RegistryEntry<Enchantment> getEnchantmentByName(String name) {
//        switch (name) {
//            case "protection":
//                return Enchantments.PROTECTION;
//            case "fire_protection":
//                return Enchantments.FIRE_PROTECTION;
//            case "feather_falling":
//                return Enchantments.FEATHER_FALLING;
//            case "blast_protection":
//                return Enchantments.BLAST_PROTECTION;
//            case "projectile_protection":
//                return Enchantments.PROJECTILE_PROTECTION;
//            case "respiration":
//                return Enchantments.RESPIRATION;
//            case "aqua_affinity":
//                return Enchantments.AQUA_AFFINITY;
//            case "thorns":
//                return Enchantments.THORNS;
//            case "depth_strider":
//                return Enchantments.DEPTH_STRIDER;
//            case "frost_walker":
//                return Enchantments.FROST_WALKER;
//            case "binding_curse":
//                return Enchantments.BINDING_CURSE;
//            case "sharpness":
//                return Enchantments.SHARPNESS;
//            case "smite":
//                return Enchantments.SMITE;
//            case "bane_of_arthropods":
//                return Enchantments.BANE_OF_ARTHROPODS;
//            case "knockback":
//                return Enchantments.KNOCKBACK;
//            case "fire_aspect":
//                return Enchantments.FIRE_ASPECT;
//            case "looting":
//                return Enchantments.LOOTING;
//            case "sweeping_edge":
//                return Enchantments.SWEEPING_EDGE;
//            case "efficiency":
//                return Enchantments.EFFICIENCY;
//            case "silk_touch":
//                return Enchantments.SILK_TOUCH;
//            case "unbreaking":
//                return Enchantments.UNBREAKING;
//            case "fortune":
//                return Enchantments.FORTUNE;
//            case "power":
//                return Enchantments.POWER;
//            case "punch":
//                return Enchantments.PUNCH;
//            case "flame":
//                return Enchantments.FLAME;
//            case "infinity":
//                return Enchantments.INFINITY;
//            case "luck_of_the_sea":
//                return Enchantments.LUCK_OF_THE_SEA;
//            case "lure":
//                return Enchantments.LURE;
//            case "loyalty":
//                return Enchantments.LOYALTY;
//            case "impaling":
//                return Enchantments.IMPALING;
//            case "riptide":
//                return Enchantments.RIPTIDE;
//            case "channeling":
//                return Enchantments.CHANNELING;
//            case "multishot":
//                return Enchantments.MULTISHOT;
//            case "quick_charge":
//                return Enchantments.QUICK_CHARGE;
//            case "piercing":
//                return Enchantments.PIERCING;
//            case "mending":
//                return Enchantments.MENDING;
//            case "vanishing_curse":
//                return Enchantments.VANISHING_CURSE;
//            case "soul_speed":
//                return Enchantments.SOUL_SPEED;
//            case "swift_sneak":
//                return Enchantments.SWIFT_SNEAK;
//            default:
//                return Enchantments.UNBREAKING;
//        }
//    }

    // Method to send a message to all operators
    public static void sendMessageToOperators(MinecraftServer server, String message) {
        // Create a Text object for the message, which allows for color formatting if desired
        Text formattedMessage = Text.literal(message);

        // Loop through all online players
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Check if the player is an operator
            if (server.getPlayerManager().isOperator(player.getGameProfile())) {
                // Send the message to the operator
                player.sendMessage(formattedMessage, false);
            }
        }
    }

    public static void smitePlayer(PlayerEntity player) {
        if (player != null) {
            LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, player.getWorld());
            lightning.updatePosition(player.getX(), player.getY(), player.getZ());
            player.getWorld().spawnEntity(lightning);
        } else {
            GPTMC.LOGGER.info("Player not found");
        }
    }
}