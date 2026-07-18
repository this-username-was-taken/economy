package com.bryce.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class IsometricHudRenderer implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (!IsometryCraftClient.isIsometric || !IsometryCraftClient.showHud) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden || client.player == null) return;

        TextRenderer textRenderer = client.textRenderer;

        int xOffset = 10;
        int yOffset = 10;
        int lineHeight = 12;

        drawContext.drawTextWithShadow(textRenderer, Text.literal("§6§lIsometryCraft HUD"), xOffset, yOffset, 0xFFFFFF);
        yOffset += lineHeight;

        String yawText = String.format("Yaw: %.1f° (Target: %.1f°)", IsometryCraftClient.cameraYaw, IsometryCraftClient.targetYaw);
        drawContext.drawTextWithShadow(textRenderer, Text.literal("§7" + yawText), xOffset, yOffset, 0xFFFFFF);
        yOffset += lineHeight;

        String pitchText = String.format("Pitch: %.1f°", IsometryCraftClient.cameraPitch);
        drawContext.drawTextWithShadow(textRenderer, Text.literal("§7" + pitchText), xOffset, yOffset, 0xFFFFFF);
        yOffset += lineHeight;

        String sizeText = String.format("Size: %.1f", IsometryCraftClient.isometricSize);
        drawContext.drawTextWithShadow(textRenderer, Text.literal("§7" + sizeText), xOffset, yOffset, 0xFFFFFF);
        yOffset += lineHeight;

        String yLockStatus = IsometryCraftClient.isYLocked ? String.format("§aLocked (%.2f)", IsometryCraftClient.lockedYValue) : "§cUnlocked";
        drawContext.drawTextWithShadow(textRenderer, Text.literal("§7Y-Lock: " + yLockStatus), xOffset, yOffset, 0xFFFFFF);

        int screenWidth = drawContext.getScaledWindowWidth();
        int compassCenterX = screenWidth - 55;
        int compassCenterY = 40;

        int radiusX = 32;
        int radiusY = 16;

        float cameraYaw = IsometryCraftClient.cameraYaw;

        drawDirectionMarker(drawContext, textRenderer, "N", 180.0F, cameraYaw, compassCenterX, compassCenterY, radiusX, radiusY, 0xFFFFFF00); // Gold highlights North
        drawDirectionMarker(drawContext, textRenderer, "E", 270.0F, cameraYaw, compassCenterX, compassCenterY, radiusX, radiusY, 0xFFFFFFFF);
        drawDirectionMarker(drawContext, textRenderer, "S", 0.0F, cameraYaw, compassCenterX, compassCenterY, radiusX, radiusY, 0xFFFFFFFF);
        drawDirectionMarker(drawContext, textRenderer, "W", 90.0F, cameraYaw, compassCenterX, compassCenterY, radiusX, radiusY, 0xFFFFFFFF);

        int innerRadiusX = radiusX - 4;
        int innerRadiusY = radiusY - 2;
        drawDirectionMarker(drawContext, textRenderer, "o", 135.0F, cameraYaw, compassCenterX, compassCenterY, innerRadiusX, innerRadiusY, 0xFFBBBBBB);
        drawDirectionMarker(drawContext, textRenderer, "o", 225.0F, cameraYaw, compassCenterX, compassCenterY, innerRadiusX, innerRadiusY, 0xFFBBBBBB);
        drawDirectionMarker(drawContext, textRenderer, "o", 315.0F, cameraYaw, compassCenterX, compassCenterY, innerRadiusX, innerRadiusY, 0xFFBBBBBB);
        drawDirectionMarker(drawContext, textRenderer, "o", 45.0F, cameraYaw, compassCenterX, compassCenterY, innerRadiusX, innerRadiusY, 0xFFBBBBBB);
    }

    private void drawDirectionMarker(DrawContext drawContext, TextRenderer textRenderer, String marker, float directionYaw, float cameraYaw, int centerX, int centerY, int radiusX, int radiusY, int color) {
        double angleRad = Math.toRadians(directionYaw - cameraYaw - 90.0F);

        int posX = centerX + (int) (radiusX * Math.cos(angleRad));
        int posY = centerY + (int) (radiusY * Math.sin(angleRad));

        int textWidth = textRenderer.getWidth(marker);
        int textHeight = textRenderer.fontHeight;

        drawContext.drawTextWithShadow(textRenderer, Text.literal(marker), posX - textWidth / 2, posY - textHeight / 2, color);
    }
}