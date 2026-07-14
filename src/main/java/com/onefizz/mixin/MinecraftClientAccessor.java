package com.onefizz.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int value);

    @Accessor("attackCooldown")
    void setAttackCooldown(int value);

    @Accessor("attackCooldown")
    int getAttackCooldown();

    @Mutable
    @Accessor("session")
    void setSessionField(Session session);
}
