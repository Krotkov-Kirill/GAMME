package com.mygdx.gravity.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.gravity.utils.Constants;

public class Box extends Entity {
    public float width;
    public float height;

    public Box(World world, Vector2 pos, Vector2 halfSize) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody; // Dynamic so it can be pushed
        def.position.set(pos);

        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfSize.x, halfSize.y);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.density = 0.5f; // Lighter than player but still has weight
        fix.friction = 0.6f; // Good friction so it doesn't slide too much
        fix.filter.categoryBits = Constants.CATEGORY_ENV;
        fix.filter.maskBits = (short)(Constants.CATEGORY_PLAYER | Constants.CATEGORY_ENV | Constants.CATEGORY_DANGER);

        body.createFixture(fix).setUserData(this);
        shape.dispose();

        this.width = halfSize.x * 2;
        this.height = halfSize.y * 2;
        
        // Prevent rotation
        body.setFixedRotation(true);
    }

    @Override
    public void update(float delta) {
        // Apply damping to prevent boxes from sliding forever
        Vector2 vel = body.getLinearVelocity();
        vel.scl(0.95f); // Reduce velocity by 5% each frame
        body.setLinearVelocity(vel);
    }
}
