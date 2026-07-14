package com.onefizz;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class AltManager {

    public static final AltManager INSTANCE = new AltManager();

    private static final Path FILE = FabricLoader.getInstance().getGameDir()
        .resolve("onefizz-alts.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum AltType { OFFLINE, MICROSOFT }

    public static class Alt {
        public String username;
        public AltType type = AltType.OFFLINE;
        public String tag = "";
        public String uuid = "";
        public String accessToken = "";
        public String refreshToken = "";
        public long addedAt;

        public Alt() {}
        public Alt(String username, AltType type, String tag) {
            this.username = username;
            this.type = type;
            this.tag = tag;
            this.addedAt = System.currentTimeMillis();
        }
    }

    private final List<Alt> alts = new ArrayList<>();

    private AltManager() {}

    public List<Alt> getAlts() { return alts; }

    public void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            JsonArray arr = GSON.fromJson(r, JsonArray.class);
            if (arr == null) return;
            alts.clear();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                Alt a = new Alt();
                a.username = obj.has("username") ? obj.get("username").getAsString() : "Steve";
                a.type = obj.has("type")
                    ? AltType.valueOf(obj.get("type").getAsString())
                    : AltType.OFFLINE;
                a.tag = obj.has("tag") ? obj.get("tag").getAsString() : "";
                a.uuid = obj.has("uuid") ? obj.get("uuid").getAsString() : "";
                a.accessToken = obj.has("accessToken") ? obj.get("accessToken").getAsString() : "";
                a.refreshToken = obj.has("refreshToken") ? obj.get("refreshToken").getAsString() : "";
                a.addedAt = obj.has("addedAt") ? obj.get("addedAt").getAsLong() : 0L;
                alts.add(a);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void save() {
        JsonArray arr = new JsonArray();
        for (Alt a : alts) {
            JsonObject obj = new JsonObject();
            obj.addProperty("username", a.username);
            obj.addProperty("type", a.type.name());
            obj.addProperty("tag", a.tag);
            obj.addProperty("uuid", a.uuid);
            obj.addProperty("accessToken", a.accessToken);
            obj.addProperty("refreshToken", a.refreshToken);
            obj.addProperty("addedAt", a.addedAt);
            arr.add(obj);
        }
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(arr, w);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void add(Alt alt) { alts.add(alt); save(); }
    public void remove(int index) {
        if (index >= 0 && index < alts.size()) {
            alts.remove(index);
            save();
        }
    }

    public boolean switchTo(Alt alt) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            UUID id;
            if (alt.uuid != null && !alt.uuid.isEmpty()) {
                id = UUID.fromString(alt.uuid);
            } else {
                id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + alt.username).getBytes());
            }

            Session.AccountType accType = (alt.type == AltType.MICROSOFT)
                ? Session.AccountType.MSA
                : Session.AccountType.LEGACY;

            String token = (alt.accessToken != null && !alt.accessToken.isEmpty())
                ? alt.accessToken
                : "0";

            Session newSession = new Session(
                alt.username,
                id,
                token,
                Optional.empty(),
                Optional.empty(),
                accType
            );
            ((com.onefizz.mixin.MinecraftClientAccessor) mc).setSessionField(newSession);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
}