package com.onefizz.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveAccessor {
    @Accessor("onGround") void setOnGroundField(boolean value);
    @Accessor("yaw")      void setYawField(float value);
    @Accessor("pitch")    void setPitchField(float value);
    @Accessor("changeLook") void setChangeLookField(boolean value);
    @Accessor("x")        void setXField(double value);
    @Accessor("y")        void setYField(double value);
    @Accessor("z")        void setZField(double value);
}
