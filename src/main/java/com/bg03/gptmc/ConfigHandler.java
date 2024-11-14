package com.bg03.gptmc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigHandler {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("gptmc_config.json");
    private static final Gson GSON = new Gson();
    private static int summarizationInterval = 30; // Default interval in seconds
    private static boolean godMode = true;
    private static String eventSummary = ""; // Stores a summary of recent events
    private static String godModeMorals = "";
    private static int minEvents = 3; // Minimum number of events to summarize
    private static boolean reportSummary = false;
    private static boolean reportResponse = false;

    // Load the configuration
    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
                JsonObject config = GSON.fromJson(reader, JsonObject.class);
                summarizationInterval = config.has("summarizationInterval") ? config.get("summarizationInterval").getAsInt() : summarizationInterval;
                godMode = config.has("godMode") ? config.get("godMode").getAsBoolean() : godMode;
                eventSummary = config.has("eventSummary") ? config.get("eventSummary").getAsString() : eventSummary;
                godModeMorals = config.has("godModeMorals") ? config.get("godModeMorals").getAsString() : godModeMorals;
                minEvents = config.has("minEvents") ? config.get("minEvents").getAsInt() : minEvents;
                reportSummary = config.has("reportSummary") ? config.get("reportSummary").getAsBoolean() : reportSummary;
                reportResponse = config.has("reportResponse") ? config.get("reportResponse").getAsBoolean() : reportResponse;
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        } else {
            saveConfig(); // Create default config file if it doesn't exist
        }
    }

    // Save the configuration
    public static void saveConfig() {
        JsonObject config = new JsonObject();
        config.addProperty("summarizationInterval", summarizationInterval);
        config.addProperty("godMode", godMode);
        config.addProperty("eventSummary", eventSummary);
        config.addProperty("godModeMorals", godModeMorals);
        config.addProperty("minEvents", minEvents);
        config.addProperty("reportSummary", reportSummary);
        config.addProperty("reportResponse", reportResponse);

        try (var writer = Files.newBufferedWriter(CONFIG_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Accessor and Mutator methods for config values
    public static int getSummarizationInterval() {
        return summarizationInterval;
    }

    public static void setSummarizationInterval(int interval) {
        summarizationInterval = interval;
        saveConfig();
    }

    public static boolean isGodMode() {
        return godMode;
    }

    public static void setGodMode(boolean mode) {
        godMode = mode;
        saveConfig();
    }

    public static String getEventSummary() {
        return eventSummary;
    }

    public static void setEventSummary(String summary) {
        eventSummary = summary;
        saveConfig();
    }

    // Append to the event summary
    public static void addEventToSummary(String event) {
        eventSummary += event + "\n";
        saveConfig();
    }

    public static String getGodModeMorals() {
        return godModeMorals;
    }

    public static void setGodModeMorals(String morals) {
        godModeMorals = morals;
        saveConfig();
    }

    public static int getMinEvents() {
        return minEvents;
    }

    public static void setMinEvents(int minimumEvents) {
        minEvents = minimumEvents;
        saveConfig();
    }

    public static boolean isReportSummary() {
        return reportSummary;
    }

    public static void setReportSummary(boolean repSummary) {
        reportSummary = repSummary;
        saveConfig();
    }

    public static void setReportResponse(boolean repResponse) {
        reportResponse = repResponse;
        saveConfig();
    }

    public static boolean isReportResponse() {
        return reportResponse;
    }
}