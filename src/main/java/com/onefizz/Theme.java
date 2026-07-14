package com.onefizz;

public enum Theme {

    AMETHYST ("Amethyst", 0xFF9333EA, 0xFFC084FC, 0x559333EA),
    AZURE    ("Azure",    0xFF2563EB, 0xFF60A5FA, 0x552563EB),
    AQUA     ("Aqua",     0xFF06B6D4, 0xFF67E8F9, 0x5506B6D4),
    EMERALD  ("Emerald",  0xFF10B981, 0xFF6EE7B7, 0x5510B981),
    LIME     ("Lime",     0xFF84CC16, 0xFFBEF264, 0x5584CC16),
    AMBER    ("Amber",    0xFFF59E0B, 0xFFFCD34D, 0x55F59E0B),
    ROSE     ("Rose",     0xFFEC4899, 0xFFF9A8D4, 0x55EC4899),
    CRIMSON  ("Crimson",  0xFFDC2626, 0xFFFCA5A5, 0x55DC2626),
    MIDNIGHT ("Midnight", 0xFF6366F1, 0xFFA5B4FC, 0x556366F1),
    MONO     ("Mono",     0xFFE5E7EB, 0xFFF9FAFB, 0x55E5E7EB);

    private final String label;
    private final int accent;
    private final int accentLight;
    private final int accentGlow;

    private static Theme current = AZURE;

    Theme(String label, int accent, int accentLight, int accentGlow) {
        this.label = label;
        this.accent = accent;
        this.accentLight = accentLight;
        this.accentGlow = accentGlow;
    }

    public String label()        { return label; }
    public int accent()          { return accent; }
    public int accentLight()     { return accentLight; }
    public int accentGlow()      { return accentGlow; }

    public float accentR() { return ((accent >> 16) & 0xFF) / 255f; }
    public float accentG() { return ((accent >> 8)  & 0xFF) / 255f; }
    public float accentB() { return (accent        & 0xFF) / 255f; }

    public static Theme get() { return current; }

    public static void set(Theme theme) {
        if (theme == null) theme = AZURE;
        current = theme;
    }

    public static Theme byName(String name) {
        if (name == null) return AZURE;
        try { return Theme.valueOf(name); }
        catch (Exception e) { return AZURE; }
    }

    public static int A()     { return current.accent; }
    public static int ALT()   { return current.accentLight; }
    public static int GLOW()  { return current.accentGlow; }
}
