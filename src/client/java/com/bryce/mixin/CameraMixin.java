package com.bryce.mixin;

import com.bryce.client.IsometryCraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private boolean ready;
    @Shadow private BlockView area;
    @Shadow private Entity focusedEntity;
    @Shadow private boolean thirdPerson;
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);
    @Shadow
    protected abstract void setPos(double x, double y, double z);
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void updateIsometric(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!IsometryCraftClient.isIsometric) return;
        if (area == null || focusedEntity == null) return;
        if (Float.isNaN(tickDelta) || Float.isInfinite(tickDelta)) return;
        try {
            this.ready = true;
            this.area = area;
            this.focusedEntity = focusedEntity;
            this.thirdPerson = true;
            final float yaw = IsometryCraftClient.cameraYaw;
            final float pitch = 35.264F;
            this.setRotation(yaw, pitch);
            double x = MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX());
            double y = IsometryCraftClient.isYLocked
                    ? IsometryCraftClient.lockedYValue
                    : MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY())
                      + focusedEntity.getStandingEyeHeight();
            double z = MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ());
            float yawRad = (float) Math.toRadians(-yaw);
            float pitchRad = (float) Math.toRadians(pitch);
            double lookX = Math.sin(yawRad) * Math.cos(pitchRad);
            double lookY = -Math.sin(pitchRad);
            double lookZ = Math.cos(yawRad) * Math.cos(pitchRad);
            double distance = 20.0;
            this.setPos(x - lookX * distance, y - lookY * distance, z - lookZ * distance);
            ci.cancel();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}