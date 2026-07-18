package com.bryce.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IsometryCraftConfigScreen extends Screen {
    private final Screen parent;
    public IsometryCraftConfigScreen(Screen parent) { super(Text.literal("IsometryCraft Options")); this.parent = parent; }

    @Override
    protected void init() {
        int cX = this.width / 2, sY = 44;
        this.addDrawableChild(createToggle(cX - 180, sY, "Isometric", () -> IsometryCraftClient.isIsometric = !IsometryCraftClient.isIsometric, () -> IsometryCraftClient.isIsometric));
        this.addDrawableChild(createToggle(cX - 58, sY, "HUD", () -> IsometryCraftClient.showHud = !IsometryCraftClient.showHud, () -> IsometryCraftClient.showHud));
        this.addDrawableChild(createToggle(cX + 64, sY, "Y-Lock", () -> IsometryCraftClient.isYLocked = !IsometryCraftClient.isYLocked, () -> IsometryCraftClient.isYLocked));
        this.addDrawableChild(new SleekSlider(cX + 50, 70, 120, 20, -64, 320, null, () -> (double) IsometryCraftClient.lockedYValue, v -> IsometryCraftClient.lockedYValue = v.floatValue()));
        this.addDrawableChild(new SleekSlider(cX + 50, 96, 120, 20, 1, 100, null, () -> (double) IsometryCraftClient.isometricSize, v -> IsometryCraftClient.isometricSize = v.floatValue()));
        this.addDrawableChild(new SleekSlider(cX + 50, 126, 120, 20, 0, 7, 8, () -> {
            float y = MathHelper.wrapDegrees(IsometryCraftClient.targetYaw); return (double) Math.round((y < 0 ? y + 360f : y) / 45f);
        }, v -> IsometryCraftClient.targetYaw = MathHelper.wrapDegrees((float) Math.round(v) * 45f)));
        this.addDrawableChild(new SleekSlider(cX + 50, 156, 120, 20, 0, 2, 3, () -> (double) IsometryCraftClient.targetPitchIndex, v -> {
            int idx = (int) Math.round(v); IsometryCraftClient.targetPitchIndex = idx; IsometryCraftClient.targetPitch = IsometryCraftClient.PITCH_VALUES[idx];
        }));
        this.addDrawableChild(new SleekButton(cX - 80, 190, 160, 20, Text.literal("Done"), b -> this.close(), true));
    }

    private SleekButton createToggle(int x, int y, String lbl, Runnable tgl, BooleanSupplier st) {
        return new SleekButton(x, y, 116, 20, getToggleText(lbl, st.getAsBoolean()), b -> { tgl.run(); b.setMessage(getToggleText(lbl, st.getAsBoolean())); IsometryCraftClient.saveConfig(); }, false);
    }

    private Text getToggleText(String pfx, boolean st) {
        return Text.literal(pfx + ": ").formatted(Formatting.GRAY).copy().append(Text.literal(st ? "ON" : "OFF").formatted(st ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int cX = this.width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("ISOMETRYCRAFT OPTIONS").formatted(Formatting.BOLD, Formatting.AQUA), cX, 16, -1);
        drawRowLabel(ctx, "Camera Height", String.format("%.1f", IsometryCraftClient.lockedYValue), cX, 76);
        drawRowLabel(ctx, "Camera Zoom", String.format("%.1f", IsometryCraftClient.isometricSize), cX, 102);
        float y = MathHelper.wrapDegrees(IsometryCraftClient.targetYaw);
        drawRowLabel(ctx, "Isometric Yaw", String.format("%.0f°", y < 0 ? y + 360f : y), cX, 132);
        drawRowLabel(ctx, "Isometric Pitch", String.format("%.1f°", IsometryCraftClient.targetPitch), cX, 162);
    }

    private void drawRowLabel(DrawContext ctx, String lbl, String val, int cX, int y) {
        ctx.drawTextWithShadow(textRenderer, Text.literal(lbl).formatted(Formatting.GRAY), cX - 172, y, -1);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(val).formatted(Formatting.DARK_AQUA), cX + 184, y, -1);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fillGradient(0, 0, this.width, this.height, 0xC0090D16, 0xC0111827);
        for (int y = 0; y < this.height; y += 16)
            for (int x = ((y / 16) % 2 * 12); x < this.width; x += 24) ctx.fill(x, y, x + 1, y + 1, 0x18818CF8);
        int cX = this.width / 2, l = cX - 190;
        ctx.fill(l, 36, l + 405, 221, 0xEE090D16);
        ctx.drawBorder(l, 36, 405, 185, 0x33818CF8); ctx.drawBorder(l + 1, 37, 403, 183, 0x15FFFFFF);
        ctx.fillGradient(cX - 120, 31, cX, 32, 0x0022D3EE, 0xFF22D3EE); ctx.fillGradient(cX, 31, cX + 120, 32, 0xFF22D3EE, 0x0022D3EE);
    }

    @Override public void close() { if (this.client != null) this.client.setScreen(this.parent); }

    private class SleekButton extends ButtonWidget {
        private float hoverProgress = 0f;
        private final boolean isAccent;
        public SleekButton(int x, int y, int w, int h, Text msg, PressAction prs, boolean acc) { super(x, y, w, h, msg, prs, DEFAULT_NARRATION_SUPPLIER); this.isAccent = acc; }

        @Override
        protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            if (!this.visible) return;
            this.hoverProgress = this.isHovered() ? Math.min(1f, this.hoverProgress + delta * 0.25f) : Math.max(0f, this.hoverProgress - delta * 0.25f);
            int x = this.getX(), y = this.getY(), w = this.getWidth(), h = this.getHeight();
            ctx.fill(x, y, x + w, y + h, 0xAA0B0F19);
            if (this.hoverProgress > 0) {
                ctx.fill(x, y, x + w, y + h, ((int) (this.hoverProgress * 0x22) << 24) | 0x22D3EE);
                if (!this.isAccent) { int th = (int) (this.hoverProgress * (h - 4)); ctx.fill(x + 1, y + (h - th) / 2, x + 3, y + (h + th) / 2, 0xFF22D3EE); }
            }
            ctx.drawBorder(x, y, w, h, this.isAccent ? 0xFF818CF8 : (this.isHovered() ? 0xFF22D3EE : 0x3394A3B8));
            ctx.drawCenteredTextWithShadow(textRenderer, this.getMessage(), x + w / 2, y + (h - 8) / 2, this.active ? (this.isHovered() ? -1 : 0xFFCBD5E1) : 0xFF64748B);
        }
    }

    private class SleekSlider extends SliderWidget {
        private final double min, max;
        private final Integer steps;
        private final Consumer<Double> setter;

        public SleekSlider(int x, int y, int w, int h, double min, double max, Integer steps, Supplier<Double> get, Consumer<Double> set) {
            super(x, y, w, h, Text.empty(), (get.get() - min) / (max - min));
            this.min = min; this.max = max; this.steps = steps; this.setter = set;
            if (steps != null) this.value = Math.round(this.value * (steps - 1)) / (double) (steps - 1);
        }

        @Override protected void updateMessage() { setMessage(Text.empty()); }

        @Override
        protected void applyValue() {
            if (steps != null) this.value = Math.round(this.value * (steps - 1)) / (double) (steps - 1);
            setter.accept(min + value * (max - min));
            IsometryCraftClient.saveConfig();
        }

        @Override
        public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            if (!visible) return;
            int x = getX(), y = getY(), w = getWidth(), h = getHeight(), th = 3, ty = y + (h - th) / 2, tx = x + 5 + (int) (value * (w - 10)), cy = y + h / 2 - 1;
            ctx.fill(x + 2, ty + th + 1, x + w - 2, ty + th + 2, 0x22000000);
            ctx.fill(x, ty - 1, x + w, ty + th + 1, 0xFF070B12);
            ctx.fill(x + 1, ty, x + w - 1, ty + th, 0xFF111827);
            ctx.fill(x + 2, ty + 1, x + w - 2, ty + th - 1, 0xFF1F2937);
            ctx.fill(x + 2, ty + 1, tx, ty + th - 1, isHovered() ? 0xFF67E8F9 : 0xFF38BDF8);
            ctx.fill(x + 2, ty + 1, tx, ty + 2, 0x30FFFFFF);
            ctx.fill(tx - 1, ty + 1, tx, ty + th - 1, 0xAAFFFFFF);
            ctx.fill(tx - 7, ty + th, tx + 7, ty + th + 1, 0x2222D3EE);
            ctx.fill(tx - 4, ty + th + 1, tx + 4, ty + th + 2, 0x1122D3EE);
            ctx.fill(tx - 3, cy - 3, tx + 4, cy + 4, 0xFF0F172A);
            ctx.fill(tx - 2, cy - 2, tx + 3, cy + 3, isHovered() ? -1 : 0xFFF1F5F9);
            ctx.fill(tx - 2, cy - 2, tx + 3, cy - 1, 0x66FFFFFF);
            ctx.fill(tx - 2, cy + 2, tx + 3, cy + 3, 0x22000000);
            ctx.fill(tx, cy, tx + 1, cy + 1, 0xFF38BDF8);
        }
    }
}