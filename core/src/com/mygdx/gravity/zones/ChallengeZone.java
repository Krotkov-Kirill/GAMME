package com.mygdx.gravity.zones;

import com.mygdx.gravity.entities.Player;

public abstract class ChallengeZone {
    public abstract void onEnter(Player player);
    public abstract void onExit(Player player);
    public void update(float delta) {}
}
