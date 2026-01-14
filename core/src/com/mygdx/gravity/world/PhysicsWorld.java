package com.mygdx.gravity.world;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.gravity.utils.Constants;

public class PhysicsWorld {
    private final World world;

    public PhysicsWorld() {
        world = new World(new Vector2(0, -Constants.WORLD_GRAVITY), true);
    }

    public World getWorld() { return world; }

    public void step(float delta, float timeScale) {
        world.step(delta * timeScale, 6, 2);
    }

    public void dispose() {
        world.dispose();
    }
}
