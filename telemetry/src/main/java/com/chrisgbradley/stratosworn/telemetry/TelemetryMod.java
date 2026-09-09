package com.chrisgbradley.stratosworn.telemetry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stratosworn Telemetry: a client-only dev mod that hosts an MCP server (Streamable HTTP)
 * on 127.0.0.1 so Claude Code can inspect the running game.
 */
@Mod(value = TelemetryMod.MOD_ID, dist = Dist.CLIENT)
public final class TelemetryMod {
    public static final String MOD_ID = "stratosworn_telemetry";
    public static final String VERSION = "0.1.0";
    public static final Logger LOG = LoggerFactory.getLogger("StratosTelemetry");

    private static McpHttpServer server;

    public TelemetryMod(IEventBus modBus) {
        modBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::onShutdown);
        NeoForge.EVENT_BUS.addListener(ChatCapture::onSystemChat);
        NeoForge.EVENT_BUS.addListener(ChatCapture::onPlayerChat);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        int port = Integer.getInteger("stratosworn.telemetry.port", 25590);
        try {
            server = new McpHttpServer(port, GameTools.registry());
            server.start();
            LOG.info("Telemetry MCP server listening on http://127.0.0.1:{}/mcp", port);
            Runtime.getRuntime().addShutdownHook(new Thread(TelemetryMod::stopServer, "telemetry-shutdown"));
        } catch (Exception e) {
            LOG.error("Failed to start telemetry MCP server on port {}", port, e);
        }
    }

    private void onShutdown(GameShuttingDownEvent event) {
        stopServer();
    }

    private static synchronized void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
