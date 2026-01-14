package com.mygdx.gravity.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.gravity.utils.Constants;

public class Enemy extends Entity {
    public float width;
    public float height;
    private boolean isDead = false;
    private float deathTimer = 0f;
    private static final float DEATH_DISPOSE_TIME = 0.5f;
    private boolean pendingKill = false; // Flag to kill enemy after physics step
    
    // Animation state
    private float animationTime = 0f;
    
    // Patrol AI
    private Vector2 startPos;
    private Vector2 endPos;
    private float fixedY; // Fixed Y coordinate to prevent flying
    private float patrolSpeed = 1.5f; // meters per second
    private boolean movingRight = true;
    private float patrolDistance = 3f; // default patrol distance if not set
    
    public Enemy(World world, Vector2 pos, Vector2 halfSize) {
        this(world, pos, halfSize, null, null);
    }
    
    public Enemy(World world, Vector2 pos, Vector2 halfSize, Vector2 patrolStart, Vector2 patrolEnd) {
        BodyDef def = new BodyDef();
        // Enemy should be kinematic so it doesn't fall but can still be moved if needed
        def.type = BodyDef.BodyType.KinematicBody;
        def.position.set(pos);
        def.fixedRotation = true;

        body = world.createBody(def);

        // Main body fixture (for collision with player - returns player to start)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfSize.x, halfSize.y);
        
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 1f;
        fix.friction = 0.2f;
        fix.filter.categoryBits = Constants.CATEGORY_DANGER;
        fix.filter.maskBits = (short)(Constants.CATEGORY_PLAYER | Constants.CATEGORY_ENV);
        
        body.createFixture(fix).setUserData(this);
        shape.dispose();
        
        // Top sensor for stomp detection (smaller, on top of enemy head)
        PolygonShape topSensorShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        float sensorHeight = 0.15f; // Slightly larger sensor for better detection
        vertices[0] = new Vector2(-halfSize.x * 0.9f, halfSize.y);
        vertices[1] = new Vector2(halfSize.x * 0.9f, halfSize.y);
        vertices[2] = new Vector2(halfSize.x * 0.9f, halfSize.y + sensorHeight);
        vertices[3] = new Vector2(-halfSize.x * 0.9f, halfSize.y + sensorHeight);
        topSensorShape.set(vertices);
        
        FixtureDef topSensorFix = new FixtureDef();
        topSensorFix.shape = topSensorShape;
        topSensorFix.isSensor = true;
        topSensorFix.filter.categoryBits = Constants.CATEGORY_DANGER;
        topSensorFix.filter.maskBits = Constants.CATEGORY_PLAYER;
        
        body.createFixture(topSensorFix).setUserData(new EnemyTopSensor(this));
        topSensorShape.dispose();
        
        this.width = halfSize.x * 2;
        this.height = halfSize.y * 2;
        
        // Store fixed Y coordinate
        this.fixedY = pos.y;
        
        // Set up patrol points
        if (patrolStart != null && patrolEnd != null) {
            this.startPos = new Vector2(patrolStart);
            this.endPos = new Vector2(patrolEnd);
        } else {
            // Default: patrol around spawn position
            this.startPos = new Vector2(pos.x - patrolDistance / 2, pos.y);
            this.endPos = new Vector2(pos.x + patrolDistance / 2, pos.y);
        }
        
        // Start moving towards end position
        movingRight = (endPos.x > startPos.x);
    }
    
    public void kill() {
        if (!isDead && !pendingKill) {
            pendingKill = true; // Mark for death, will be processed after physics step
        }
    }
    
    // Call this after physics step to actually kill the enemy
    public void processKill() {
        if (pendingKill && !isDead) {
            isDead = true;
            if (body != null && body.isActive()) {
                body.setActive(false);
            }
            pendingKill = false;
        }
    }
    
    public boolean isDead() {
        return isDead;
    }
    
    @Override
    public void update(float delta) {
        if (!isDead && !pendingKill && body != null && body.isActive()) {
            // Update animation time for alive enemies
            animationTime += delta;
            
            // Patrol AI - move from start to end and back
            Vector2 currentPos = body.getPosition();
            
            // Always fix Y coordinate to prevent flying
            float currentX = currentPos.x;
            if (Math.abs(currentPos.y - fixedY) > 0.01f) {
                body.setTransform(currentX, fixedY, 0);
                currentPos = body.getPosition();
                currentX = currentPos.x;
            }
            
            Vector2 targetPos = movingRight ? endPos : startPos;
            
            // Clamp current position to patrol bounds to prevent going outside platform
            float minX = Math.min(startPos.x, endPos.x);
            float maxX = Math.max(startPos.x, endPos.x);
            
            if (currentX < minX) {
                body.setTransform(minX, fixedY, 0);
                currentPos = body.getPosition();
                currentX = minX;
                movingRight = true;
                targetPos = endPos;
            } else if (currentX > maxX) {
                body.setTransform(maxX, fixedY, 0);
                currentPos = body.getPosition();
                currentX = maxX;
                movingRight = false;
                targetPos = startPos;
            }
            
            // Calculate direction to target
            float dx = targetPos.x - currentX;
            float distance = Math.abs(dx);
            
            // If reached target, turn around
            if (distance < 0.1f) {
                movingRight = !movingRight;
                targetPos = movingRight ? endPos : startPos;
                dx = targetPos.x - currentX;
            }
            
            // Move towards target
            float moveSpeed = patrolSpeed;
            float newX;
            if (Math.abs(dx) < moveSpeed * delta) {
                // Close enough, snap to target (but clamp to bounds)
                newX = Math.max(minX, Math.min(maxX, targetPos.x));
            } else {
                // Move towards target
                float moveX = (dx > 0 ? 1 : -1) * moveSpeed * delta;
                newX = currentX + moveX;
                // Clamp to bounds
                newX = Math.max(minX, Math.min(maxX, newX));
            }
            // Always fix Y coordinate and use setTransform for full control
            body.setTransform(newX, fixedY, 0);
        } else if (isDead) {
            deathTimer += delta;
            if (deathTimer >= DEATH_DISPOSE_TIME && body != null && body.isActive()) {
                body.setActive(false);
            }
        }
    }
    
    public float getAnimationTime() {
        return animationTime;
    }
    
    public boolean isMovingRight() {
        return movingRight;
    }
    
    // Inner class to mark top sensor for stomp detection
    public static class EnemyTopSensor {
        public final Enemy enemy;
        public EnemyTopSensor(Enemy enemy) {
            this.enemy = enemy;
        }
    }
}
