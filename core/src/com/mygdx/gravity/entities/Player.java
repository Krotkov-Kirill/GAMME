package com.mygdx.gravity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.gravity.mechanics.GravityManager;
import com.mygdx.gravity.utils.Constants;
import com.mygdx.gravity.mechanics.GravityDirection;


public class Player extends Entity {
    private final GravityManager gravity;
    private boolean canJump = false;
    
    // External control flags for UI
    private boolean moveLeftRequested = false;
    private boolean moveRightRequested = false;
    private boolean jumpRequested = false;
    
    // Animation state
    private float animationTime = 0f;
    private boolean isMoving = false;
    private boolean facingRight = true;

    public Player(World world, GravityManager gravity, Vector2 spawn) {
        this.gravity = gravity;

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(spawn);

        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.6f);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 1f;
        fix.friction = 0.2f;
        fix.filter.categoryBits = Constants.CATEGORY_PLAYER;
        fix.filter.maskBits = (short)(Constants.CATEGORY_ENV | Constants.CATEGORY_DANGER | Constants.CATEGORY_SENSOR);

        body.createFixture(fix).setUserData(this);
        shape.dispose();
        
        // Lock rotation to prevent sideways falling
        body.setFixedRotation(true);
    }

    @Override
    public void update(float delta) {
        handleInput();
        animationTime += delta;
    }

    private void handleInput() {
        float move = 0f;
        boolean jumpPressed = false;
        
        // Keyboard input
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A) || moveLeftRequested) {
            move -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) || moveRightRequested) {
            move += 1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || jumpRequested) {
            jumpPressed = true;
            jumpRequested = false; // Reset jump request
        }
        
        // Apply horizontal movement
        Vector2 vel = body.getLinearVelocity();
        GravityDirection currentGravity = gravity.get();
        
        // Update animation state
        isMoving = Math.abs(move) > 0.1f;
        if (move > 0.1f) facingRight = true;
        if (move < -0.1f) facingRight = false;
        
        // Movement direction depends on current gravity
        if (currentGravity == GravityDirection.UP || currentGravity == GravityDirection.DOWN) {
            body.setLinearVelocity(move * 3f, vel.y);
        } else {
            body.setLinearVelocity(vel.x, move * 3f);
        }

        // Handle jump
        if (jumpPressed && canJump) {
            jump();
            canJump = false;
        }
    }

    private void jump() {
        Vector2 impulse;

        switch (gravity.get()) {
            case DOWN:
                impulse = new Vector2(0, Constants.PLAYER_JUMP_FORCE);
                break;
            case UP:
                impulse = new Vector2(0, -Constants.PLAYER_JUMP_FORCE);
                break;
            case LEFT:
                impulse = new Vector2(Constants.PLAYER_JUMP_FORCE, 0);
                break;
            case RIGHT:
                impulse = new Vector2(-Constants.PLAYER_JUMP_FORCE, 0);
                break;
            default:
                impulse = new Vector2(0, Constants.PLAYER_JUMP_FORCE);
                break;
        }

        body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
    }

    public void setMoveLeft(boolean move) { this.moveLeftRequested = move; }
    public void setMoveRight(boolean move) { this.moveRightRequested = move; }
    public void requestJump() { this.jumpRequested = true; }

    public void allowJump() { canJump = true; }
    public void forbidJump() { canJump = false; }
    
    public float getAnimationTime() { return animationTime; }
    public boolean isMoving() { return isMoving; }
    public boolean isFacingRight() { return facingRight; }
}
