package com.bryce.mixin;

import com.bryce.client.EconomyClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
public class FrustumMixin {
    @Inject(method = "isVisible*", at = @At("HEAD"), cancellable = true)
    private void economy$isometric(Box box, CallbackInfoReturnable<Boolean> cir) {
        if (EconomyClient.isIsometric) {
            cir.setReturnValue(true);
        }
    }
}