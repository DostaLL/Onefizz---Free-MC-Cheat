package com.onefizz;

import java.lang.reflect.Field;
import java.util.*;

public abstract class Module {

    private boolean enabled = false;
    private final String name;
    private final String description;
    private List<Field> settingsCache;
    private int keyBind = -1;

    public Module(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int code) { this.keyBind = code; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean value) {
        this.enabled = value;
        if (value) onEnable(); else onDisable();
        ToastManager.INSTANCE.push(name, value);
    }

    protected void setEnabledSilent(boolean value) {
        this.enabled = value;
        if (value) onEnable(); else onDisable();
    }

    public boolean isEnabled() { return enabled; }
    public String getName()    { return name; }
    public String getDescription() { return description; }

    protected void onEnable()  {}
    protected void onDisable() {}

    public List<Field> getSettings() {
        if (settingsCache != null) return settingsCache;
        settingsCache = new ArrayList<>();
        Class<?> clazz = getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Setting.class)) {
                    f.setAccessible(true);
                    settingsCache.add(f);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return settingsCache;
    }

    public Object getSettingValue(String name) {
        for (Field f : getSettings()) {
            Setting s = f.getAnnotation(Setting.class);
            String n = s.name().isEmpty() ? f.getName() : s.name();
            if (n.equals(name)) {
                try { return f.get(this); } catch (IllegalAccessException e) { return null; }
            }
        }
        if ("enabled".equals(name)) return enabled;
        return null;
    }

    public void setSettingValue(String name, Object value) {
        for (Field f : getSettings()) {
            Setting s = f.getAnnotation(Setting.class);
            String n = s.name().isEmpty() ? f.getName() : s.name();
            if (n.equals(name)) {
                try { f.set(this, value); } catch (IllegalAccessException ignored) {}
                return;
            }
        }
    }
}