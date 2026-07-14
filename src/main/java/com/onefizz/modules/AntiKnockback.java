package com.onefizz.modules;

import com.onefizz.Module;

/**
 * AntiKnockback (AKB) — полная отмена нокбэка.
 * Дублирует Velocity 0%/0%, но удобнее как одна кнопка.
 * Если включены оба — AKB имеет приоритет.
 */
public class AntiKnockback extends Module {

    public AntiKnockback() { super("AntiKnockback", "Уменьшение отбрасывания от ударов"); }
}
