package com.chrisgbradley.stratosworn.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class McpJson {
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
    private static final Gson COMPACT = new GsonBuilder().disableHtmlEscaping().create();

    private McpJson() {}

    public static String pretty(JsonElement el) { return PRETTY.toJson(el); }
    public static String compact(JsonElement el) { return COMPACT.toJson(el); }

    public static int intArg(JsonObject args, String key, int def) {
        return args.has(key) && !args.get(key).isJsonNull() ? args.get(key).getAsInt() : def;
    }

    public static double doubleArg(JsonObject args, String key, double def) {
        return args.has(key) && !args.get(key).isJsonNull() ? args.get(key).getAsDouble() : def;
    }

    public static String strArg(JsonObject args, String key, String def) {
        return args.has(key) && !args.get(key).isJsonNull() ? args.get(key).getAsString() : def;
    }

    public static boolean boolArg(JsonObject args, String key, boolean def) {
        return args.has(key) && !args.get(key).isJsonNull() ? args.get(key).getAsBoolean() : def;
    }
}
