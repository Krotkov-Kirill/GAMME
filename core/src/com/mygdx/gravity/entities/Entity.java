package com.mygdx.gravity.entities;

import com.badlogic.gdx.physics.box2d.Body;

public abstract class Entity {
    protected Body body;
    public abstract void update(float delta);
    public Body getBody() { return body; }
}
