package com.mygdx.gravity.utils;

public final class Constants {
    private Constants() {}

    public static final float WORLD_GRAVITY = 9.8f;
    // Увеличили силу прыжка, чтобы игрок мог перепрыгивать более широкие ямы на уровнях 2–5
    public static final float PLAYER_JUMP_FORCE = 7.0f;
    public static final float TIME_SLOW_SCALE = 0.5f;

    public static final float PPM = 100f; // pixels per meter
    public static final int V_WIDTH = 800;
    public static final int V_HEIGHT = 480;

    public static final short CATEGORY_PLAYER = 0x0001;
    public static final short CATEGORY_ENV = 0x0002;
    public static final short CATEGORY_DANGER = 0x0004;
    public static final short CATEGORY_SENSOR = 0x0008;
}
