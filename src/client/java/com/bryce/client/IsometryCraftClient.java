package com.bryce.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class IsometryCraftClient implements ClientModInitializer {
	public static boolean isIsometric = false;
	public static float targetYaw = 45.0F;
	public static float cameraYaw = 45.0F;
	public static float prevCameraYaw = 45.0F;
	public static final float[] PITCH_VALUES = {0.0F, (float) Math.toDegrees(Math.atan(1.0 / Math.sqrt(2.0))), 90.0F};
	public static int targetPitchIndex = 1;
	public static float targetPitch = PITCH_VALUES[1];
	public static float cameraPitch = PITCH_VALUES[1];
	public static float prevCameraPitch = PITCH_VALUES[1];
	public static float isometricSize = 15.0f;
	public static boolean isMiddleMouseDown = false;
	public static double lastMouseX = 0.0;
	public static double lastMouseY = 0.0;
	public static boolean isYLocked = false;
	public static double lockedYValue = 70.0;
	public static boolean showHud = true;

	private static KeyBinding toggleKey;
	private static KeyBinding lockYKey;
	private static KeyBinding rotateLeftKey;
	private static KeyBinding rotateRightKey;
	private static KeyBinding pitchUpKey;
	private static KeyBinding pitchDownKey;
	private static KeyBinding openConfigKey;
	private static CloudRenderMode previousCloudMode;
	private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "isometrycraft_client.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static boolean firstTick = true;

	@Override
	public void onInitializeClient() {
		loadConfig();
		toggleKey = registerKey("toggle_iso", GLFW.GLFW_KEY_I);
		lockYKey = registerKey("lock_y", GLFW.GLFW_KEY_Y);
		rotateLeftKey = registerKey("rotate_left", GLFW.GLFW_KEY_LEFT);
		rotateRightKey = registerKey("rotate_right", GLFW.GLFW_KEY_RIGHT);
		pitchUpKey = registerKey("pitch_up", GLFW.GLFW_KEY_UP);
		pitchDownKey = registerKey("pitch_down", GLFW.GLFW_KEY_DOWN);
		openConfigKey = registerKey("open_config", GLFW.GLFW_KEY_O);

		// Register HUD overlay rendering callback
		HudRenderCallback.EVENT.register(new IsometricHudRenderer());

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (firstTick) {
				firstTick = false;
				if (isIsometric) {
					client.mouse.unlockCursor();
					previousCloudMode = client.options.getCloudRenderMode().getValue();
					client.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
				}
			}
			if (isIsometric) {
				prevCameraYaw = cameraYaw;
				float deltaYaw = MathHelper.wrapDegrees(targetYaw - cameraYaw);
				cameraYaw = Math.abs(deltaYaw) > 0.01F ? cameraYaw + deltaYaw * 0.25F : targetYaw;
				prevCameraPitch = cameraPitch;
				float deltaPitch = targetPitch - cameraPitch;
				cameraPitch = Math.abs(deltaPitch) > 0.01F ? cameraPitch + deltaPitch * 0.25F : targetPitch;
			}

			while (openConfigKey.wasPressed()) {
				client.setScreen(new IsometryCraftConfigScreen(client.currentScreen));
			}

			while (toggleKey.wasPressed()) {
				isIsometric = !isIsometric;
				if (isIsometric) {
					client.mouse.unlockCursor();
					previousCloudMode = client.options.getCloudRenderMode().getValue();
					client.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
				} else {
					isYLocked = isMiddleMouseDown = false;
					client.mouse.lockCursor();
					if (previousCloudMode != null) {
						client.options.getCloudRenderMode().setValue(previousCloudMode);
					}
				}
				if (client.player != null) {
					client.player.sendMessage(Text.literal("View Mode: " + (isIsometric ? "§aIsometric" : "§cNormal")), true);
				}
				saveConfig();
			}

			while (lockYKey.wasPressed()) {
				if (isIsometric && client.player != null) {
					isYLocked = !isYLocked;
					if (isYLocked) {
						lockedYValue = client.player.getY() + client.player.getStandingEyeHeight();
					}
					String status = isYLocked ? "§aEnabled (§f" + String.format("%.2f", lockedYValue) + "§a)" : "§cDisabled";
					client.player.sendMessage(Text.literal("Camera Y-Lock: " + status), true);
					saveConfig();
				}
			}

			while (rotateLeftKey.wasPressed()) if (isIsometric) { targetYaw = MathHelper.wrapDegrees(targetYaw + 45.0F); saveConfig(); }
			while (rotateRightKey.wasPressed()) if (isIsometric) { targetYaw = MathHelper.wrapDegrees(targetYaw - 45.0F); saveConfig(); }

			while (pitchUpKey.wasPressed()) if (isIsometric && targetPitchIndex < PITCH_VALUES.length - 1) {
				targetPitch = PITCH_VALUES[++targetPitchIndex];
				saveConfig();
			}

			while (pitchDownKey.wasPressed()) if (isIsometric && targetPitchIndex > 0) {
				targetPitch = PITCH_VALUES[--targetPitchIndex];
				saveConfig();
			}
		});
	}

	private static KeyBinding registerKey(String path, int key) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.isometrycraft." + path, InputUtil.Type.KEYSYM, key, "category.isometrycraft.general"
		));
	}

	public static void loadConfig() {
		if (!CONFIG_FILE.exists()) {
			saveConfig();
			return;
		}
		try (FileReader reader = new FileReader(CONFIG_FILE)) {
			IsometryCraftConfig config = GSON.fromJson(reader, IsometryCraftConfig.class);
			if (config != null) {
				isIsometric = config.isIsometric;
				targetYaw = config.targetYaw;
				cameraYaw = config.cameraYaw;
				prevCameraYaw = config.prevCameraYaw;
				targetPitchIndex = (int) config.targetPitchIndex;
				targetPitch = config.targetPitch;
				cameraPitch = config.cameraPitch;
				prevCameraPitch = config.prevCameraPitch;
				isometricSize = config.isometricSize;
				isYLocked = config.isYLocked;
				lockedYValue = config.lockedYValue;
				showHud = config.showHud;
				try {
					previousCloudMode = config.previousCloudMode != null ? CloudRenderMode.valueOf(config.previousCloudMode) : CloudRenderMode.FANCY;
				} catch (IllegalArgumentException e) {
					previousCloudMode = CloudRenderMode.FANCY;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveConfig() {
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			IsometryCraftConfig config = new IsometryCraftConfig();
			config.isIsometric = isIsometric;
			config.targetYaw = targetYaw;
			config.cameraYaw = cameraYaw;
			config.prevCameraYaw = prevCameraYaw;
			config.targetPitchIndex = targetPitchIndex;
			config.targetPitch = targetPitch;
			config.cameraPitch = cameraPitch;
			config.prevCameraPitch = prevCameraPitch;
			config.isometricSize = isometricSize;
			config.isYLocked = isYLocked;
			config.lockedYValue = lockedYValue;
			config.showHud = showHud;
			config.previousCloudMode = previousCloudMode != null ? previousCloudMode.name() : "FANCY";
			GSON.toJson(config, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}