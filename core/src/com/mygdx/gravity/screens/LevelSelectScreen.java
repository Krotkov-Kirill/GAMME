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
import com.mygdx.gravity.levels.LevelManager;
import com.mygdx.gravity.utils.Constants;

public class LevelSelectScreen extends ScreenAdapter {
    private final GravityGame game;
    private final LevelManager levelManager;
    private OrthographicCamera camera;
    private BitmapFont font;
    private BitmapFont titleFont;
    
    private final float BUTTON_WIDTH = 200;
    private final float BUTTON_HEIGHT = 60;
    private final float SPACING = 40;
    
    private int selectedLevel = 0;

    public LevelSelectScreen(GravityGame game) {
        this.game = game;
        this.levelManager = new LevelManager();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.V_WIDTH, Constants.V_HEIGHT);
        
        font = game.font;
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.0f);
    }

    @Override
    public void render(float delta) {
        handleInput();
        
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        
        // Draw title
        String title = "Select Level";
        GlyphLayout titleLayout = new GlyphLayout(titleFont, title);
        titleFont.draw(game.batch, title, (Constants.V_WIDTH - titleLayout.width) / 2f, Constants.V_HEIGHT - 50);
        
        // Draw level buttons (5 levels)
        int levels = levelManager.count();
        float startX = (Constants.V_WIDTH - (BUTTON_WIDTH * 3 + SPACING * 2)) / 2f;
        float startY = Constants.V_HEIGHT - 150;
        
        for (int i = 0; i < levels; i++) {
            int row = i / 3;
            int col = i % 3;
            float x = startX + col * (BUTTON_WIDTH + SPACING);
            float y = startY - row * (BUTTON_HEIGHT + SPACING);
            
            drawLevelButton("Level " + (i + 1), x + BUTTON_WIDTH / 2f, y + BUTTON_HEIGHT / 2f, selectedLevel == i);
        }
        
        // Back button
        drawLevelButton("Back", Constants.V_WIDTH / 2f, 50, selectedLevel == levels);
        
        game.batch.end();
    }
    
    private void drawLevelButton(String text, float x, float y, boolean selected) {
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x - layout.width / 2f;
        float textY = y + layout.height / 2f;
        
        if (selected) {
            font.setColor(1f, 1f, 0.5f, 1f);
        } else {
            font.setColor(1f, 1f, 1f, 1f);
        }
        
        font.draw(game.batch, text, textX, textY);
    }
    
    private void handleInput() {
        int levels = levelManager.count();
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            selectedLevel = (selectedLevel - 1 + (levels + 1)) % (levels + 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            selectedLevel = (selectedLevel + 1) % (levels + 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            if (selectedLevel >= 3) selectedLevel -= 3;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            if (selectedLevel < levels - 2) selectedLevel = Math.min(selectedLevel + 3, levels);
            else selectedLevel = levels;
        }
        
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            
            // Check level buttons
            float startX = (Constants.V_WIDTH - (BUTTON_WIDTH * 3 + SPACING * 2)) / 2f;
            float startY = Constants.V_HEIGHT - 150;
            for (int i = 0; i < levels; i++) {
                int row = i / 3;
                int col = i % 3;
                float x = startX + col * (BUTTON_WIDTH + SPACING);
                float y = startY - row * (BUTTON_HEIGHT + SPACING);
                
                if (touchPos.x >= x && touchPos.x <= x + BUTTON_WIDTH &&
                    touchPos.y >= y && touchPos.y <= y + BUTTON_HEIGHT) {
                    startLevel(i);
                    return;
                }
            }
            
            // Check back button
            if (touchPos.x >= Constants.V_WIDTH / 2f - BUTTON_WIDTH / 2f &&
                touchPos.x <= Constants.V_WIDTH / 2f + BUTTON_WIDTH / 2f &&
                touchPos.y >= 50 - BUTTON_HEIGHT / 2f &&
                touchPos.y <= 50 + BUTTON_HEIGHT / 2f) {
                goBack();
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (selectedLevel < levels) {
                startLevel(selectedLevel);
            } else {
                goBack();
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            goBack();
        }
    }
    
    private void startLevel(int index) {
        game.setScreen(new GameScreen(game, index));
        dispose();
    }
    
    private void goBack() {
        game.setScreen(new MenuScreen(game));
        dispose();
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
    }
}
