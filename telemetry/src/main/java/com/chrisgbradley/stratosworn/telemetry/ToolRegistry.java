package com.chrisgbradley.stratosworn.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

/** Holds MCP tool definitions and dispatches tools/call. */
public final class ToolRegistry {
    public static final class ToolException extends RuntimeException {
        public ToolException(String msg) { super(msg); }
    }

    @FunctionalInterface
    public interface Handler {
        /** Returns the MCP "content" array for the result. May throw to signal isError. */
        JsonArray run(JsonObject args) throws Exception;
    }

    private record Tool(String name, String description, JsonObject inputSchema, Handler handler) {}

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public ToolRegistry add(String name, String description, JsonObject schema, Handler handler) {
        tools.put(name, new Tool(name, description, schema, handler));
        return this;
    }

    public JsonObject list() {
        JsonArray arr = new JsonArray();
        for (Tool t : tools.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("name", t.name);
            o.addProperty("description", t.description);
            o.add("inputSchema", t.inputSchema);
            arr.add(o);
        }
        JsonObject res = new JsonObject();
        res.add("tools", arr);
        return res;
    }

    public JsonObject call(JsonObject params) {
        String name = params.has("name") ? params.get("name").getAsString() : null;
        Tool t = name == null ? null : tools.get(name);
        if (t == null) throw new ToolException("unknown tool: " + name);
        JsonObject args = params.has("arguments") && params.get("arguments").isJsonObject()
                ? params.getAsJsonObject("arguments") : new JsonObject();
        JsonObject res = new JsonObject();
        try {
            res.add("content", t.handler.run(args));
            res.addProperty("isError", false);
        } catch (Throwable e) {
            TelemetryMod.LOG.warn("tool {} failed: {}", name, e.toString());
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            res.add("content", text(cause.getClass().getSimpleName() + ": " + cause.getMessage()));
            res.addProperty("isError", true);
        }
        return res;
    }

    // ---- helpers for building schemas and content ----

    public static JsonArray text(String s) {
        JsonArray arr = new JsonArray();
        JsonObject o = new JsonObject();
        o.addProperty("type", "text");
        o.addProperty("text", s);
        arr.add(o);
        return arr;
    }

    public static JsonArray json(JsonElement el) {
        return text(McpJson.pretty(el));
    }

    public static JsonArray image(String base64Png, String caption) {
        JsonArray arr = new JsonArray();
        JsonObject img = new JsonObject();
        img.addProperty("type", "image");
        img.addProperty("data", base64Png);
        img.addProperty("mimeType", "image/png");
        arr.add(img);
        if (caption != null) {
            JsonObject t = new JsonObject();
            t.addProperty("type", "text");
            t.addProperty("text", caption);
            arr.add(t);
        }
        return arr;
    }

    /** Builds a JSON schema object from (name, type, description) triples. Nothing is required. */
    public static JsonObject schema(Object... triples) {
        JsonObject s = new JsonObject();
        s.addProperty("type", "object");
        JsonObject props = new JsonObject();
        for (int i = 0; i + 2 < triples.length; i += 3) {
            JsonObject p = new JsonObject();
            p.addProperty("type", (String) triples[i + 1]);
            p.addProperty("description", (String) triples[i + 2]);
            props.add((String) triples[i], p);
        }
        s.add("properties", props);
        s.add("required", new JsonArray());
        return s;
    }

    public static JsonObject required(JsonObject schema, String... required) {
        JsonArray req = new JsonArray();
        for (String r : required) req.add(new JsonPrimitive(r));
        schema.add("required", req);
        return schema;
    }
}
