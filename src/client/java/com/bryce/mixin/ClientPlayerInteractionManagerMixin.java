package com.bryce.mixin;

import com.bryce.client.IsometryCraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Shadow @Final
    private MinecraftClient client;

    @Unique
    private void isometrycraft$lookAt(Vec3d targetPos) {
        if (!IsometryCraftClient.isIsometric || this.client.player == null) {
            return;
        }
        ClientPlayerEntity player = this.client.player;

        double dx = targetPos.x - player.getX();
        double dy = targetPos.y - player.getEyeY();
        double dz = targetPos.z - player.getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);

        float yaw = MathHelper.wrapDegrees((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        float pitch = MathHelper.wrapDegrees((float) (-Math.toDegrees(Math.atan2(dy, dh))));

        player.setYaw(yaw);
        player.setPitch(pitch);

        if (this.client.getNetworkHandler() != null) {
            this.client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, player.isOnGround()));
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void isometrycraft$onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        this.isometrycraft$lookAt(hitResult.getPos());
    }

    @Inject(method = "interactEntity", at = @At("HEAD"))
    private void isometrycraft$onInteractEntity(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        this.isometrycraft$lookAt(entity.getPos());
    }

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void isometrycraft$onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        this.isometrycraft$lookAt(target.getPos());
    }

    @Inject(method = "breakBlock", at = @At("HEAD"))
    private void isometrycraft$onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.isometrycraft$lookAt(Vec3d.ofCenter(pos));
    }

    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void isometrycraft$onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        this.isometrycraft$lookAt(Vec3d.ofCenter(pos));
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void isometrycraft$onUpdateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        this.isometrycraft$lookAt(Vec3d.ofCenter(pos));
    }
}