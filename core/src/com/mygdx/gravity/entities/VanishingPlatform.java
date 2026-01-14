package com.mygdx.gravity.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class VanishingPlatform extends Platform {
    private float lifeTime = 1.2f;
    private boolean activated = false;

    public VanishingPlatform(World world, Vector2 pos, Vector2 halfSize) {
        super(world, pos, halfSize, 0.3f);
    }

    @Override
    public void update(float delta) {
        if (activated) {
            lifeTime -= delta;
            if (lifeTime <= 0 && body.isActive()) {
                body.setActive(false);
            }
        }
    }

    public void activate() {
        activated = true;
    }
    
    public boolean isActivated() {
        return activated;
    }
    
    public float getLifeTime() {
        return lifeTime;
    }
}
