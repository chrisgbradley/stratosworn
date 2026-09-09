package com.chrisgbradley.stratosworn.telemetry;

import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Ring buffer of chat lines the client received, with timestamps, so run_command can return output. */
public final class ChatCapture {
    public record Line(long atMillis, String kind, String text) {}

    private static final int CAPACITY = 500;
    private static final ArrayDeque<Line> LINES = new ArrayDeque<>();

    private ChatCapture() {}

    static void onSystemChat(ClientChatReceivedEvent.System event) {
        push("system", event.getMessage().getString());
    }

    static void onPlayerChat(ClientChatReceivedEvent.Player event) {
        push("player", event.getMessage().getString());
    }

    private static synchronized void push(String kind, String text) {
        if (LINES.size() >= CAPACITY) LINES.pollFirst();
        LINES.addLast(new Line(System.currentTimeMillis(), kind, text));
    }

    public static synchronized List<Line> since(long millis) {
        List<Line> out = new ArrayList<>();
        for (Line l : LINES) if (l.atMillis >= millis) out.add(l);
        return out;
    }
}
