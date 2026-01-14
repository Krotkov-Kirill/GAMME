package com.mygdx.gravity.mechanics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.gravity.utils.Constants;

public class GravityManager {
    private final World world;
    private GravityDirection current = GravityDirection.DOWN;

    public GravityManager(World world) {
        this.world = world;
        apply();
    }

    public void set(GravityDirection dir) {
        current = dir;
        apply();
    }

    private void apply() {
        switch (current) {
            case DOWN:
                world.setGravity(new Vector2(0, -Constants.WORLD_GRAVITY));
                break;
            case UP:
                world.setGravity(new Vector2(0, Constants.WORLD_GRAVITY));
                break;
            case LEFT:
                world.setGravity(new Vector2(-Constants.WORLD_GRAVITY, 0));
                break;
            case RIGHT:
                world.setGravity(new Vector2(Constants.WORLD_GRAVITY, 0));
                break;
            default:
                world.setGravity(new Vector2(0, -Constants.WORLD_GRAVITY));
                break;
        }
    }


    public GravityDirection get() { return current; }
}
