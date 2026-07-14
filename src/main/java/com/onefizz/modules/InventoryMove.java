package com.onefizz.modules;

import com.onefizz.Module;

/**
 * Логика реализована через mixin InventoryMoveMixin:
 * блокирует остановку движения при открытом GUI.
 */
public class InventoryMove extends Module {

    public InventoryMove() {
        super("InventoryMove", "Движение с открытым инвентарем");
    }
}
