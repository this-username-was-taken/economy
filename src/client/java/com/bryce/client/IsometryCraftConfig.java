package com.bryce.client;

public class IsometryCraftConfig {
    public boolean isIsometric = false;
    public float targetYaw = 45.0F;
    public float cameraYaw = 45.0F;
    public float cameraPitch;
    public float targetPitchIndex = 1;
    public float targetPitch = (float) Math.toDegrees(Math.atan(1.0 / Math.sqrt(2.0)));
    public float prevCameraYaw = 45.0F;
    public float prevCameraPitch = (float) Math.toDegrees(Math.atan(1.0 / Math.sqrt(2.0)));
    public float isometricSize = 15.0f;
    public boolean isYLocked = false;
    public double lockedYValue = 70.0;
    public String previousCloudMode = "FANCY";
    public boolean showHud = true;
}