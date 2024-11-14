package com.bg03.gptmc.openai;

import com.bg03.gptmc.ConfigHandler;
import com.bg03.gptmc.GPTMC;
import com.bg03.gptmc.ModEventListeners;
import com.bg03.gptmc.PlayerUtils;
import com.bg03.gptmc.timers.GPTMCLightningTimer;
import com.google.gson.JsonParser;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import okhttp3.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.bg03.gptmc.GPTMC.server;
import static com.bg03.gptmc.ModEventListeners.recentActions;
import static com.bg03.gptmc.PlayerUtils.smitePlayer;

public class OpenAIHelper {
    public static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static CompletableFuture<String> getResponseFromOpenAI(String prompt, String model, String systemInstructions) {
        return CompletableFuture.supplyAsync(() -> {

            if (API_KEY == null || API_KEY.isEmpty()) {
                GPTMC.LOGGER.info("API Key is missing.");
                throw new IllegalStateException("API Key is missing. Ensure OPENAI_API_KEY is set.");
            }

            // Create JSON payload using gson
            JsonObject json = new JsonObject();
            json.addProperty("model", model);

            // Create the messages array
            JsonArray messages = new JsonArray();

            // Add the system message for instructions
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", systemInstructions);
            messages.add(systemMessage);

            // Add the user message
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);

            json.add("messages", messages);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                    JsonArray choices = responseJson.getAsJsonArray("choices");

                    String content = choices.get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
                    return content;
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        });
    }

    public static CompletableFuture<String> getResponseFromOpenAI(String prompt, String model) {
        return ConfigHandler.isGodMode()
                ? getResponseFromOpenAI(prompt, model, "Your job is to be a god of a Minecraft world. You must roleplay as a god, and give your subjects commands and punish them if they do not comply. You will be told about events that have happened in the past 30 seconds, and you must decide what to do to the players and/or world based on what has happened. Do not reward players for doing useless things like mining leaves or breaking dirt. Only reward players when they do something particularly of note, like mining diamonds or placing a beacon. Also, make sure to give players rewards that make sense; like if a player mines diamonds, maybe give them a golden apple and a pickaxe, or if they kill an evoker, give them a powerful sword! You shouldn't just give a player an item because they asked for it, they have to earn it. You have a list of commands to choose from; the usage, syntax, and effect will be described as well: " +
                "`give <player> <item> <count>` will give a player an item with a count, make sure to use real minecraft items (for example, `minecraft:stone`), " +
                "`smite <player>` will strike a player with lightning, " +
                "`clear <player>` will clear a player's inventory, " +
                "`lightning <player>` will repeatedly strike a player with lightning until they die, " +
                "`whisper <player> <message>` will send a message to only one player, " +
                "`effect <player> <effect> <duration> <amplifier>` will effect a player with a potion effect. Make sure to use real minecraft effects (for example, `minecraft:strength`). Duration is in seconds, and an amplifier of 0 is a level 1 effect, an amplifier of 2 is a level 3 effect, etc. " +
                "`time <day / night>` will change the time of day, " +
                "`heal <player>` will heal a player to max health, " +
                "`explode <player>` will explode a player. " +
                //"`enchant <player> <enchantment> <level> <head/chest/legs/feet/hand>` will enchant the item with an enchantment at the level specified, in the slot specified. " +
                "You can also use `say <message>` to send a message to all players. You can send multiple commands by separating them with a '|', like this: `give player minecraft:diamond 1 | give player minecraft:netherite_ingot 1`. You must only send the command in the exact format specified, otherwise the commands will not work. Don't use any symbols in your commands, only the commands themselves and the text meant to be contained within. However, you can use commas, periods, colons, semicolons, exclamation points and question marks in your `say` messages. Also, don't use any quotation marks or tildes, as those will mess with the formatting. Your morals are: " + ConfigHandler.getGodModeMorals())
                : getResponseFromOpenAI(prompt, model, "You are a Minecraft helper. Your job is to assist players in a Minecraft world. You will be given a prompt describing a situation, and you must respond with a helpful message to guide the player. You can provide information, suggestions, or instructions to help the player progress in the game.");
    }

    public static CompletableFuture<String> getResponseFromOpenAI(String prompt) {
        return getResponseFromOpenAI(prompt, "gpt-4-turbo");
    }

    public static CompletableFuture<String> summarizePastEvents() {

        StringBuilder summary = new StringBuilder();
        for (String action : ModEventListeners.getRecentActions()) {
            summary.append(action).append("\n");
        }

        return getResponseFromOpenAI("Previous summary: '" + ConfigHandler.getEventSummary() + "', events within the past " + ConfigHandler.getSummarizationInterval() + "seconds: " + summary, "gpt-4-turbo",
                "Your job is to summarize recent events in Minecraft, to act as a memory for events that have happened for another AI. You will be fed a list of events that have happened in the past " + ConfigHandler.getSummarizationInterval() + " seconds, " +
                        "and your job is to summarize them in a concise manner, to be used in a prompt for another AI. You may also be given a previous summary, in which case you must incorporate that into your response. " +
                        "Make sure to provide a clear and concise summary of the events, and ensure that the summary is accurate and relevant to the events that have occurred, both in the past and the present. " +
                        "Make sure to use the previous summary as a reference, and build upon it to create a new summary that is informative and useful to the AI. " +
                        "Do not try to explain the events that have happened, only summarize them as they happened." +
                        "For example, if a player breaks 50 deepslate in a 30 second period, and then breaks 15 deepslate and 3 diamond ore in the next, you might say, 'Player broke 65 deepslate and then found diamonds.' But if something happened in the past, like a few minutes ago, don't bother including it. " +
                        "If you are given an action made by 'GPT' you should address it in the second person, as if you are talking to another AI. For example, 'GPT set the time to day' should be summarized as 'You set the time to day.'");
    }

    public static void evaluateResponse(String response) {
        if (response.contains("|")) {
            String[] commands = response.split("\\|");
            for (String command : commands) {
                evaluateCommand(command.trim());
            }
        } else {
            evaluateCommand(response);
        }
    }

    private static void evaluateCommand(String response) {
        if (response.contains("say")) {
            String message = response.substring(4).trim();
            for (String playerName : server.getPlayerNames()) {
                PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
                if (player != null) {
                    player.sendMessage(Text.of(message), false);
                    recentActions.add("GPT said: " + message);
                }
            }
        } else if (response.contains("give")) {
            String[] parts = response.substring(5).trim().split(" ");
            if (parts.length == 3) {
                String playerName = parts[0];
                String itemName = parts[1];
                int count = Integer.parseInt(parts[2]);
                PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
                if (player != null) {
                    Item item = Registries.ITEM.get(Identifier.of(itemName));
                    ItemStack stack = new ItemStack(item, count);
                    player.giveItemStack(stack);
                    recentActions.add("GPT gave " + count + " of " + itemName + " to " + playerName);
                    GPTMC.LOGGER.info("GPT gave " + count + " of " + itemName + " to " + playerName);
                }
            }
        } else if (response.contains("smite")) {
            String playerName = response.substring(6).trim();
            PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
            smitePlayer(player);
            recentActions.add("GPT struck " + playerName + " with lightning");
            GPTMC.LOGGER.info("GPT struck " + playerName + " with lightning");
        } else if (response.contains("clear")) {
            String playerName = response.substring(6).trim();
            PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
            if (player != null) {
                player.getInventory().clear();
                recentActions.add("GPT cleared inventory of " + playerName);
                GPTMC.LOGGER.info("GPT cleared inventory of " + playerName);
            }
        } else if (response.contains("lightning")) {
            String playerName = response.substring(10).trim();
            ServerPlayerEntity player = PlayerUtils.getPlayerByName(playerName);
            if (player != null) {
                GPTMCLightningTimer.setPlayerTimer(player);
                recentActions.add("GPT struck " + playerName + " with lightning repeatedly");
                GPTMC.LOGGER.info("GPT struck " + playerName + " with lightning repeatedly");
            } else {
                GPTMC.LOGGER.info("Player not found: " + playerName);
            }

        } else if (response.contains("whisper")) {
            String[] parts = response.substring(8).trim().split(" ");
            if (parts.length >= 2) {
                String playerName = parts[0];
                String message = response.substring(8 + playerName.length()).trim();
                PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
                if (player != null) {
                    player.sendMessage(Text.of(message), false);
                    recentActions.add("GPT whispered message to " + playerName + " - " + message);
                    GPTMC.LOGGER.info("GPT whispered message to " + playerName + " - " + message);
                }
            }

        } else if (response.contains("effect")) {
            String[] parts = response.substring(7).trim().split(" ");
            if (parts.length == 4) {
                String playerName = parts[0];
                String effectName = parts[1];
                int duration = Integer.parseInt(parts[2]);
                int amplifier = Integer.parseInt(parts[3]);
                PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
                if (player != null) {
                    StatusEffectInstance effect = new StatusEffectInstance(PlayerUtils.getEffectByName(effectName), duration, amplifier, false, true);
                    player.addStatusEffect(effect);
                    recentActions.add("GPT applied " + effectName + " to " + playerName + " for " + duration + " seconds");
                    GPTMC.LOGGER.info("GPT applied " + effectName + " to " + playerName + " for " + duration + " seconds");
                }
            }
        } else if (response.contains("time")) {
            String time = response.substring(5).trim();
            ServerWorld world = server.getWorld(server.getOverworld().getRegistryKey());
            if (time.equals("day")) {
                world.setTimeOfDay(1000);
                recentActions.add("GPT set time to day");
                GPTMC.LOGGER.info("GPT set time to day");
            } else if (time.equals("night")) {
                world.setTimeOfDay(13000);
                recentActions.add("GPT set time to night");
                GPTMC.LOGGER.info("GPT set time to night");
            }

        } else if (response.contains("heal")) {
            String playerName = response.substring(5).trim();
            PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
            if (player != null) {
                player.setHealth(player.getMaxHealth());
                player.getHungerManager().setFoodLevel(20);
                player.getHungerManager().setSaturationLevel(5.0F);
                recentActions.add("GPT healed " + playerName + " to max health");
                GPTMC.LOGGER.info("GPT healed " + playerName + " to max health");
            }
        } else if (response.contains("explode")) {
            String playerName = response.substring(8).trim();
            PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
            if (player != null) {
                TntEntity tnt = new TntEntity(EntityType.TNT, player.getWorld());
                tnt.updatePosition(player.getX(), player.getY(), player.getZ());
                tnt.setFuse(0);
                player.getWorld().spawnEntity(tnt);
                recentActions.add("GPT exploded " + playerName);
                GPTMC.LOGGER.info("GPT exploded " + playerName);
            }
        } //else if (response.contains("enchant")) {
//            String[] parts = response.substring(8).trim().split(" ");
//            if (parts.length == 4) {
//                String playerName = parts[0];
//                String enchantmentName = parts[1];
//                int level = Integer.parseInt(parts[2]);
//                String slot = parts[3];
//                PlayerEntity player = PlayerUtils.getPlayerByName(playerName);
//                if (player != null) {
//                    Enchantment enchantment = getEnchantmentByName(enchantmentName);
//                    ItemStack stack = player.getEquippedStack();
//                    if (enchantment != null && stack != null) {
//                        stack.addEnchantment(enchantment, level);
//                        GPTMC.LOGGER.info("GPT enchanted item with " + enchantmentName + " at level " + level + " in slot " + slot + " for player: " + playerName);
//                    }
//                }
//            }
//        }
    }

}
