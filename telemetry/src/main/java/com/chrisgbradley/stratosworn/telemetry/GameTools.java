package com.chrisgbradley.stratosworn.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundDebugSampleSubscriptionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.chrisgbradley.stratosworn.telemetry.McpJson.boolArg;
import static com.chrisgbradley.stratosworn.telemetry.McpJson.doubleArg;
import static com.chrisgbradley.stratosworn.telemetry.McpJson.intArg;
import static com.chrisgbradley.stratosworn.telemetry.McpJson.strArg;
import static com.chrisgbradley.stratosworn.telemetry.ToolRegistry.json;
import static com.chrisgbradley.stratosworn.telemetry.ToolRegistry.schema;
import static com.chrisgbradley.stratosworn.telemetry.ToolRegistry.text;

/** The MCP tools. Every game read runs on the client thread via {@link Minecraft#submit}. */
public final class GameTools {
    private GameTools() {}

    private static final long CLIENT_TIMEOUT_SECONDS = 15;

    /** Run on the client (render) thread and block the HTTP worker until done. */
    private static <T> T onClient(Supplier<T> task) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        CompletableFuture<T> f = mc.submit(task);
        return f.get(CLIENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public static ToolRegistry registry() {
        ToolRegistry r = new ToolRegistry();

        r.add("screenshot",
                "PNG screenshot of the game window, base64 encoded. Optional scale (0.1-1.0, default 0.5) downsamples to save tokens.",
                schema("scale", "number", "Scale factor for the returned image, 0.1 to 1.0. Default 0.5."),
                GameTools::screenshot);

        r.add("perf",
                "Performance snapshot: FPS, frame time, server tick ms, heap used/max, loaded chunks, entity count, render distance.",
                schema(),
                args -> json(onClient(GameTools::perf)));

        r.add("log_tail",
                "Last N lines of logs/latest.log (default 200). Optional grep filter (case-insensitive substring).",
                schema("lines", "integer", "Number of trailing lines to return. Default 200, max 5000.",
                        "grep", "string", "Only return lines containing this text (case-insensitive)."),
                GameTools::logTail);

        r.add("mods",
                "Loaded mod IDs and versions.",
                schema(),
                args -> json(mods()));

        r.add("look_at",
                "Block or entity under the crosshair, with block state properties and NBT. Optional range in blocks (default 20).",
                schema("range", "number", "Max raycast distance in blocks. Default 20."),
                args -> json(onClient(() -> lookAt(doubleArg(args, "range", 20.0)))));

        r.add("player",
                "Player position, dimension, biome, health, hunger, XP, game mode, and an inventory summary.",
                schema(),
                args -> json(onClient(GameTools::player)));

        r.add("run_command",
                "Run a command as the player (leading slash optional). Returns chat lines received within wait_ms (default 750).",
                ToolRegistry.required(schema(
                        "command", "string", "The command, e.g. 'time set day' or '/tp 0 100 0'.",
                        "wait_ms", "integer", "How long to collect chat output after sending. Default 750."), "command"),
                GameTools::runCommand);

        r.add("recipe_lookup",
                "All recipes that make or use an item ID (e.g. 'create:brass_ingot'). Returns each recipe as JSON. Default limit 25.",
                ToolRegistry.required(schema(
                        "item", "string", "Item ID, namespaced (minecraft:stick, create:andesite_alloy).",
                        "mode", "string", "'makes' (item is a result), 'uses' (item is an input), or 'any'. Default 'any'.",
                        "limit", "integer", "Max recipes to return. Default 25, max 200."), "item"),
                args -> json(onClient(() -> recipeLookup(
                        strArg(args, "item", ""), strArg(args, "mode", "any"), intArg(args, "limit", 25)))));

        r.add("quest_state",
                "FTB Quests progress for the player: chapters, quests, completion and progress percent. Best-effort via reflection.",
                schema("only_incomplete", "boolean", "If true, omit completed quests. Default false."),
                args -> json(onClient(() -> questState(boolArg(args, "only_incomplete", false)))));

        return r;
    }

    // ------------------------------------------------------------------ screenshot

    private static JsonArray screenshot(JsonObject args) throws Exception {
        double scale = Math.max(0.1, Math.min(1.0, doubleArg(args, "scale", 0.5)));
        byte[] png = onClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            try (NativeImage full = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                if (scale >= 0.999) {
                    return toPng(full);
                }
                int w = Math.max(1, (int) Math.round(full.getWidth() * scale));
                int h = Math.max(1, (int) Math.round(full.getHeight() * scale));
                try (NativeImage small = new NativeImage(NativeImage.Format.RGBA, w, h, false)) {
                    // nearest-neighbour downsample: good enough for "what is on screen"
                    for (int y = 0; y < h; y++) {
                        int sy = Math.min(full.getHeight() - 1, (int) (y / scale));
                        for (int x = 0; x < w; x++) {
                            int sx = Math.min(full.getWidth() - 1, (int) (x / scale));
                            small.setPixelRGBA(x, y, full.getPixelRGBA(sx, sy));
                        }
                    }
                    return toPng(small);
                }
            }
        });
        String b64 = Base64.getEncoder().encodeToString(png);
        return ToolRegistry.image(b64, "screenshot " + png.length + " bytes, scale " + scale);
    }

    private static byte[] toPng(NativeImage img) {
        try {
            return img.asByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ------------------------------------------------------------------ perf

    private static JsonObject perf() {
        Minecraft mc = Minecraft.getInstance();
        JsonObject o = new JsonObject();
        o.addProperty("fps", mc.getFps());
        o.addProperty("frame_time_ms", round2(averageMs(mc.getDebugOverlay().getFrameTimeLogger(), 100)));

        MinecraftServer integrated = mc.getSingleplayerServer();
        if (integrated != null) {
            o.addProperty("server_tick_ms", round2(integrated.getAverageTickTimeNanos() / 1_000_000.0));
            o.addProperty("server_kind", "integrated");
        } else if (mc.getConnection() != null) {
            // Ask the remote server for tick samples; they arrive over the next few ticks.
            mc.getConnection().send(new ServerboundDebugSampleSubscriptionPacket(RemoteDebugSampleType.TICK_TIME));
            double ms = averageMs(mc.getDebugOverlay().getTickTimeLogger(), 100);
            o.addProperty("server_tick_ms", ms > 0 ? round2(ms) : null);
            o.addProperty("server_kind", "remote");
            if (ms <= 0) o.addProperty("server_tick_note", "subscribed to tick samples; call perf again in ~1s");
        } else {
            o.addProperty("server_kind", "none");
        }

        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        o.addProperty("heap_used_mb", used / (1024 * 1024));
        o.addProperty("heap_max_mb", rt.maxMemory() / (1024 * 1024));

        ClientLevel level = mc.level;
        if (level != null) {
            o.addProperty("loaded_chunks", level.getChunkSource().getLoadedChunksCount());
            o.addProperty("entity_count", level.getEntityCount());
            o.addProperty("dimension", level.dimension().location().toString());
        }
        o.addProperty("render_distance", mc.options.renderDistance().get());
        o.addProperty("renderer", mc.levelRenderer.getSectionStatistics());
        o.addProperty("window", mc.getWindow().getWidth() + "x" + mc.getWindow().getHeight());
        o.addProperty("screen", mc.screen == null ? null : mc.screen.getClass().getName());
        return o;
    }

    private static double averageMs(LocalSampleLogger logger, int lastN) {
        int size = logger.size();
        if (size <= 0) return 0;
        int n = Math.min(lastN, size);
        long sum = 0;
        for (int i = size - n; i < size; i++) sum += logger.get(i);
        return (sum / (double) n) / 1_000_000.0;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ------------------------------------------------------------------ log_tail

    private static JsonArray logTail(JsonObject args) throws Exception {
        int n = Math.max(1, Math.min(5000, intArg(args, "lines", 200)));
        String grep = strArg(args, "grep", null);
        Path log = Minecraft.getInstance().gameDirectory.toPath().resolve("logs").resolve("latest.log");
        if (!Files.exists(log)) return text("no log at " + log);
        List<String> all = Files.readAllLines(log, StandardCharsets.UTF_8);
        if (grep != null && !grep.isBlank()) {
            String g = grep.toLowerCase(Locale.ROOT);
            all = all.stream().filter(l -> l.toLowerCase(Locale.ROOT).contains(g)).toList();
        }
        int from = Math.max(0, all.size() - n);
        return text(String.join("\n", all.subList(from, all.size())));
    }

    // ------------------------------------------------------------------ mods

    private static JsonArray mods() {
        JsonArray arr = new JsonArray();
        for (IModInfo mi : ModList.get().getMods()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", mi.getModId());
            o.addProperty("version", mi.getVersion().toString());
            o.addProperty("name", mi.getDisplayName());
            arr.add(o);
        }
        return arr;
    }

    // ------------------------------------------------------------------ look_at

    private static JsonObject lookAt(double range) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ClientLevel level = mc.level;
        JsonObject o = new JsonObject();
        if (p == null || level == null) {
            o.addProperty("error", "not in a world");
            return o;
        }

        Vec3 eye = p.getEyePosition(1.0f);
        Vec3 look = p.getViewVector(1.0f);
        Vec3 end = eye.add(look.scale(range));

        HitResult blockHit = p.pick(range, 1.0f, false);
        AABB box = p.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(p, eye, end, box,
                e -> !e.isSpectator() && e.isPickable(), range * range);

        double blockDist = blockHit.getType() == HitResult.Type.MISS ? Double.MAX_VALUE : blockHit.getLocation().distanceTo(eye);
        double entityDist = entityHit == null ? Double.MAX_VALUE : entityHit.getLocation().distanceTo(eye);

        if (entityHit != null && entityDist < blockDist) {
            Entity e = entityHit.getEntity();
            o.addProperty("kind", "entity");
            o.addProperty("distance", round2(entityDist));
            o.addProperty("type", BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());
            o.addProperty("entity_id", e.getId());
            o.addProperty("name", e.getName().getString());
            o.add("pos", vec(e.position()));
            CompoundTag tag = new CompoundTag();
            e.saveWithoutId(tag);
            o.addProperty("nbt", tag.toString());
            return o;
        }

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bh = (BlockHitResult) blockHit;
            BlockPos pos = bh.getBlockPos();
            BlockState state = level.getBlockState(pos);
            o.addProperty("kind", "block");
            o.addProperty("distance", round2(blockDist));
            o.addProperty("block", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            JsonObject bp = new JsonObject();
            bp.addProperty("x", pos.getX());
            bp.addProperty("y", pos.getY());
            bp.addProperty("z", pos.getZ());
            o.add("pos", bp);
            o.addProperty("face", bh.getDirection().getName());
            JsonObject props = new JsonObject();
            for (Map.Entry<Property<?>, Comparable<?>> en : state.getValues().entrySet()) {
                props.addProperty(en.getKey().getName(), String.valueOf(en.getValue()));
            }
            o.add("properties", props);
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                o.addProperty("block_entity", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString());
                o.addProperty("nbt", be.saveWithFullMetadata(level.registryAccess()).toString());
            }
            return o;
        }

        o.addProperty("kind", "miss");
        o.addProperty("range", range);
        return o;
    }

    private static JsonObject vec(Vec3 v) {
        JsonObject o = new JsonObject();
        o.addProperty("x", round2(v.x));
        o.addProperty("y", round2(v.y));
        o.addProperty("z", round2(v.z));
        return o;
    }

    // ------------------------------------------------------------------ player

    private static JsonObject player() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        ClientLevel level = mc.level;
        JsonObject o = new JsonObject();
        if (p == null || level == null) {
            o.addProperty("error", "not in a world");
            o.addProperty("screen", mc.screen == null ? null : mc.screen.getClass().getName());
            return o;
        }
        o.addProperty("name", p.getGameProfile().getName());
        o.add("pos", vec(p.position()));
        o.addProperty("yaw", round2(p.getYRot()));
        o.addProperty("pitch", round2(p.getXRot()));
        o.addProperty("dimension", level.dimension().location().toString());
        o.addProperty("biome", level.getBiome(p.blockPosition()).unwrapKey()
                .map(k -> k.location().toString()).orElse("unknown"));
        o.addProperty("health", round2(p.getHealth()));
        o.addProperty("max_health", round2(p.getMaxHealth()));
        o.addProperty("food", p.getFoodData().getFoodLevel());
        o.addProperty("saturation", round2(p.getFoodData().getSaturationLevel()));
        o.addProperty("xp_level", p.experienceLevel);
        o.addProperty("game_mode", mc.gameMode == null ? null : mc.gameMode.getPlayerMode().getName());
        o.addProperty("on_ground", p.onGround());
        o.addProperty("day_time", level.getDayTime() % 24000L);
        o.addProperty("game_time", level.getGameTime());

        JsonObject inv = new JsonObject();
        inv.add("main_hand", stack(p.getMainHandItem()));
        inv.add("off_hand", stack(p.getOffhandItem()));
        JsonArray armor = new JsonArray();
        for (ItemStack s : p.getInventory().armor) if (!s.isEmpty()) armor.add(stack(s));
        inv.add("armor", armor);
        JsonArray items = new JsonArray();
        for (int i = 0; i < p.getInventory().items.size(); i++) {
            ItemStack s = p.getInventory().items.get(i);
            if (s.isEmpty()) continue;
            JsonObject e = stack(s);
            e.addProperty("slot", i);
            items.add(e);
        }
        inv.add("items", items);
        o.add("inventory", inv);
        return o;
    }

    private static JsonObject stack(ItemStack s) {
        JsonObject o = new JsonObject();
        if (s.isEmpty()) {
            o.addProperty("id", "minecraft:air");
            o.addProperty("count", 0);
            return o;
        }
        o.addProperty("id", BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
        o.addProperty("count", s.getCount());
        if (s.isDamageableItem()) {
            o.addProperty("damage", s.getDamageValue());
            o.addProperty("max_damage", s.getMaxDamage());
        }
        return o;
    }

    // ------------------------------------------------------------------ run_command

    private static JsonArray runCommand(JsonObject args) throws Exception {
        String cmd = strArg(args, "command", "").trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        if (cmd.isEmpty()) throw new ToolRegistry.ToolException("command is required");
        int waitMs = Math.max(0, Math.min(10_000, intArg(args, "wait_ms", 750)));
        final String finalCmd = cmd;

        long start = onClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.getConnection() == null) {
                throw new IllegalStateException("not connected to a world");
            }
            long t = System.currentTimeMillis();
            mc.getConnection().sendCommand(finalCmd);
            return t;
        });

        Thread.sleep(waitMs);

        JsonObject o = new JsonObject();
        o.addProperty("command", "/" + cmd);
        JsonArray lines = new JsonArray();
        for (ChatCapture.Line l : ChatCapture.since(start - 5)) {
            JsonObject e = new JsonObject();
            e.addProperty("kind", l.kind());
            e.addProperty("text", l.text());
            lines.add(e);
        }
        o.add("chat", lines);
        return json(o);
    }

    // ------------------------------------------------------------------ recipe_lookup

    /** recipe id -> compact JSON, cached per RecipeManager instance. */
    private static final Map<ResourceLocation, String> RECIPE_JSON = new ConcurrentHashMap<>();
    private static Object recipeCacheOwner;

    private static JsonObject recipeLookup(String item, String mode, int limit) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        JsonObject o = new JsonObject();
        if (level == null) {
            o.addProperty("error", "not in a world (recipes are synced from the server)");
            return o;
        }
        if (item.isBlank()) {
            o.addProperty("error", "item is required");
            return o;
        }
        limit = Math.max(1, Math.min(200, limit));
        String needle = "\"" + item + "\"";
        String needleLoose = item.contains(":") ? item : "minecraft:" + item;

        var manager = level.getRecipeManager();
        if (recipeCacheOwner != manager) {
            RECIPE_JSON.clear();
            recipeCacheOwner = manager;
        }
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
        Collection<RecipeHolder<?>> all = manager.getRecipes();

        JsonArray makes = new JsonArray();
        JsonArray uses = new JsonArray();
        int scanned = 0, matched = 0;
        for (RecipeHolder<?> holder : all) {
            scanned++;
            String js = RECIPE_JSON.get(holder.id());
            if (js == null) {
                js = encodeRecipe(holder.value(), ops);
                RECIPE_JSON.put(holder.id(), js);
            }
            if (!js.contains(needle) && !js.contains("\"" + needleLoose + "\"")) continue;

            boolean isResult = false;
            try {
                ItemStack res = holder.value().getResultItem(level.registryAccess());
                if (!res.isEmpty()) {
                    isResult = BuiltInRegistries.ITEM.getKey(res.getItem()).toString().equals(needleLoose);
                }
            } catch (Throwable ignored) {}
            if (!isResult) {
                // heuristic for modded recipes with custom output keys
                isResult = js.matches("(?s).*\"(result|results|output|outputs|primary_output|item_outputs)\".*")
                        && resultSectionContains(js, needleLoose);
            }

            JsonObject e = new JsonObject();
            e.addProperty("id", holder.id().toString());
            e.addProperty("type", BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType()).toString());
            e.addProperty("json", js.length() > 4000 ? js.substring(0, 4000) + "...(truncated)" : js);

            if (isResult) {
                if (!"uses".equals(mode) && makes.size() < limit) { makes.add(e); matched++; }
            } else {
                if (!"makes".equals(mode) && uses.size() < limit) { uses.add(e); matched++; }
            }
            if (makes.size() >= limit && uses.size() >= limit) break;
        }
        o.addProperty("item", needleLoose);
        o.addProperty("recipes_scanned", scanned);
        o.addProperty("returned", matched);
        o.add("makes", makes);
        o.add("uses", uses);
        return o;
    }

    private static boolean resultSectionContains(String js, String item) {
        // crude: does the item appear after the first occurrence of a result-ish key?
        int idx = -1;
        for (String key : new String[]{"\"result\"", "\"results\"", "\"output\"", "\"outputs\"", "\"primary_output\"", "\"item_outputs\""}) {
            int i = js.indexOf(key);
            if (i >= 0 && (idx < 0 || i < idx)) idx = i;
        }
        return idx >= 0 && js.indexOf("\"" + item + "\"", idx) >= 0;
    }

    private static String encodeRecipe(Recipe<?> recipe, RegistryOps<JsonElement> ops) {
        try {
            var res = Recipe.CODEC.encodeStart(ops, recipe);
            return res.result().map(McpJson::compact)
                    .orElseGet(() -> "{\"encode_error\":\"" + res.error().map(Object::toString).orElse("?") + "\"}");
        } catch (Throwable t) {
            return "{\"encode_error\":\"" + t + "\"}";
        }
    }

    // ------------------------------------------------------------------ quest_state (reflection)

    private static JsonObject questState(boolean onlyIncomplete) {
        JsonObject o = new JsonObject();
        if (!ModList.get().isLoaded("ftbquests")) {
            o.addProperty("error", "ftbquests is not loaded");
            return o;
        }
        try {
            Class<?> cqf = Class.forName("dev.ftb.mods.ftbquests.client.ClientQuestFile");
            Object file = staticField(cqf, "INSTANCE");
            if (file == null) {
                o.addProperty("error", "ClientQuestFile.INSTANCE is null (not in a world?)");
                return o;
            }
            Object team = fieldOrCall(file, "selfTeamData", "getSelfTeamData");
            List<?> chapters = (List<?>) call(file, "getAllChapters");
            JsonArray chArr = new JsonArray();
            int total = 0, done = 0;
            for (Object ch : chapters) {
                JsonObject c = new JsonObject();
                c.addProperty("title", componentString(call(ch, "getTitle")));
                c.addProperty("id", String.valueOf(call(ch, "getCodeString")));
                JsonArray qArr = new JsonArray();
                List<?> quests = (List<?>) call(ch, "getQuests");
                for (Object q : quests) {
                    total++;
                    boolean completed = team != null && Boolean.TRUE.equals(call(team, "isCompleted", q));
                    if (completed) done++;
                    if (onlyIncomplete && completed) continue;
                    JsonObject qo = new JsonObject();
                    qo.addProperty("title", componentString(call(q, "getTitle")));
                    qo.addProperty("id", String.valueOf(call(q, "getCodeString")));
                    qo.addProperty("completed", completed);
                    if (team != null) {
                        Object prog = callQuiet(team, "getRelativeProgress", q);
                        if (prog != null) qo.addProperty("progress_pct", String.valueOf(prog));
                        Object started = callQuiet(team, "isStarted", q);
                        if (started != null) qo.addProperty("started", String.valueOf(started));
                    }
                    qArr.add(qo);
                }
                c.add("quests", qArr);
                chArr.add(c);
            }
            o.addProperty("quests_total", total);
            o.addProperty("quests_completed", done);
            o.add("chapters", chArr);
        } catch (Throwable t) {
            o.addProperty("error", "reflection failed: " + t);
        }
        return o;
    }

    private static String componentString(Object maybeComponent) {
        if (maybeComponent instanceof net.minecraft.network.chat.Component c) return c.getString();
        return String.valueOf(maybeComponent);
    }

    private static Object staticField(Class<?> cls, String name) throws Exception {
        Field f = cls.getField(name);
        return f.get(null);
    }

    private static Object fieldOrCall(Object target, String field, String method) {
        try {
            Field f = target.getClass().getField(field);
            return f.get(target);
        } catch (Throwable ignored) {}
        return callQuiet(target, method);
    }

    private static Object call(Object target, String name, Object... args) throws Exception {
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                return m.invoke(target, args);
            }
        }
        throw new NoSuchMethodException(target.getClass().getName() + "#" + name + "/" + args.length);
    }

    private static Object callQuiet(Object target, String name, Object... args) {
        try {
            return call(target, name, args);
        } catch (Throwable t) {
            return null;
        }
    }

}
