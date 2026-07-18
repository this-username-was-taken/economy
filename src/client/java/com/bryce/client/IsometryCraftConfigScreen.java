package com.bryce.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.function.BooleanSupplier;

public class IsometryCraftConfigScreen extends Screen {
    private final Screen parent;

    public IsometryCraftConfigScreen(Screen parent) {
        super(Text.literal("IsometryCraft Options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 48;

        this.addDrawableChild(createToggle(centerX - 180, startY, "Isometric", () -> IsometryCraftClient.isIsometric = !IsometryCraftClient.isIsometric, () -> IsometryCraftClient.isIsometric));
        this.addDrawableChild(createToggle(centerX - 58, startY, "HUD", () -> IsometryCraftClient.showHud = !IsometryCraftClient.showHud, () -> IsometryCraftClient.showHud));
        this.addDrawableChild(createToggle(centerX + 64, startY, "Y-Lock", () -> IsometryCraftClient.isYLocked = !IsometryCraftClient.isYLocked, () -> IsometryCraftClient.isYLocked));

        addAdjuster(centerX + 58, 76, 30, "-1.0", () -> IsometryCraftClient.lockedYValue -= 1.0);
        addAdjuster(centerX + 140, 76, 30, "+1.0", () -> IsometryCraftClient.lockedYValue += 1.0);
        addAdjuster(centerX + 58, 106, 30, "-1.0", () -> IsometryCraftClient.isometricSize = Math.max(1.0f, IsometryCraftClient.isometricSize - 1.0f));
        addAdjuster(centerX + 140, 106, 30, "+1.0", () -> IsometryCraftClient.isometricSize = Math.min(100.0f, IsometryCraftClient.isometricSize + 1.0f));
        addAdjuster(centerX + 50, 136, 38, "< 45°", () -> IsometryCraftClient.targetYaw = (IsometryCraftClient.targetYaw + 45.0f) % 360.0f);
        addAdjuster(centerX + 140, 136, 38, "45° >", () -> IsometryCraftClient.targetYaw = (IsometryCraftClient.targetYaw - 45.0f + 360.0f) % 360.0f);

        this.addDrawableChild(new SleekButton(centerX - 80, 185, 160, 20, Text.literal("Done"), b -> this.close(), true));
    }

    private SleekButton createToggle(int x, int y, String label, Runnable toggle, BooleanSupplier state) {
        return new SleekButton(x, y, 116, 20, getToggleText(label, state.getAsBoolean()), btn -> {
            toggle.run();
            btn.setMessage(getToggleText(label, state.getAsBoolean()));
            IsometryCraftClient.saveConfig();
        }, false);
    }

    private void addAdjuster(int x, int y, int width, String label, Runnable action) {
        this.addDrawableChild(new SleekButton(x, y, width, 20, Text.literal(label), btn -> {
            action.run();
            IsometryCraftClient.saveConfig();
        }, false));
    }

    private Text getToggleText(String prefix, boolean state) {
        return Text.literal(prefix + ": ").formatted(Formatting.GRAY).copy()
                .append(Text.literal(state ? "ON" : "OFF").formatted(state ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;

        context.drawCenteredTextWithShadow(textRenderer, Text.literal("ISOMETRYCRAFT OPTIONS").formatted(Formatting.BOLD, Formatting.AQUA), centerX, 16, 0xFFFFFF);
        drawRowLabel(context, "Camera Height", String.format("%.1f", IsometryCraftClient.lockedYValue), centerX, 82);
        drawRowLabel(context, "Camera Zoom", String.format("%.1f", IsometryCraftClient.isometricSize), centerX, 112);
        drawRowLabel(context, "Isometric Angle", String.format("%.0f°", IsometryCraftClient.targetYaw), centerX, 142);
    }

    private void drawRowLabel(DrawContext context, String label, String value, int centerX, int y) {
        context.drawTextWithShadow(textRenderer, Text.literal(label).formatted(Formatting.GRAY), centerX - 172, y, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(value).formatted(Formatting.DARK_AQUA), centerX + 114, y, 0xFFFFFF);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        context.fillGradient(0, 0, this.width, this.height, 0xC0090D16, 0xC0111827);

        for (int y = 0; y < this.height; y += 16) {
            int xOffset = (y / 16) % 2 * 12;
            for (int x = xOffset; x < this.width; x += 24) {
                context.fill(x, y, x + 1, y + 1, 0x18818CF8);
            }
        }

        int centerX = this.width / 2;
        int left = centerX - 190, top = 40, w = 380, h = 175;

        context.fill(left, top, left + w, top + h, 0xEE090D16);
        context.drawBorder(left, top, w, h, 0x33818CF8);
        context.drawBorder(left + 1, top + 1, w - 2, h - 2, 0x15FFFFFF);

        context.fillGradient(centerX - 120, 31, centerX, 32, 0x0022D3EE, 0xFF22D3EE);
        context.fillGradient(centerX, 31, centerX + 120, 32, 0xFF22D3EE, 0x0022D3EE);
    }

    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(this.parent);
    }

    private class SleekButton extends ButtonWidget {
        private float hoverProgress = 0.0f;
        private final boolean isAccent;

        public SleekButton(int x, int y, int width, int height, Text message, PressAction onPress, boolean isAccent) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.isAccent = isAccent;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            if (!this.visible) return;

            this.hoverProgress = this.isHovered()
                    ? Math.min(1.0f, this.hoverProgress + delta * 0.25f)
                    : Math.max(0.0f, this.hoverProgress - delta * 0.25f);

            int x = this.getX(), y = this.getY(), w = this.getWidth(), h = this.getHeight();
            context.fill(x, y, x + w, y + h, 0xAA0B0F19);

            if (this.hoverProgress > 0) {
                context.fill(x, y, x + w, y + h, ((int) (this.hoverProgress * 0x22) << 24) | 0x22D3EE);
            }

            context.drawBorder(x, y, w, h, this.isAccent ? 0xFF818CF8 : (this.isHovered() ? 0xFF22D3EE : 0x3394A3B8));

            if (this.hoverProgress > 0 && !this.isAccent) {
                int tagHeight = (int) (this.hoverProgress * (h - 4));
                context.fill(x + 1, y + (h - tagHeight) / 2, x + 3, y + (h + tagHeight) / 2, 0xFF22D3EE);
            }

            int textCol = this.active ? (this.isHovered() ? 0xFFFFFFFF : 0xFFCBD5E1) : 0xFF64748B;
            context.drawCenteredTextWithShadow(textRenderer, this.getMessage(), x + w / 2, y + (h - 8) / 2, textCol);
        }
    }
}