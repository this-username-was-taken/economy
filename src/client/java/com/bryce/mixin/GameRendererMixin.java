package com.bryce.mixin;

import com.bryce.client.EconomyClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
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

    @Shadow @Final
    MinecraftClient client;

    @Unique
    private final Matrix4f economy$cachedProjection = new Matrix4f();

    @Unique
    private int economy$lastWidth = -1;

    @Unique
    private int economy$lastHeight = -1;

    @Unique
    private float economy$lastSize = Float.NaN;

    @Inject(method = "getBasicProjectionMatrix", at = @At("HEAD"), cancellable = true)
    private void economy$ortho(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (!EconomyClient.isIsometric)
            return;

        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        float size = EconomyClient.isometricSize;

        if (width != economy$lastWidth ||
                height != economy$lastHeight ||
                size != economy$lastSize) {
            float aspect = (float) width / height;

            economy$cachedProjection
                    .identity()
                    .ortho(-size * aspect, size * aspect, -size, size, -1000f, 1000f);
            economy$lastWidth = width;
            economy$lastHeight = height;
            economy$lastSize = size;
        }
        cir.setReturnValue(economy$cachedProjection);
    }

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void economy$updateTargetedEntity(float tickDelta, CallbackInfo ci) {
        if (!EconomyClient.isIsometric || this.client.world == null || this.client.getCameraEntity() == null) {
            return;
        }

        Entity cameraEntity = this.client.getCameraEntity();
        Camera camera = this.client.gameRenderer.getCamera();

        double mouseX = this.client.mouse.getX();
        double mouseY = this.client.mouse.getY();
        int width = this.client.getWindow().getFramebufferWidth();
        int height = this.client.getWindow().getFramebufferHeight();
        float size = EconomyClient.isometricSize;
        float aspect = (float) width / height;
        float ndcX = (float) ((2.0 * mouseX) / width - 1.0);
        float ndcY = (float) (1.0 - (2.0 * mouseY) / height);
        float viewX = ndcX * size * aspect;
        float viewY = ndcY * size;
        Vector3f worldOffset = new Vector3f(viewX, viewY, 0.0f).rotate(camera.getRotation());
        Vec3d rayStart = camera.getPos().add(worldOffset.x(), worldOffset.y(), worldOffset.z());
        Vector3f dirVec = new Vector3f(0.0f, 0.0f, -1.0f).rotate(camera.getRotation());
        Vec3d rayDirection = new Vec3d(dirVec.x(), dirVec.y(), dirVec.z());

        double maxDistance = 150.0;
        Vec3d rayEnd = rayStart.add(rayDirection.multiply(maxDistance));

        RaycastContext context = new RaycastContext(
                rayStart,
                rayEnd,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                cameraEntity
        );
        BlockHitResult blockHit = this.client.world.raycast(context);

        double extendedDistance = maxDistance;
        if (blockHit.getType() != HitResult.Type.MISS) {
            extendedDistance = blockHit.getPos().distanceTo(rayStart);
        }

        Vec3d viewVector = rayDirection.multiply(extendedDistance);
        Box searchBox = cameraEntity.getBoundingBox().stretch(viewVector).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                cameraEntity,
                rayStart,
                rayStart.add(viewVector),
                searchBox,
                (entity) -> !entity.isSpectator() && entity.canHit(),
                extendedDistance * extendedDistance
        );

        if (entityHit != null) {
            this.client.crosshairTarget = entityHit;
            this.client.targetedEntity = entityHit.getEntity();
        } else {
            this.client.crosshairTarget = blockHit;
            this.client.targetedEntity = null;
        }

        ci.cancel();
    }
}