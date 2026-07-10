package com.bryce.mixin;

import com.bryce.client.IsometryCraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "getBlockInteractionRange", at = @At("HEAD"), cancellable = true)
    private void isometrycraft$blockReach(CallbackInfoReturnable<Double> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (IsometryCraftClient.isIsometric && player.getAbilities().creativeMode) {
            cir.setReturnValue(128.0);
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At("HEAD"), cancellable = true)
    private void isometrycraft$entityReach(CallbackInfoReturnable<Double> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (IsometryCraftClient.isIsometric && player.getAbilities().creativeMode) {
            cir.setReturnValue(128.0);
        }
    }
}