package com.bg03.gptmc.timers;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;

public class GPTMCTimer {
    private static final Map<ServerPlayerEntity, Integer> playerTimers = new HashMap<>();
    private static final Map<ServerPlayerEntity, Runnable> playerActions = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (Map.Entry<ServerPlayerEntity, Integer> entry : playerTimers.entrySet()) {
                ServerPlayerEntity player = entry.getKey();
                int ticks = entry.getValue() - 1;
                if (ticks <= 0) {
                    Runnable action = playerActions.get(player);
                    if (action != null) {
                        action.run();
                    }
                    playerTimers.remove(player);
                    playerActions.remove(player);
                } else {
                    playerTimers.put(player, ticks);
                }
            }
        });
    }

    public static void setPlayerTimer(ServerPlayerEntity player, int ticks) {
        playerTimers.put(player, ticks);
    }

    public static void setPlayerTimerWithAction(ServerPlayerEntity player, int ticks, Runnable action) {
        playerTimers.put(player, ticks);
        playerActions.put(player, action);
    }
}