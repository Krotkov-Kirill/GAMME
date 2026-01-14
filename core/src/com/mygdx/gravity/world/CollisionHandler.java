package com.mygdx.gravity.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.gravity.entities.*;
import com.mygdx.gravity.mechanics.GravityManager;
import com.mygdx.gravity.mechanics.TimeManager;
import com.mygdx.gravity.screens.GameScreen;

public class CollisionHandler implements ContactListener {

    private final GravityManager gravity;
    private final TimeManager time;
    private GameScreen gameScreen;

    public CollisionHandler(GravityManager gravity, TimeManager time) {
        this.gravity = gravity;
        this.time = time;
    }

    public CollisionHandler(GravityManager gravity, TimeManager time, GameScreen gameScreen) {
        this.gravity = gravity;
        this.time = time;
        this.gameScreen = gameScreen;
    }

    @Override
    public void beginContact(Contact contact) {
        if (contact == null) {
            return;
        }
        
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        
        if (a == null || b == null) {
            return;
        }

        Object ua = a.getUserData();
        Object ub = b.getUserData();
        
        if (ua == null || ub == null) {
            return;
        }

        // Check for enemy head sensor collision first (before body collision)
        // This prevents body collision from triggering when player stomps enemy
        if (ua instanceof Player && ub instanceof Enemy.EnemyTopSensor) {
            handleEnemyStomp((Player) ua, (Enemy.EnemyTopSensor) ub);
            return; // Don't process other collisions for this contact
        }
        if (ub instanceof Player && ua instanceof Enemy.EnemyTopSensor) {
            handleEnemyStomp((Player) ub, (Enemy.EnemyTopSensor) ua);
            return; // Don't process other collisions for this contact
        }

        handleBegin(ua, ub);
        handleBegin(ub, ua);
    }
    
    private void handleEnemyStomp(Player player, Enemy.EnemyTopSensor sensor) {
        if (player == null || sensor == null || sensor.enemy == null) {
            return;
        }
        
        Enemy enemy = sensor.enemy;
        // Check if enemy is dead, pending kill, or body is inactive
        if (enemy.isDead() || enemy.getBody() == null || !enemy.getBody().isActive()) {
            return;
        }
        
        if (player.getBody() == null || !player.getBody().isActive()) {
            return;
        }
        
        // Check if player is falling onto enemy head (stomp)
        Vector2 playerVel = player.getBody().getLinearVelocity();
        Vector2 playerPos = player.getBody().getPosition();
        Vector2 enemyPos = enemy.getBody().getPosition();
        
        // Player must be above enemy and falling (or moving downward)
        boolean isAbove = playerPos.y > enemyPos.y;
        // Check if player is moving downward (falling)
        boolean isFalling = playerVel.y < 0.3f;
        
        if (isAbove && isFalling) {
            // Stomp! Kill enemy (mark for death, will be processed after physics step)
            enemy.kill();
            // Give player a small bounce upward
            player.getBody().applyLinearImpulse(new Vector2(0, 4f), player.getBody().getWorldCenter(), true);
        }
    }

    private void handleBegin(Object primary, Object other) {
        if (primary instanceof Player) {
            Player player = (Player) primary;

            if (other instanceof Platform) {
                player.allowJump();
                if (other instanceof VanishingPlatform) {
                    ((VanishingPlatform) other).activate();
                }
                if (other instanceof ImpulsePlatform) {
                    ((ImpulsePlatform) other).applyImpulseTo(player);
                }
            }

            if (other instanceof Spike) {
                // Trigger respawn when player hits spike
                if (gameScreen != null) {
                    gameScreen.triggerRespawn();
                }
            }

            // Enemy body collision - return player to start of level
            // Note: Head sensor collisions are handled separately in beginContact
            if (other instanceof Enemy) {
                Enemy enemy = (Enemy) other;
                if (enemy != null && !enemy.isDead() && enemy.getBody() != null && enemy.getBody().isActive()) {
                    // Player touched enemy body (not head) - return to start
                    if (gameScreen != null) {
                        gameScreen.triggerRespawn();
                    }
                }
            }

            if (other instanceof com.mygdx.gravity.entities.GravityZoneEntity) {
                com.mygdx.gravity.entities.GravityZoneEntity zone = 
                    (com.mygdx.gravity.entities.GravityZoneEntity) other;
                gravity.set(zone.getDirection());
            }

            if (other instanceof com.mygdx.gravity.entities.TimeSlowZoneEntity) {
                time.setSlow(true);
            }

            if (other instanceof com.mygdx.gravity.entities.FinishZone) {
                // Level complete - go to next level or show victory screen
                if (gameScreen != null) {
                    gameScreen.queueLevelComplete();
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        Object ua = a.getUserData();
        Object ub = b.getUserData();

        handleEnd(ua, ub);
        handleEnd(ub, ua);
    }

    private void handleEnd(Object primary, Object other) {
        if (primary instanceof Player) {
            Player player = (Player) primary;
            if (other instanceof Platform) {
                player.forbidJump();
            }

            if (other instanceof com.mygdx.gravity.entities.TimeSlowZoneEntity) {
                time.setSlow(false);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // Fix player floating on platforms
        Fixture a = contact.getFixtureA();
        Fixture b = contact.getFixtureB();
        
        if (a == null || b == null) {
            return;
        }
        
        Object ua = a.getUserData();
        Object ub = b.getUserData();
        
        if (ua == null || ub == null) {
            return;
        }
        
        // Check if player is in contact with a platform
        if (ua instanceof Player && ub instanceof Platform) {
            adjustPlayerOnPlatform((Player) ua, (Platform) ub, contact);
        } else if (ub instanceof Player && ua instanceof Platform) {
            adjustPlayerOnPlatform((Player) ub, (Platform) ua, contact);
        }
    }
    
    private void adjustPlayerOnPlatform(Player player, Platform platform, Contact contact) {
        if (player.getBody() == null || platform.getBody() == null) {
            return;
        }
        
        Vector2 playerPos = player.getBody().getPosition();
        Vector2 platformPos = platform.getBody().getPosition();
        
        // Get platform bounds
        float platformTop = platformPos.y + platform.height / 2f;
        
        // Player's bottom should be at platform's top
        float playerBottom = playerPos.y - 0.6f; // 0.6f is player's half-height
        
        // Only adjust if player is standing on platform (not jumping up)
        Vector2 playerVel = player.getBody().getLinearVelocity();
        
        // Check if player is on top of platform and not moving upward
        if (playerBottom >= platformTop - 0.05f && playerBottom <= platformTop + 0.15f && playerVel.y <= 0.2f) {
            // Adjust player position to sit properly on platform (small correction only)
            float targetY = platformTop + 0.6f;
            float diff = targetY - playerPos.y;
            
            // Only make small corrections to prevent floating, but allow normal physics
            if (diff > 0.02f && diff < 0.1f) {
                player.getBody().setTransform(playerPos.x, targetY, 0);
                // Stop small downward velocity when landing
                if (playerVel.y < 0 && playerVel.y > -0.5f) {
                    player.getBody().setLinearVelocity(playerVel.x, 0);
                }
            }
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { }
}
