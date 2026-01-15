package com.mygdx.gravity.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;
import com.mygdx.gravity.GravityGame;
import com.mygdx.gravity.entities.*;
import com.mygdx.gravity.levels.LevelData;
import com.mygdx.gravity.levels.LevelManager;
import com.mygdx.gravity.mechanics.GravityDirection;
import com.mygdx.gravity.mechanics.GravityManager;
import com.mygdx.gravity.mechanics.TimeManager;
import com.mygdx.gravity.utils.Constants;
import com.mygdx.gravity.world.CollisionHandler;
import com.mygdx.gravity.world.PhysicsWorld;

/**
 * GameScreen - main game screen with level support, respawn and Android controls
 */
public class GameScreen extends ScreenAdapter {

    private final GravityGame game;
    private PhysicsWorld physics;
    private GravityManager gravity;
    private TimeManager time;
    private final Array<Entity> entities = new Array<>();
    private final LevelManager levels;
    private final int levelIndex;
    private boolean levelCompletePending = false;
    private int pendingNextLevel = -1;

    private Player player;
    private Vector2 spawnPoint;
    private boolean needsRespawn = false;
    private float respawnTimer = 0f;
    private final float RESPAWN_DELAY = 0.5f;

    private boolean isPaused = false;
    private Stage uiStage;
    private Stage pauseStage;

    // rendering helpers
    private OrthographicCamera camera;
    private Box2DDebugRenderer debugRenderer;
    
    // Textures
    private Texture playerTexture;
    private TextureRegion[] playerFrames;
    private Animation<TextureRegion> playerAnimation;
    private TextureRegion playerIdleFrame;
    private Texture platformTexture;
    private Texture spikeTexture;
    private Texture vanishTexture;
    private Texture impulseTexture;
    private Texture bgTexture;
    private Texture enemyTexture;
    private TextureRegion[] enemyFrames;
    private Animation<TextureRegion> enemyAnimation;
    private TextureRegion enemyIdleFrame;
    private Texture boxTexture;

    // Collision handler reference for respawn
    private CollisionHandler collisionHandler;

    public GameScreen(GravityGame game, int levelIndex) {
        this.game = game;
        this.levelIndex = levelIndex;

        levels = new LevelManager();
        
        // Load textures
        loadTextures();
        
        // Initialize world
        resetWorld();
        
        // Create level
        createLevel(levelIndex);

        // Initialize UI
        createUI();
    }

    private void createUI() {
        uiStage = new Stage(new FitViewport(Constants.V_WIDTH, Constants.V_HEIGHT), game.batch);
        pauseStage = new Stage(new FitViewport(Constants.V_WIDTH, Constants.V_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(uiStage);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = game.font;
        style.fontColor = Color.WHITE;

        // Кнопки движения слева
        TextButton leftBtn = new TextButton("LEFT", style);
        TextButton rightBtn = new TextButton("RIGHT", style);
        
        // Кнопка прыжка справа
        TextButton jumpBtn = new TextButton("JUMP", style);
        
        // Кнопка паузы в правом верхнем углу
        TextButton pauseBtn = new TextButton("PAUSE", style);

        leftBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (player != null && !isPaused) player.setMoveLeft(true);
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (player != null) player.setMoveLeft(false);
            }
        });

        rightBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (player != null && !isPaused) player.setMoveRight(true);
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (player != null) player.setMoveRight(false);
            }
        });

        jumpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (player != null && !isPaused) player.requestJump();
            }
        });

        pauseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
            }
        });

        // Левая часть: 2 кнопки движения внизу слева
        Table leftTable = new Table();
        leftTable.setFillParent(true);
        leftTable.bottom().left();
        leftTable.add(leftBtn).size(80, 80).pad(15);
        leftTable.add(rightBtn).size(80, 80).pad(15);
        
        // Правая часть: кнопка прыжка внизу справа
        Table rightTable = new Table();
        rightTable.setFillParent(true);
        rightTable.bottom().right();
        rightTable.add(jumpBtn).size(100, 80).pad(15);
        
        // Верхняя часть: кнопка паузы в правом верхнем углу
        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top().right();
        topTable.add(pauseBtn).size(60, 50).pad(10);

        uiStage.addActor(leftTable);
        uiStage.addActor(rightTable);
        uiStage.addActor(topTable);

        // Pause Menu
        Table pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.center();
        
        Label.LabelStyle labelStyle = new Label.LabelStyle(game.font, Color.YELLOW);
        pauseTable.add(new Label("PAUSED", labelStyle)).padBottom(30).row();

        TextButton resumeBtn = new TextButton("RESUME", style);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                togglePause();
            }
        });
        pauseTable.add(resumeBtn).size(200, 50).padBottom(10).row();

        TextButton menuBtn = new TextButton("MENU", style);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });
        pauseTable.add(menuBtn).size(200, 50).padBottom(10).row();

        pauseStage.addActor(pauseTable);
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            Gdx.input.setInputProcessor(pauseStage);
        } else {
            Gdx.input.setInputProcessor(uiStage);
        }
    }

    private void loadTextures() {
        // Load textures from AssetManager
        playerTexture = game.assets.get("player.png", Texture.class);
        
        // Try to load player as sprite sheet (horizontal strip with multiple frames)
        int textureWidth = playerTexture.getWidth();
        int textureHeight = playerTexture.getHeight();
        
        // If texture is wide (sprite sheet), split it into frames
        // Assume frames are arranged horizontally and are square or have consistent width
        if (textureWidth > textureHeight * 1.5f) {
            // This looks like a sprite sheet - try to detect frame size
            // Common sprite sheet: frames are usually square (height x height) or have consistent aspect ratio
            int frameSize = textureHeight; // Assume square frames (height x height)
            int frameCount = textureWidth / frameSize;
            
            // If frames don't divide evenly, try using texture height as frame height
            // and calculate frame width
            if (frameCount < 2 || textureWidth % frameSize != 0) {
                // Try to detect frame count by looking for common patterns
                // For now, assume square frames
                frameCount = Math.max(1, textureWidth / textureHeight);
                frameSize = textureHeight;
            }
            
            if (frameCount < 1) frameCount = 1;
            
            playerFrames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                int frameX = i * frameSize;
                if (frameX + frameSize <= textureWidth) {
                    playerFrames[i] = new TextureRegion(playerTexture, frameX, 0, frameSize, frameSize);
                } else {
                    // Last frame might be smaller
                    playerFrames[i] = new TextureRegion(playerTexture, frameX, 0, textureWidth - frameX, frameSize);
                }
            }
            
            // Create animation (0.1f seconds per frame for walking)
            playerAnimation = new Animation<TextureRegion>(0.1f, playerFrames);
            playerAnimation.setPlayMode(Animation.PlayMode.LOOP);
            playerIdleFrame = playerFrames[0]; // Use first frame as idle
        } else {
            // Single frame texture - use as is
            playerIdleFrame = new TextureRegion(playerTexture);
            playerFrames = new TextureRegion[]{playerIdleFrame};
            playerAnimation = new Animation<TextureRegion>(0.1f, playerFrames);
        }
        
        platformTexture = game.assets.get("platform.png", Texture.class);
        spikeTexture = game.assets.get("spike.png", Texture.class);
        impulseTexture = game.assets.get("impulse.png", Texture.class);
        bgTexture = game.assets.get("bg.png", Texture.class);
        // Load vanish texture if available, otherwise use platform texture as fallback
        if (game.assets.isLoaded("vanish.png")) {
            vanishTexture = game.assets.get("vanish.png", Texture.class);
        } else {
            vanishTexture = platformTexture; // Use platform texture as fallback
        }
        // Load enemy texture if available, otherwise use player texture as fallback
        if (game.assets.isLoaded("enemy.png")) {
            enemyTexture = game.assets.get("enemy.png", Texture.class);
        } else {
            enemyTexture = playerTexture; // Use player texture as fallback
        }
        
        // Load enemy animation frames (similar to player)
        int enemyTextureWidth = enemyTexture.getWidth();
        int enemyTextureHeight = enemyTexture.getHeight();
        
        if (enemyTextureWidth > enemyTextureHeight * 1.5f) {
            // Sprite sheet - split into frames
            int frameSize = enemyTextureHeight;
            int frameCount = enemyTextureWidth / frameSize;
            
            if (frameCount < 2 || enemyTextureWidth % frameSize != 0) {
                frameCount = Math.max(1, enemyTextureWidth / enemyTextureHeight);
                frameSize = enemyTextureHeight;
            }
            
            if (frameCount < 1) frameCount = 1;
            
            enemyFrames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                int frameX = i * frameSize;
                if (frameX + frameSize <= enemyTextureWidth) {
                    enemyFrames[i] = new TextureRegion(enemyTexture, frameX, 0, frameSize, frameSize);
                } else {
                    enemyFrames[i] = new TextureRegion(enemyTexture, frameX, 0, enemyTextureWidth - frameX, frameSize);
                }
            }
            
            // Create animation (0.15f seconds per frame for enemy - slightly slower than player)
            enemyAnimation = new Animation<TextureRegion>(0.15f, enemyFrames);
            enemyAnimation.setPlayMode(Animation.PlayMode.LOOP);
            enemyIdleFrame = enemyFrames[0]; // Use first frame as idle
        } else {
            // Single frame texture - use as is
            enemyIdleFrame = new TextureRegion(enemyTexture);
            enemyFrames = new TextureRegion[]{enemyIdleFrame};
            enemyAnimation = new Animation<TextureRegion>(0.15f, enemyFrames);
        }
        
        // Load box texture if available, otherwise use platform texture as fallback
        if (game.assets.isLoaded("box.png")) {
            boxTexture = game.assets.get("box.png", Texture.class);
        } else {
            boxTexture = platformTexture; // Use platform texture as fallback
        }
    }

    private void resetWorld() {
        // Dispose old world if exists
        if (physics != null) {
            physics.dispose();
        }

        // world / mechanics
        physics = new PhysicsWorld();
        gravity = new GravityManager(physics.getWorld());
        time = new TimeManager();

        // register collision listener
        collisionHandler = new CollisionHandler(gravity, time, this);
        physics.getWorld().setContactListener(collisionHandler);

        // camera in pixels
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.V_WIDTH, Constants.V_HEIGHT);
        camera.update();

        // debug renderer (Box2D)
        if (debugRenderer == null) {
            debugRenderer = new Box2DDebugRenderer();
        }
    }

    private void createLevel(int index) {
        LevelData levelData = levels.get(index);
        if (levelData == null) {
            Gdx.app.error("GameScreen", "Level " + index + " not found!");
            return;
        }

        // Clear entities
        entities.clear();

        // Store spawn point
        spawnPoint = new Vector2(levelData.playerSpawn);

        // Create player
        player = new Player(physics.getWorld(), gravity, spawnPoint);
        entities.add(player);

        // Create platforms
        for (LevelData.PlatformData platformData : levelData.platforms) {
            Entity platform;
            switch (platformData.type) {
                case VANISHING:
                    platform = new VanishingPlatform(physics.getWorld(), 
                        platformData.position, platformData.halfSize);
                    break;
                case IMPULSE:
                    platform = new ImpulsePlatform(physics.getWorld(), 
                        platformData.position, platformData.halfSize, 
                        platformData.impulse != null ? platformData.impulse : new Vector2(0, 0));
                    break;
                default:
                    platform = new Platform(physics.getWorld(), 
                        platformData.position, platformData.halfSize, platformData.friction);
                    break;
            }
            entities.add(platform);
        }

        // Create spikes
        for (LevelData.SpikeData spikeData : levelData.spikes) {
            Spike spike = new Spike(physics.getWorld(), spikeData.position, spikeData.halfSize);
            
            // Update spike fixture - use smaller hitbox and match visual size to hitbox
            if (spikeTexture != null) {
                // Get spike texture size
                float spikeWidth = spikeTexture.getWidth() / Constants.PPM;
                float spikeHeight = spikeTexture.getHeight() / Constants.PPM;
                
                // Use smaller hitbox: width ~0.4f (was 0.7f), height ~0.5f (was 0.25f * 2 = 0.5f)
                float hitboxWidth = Math.min(0.4f, spikeWidth * 0.6f); // Smaller width
                float hitboxHeight = Math.min(0.5f, spikeHeight * 0.9f); // Keep similar height
                
                // Remove old fixture and create new one with smaller hitbox
                com.badlogic.gdx.physics.box2d.Fixture oldFixture = spike.getBody().getFixtureList().get(0);
                spike.getBody().destroyFixture(oldFixture);
                
                com.badlogic.gdx.physics.box2d.PolygonShape shape = new com.badlogic.gdx.physics.box2d.PolygonShape();
                shape.setAsBox(hitboxWidth / 2f, hitboxHeight / 2f);
                
                com.badlogic.gdx.physics.box2d.FixtureDef fix = new com.badlogic.gdx.physics.box2d.FixtureDef();
                fix.shape = shape;
                fix.isSensor = false;
                fix.filter.categoryBits = Constants.CATEGORY_DANGER;
                fix.filter.maskBits = Constants.CATEGORY_PLAYER;
                spike.getBody().createFixture(fix).setUserData(spike);
                shape.dispose();
                
                // Update spike dimensions to match hitbox size (visual size = hitbox size)
                spike.width = hitboxWidth;
                spike.height = hitboxHeight;
            }
            
            entities.add(spike);
        }

        // Create gravity zones
        for (LevelData.GravityZoneData zoneData : levelData.gravityZones) {
            entities.add(new GravityZoneEntity(physics.getWorld(), 
                zoneData.position, zoneData.halfSize, zoneData.direction));
        }

        // Create time slow zones
        for (LevelData.TimeSlowZoneData tsData : levelData.timeSlowZones) {
            entities.add(new TimeSlowZoneEntity(physics.getWorld(),
                tsData.position, tsData.halfSize));
        }

        // Create enemies with patrol behavior
        if (levelData.enemies != null) {
            for (LevelData.EnemyData enemyData : levelData.enemies) {
                // Find the platform this enemy is on to set patrol points
                Vector2 patrolStart = null;
                Vector2 patrolEnd = null;
                
                for (LevelData.PlatformData platformData : levelData.platforms) {
                    float platformLeft = platformData.position.x - platformData.halfSize.x;
                    float platformRight = platformData.position.x + platformData.halfSize.x;
                    float platformTop = platformData.position.y + platformData.halfSize.y;
                    float platformBottom = platformData.position.y - platformData.halfSize.y;
                    
                    // Check if enemy is on this platform (within X bounds and slightly above platform)
                    if (enemyData.position.x >= platformLeft && enemyData.position.x <= platformRight &&
                        enemyData.position.y >= platformBottom && enemyData.position.y <= platformTop + 0.5f) {
                        // Set patrol points to platform edges with some margin
                        float margin = 0.3f; // Small margin from edges
                        patrolStart = new Vector2(platformLeft + margin, platformData.position.y);
                        patrolEnd = new Vector2(platformRight - margin, platformData.position.y);
                        break;
                    }
                }
                
                // If no platform found, use default patrol around spawn
                if (patrolStart == null || patrolEnd == null) {
                    float defaultPatrol = 2f;
                    patrolStart = new Vector2(enemyData.position.x - defaultPatrol, enemyData.position.y);
                    patrolEnd = new Vector2(enemyData.position.x + defaultPatrol, enemyData.position.y);
                }
                
                entities.add(new Enemy(physics.getWorld(), 
                    enemyData.position, enemyData.halfSize, patrolStart, patrolEnd));
            }
        }

        // Create boxes
        if (levelData.boxes != null) {
            for (LevelData.BoxData boxData : levelData.boxes) {
                entities.add(new Box(physics.getWorld(), 
                    boxData.position, boxData.halfSize));
            }
        }

        // Create finish zone
        entities.add(new FinishZone(physics.getWorld(), 
            levelData.finishPosition, new Vector2(1f, 1f)));

        // Reset gravity to default
        gravity.set(GravityDirection.DOWN);
    }

    @Override
    public void render(float delta) {
        // Update game logic if not paused
        if (!isPaused) {
            update(delta);
        }

        // Clear screen
        Gdx.gl.glClearColor(0.12f, 0.14f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera to follow player
        if (player != null && player.getBody() != null) {
            Vector2 playerPos = player.getBody().getPosition();
            camera.position.set(
                playerPos.x * Constants.PPM,
                playerPos.y * Constants.PPM,
                0
            );
            camera.update();
        }

        // Draw background
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        
        // Draw background texture (tiled)
        float bgX = camera.position.x - Constants.V_WIDTH / 2;
        float bgY = camera.position.y - Constants.V_HEIGHT / 2;
        float bgTileWidth = bgTexture.getWidth();
        float bgTileHeight = bgTexture.getHeight();
        for (float x = bgX; x < bgX + Constants.V_WIDTH; x += bgTileWidth) {
            for (float y = bgY; y < bgY + Constants.V_HEIGHT; y += bgTileHeight) {
                game.batch.draw(bgTexture, x, y, bgTileWidth, bgTileHeight);
            }
        }
        
        // Draw entities with textures
        for (Entity e : entities) {
            drawEntity(e);
        }
        
        game.batch.end();

        // Draw UI
        if (isPaused) {
            pauseStage.act(delta);
            pauseStage.draw();
        } else {
            uiStage.act(delta);
            uiStage.draw();
        }

        // Debug render (optional - comment out for release)
        // if (debugRenderer != null && physics != null && physics.getWorld() != null) {
        //     Matrix4 debugMatrix = camera.combined.cpy().scl(1f / Constants.PPM);
        //     debugRenderer.render(physics.getWorld(), debugMatrix);
        // }
    }

    private void drawEntity(Entity e) {
        Vector2 pos = e.getBody().getPosition();
        float x = pos.x * Constants.PPM;
        float y = pos.y * Constants.PPM;
        
        float width = 0;
        float height = 0;
        Texture texture = null;
        float alpha = 1f;

        if (e instanceof Player) {
            // Player texture with animation
            Player p = (Player) e;
            width = 0.8f * Constants.PPM; // 0.4f * 2
            height = 1.2f * Constants.PPM; // 0.6f * 2
            
            // Get current animation frame
            TextureRegion currentFrame;
            if (p.isMoving() && playerAnimation != null && playerFrames.length > 1) {
                currentFrame = playerAnimation.getKeyFrame(p.getAnimationTime(), true);
            } else {
                currentFrame = playerIdleFrame;
            }
            
            // Draw player with animation and facing direction
            // pos, x, y are already defined at the start of the method
            Color originalColor = game.batch.getColor();
            game.batch.setColor(1f, 1f, 1f, alpha);
            
            // Flip horizontally if facing left
            if (!p.isFacingRight()) {
                currentFrame.flip(true, false);
                game.batch.draw(currentFrame, x - width / 2, y - height / 2, width, height);
                currentFrame.flip(true, false); // Flip back
            } else {
                game.batch.draw(currentFrame, x - width / 2, y - height / 2, width, height);
            }
            
            game.batch.setColor(originalColor);
            return; // Skip normal texture drawing
        } else if (e instanceof Platform) {
            Platform p = (Platform) e;
            width = p.width * Constants.PPM;
            height = p.height * Constants.PPM;
            texture = platformTexture;
        } else if (e instanceof Spike) {
            Spike spike = (Spike) e;
            width = spike.width * Constants.PPM;
            height = spike.height * Constants.PPM;
            texture = spikeTexture;
        } else if (e instanceof VanishingPlatform) {
            VanishingPlatform vp = (VanishingPlatform) e;
            width = vp.width * Constants.PPM;
            height = vp.height * Constants.PPM;
            texture = vanishTexture;
            // Make it fade out when activated
            if (vp.isActivated()) {
                alpha = Math.max(0.3f, vp.getLifeTime() / 1.2f);
            }
        } else if (e instanceof ImpulsePlatform) {
            ImpulsePlatform ip = (ImpulsePlatform) e;
            width = ip.width * Constants.PPM;
            height = ip.height * Constants.PPM;
            texture = impulseTexture;
        } else if (e instanceof GravityZoneEntity) {
            // Gravity zones - tint blue and semi-transparent so player knows it's not solid
            GravityZoneEntity gz = (GravityZoneEntity) e;
            width = 2f * Constants.PPM;
            height = 6f * Constants.PPM;
            texture = platformTexture;
            alpha = 0.35f;
            game.batch.setColor(0.4f, 0.7f, 1f, alpha);
        } else if (e instanceof TimeSlowZoneEntity) {
            // Time slow zones - tint orange and semi-transparent
            TimeSlowZoneEntity tz = (TimeSlowZoneEntity) e;
            width = 2.5f * Constants.PPM;
            height = 2.5f * Constants.PPM;
            texture = platformTexture;
            alpha = 0.35f;
            game.batch.setColor(1f, 0.7f, 0.3f, alpha);
        } else if (e instanceof FinishZone) {
            // Finish zone - tint green and semi-transparent
            FinishZone fz = (FinishZone) e;
            width = 2f * Constants.PPM;
            height = 2f * Constants.PPM;
            texture = platformTexture;
            alpha = 0.55f;
            game.batch.setColor(0.4f, 1f, 0.4f, alpha);
        } else if (e instanceof Enemy) {
            Enemy enemy = (Enemy) e;
            if (!enemy.isDead()) {
                width = enemy.width * Constants.PPM;
                height = enemy.height * Constants.PPM;
                
                // Use enemy animation (similar to player)
                TextureRegion currentFrame;
                if (enemyAnimation != null && enemyFrames != null && enemyFrames.length > 1) {
                    currentFrame = enemyAnimation.getKeyFrame(enemy.getAnimationTime(), true);
                } else if (enemyIdleFrame != null) {
                    currentFrame = enemyIdleFrame;
                } else {
                    // Fallback to texture if animation not available
                    texture = enemyTexture;
                    currentFrame = null;
                }
                
                // Draw enemy with animation and facing direction
                if (currentFrame != null) {
                    Color originalColor = game.batch.getColor();
                    game.batch.setColor(1f, 1f, 1f, alpha);
                    
                    // Flip horizontally if moving left
                    boolean flipX = !enemy.isMovingRight();
                    if (flipX) {
                        currentFrame.flip(true, false);
                        game.batch.draw(currentFrame, x - width / 2, y - height / 2, width, height);
                        currentFrame.flip(true, false); // Flip back
                    } else {
                        game.batch.draw(currentFrame, x - width / 2, y - height / 2, width, height);
                    }
                    
                    game.batch.setColor(originalColor);
                    return; // Skip normal texture drawing
                }
                // If currentFrame is null, fall through to normal texture drawing
            } else {
                // Don't draw dead enemies
                return;
            }
        } else if (e instanceof Box) {
            Box box = (Box) e;
            width = box.width * Constants.PPM;
            height = box.height * Constants.PPM;
            texture = boxTexture;
        }

        // Draw texture for entity
        if (texture != null) {
            Color originalColor = game.batch.getColor();
            // If no tint was set above, keep white with alpha
            if (!(e instanceof GravityZoneEntity) && !(e instanceof TimeSlowZoneEntity) && !(e instanceof FinishZone)) {
                game.batch.setColor(1f, 1f, 1f, alpha);
            }
            game.batch.draw(texture, x - width / 2, y - height / 2, width, height);
            game.batch.setColor(originalColor);
        }
    }

    private void update(float delta) {
        // Handle back button (Android)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new com.mygdx.gravity.screens.MenuScreen(game));
            dispose();
            return;
        }

        // Handle respawn
        if (needsRespawn) {
            respawnTimer += delta;
            if (respawnTimer >= RESPAWN_DELAY) {
                respawnPlayer();
                needsRespawn = false;
                respawnTimer = 0f;
            }
        }

        // Check if player fell off the map
        if (player != null && player.getBody() != null) {
            Vector2 playerPos = player.getBody().getPosition();
            // If player falls below -5 meters, respawn
            if (playerPos.y < -5f) {
                triggerRespawn();
            }
        }

        // Update entities
        for (Entity e : entities) {
            e.update(delta);
        }

        // Step physics
        physics.step(delta, time.get());
        
        // Process enemy kills after physics step (prevents crash when destroying bodies during collision)
        for (Entity e : entities) {
            if (e instanceof Enemy) {
                ((Enemy) e).processKill();
            }
        }

        // Handle deferred level completion after physics step
        if (levelCompletePending) {
            levelCompletePending = false;
            onLevelCompleteInternal();
        }
    }

    public void triggerRespawn() {
        if (!needsRespawn) {
            needsRespawn = true;
            respawnTimer = 0f;
        }
    }

    private void respawnPlayer() {
        if (player == null || spawnPoint == null) return;

        // Reset player position and velocity
        player.getBody().setTransform(spawnPoint, 0);
        player.getBody().setLinearVelocity(0, 0);
        player.getBody().setAngularVelocity(0);
        
        // Reset gravity to default
        gravity.set(GravityDirection.DOWN);
    }

    public void queueLevelComplete() {
        if (!levelCompletePending) {
            pendingNextLevel = levelIndex + 1;
            levelCompletePending = true;
        }
    }

    private void onLevelCompleteInternal() {
        // Go to next level or show victory screen
        if (pendingNextLevel < levels.count()) {
            // Go to next level
            game.setScreen(new GameScreen(game, pendingNextLevel));
            dispose();
        } else {
            // All levels completed - return to menu
            game.setScreen(new com.mygdx.gravity.screens.MenuScreen(game));
            dispose();
        }
        pendingNextLevel = -1;
    }

    @Override
    public void dispose() {
        if (physics != null) {
            physics.dispose();
        }
        if (debugRenderer != null) {
            debugRenderer.dispose();
        }
        if (uiStage != null) {
            uiStage.dispose();
        }
        if (pauseStage != null) {
            pauseStage.dispose();
        }
    }
}
