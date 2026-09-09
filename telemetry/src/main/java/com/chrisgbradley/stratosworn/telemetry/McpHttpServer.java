package com.chrisgbradley.stratosworn.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Minimal MCP "Streamable HTTP" transport using only the JDK's com.sun.net.httpserver.
 * <p>
 * POST /mcp   JSON-RPC 2.0 request(s); replies with application/json.
 * GET /mcp    405 (no server-initiated stream is offered).
 * DELETE /mcp 200 (session teardown is a no-op).
 */
public final class McpHttpServer {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Set<String> SUPPORTED_PROTOCOLS = Set.of("2024-11-05", "2025-03-26", "2025-06-18");
    private static final String DEFAULT_PROTOCOL = "2025-06-18";

    private final HttpServer http;
    private final ExecutorService pool;
    private final ToolRegistry tools;
    private final String sessionId = UUID.randomUUID().toString();

    public McpHttpServer(int port, ToolRegistry tools) throws IOException {
        this.tools = tools;
        this.pool = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "telemetry-mcp");
            t.setDaemon(true);
            return t;
        });
        // Literal IPv4 loopback: getLoopbackAddress() can resolve to ::1, which a 127.0.0.1 client cannot reach.
        this.http = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port), 8);
        this.http.createContext("/mcp", this::handle);
        this.http.setExecutor(pool);
    }

    public void start() { http.start(); }

    public void stop() {
        http.stop(0);
        pool.shutdownNow();
    }

    private void handle(HttpExchange ex) throws IOException {
        try {
            String origin = ex.getRequestHeaders().getFirst("Origin");
            if (origin != null && !isLocalOrigin(origin)) {
                send(ex, 403, "text/plain", "forbidden origin");
                return;
            }
            switch (ex.getRequestMethod()) {
                case "POST" -> handlePost(ex);
                case "GET" -> send(ex, 405, "text/plain", "no SSE stream; use POST");
                case "DELETE" -> send(ex, 200, "text/plain", "ok");
                default -> send(ex, 405, "text/plain", "method not allowed");
            }
        } catch (Throwable t) {
            TelemetryMod.LOG.error("MCP request failed", t);
            try { send(ex, 500, "text/plain", "internal error: " + t); } catch (IOException ignored) {}
        }
    }

    private static boolean isLocalOrigin(String origin) {
        String o = origin.toLowerCase();
        return o.startsWith("http://localhost") || o.startsWith("http://127.0.0.1")
                || o.startsWith("https://localhost") || o.startsWith("https://127.0.0.1");
    }

    private void handlePost(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(body);
        } catch (Exception e) {
            sendJson(ex, 400, errorResponse(null, -32700, "parse error: " + e.getMessage()));
            return;
        }

        if (parsed.isJsonArray()) {
            JsonArray out = new JsonArray();
            for (JsonElement el : parsed.getAsJsonArray()) {
                JsonObject resp = dispatch(el);
                if (resp != null) out.add(resp);
            }
            if (out.isEmpty()) send(ex, 202, "text/plain", "");
            else sendJson(ex, 200, out);
            return;
        }

        JsonObject resp = dispatch(parsed);
        if (resp == null) {
            send(ex, 202, "text/plain", "");
        } else {
            sendJson(ex, 200, resp);
        }
    }

    /** Returns null for notifications and client responses (nothing to send back). */
    private JsonObject dispatch(JsonElement el) {
        if (!el.isJsonObject()) return errorResponse(null, -32600, "invalid request");
        JsonObject req = el.getAsJsonObject();
        JsonElement id = req.get("id");
        String method = req.has("method") && !req.get("method").isJsonNull() ? req.get("method").getAsString() : null;
        JsonObject params = req.has("params") && req.get("params").isJsonObject()
                ? req.getAsJsonObject("params") : new JsonObject();

        if (method == null) return null;
        if (id == null || id.isJsonNull()) return null;

        try {
            return switch (method) {
                case "initialize" -> result(id, initialize(params));
                case "ping" -> result(id, new JsonObject());
                case "tools/list" -> result(id, tools.list());
                case "tools/call" -> result(id, tools.call(params));
                case "resources/list" -> result(id, emptyList("resources"));
                case "prompts/list" -> result(id, emptyList("prompts"));
                default -> errorResponse(id, -32601, "method not found: " + method);
            };
        } catch (ToolRegistry.ToolException te) {
            return errorResponse(id, -32602, te.getMessage());
        } catch (Throwable t) {
            TelemetryMod.LOG.error("MCP method {} failed", method, t);
            return errorResponse(id, -32603, "internal error: " + t);
        }
    }

    private JsonObject initialize(JsonObject params) {
        String requested = params.has("protocolVersion") ? params.get("protocolVersion").getAsString() : DEFAULT_PROTOCOL;
        String version = SUPPORTED_PROTOCOLS.contains(requested) ? requested : DEFAULT_PROTOCOL;
        JsonObject res = new JsonObject();
        res.addProperty("protocolVersion", version);
        JsonObject caps = new JsonObject();
        caps.add("tools", new JsonObject());
        res.add("capabilities", caps);
        JsonObject info = new JsonObject();
        info.addProperty("name", "stratosworn-telemetry");
        info.addProperty("version", TelemetryMod.VERSION);
        res.add("serverInfo", info);
        res.addProperty("instructions", "Telemetry for the running Minecraft client. Game reads run on the client thread. "
                + "Use perf for FPS/TPS/heap, screenshot to see the screen, run_command to run chat commands (leading slash optional).");
        return res;
    }

    private static JsonObject emptyList(String key) {
        JsonObject o = new JsonObject();
        o.add(key, new JsonArray());
        return o;
    }

    private static JsonObject result(JsonElement id, JsonElement result) {
        JsonObject o = new JsonObject();
        o.addProperty("jsonrpc", "2.0");
        o.add("id", id);
        o.add("result", result);
        return o;
    }

    private static JsonObject errorResponse(JsonElement id, int code, String message) {
        JsonObject o = new JsonObject();
        o.addProperty("jsonrpc", "2.0");
        o.add("id", id == null ? JsonNull.INSTANCE : id);
        JsonObject err = new JsonObject();
        err.addProperty("code", code);
        err.addProperty("message", message);
        o.add("error", err);
        return o;
    }

    private void sendJson(HttpExchange ex, int status, JsonElement body) throws IOException {
        ex.getResponseHeaders().set("Mcp-Session-Id", sessionId);
        send(ex, status, "application/json", GSON.toJson(body));
    }

    private static void send(HttpExchange ex, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        if (bytes.length == 0) {
            ex.sendResponseHeaders(status, -1);
        } else {
            ex.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
        }
        ex.close();
    }
}
