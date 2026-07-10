package com.bryce.mixin;

import com.bryce.client.IsometryCraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private double x;
    @Shadow private double y;

    @Inject(method = "lockCursor", at = @At("HEAD"), cancellable = true)
    private void preventCursorLock(CallbackInfo ci) {
        if (IsometryCraftClient.isIsometric) {
            GLFW.glfwSetInputMode(this.client.getWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            ci.cancel();
        }
    }

    @Inject(method = "isCursorLocked", at = @At("HEAD"), cancellable = true)
    private void isometrycraft$overrideIsCursorLocked(CallbackInfoReturnable<Boolean> cir) {
        if (IsometryCraftClient.isIsometric) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void onMouseCursorPos(long window, double x, double y, CallbackInfo ci) {
        if (IsometryCraftClient.isIsometric && IsometryCraftClient.isMiddleMouseDown && this.client.player != null) {
            ci.cancel();
            double dx = x - IsometryCraftClient.lastMouseX;
            double dy = y - IsometryCraftClient.lastMouseY;
            double sensitivity = 0.05;
            float yawRad = (float) Math.toRadians(IsometryCraftClient.cameraYaw);
            float cosYaw = (float) Math.cos(yawRad);
            float sinYaw = (float) Math.sin(yawRad);
            float pitchRad = (float) Math.toRadians(35.264F);
            float cosPitch = (float) Math.cos(pitchRad);
            float sinPitch = (float) Math.sin(pitchRad);
            double rightZ = -sinYaw;
            double upX = sinYaw * sinPitch;
            double upZ = cosYaw * sinPitch;
            double moveX = (dx * (double) cosYaw + dy * upX) * sensitivity;
            double moveY = dy * (double) cosPitch * sensitivity;
            double moveZ = (dx * rightZ + dy * upZ) * sensitivity;
            double newX = client.player.getX() - moveX;
            double newY = IsometryCraftClient.isYLocked
                    ? IsometryCraftClient.lockedYValue - client.player.getStandingEyeHeight()
                    : client.player.getY() - moveY;
            double newZ = client.player.getZ() - moveZ;
            client.player.setPosition(newX, newY, newZ);
        }
        IsometryCraftClient.lastMouseX = x;
        IsometryCraftClient.lastMouseY = y;
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (IsometryCraftClient.isIsometric && this.client.currentScreen == null) {
            IsometryCraftClient.isometricSize -= (float) vertical * 1.5f;
            IsometryCraftClient.isometricSize = Math.clamp(IsometryCraftClient.isometricSize, 2.0f, 60.0f);
            ci.cancel();
        }
    }
}