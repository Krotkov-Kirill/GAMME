package com.mygdx.gravity;

import com.mygdx.gravity.screens.MenuScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;


public class GravityGame extends Game {
    public SpriteBatch batch;
    public AssetManager assets;
    public BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new AssetManager();
        // load basic textures (place these PNGs into assets/)
        assets.load("player.png", Texture.class);
        assets.load("platform.png", Texture.class);
        assets.load("impulse.png", Texture.class);
        assets.load("spike.png", Texture.class);
        assets.load("bg.png", Texture.class);
        // Load vanish texture only if it exists and is valid
        FileHandle vanishFile = Gdx.files.internal("vanish.png");
        if (vanishFile.exists() && isValidImageFile(vanishFile)) {
            assets.load("vanish.png", Texture.class);
        }
        // Load enemy texture only if it exists and is valid
        FileHandle enemyFile = Gdx.files.internal("enemy.png");
        if (enemyFile.exists() && isValidImageFile(enemyFile)) {
            assets.load("enemy.png", Texture.class);
        }
        // Load box texture only if it exists and is valid
        FileHandle boxFile = Gdx.files.internal("box.png");
        if (boxFile.exists() && isValidImageFile(boxFile)) {
            assets.load("box.png", Texture.class);
        }
        assets.finishLoading();

        // Try to load pixel font from .fnt file (if font.png exists, font.fnt should also exist)
        FileHandle fontFile = Gdx.files.internal("font.fnt");
        FileHandle fontImageFile = Gdx.files.internal("font.png");
        if (fontFile.exists() && fontImageFile.exists()) {
            // Load bitmap font from .fnt file and .png image
            font = new BitmapFont(fontFile, fontImageFile, false);
        } else {
            // Create default font
            font = new BitmapFont();
        }

        setScreen(new MenuScreen(this));

    }

    /**
     * Validates that a file is a valid image file by attempting to create a Pixmap from it.
     * Returns true if the file can be loaded as an image, false otherwise.
     */
    private boolean isValidImageFile(FileHandle file) {
        try {
            Pixmap pixmap = new Pixmap(file);
            pixmap.dispose();
            return true;
        } catch (Exception e) {
            // File exists but is corrupted or not a valid image
            return false;
        }
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (font != null) font.dispose();
        assets.dispose();
        batch.dispose();
    }
}
