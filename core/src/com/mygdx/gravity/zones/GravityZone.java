package com.mygdx.gravity.zones;

import com.mygdx.gravity.entities.Player;
import com.mygdx.gravity.mechanics.GravityDirection;
import com.mygdx.gravity.mechanics.GravityManager;

public class GravityZone extends ChallengeZone {

    private final GravityDirection direction;
    private final GravityManager gravity;

    public GravityZone(GravityManager gravity, GravityDirection direction) {
        this.gravity = gravity;
        this.direction = direction;
    }

    @Override
    public void onEnter(Player player) {
        gravity.set(direction);
    }

    @Override
    public void onExit(Player player) {
        gravity.set(GravityDirection.DOWN);
    }
}
