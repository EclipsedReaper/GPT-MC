package com.eclipse.gptmc;

import com.eclipse.gptmc.openai.OpenAIHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.eclipse.gptmc.openai.OpenAIHelper.evaluateResponse;
import static com.eclipse.gptmc.PlayerUtils.sendMessageToOperators;

public class ModEventListeners {
    public static final List<String> recentActions = new ArrayList<>();
    private static final Timer timer = new Timer(true);
    private static Timer summarizationTimer = new Timer(true);
    static int oldTime = ConfigHandler.getSummarizationInterval();

    public static void registerEventListeners() {
        startTimer();
        // Schedule a task to check for interval changes every second
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (oldTime != ConfigHandler.getSummarizationInterval()) {

                    // Cancel and replace the old timer
                    summarizationTimer.cancel();
                    summarizationTimer = new Timer(true); // Re-initialize the timer

                    startTimer();
                }
                oldTime = ConfigHandler.getSummarizationInterval();
            }
        }, 1000, 1000);

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            String action = player.getName().getString() + " broke a " + state.getBlock().getTranslationKey();
            recentActions.add(action);
        });

        UseBlockCallback.EVENT.register((player, world, hand, result) -> {
            String action = player.getName().getString() + " placed a " + world.getBlockState(result.getBlockPos()).getBlock().getTranslationKey();
            recentActions.add(action);
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
            String action;
            if (player.isSpectator()) {
                return ActionResult.FAIL;
            }
            if (entity instanceof MobEntity mobEntity) {
                if (mobEntity.isBaby()) {
                    action = player.getName().getString() + " attacked a baby " + entity.getType().getTranslationKey();
                } else {
                    action = player.getName().getString() + " attacked a " + entity.getType().getTranslationKey();
                }
            } else {
                action = player.getName().getString() + " attacked a " + entity.getType().getTranslationKey();
            }
            recentActions.add(action);
            return ActionResult.PASS;
        });

        ServerMessageEvents.CHAT_MESSAGE.register((signedMessage, player, parameters) -> {
            String action = player.getName().getString() + " sent a message saying " + signedMessage.getContent().getLiteralString();;
            recentActions.add(action);
        });

        ServerMessageEvents.GAME_MESSAGE.register((server, message, sender) -> {
            String action = message.getString();
            recentActions.add(action);
        });
    }

    public static List<String> getRecentActions() {
        return recentActions;
    }

    private static void startTimer() {

        summarizationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!recentActions.isEmpty() && recentActions.size() >= ConfigHandler.getMinEvents() && ConfigHandler.isGodMode()) {
                    OpenAIHelper.summarizePastEvents()
                            .thenAccept(summary -> {
                                ConfigHandler.setEventSummary(summary);
                                if (ConfigHandler.isReportSummary()) {
                                    sendMessageToOperators(GPTMC.server, ConfigHandler.getEventSummary());
                                }
                                OpenAIHelper.getResponseFromOpenAI(summary)
                                        .thenAccept(response -> {
                                            if (ConfigHandler.isReportResponse()) {
                                                sendMessageToOperators(GPTMC.server, response);
                                            }
                                            evaluateResponse(response);
                                        });
                            });
                }
                recentActions.clear();
            }
        }, ConfigHandler.getSummarizationInterval() * 1000L, ConfigHandler.getSummarizationInterval() * 1000L);
    }

}
