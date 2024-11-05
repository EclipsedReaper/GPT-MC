package com.bg03.gptmc;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ModCommands {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("mcgpt")
                    .then(CommandManager.literal("enable")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ConfigHandler.setGodMode(!ConfigHandler.isGodMode());
                                source.sendFeedback(() -> Text.of("God mode has been enabled"), true);
                                return 1;
                            })
                    ).then(CommandManager.literal("disable")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ConfigHandler.setGodMode(!ConfigHandler.isGodMode());
                                source.sendFeedback(() -> Text.of("God mode has been disabled"), true);
                                return 1;
                            })
                    ).then(CommandManager.literal("status")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                source.sendFeedback(() -> Text.of("God mode is " + (ConfigHandler.isGodMode() ? "enabled" : "disabled")), true);
                                return 1;
                            })
                    ).then(CommandManager.literal("morals")
                            .then(CommandManager.literal("set") // Set god mode morals to the argument
                                    .then(CommandManager.argument("morals", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String morals = StringArgumentType.getString(context, "morals");
                                                ServerCommandSource source = context.getSource();
                                                ConfigHandler.setGodModeMorals(morals);
                                                source.sendFeedback(() -> Text.of("God mode morals set to: " + morals), true);
                                                return 1;
                                            })
                                    )
                            ).then(CommandManager.literal("reset") // Clear god mode morals
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        ConfigHandler.setGodModeMorals("");
                                        source.sendFeedback(() -> Text.of("God mode morals cleared"), true);
                                        return 1;
                                    })
                            ).then(CommandManager.literal("get") // Get god mode morals
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        source.sendFeedback(() -> Text.of("God mode morals: " + ConfigHandler.getGodModeMorals()), true);
                                        return 1;
                                    })
                            )
                    ).then(CommandManager.literal("minimumevents")
                            .then(CommandManager.literal("set") // Set minimum events to the argument
                                    .then(CommandManager.argument("minevents", StringArgumentType.word())
                                            .executes(context -> {
                                                int events = Integer.parseInt(StringArgumentType.getString(context, "minevents"));
                                                ServerCommandSource source = context.getSource();
                                                ConfigHandler.setMinEvents(events);
                                                source.sendFeedback(() -> Text.of("Minimum events set to: " + events), true);
                                                return 1;
                                            })
                                    )
                            ).then(CommandManager.literal("get") // Get minimum events
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        source.sendFeedback(() -> Text.of("Minimum events: " + ConfigHandler.getMinEvents()), true);
                                        return 1;
                                    })
                            ).then(CommandManager.literal("reset") // Reset minimum events
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        ConfigHandler.setMinEvents(30);
                                        source.sendFeedback(() -> Text.of("Minimum events reset"), true);
                                        return 1;
                                    })
                            )

                    ).then(CommandManager.literal("summary")
                            .then(CommandManager.literal("get") // Get event summary
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        source.sendFeedback(() -> Text.of("Event summary: " + ConfigHandler.getEventSummary()), true);
                                        return 1;
                                    })
                            ).then(CommandManager.literal("clear") // Clear event summary
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        ConfigHandler.setEventSummary("");
                                        source.sendFeedback(() -> Text.of("Event summary cleared"), true);
                                        return 1;
                                    })
                            ).then(CommandManager.literal("report") // Report event summary
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        ConfigHandler.setReportSummary(!ConfigHandler.isReportSummary());
                                        source.sendFeedback(() -> Text.of("Report summary is now " + ConfigHandler.isReportSummary()), true);
                                        return 1;
                                    })
                            )
                    ).then(CommandManager.literal("interval")
                            .then(CommandManager.literal("get") // Get summarization interval
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        source.sendFeedback(() -> Text.of("Summarization interval: " + ConfigHandler.getSummarizationInterval()), true);
                                        return 1;
                                    })
                            ).then(CommandManager.literal("set") // Set summarization interval to the argument
                                    .then(CommandManager.argument("interval", StringArgumentType.word())
                                            .executes(context -> {
                                                int interval = Integer.parseInt(StringArgumentType.getString(context, "interval"));
                                                ServerCommandSource source = context.getSource();
                                                ConfigHandler.setSummarizationInterval(interval);
                                                source.sendFeedback(() -> Text.of("Summarization interval set to " + interval + " seconds"), true);
                                                return 1;
                                            })
                                    )
                            )
                    ).then(CommandManager.argument("prompt", StringArgumentType.greedyString())
                            .executes(context -> {
                                String prompt = StringArgumentType.getString(context, "prompt");
                                ServerCommandSource source = context.getSource();

                                if (ConfigHandler.isGodMode()) {
                                    source.sendFeedback(() -> Text.of("This command is currently disabled, as god mode is activated."), false);
                                    return 1;
                                }
                                // Call OpenAI API asynchronously
                                OpenAIHelper.getResponseFromOpenAI(prompt)
                                        .thenAccept(response -> {
                                            // Send response back to player
                                            source.sendFeedback(() -> Text.literal(response), true);
                                        });

                                return 1;
                            })
                    )
            );
        });
    }
}
