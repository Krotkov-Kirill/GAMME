package com.mygdx.gravity.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class ImpulsePlatform extends com.mygdx.gravity.entities.Platform {
    private final Vector2 impulse;

    public ImpulsePlatform(World world, Vector2 pos, Vector2 halfSize, Vector2 impulse) {
        super(world, pos, halfSize, 0.3f);
        this.impulse = impulse;
    }

    public void applyImpulseTo(com.mygdx.gravity.entities.Player player) {
        player.getBody().applyLinearImpulse(impulse, player.getBody().getWorldCenter(), true);
    }
}
