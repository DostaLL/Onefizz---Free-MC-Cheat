package com.onefizz;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UserCounter {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Path CONFIG_PATH = Path.of("config/onefizz-counter.json");
    private static String apiUrl = null;
    private static String userId = null;
    private static int cachedCount = -1;
    private static long lastFetch = 0;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        loadConfig();
        userId = getOrCreateUserId();

        if (apiUrl != null && !apiUrl.isEmpty()) {
            new Thread(UserCounter::trackAndFetch, "OneFizz-Counter").start();
        }
    }

    private static void loadConfig() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                apiUrl = obj.has("apiUrl") ? obj.get("apiUrl").getAsString() : null;
            } else {
                JsonObject obj = new JsonObject();
                obj.addProperty("apiUrl", "");
                obj.addProperty("info", "Paste your Firebase RTDB URL here, e.g. https://onefizz-default-rtdb.firebaseio.com/");
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.writeString(CONFIG_PATH, obj.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getOrCreateUserId() {
        Path idPath = Path.of("config/onefizz-user.id");
        try {
            if (Files.exists(idPath)) {
                return Files.readString(idPath).trim();
            } else {
                String id = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                Files.writeString(idPath, id);
                return id;
            }
        } catch (IOException e) {
            return UUID.randomUUID().toString().substring(0, 16);
        }
    }

    private static void trackAndFetch() {
        try {
            String trackUrl = apiUrl + "/users/" + userId + ".json";
            HttpRequest trackReq = HttpRequest.newBuilder()
                .uri(URI.create(trackUrl))
                .PUT(HttpRequest.BodyPublishers.ofString("{" + System.currentTimeMillis() + ":1}"))
                .build();
            CLIENT.send(trackReq, HttpResponse.BodyHandlers.ofString());

            Thread.sleep(500);

            String countUrl = apiUrl + "/users.json?shallow=true";
            HttpRequest countReq = HttpRequest.newBuilder()
                .uri(URI.create(countUrl))
                .GET()
                .build();
            HttpResponse<String> resp = CLIENT.send(countReq, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && resp.body() != null) {
                JsonElement el = JsonParser.parseString(resp.body());
                if (el.isJsonObject()) {
                    cachedCount = el.getAsJsonObject().size();
                    lastFetch = System.currentTimeMillis();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getUserCount() {
        if (apiUrl == null || apiUrl.isEmpty()) return -1;
        if (System.currentTimeMillis() - lastFetch > 60000) {
            lastFetch = System.currentTimeMillis();
            new Thread(UserCounter::trackAndFetch, "OneFizz-Counter-Refresh").start();
        }
        return cachedCount;
    }

    public static boolean isConfigured() {
        return apiUrl != null && !apiUrl.isEmpty();
    }
}
