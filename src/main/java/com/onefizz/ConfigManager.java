package com.onefizz;

import com.google.gson.*;
import com.onefizz.modules.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

public class ConfigManager {

    private static final Path CONFIG_DIR =
        FabricLoader.getInstance().getGameDir().resolve("onefizz-configs");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        try { Files.createDirectories(CONFIG_DIR); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public static void save(String name) {
        JsonObject root = new JsonObject();

        for (Module m : OneFizzMod.modules.getAll()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", m.isEnabled());
            obj.addProperty("keyBind", m.getKeyBind());

            for (Field f : m.getSettings()) {
                try {
                    Object val = f.get(m);
                    Setting s = f.getAnnotation(Setting.class);
                    String key = s.name().isEmpty() ? f.getName() : s.name();
                    if (val instanceof Boolean) obj.addProperty(key, (Boolean) val);
                    else if (val instanceof Number) obj.addProperty(key, (Number) val);
                    else if (val instanceof String) obj.addProperty(key, (String) val);
                    else if (val instanceof Enum) obj.addProperty(key, ((Enum<?>) val).name());
                } catch (IllegalAccessException ignored) {}
            }
            root.add(m.getName(), obj);
        }

        try (Writer w = Files.newBufferedWriter(CONFIG_DIR.resolve(name + ".json"))) {
            GSON.toJson(root, w);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void load(String name) {
        Path file = CONFIG_DIR.resolve(name + ".json");
        if (!Files.exists(file)) return;

        JsonObject root;
        try (Reader r = Files.newBufferedReader(file)) {
            root = GSON.fromJson(r, JsonObject.class);
        } catch (IOException e) { e.printStackTrace(); return; }

        if (root == null) return;

        for (Module m : OneFizzMod.modules.getAll()) {
            if (!root.has(m.getName())) continue;
            JsonObject obj = root.getAsJsonObject(m.getName());

            if (obj.has("enabled")) {
                m.setEnabled(obj.get("enabled").getAsBoolean());
            }
            if (obj.has("keyBind")) {
                m.setKeyBind(obj.get("keyBind").getAsInt());
            }

            for (Field f : m.getSettings()) {
                Setting s = f.getAnnotation(Setting.class);
                String key = s.name().isEmpty() ? f.getName() : s.name();
                if (!obj.has(key)) continue;
                JsonElement el = obj.get(key);
                try {
                    Class<?> type = f.getType();
                    if (type == boolean.class || type == Boolean.class) {
                        f.set(m, el.getAsBoolean());
                    } else if (type == int.class || type == Integer.class) {
                        f.set(m, el.getAsInt());
                    } else if (type == long.class || type == Long.class) {
                        f.set(m, el.getAsLong());
                    } else if (type == float.class || type == Float.class) {
                        f.set(m, el.getAsFloat());
                    } else if (type == double.class || type == Double.class) {
                        f.set(m, el.getAsDouble());
                    } else if (type == String.class) {
                        f.set(m, el.getAsString());
                    } else if (type.isEnum()) {
                        for (Object e : type.getEnumConstants()) {
                            if (((Enum<?>) e).name().equals(el.getAsString())) {
                                f.set(m, e);
                                break;
                            }
                        }
                    }
                } catch (IllegalAccessException ignored) {}
            }
        }
    }

    public static List<String> listConfigs() {
        try {
            return Files.list(CONFIG_DIR)
                .filter(p -> p.toString().endsWith(".json"))
                .map(p -> p.getFileName().toString().replace(".json", ""))
                .sorted()
                .toList();
        } catch (IOException e) { return List.of(); }
    }

    public static void delete(String name) {
        try { Files.deleteIfExists(CONFIG_DIR.resolve(name + ".json")); }
        catch (IOException e) { e.printStackTrace(); }
    }
}