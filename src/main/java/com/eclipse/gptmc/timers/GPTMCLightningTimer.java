package com.eclipse.gptmc.timers;

import com.eclipse.gptmc.PlayerUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GPTMCLightningTimer {
    private static final List<ServerPlayerEntity> players = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!players.isEmpty()) {
                Iterator<ServerPlayerEntity> iterator = players.iterator();
                while (iterator.hasNext()) {
                    ServerPlayerEntity player = iterator.next();
                    PlayerUtils.smitePlayer(player);
                    if (player.isDead()) {
                        iterator.remove();
                    }
                }
            }
        });
    }

    public static void setPlayerTimer(ServerPlayerEntity player) {
        players.add(player);
    }
}