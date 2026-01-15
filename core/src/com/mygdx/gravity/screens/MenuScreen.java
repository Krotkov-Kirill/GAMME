package com.mygdx.gravity.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.gravity.GravityGame;
import com.mygdx.gravity.utils.Constants;

public class MenuScreen extends ScreenAdapter {
    private final GravityGame game;
    private OrthographicCamera camera;
    private BitmapFont font;
    private BitmapFont titleFont;
    
    private float playButtonY;
    private float settingsButtonY;
    private float exitButtonY;
    private float titleY;
    
    private final float BUTTON_WIDTH = 300;
    private final float BUTTON_HEIGHT = 60;
    private final float BUTTON_SPACING = 80;
    
    private int selectedButton = 0; // 0 - Play, 1 - Settings, 2 - Exit

    public MenuScreen(GravityGame game) { 
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.V_WIDTH, Constants.V_HEIGHT);
        
        font = game.font;
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.0f);
        
        // Calculate button positions
        float centerX = Constants.V_WIDTH / 2f;
        float centerY = Constants.V_HEIGHT / 2f;
        
        titleY = centerY + 150;
        playButtonY = centerY + 40;
        settingsButtonY = centerY - 40;
        exitButtonY = centerY - 120;
    }

    @Override
    public void render(float delta) {
        // Handle input
        handleInput();
        
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        
        game.batch.begin();
        
        // Draw title
        String title = "Pixel Jump";
        GlyphLayout titleLayout = new GlyphLayout(titleFont, title);
        titleFont.draw(game.batch, title, 
            (Constants.V_WIDTH - titleLayout.width) / 2f, 
            titleY);
        
        // Draw buttons
        drawButton("Play", Constants.V_WIDTH / 2f, playButtonY, selectedButton == 0);
        drawButton("Exit", Constants.V_WIDTH / 2f, exitButtonY, selectedButton == 2);
        
        game.batch.end();
    }
    
    private void drawButton(String text, float x, float y, boolean selected) {
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x - layout.width / 2f;
        float textY = y + layout.height / 2f;
        
        // Draw button background (simple rectangle)
        if (selected) {
            font.setColor(1f, 1f, 0.5f, 1f); // Yellow when selected
        } else {
            font.setColor(1f, 1f, 1f, 1f); // White when not selected
        }
        
        font.draw(game.batch, text, textX, textY);
    }
    
    private void handleInput() {
        // Keyboard navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedButton = (selectedButton - 1 + 3) % 3;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedButton = (selectedButton + 1) % 3;
        }
        
        // Touch input for Android
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            
            // Check which button was touched
            if (touchPos.y >= playButtonY - BUTTON_HEIGHT / 2 && 
                touchPos.y <= playButtonY + BUTTON_HEIGHT / 2 &&
                touchPos.x >= Constants.V_WIDTH / 2f - BUTTON_WIDTH / 2 &&
                touchPos.x <= Constants.V_WIDTH / 2f + BUTTON_WIDTH / 2) {
                startGame();
            } else if (touchPos.y >= settingsButtonY - BUTTON_HEIGHT / 2 && 
                       touchPos.y <= settingsButtonY + BUTTON_HEIGHT / 2 &&
                       touchPos.x >= Constants.V_WIDTH / 2f - BUTTON_WIDTH / 2 &&
                       touchPos.x <= Constants.V_WIDTH / 2f + BUTTON_WIDTH / 2) {
                openSettings();
            } else if (touchPos.y >= exitButtonY - BUTTON_HEIGHT / 2 && 
                       touchPos.y <= exitButtonY + BUTTON_HEIGHT / 2 &&
                       touchPos.x >= Constants.V_WIDTH / 2f - BUTTON_WIDTH / 2 &&
                       touchPos.x <= Constants.V_WIDTH / 2f + BUTTON_WIDTH / 2) {
                exitGame();
            }
        }
        
        // Enter/Space to select
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (selectedButton == 0) {
                startGame();
            } else if (selectedButton == 1) {
                openSettings();
            } else if (selectedButton == 2) {
                exitGame();
            }
        }
    }
    
    private void startGame() {
        game.setScreen(new LevelSelectScreen(game));
        dispose();
    }
    
    private void openSettings() {
        // Placeholder for settings
        // Can add SettingsScreen in the future
        System.out.println("Settings (placeholder)");
    }
    
    private void exitGame() {
        Gdx.app.exit();
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
        }
    }
}
