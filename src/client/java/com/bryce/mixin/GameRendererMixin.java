package com.bryce.mixin;

import com.bryce.client.IsometryCraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final MinecraftClient client;

    @Unique private final Matrix4f isometrycraft$cachedProjection = new Matrix4f();
    @Unique private int isometrycraft$lastWidth = -1, isometrycraft$lastHeight = -1;
    @Unique private float isometrycraft$lastSize = Float.NaN;

    @Inject(method = "getBasicProjectionMatrix", at = @At("HEAD"), cancellable = true)
    private void isometrycraft$ortho(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (!IsometryCraftClient.isIsometric) return;
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        if (width <= 0 || height <= 0) return;
        float size = IsometryCraftClient.isometricSize;
        if (width != isometrycraft$lastWidth || height != isometrycraft$lastHeight || size != isometrycraft$lastSize) {
            float aspect = (float) width / height;
            isometrycraft$cachedProjection.identity().ortho(-size * aspect, size * aspect, -size, size, -1000f, 1000f);
            isometrycraft$lastWidth = width;
            isometrycraft$lastHeight = height;
            isometrycraft$lastSize = size;
        }
        cir.setReturnValue(isometrycraft$cachedProjection);
    }

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void isometrycraft$updateTargetedEntity(float tickDelta, CallbackInfo ci) {
        if (!IsometryCraftClient.isIsometric) return;
        if (client == null) return;
        if (client.world == null) return;
        if (client.player == null) return;
        Entity camEntity = client.getCameraEntity();
        if (camEntity == null) return;
        Camera camera = client.gameRenderer.getCamera();
        if (camera == null) return;
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        if (width <= 0 || height <= 0) return;
        float size = IsometryCraftClient.isometricSize;
        float ndcX = (float) ((2.0 * client.mouse.getX()) / width - 1.0);
        float ndcY = (float) (1.0 - (2.0 * client.mouse.getY()) / height);
        Vector3f offset = new Vector3f(ndcX * size * ((float) width / height), ndcY * size, 0.0f).rotate(camera.getRotation());
        Vec3d rayStart = camera.getPos().add(offset.x(), offset.y(), offset.z());
        Vector3f dirVec = new Vector3f(0.0f, 0.0f, -1.0f).rotate(camera.getRotation());
        Vec3d rayDir = new Vec3d(dirVec.x(), dirVec.y(), dirVec.z());
        Vec3d rayEnd = rayStart.add(rayDir.multiply(150.0));
        BlockHitResult blockHit = client.world.raycast(new RaycastContext(rayStart, rayEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, camEntity));
        double dist = blockHit.getType() != HitResult.Type.MISS ? blockHit.getPos().distanceTo(rayStart) : 150.0;
        Vec3d viewVec = rayDir.multiply(dist);

        Box searchBox = new Box(rayStart, rayStart.add(viewVec)).expand(1.0);

        EntityHitResult entityHit = ProjectileUtil.raycast(
                camEntity, rayStart, rayStart.add(viewVec),
                searchBox,
                e -> !e.isSpectator() && e.canHit(), dist * dist
        );

        client.crosshairTarget = entityHit != null ? entityHit : blockHit;
        client.targetedEntity = entityHit != null ? entityHit.getEntity() : null;
        ci.cancel();
    }

    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    private void isometrycraft$ignoreFovEffects(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        if (IsometryCraftClient.isIsometric) {
            cir.setReturnValue(Double.valueOf(this.client.options.getFov().getValue()));
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void isometrycraft$disableViewBobbing(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (IsometryCraftClient.isIsometric) ci.cancel();
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void disableHurtTilt(CallbackInfo ci) {
        if (IsometryCraftClient.isIsometric) ci.cancel();
    }

    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    private void hideHand(Camera camera, float tickDelta, Matrix4f positionMatrix, CallbackInfo ci) {
        if (IsometryCraftClient.isIsometric) {
            ci.cancel();
        }
    }
}