package com.bryce.mixin;

import com.bryce.client.EconomyClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void correctIsometricMovement(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        if (!EconomyClient.isIsometric) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        KeyboardInput input = (KeyboardInput) (Object) this;

        float forward = input.movementForward;
        float sideways = input.movementSideways;

        if (forward == 0 && sideways == 0) return;

        float playerYaw = client.player.getYaw();
        float angleDifference = (EconomyClient.cameraYaw - playerYaw);

        double radians = Math.toRadians(angleDifference);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        float correctedSideways = (float) (sideways * cos - forward * sin);
        float correctedForward = (float) (forward * cos + sideways * sin);

        input.movementSideways = correctedSideways;
        input.movementForward = correctedForward;
    }
}