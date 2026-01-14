package com.mygdx.gravity.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.gravity.utils.Constants;

public class Platform extends Entity {
    public float width;
    public float height;

    public Platform(World world, Vector2 pos, Vector2 halfSize, float friction) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(pos);

        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfSize.x, halfSize.y);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.friction = friction;
        fix.filter.categoryBits = Constants.CATEGORY_ENV;
        fix.filter.maskBits = Constants.CATEGORY_PLAYER;

        body.createFixture(fix).setUserData(this);
        shape.dispose();

        this.width = halfSize.x * 2;
        this.height = halfSize.y * 2;
    }

    @Override
    public void update(float delta) {}
}
