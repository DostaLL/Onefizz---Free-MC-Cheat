package com.onefizz.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.onefizz.Module;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Spammer extends Module {

    private static final Path CONFIG_PATH = Path.of("config/onefizz-spammer.json");

    public int cooldownSec = 2;
    public boolean antiSpam = false;
    public int antiSpamChars = 3;
    public final List<String> messages = new ArrayList<>();

    private int timer = 0;
    private int index = 0;
    private final Random random = new Random();

    private static final char[][] LOOKALIKES = {
        {'a', 'а'}, {'A', 'А'},
        {'e', 'е'}, {'E', 'Е'},
        {'o', 'о'}, {'O', 'О'},
        {'c', 'с'}, {'C', 'С'},
        {'p', 'р'}, {'P', 'Р'},
        {'x', 'х'}, {'X', 'Х'},
        {'y', 'у'}, {'Y', 'У'},
        {'k', 'к'}, {'K', 'К'},
        {'h', 'н'}, {'H', 'Н'},
        {'b', 'ь'}, {'B', 'В'},
        {'m', 'м'}, {'M', 'М'},
        {'t', 'т'}, {'T', 'Т'},
    };

    public Spammer() {
        super("Spammer", "Автоматическая отправка сообщений");
        loadMessages();
    }

    public void saveMessages() {
        try {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("cooldownSec", cooldownSec);
            obj.addProperty("antiSpam", antiSpam);
            obj.addProperty("antiSpamChars", antiSpamChars);
            JsonArray arr = new JsonArray();
            for (String s : messages) arr.add(s);
            obj.add("messages", arr);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMessages() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                com.google.gson.JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("cooldown")) cooldownSec = obj.get("cooldown").getAsInt();
                if (obj.has("cooldownSec")) cooldownSec = obj.get("cooldownSec").getAsInt();
                if (obj.has("antiSpam")) antiSpam = obj.get("antiSpam").getAsBoolean();
                if (obj.has("antiSpamChars")) antiSpamChars = obj.get("antiSpamChars").getAsInt();
                if (obj.has("messages")) {
                    messages.clear();
                    for (JsonElement el : obj.getAsJsonArray("messages")) {
                        String s = el.getAsString().trim();
                        if (!s.isEmpty()) messages.add(s);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTick(MinecraftClient mc) {
        if (!isEnabled() || mc.player == null) return;
        if (timer > 0) { timer--; return; }

        if (messages.isEmpty()) return;

        String msg = messages.get(index % messages.size());
        if (antiSpam) msg = obfuscate(msg);

        mc.player.networkHandler.sendChatMessage(msg);

        index = (index + 1) % messages.size();
        timer = Math.max(1, (int)(cooldownSec * 20f));
    }

    private String obfuscate(String input) {
        char[] chars = input.toCharArray();
        int changes = Math.min(antiSpamChars, Math.max(1, chars.length));

        for (int i = 0; i < changes; i++) {
            int idx = random.nextInt(chars.length);
            char c = chars[idx];
            for (char[] pair : LOOKALIKES) {
                if (pair[0] == c) {
                    chars[idx] = pair[1];
                    break;
                }
            }
        }
        return new String(chars);
    }

    @Override
    protected void onDisable() {
        timer = 0;
    }
}
