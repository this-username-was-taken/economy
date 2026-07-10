package com.bryce.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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

public class EconomyClient implements ClientModInitializer {
	public static boolean isIsometric = false;

	public static float targetYaw = 45.0F;
	public static float cameraYaw = 45.0F;
	public static float prevCameraYaw = 45.0F;

	public static float isometricSize = 15.0f;

	public static boolean isMiddleMouseDown = false;
	public static double lastMouseX = 0.0;
	public static double lastMouseY = 0.0;

	public static boolean isYLocked = false;
	public static double lockedYValue = 70.0;

	private static KeyBinding toggleKey;
	private static KeyBinding lockYKey;
	private static KeyBinding rotateLeftKey;
	private static KeyBinding rotateRightKey;
	private static CloudRenderMode previousCloudMode;

	private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "economy_client.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static boolean firstTick = true;

	@Override
	public void onInitializeClient() {
		loadConfig();

		// Mode toggle key (I)
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.economy.toggle_iso", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "category.economy.general"
		));
		// Y-Axis Lock toggle key (Y)
		lockYKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.economy.lock_y", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "category.economy.general"
		));
		// Rotate Left key (Left Arrow Key)
		rotateLeftKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.economy.rotate_left", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, "category.economy.general"
		));
		// Rotate Right key (Right Arrow Key)
		rotateRightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.economy.rotate_right", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "category.economy.general"
		));

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

				if (Math.abs(deltaYaw) > 0.01F) {
					cameraYaw += deltaYaw * 0.25F;
				} else {
					cameraYaw = targetYaw;
				}
			}
			while (toggleKey.wasPressed()) {
				isIsometric = !isIsometric;
				if (!isIsometric) {
					isYLocked = false;
					isMiddleMouseDown = false;
					client.mouse.lockCursor();
				} else {
					client.mouse.unlockCursor();
				}
				if (isIsometric) {
					previousCloudMode = client.options.getCloudRenderMode().getValue();
					client.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
				} else if (previousCloudMode != null) {
					client.options.getCloudRenderMode().setValue(previousCloudMode);
				}

				if (client.player != null) {
					String status = isIsometric ? "§aIsometric" : "§cNormal";
					client.player.sendMessage(Text.literal("View Mode: " + status), true);
				}
				saveConfig();
			}

			while (lockYKey.wasPressed()) {
				if (isIsometric && client.player != null) {
					isYLocked = !isYLocked;
					if (isYLocked) {
						lockedYValue = client.player.getY() + client.player.getStandingEyeHeight();
						client.player.sendMessage(Text.literal("Camera Y-Lock: §aEnabled (§f" + String.format("%.2f", lockedYValue) + "§a)"), true);
					} else {
						client.player.sendMessage(Text.literal("Camera Y-Lock: §cDisabled"), true);
					}
					saveConfig();
				}
			}

			while (rotateLeftKey.wasPressed()) {
				if (isIsometric) {
					targetYaw = MathHelper.wrapDegrees(targetYaw + 90.0F);
					saveConfig();
				}
			}

			while (rotateRightKey.wasPressed()) {
				if (isIsometric) {
					targetYaw = MathHelper.wrapDegrees(targetYaw - 90.0F);
					saveConfig();
				}
			}
		});
	}

	public static void loadConfig() {
		if (!CONFIG_FILE.exists()) {
			saveConfig();
			return;
		}
		try (FileReader reader = new FileReader(CONFIG_FILE)) {
			EconomyConfig config = GSON.fromJson(reader, EconomyConfig.class);
			if (config != null) {
				isIsometric = config.isIsometric;
				targetYaw = config.targetYaw;
				cameraYaw = config.cameraYaw;
				prevCameraYaw = config.prevCameraYaw;
				isometricSize = config.isometricSize;
				isYLocked = config.isYLocked;
				lockedYValue = config.lockedYValue;
				if (config.previousCloudMode != null) {
					try {
						previousCloudMode = CloudRenderMode.valueOf(config.previousCloudMode);
					} catch (IllegalArgumentException e) {
						previousCloudMode = CloudRenderMode.FANCY;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveConfig() {
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			EconomyConfig config = new EconomyConfig();
			config.isIsometric = isIsometric;
			config.targetYaw = targetYaw;
			config.cameraYaw = cameraYaw;
			config.prevCameraYaw = prevCameraYaw;
			config.isometricSize = isometricSize;
			config.isYLocked = isYLocked;
			config.lockedYValue = lockedYValue;
			config.previousCloudMode = previousCloudMode != null ? previousCloudMode.name() : "FANCY";
			GSON.toJson(config, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}