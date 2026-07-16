package com.bryce.mixin;

import com.bryce.client.IsometryCraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void correctIsometricMovement(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        if (!IsometryCraftClient.isIsometric) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        KeyboardInput input = (KeyboardInput) (Object) this;

        float forward = input.movementForward;
        float sideways = input.movementSideways;
        if (forward == 0 && sideways == 0) return;

        float inputAngle = (float) Math.toDegrees(Math.atan2(-sideways, forward));
        float targetYaw = MathHelper.wrapDegrees(IsometryCraftClient.cameraYaw + inputAngle);
        client.player.setYaw(targetYaw);
        client.player.setPitch(0.0f);
        client.player.headYaw = targetYaw;
        client.player.bodyYaw = targetYaw;
        input.movementForward = (float) Math.hypot(forward, sideways);
        input.movementSideways = 0.0f;
    }
}