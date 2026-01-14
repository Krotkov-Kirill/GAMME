package com.mygdx.gravity.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.gravity.mechanics.GravityDirection;
import com.mygdx.gravity.utils.Constants;

public class GravityZoneEntity extends Entity {
    private final GravityDirection direction;
    
    public GravityZoneEntity(World world, Vector2 pos, Vector2 halfSize, GravityDirection direction) {
        this.direction = direction;
        
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(pos);
        body = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfSize.x, halfSize.y);

        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.isSensor = true;
        fix.filter.categoryBits = Constants.CATEGORY_SENSOR;
        fix.filter.maskBits = Constants.CATEGORY_PLAYER;

        body.createFixture(fix).setUserData(this);
        shape.dispose();
    }
    
    public GravityDirection getDirection() {
        return direction;
    }

    @Override
    public void update(float delta) {}
}
