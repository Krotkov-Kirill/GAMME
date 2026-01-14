package com.mygdx.gravity.zones;

import com.mygdx.gravity.entities.Player;
import com.mygdx.gravity.mechanics.TimeManager;

public class TimeSlowZone extends ChallengeZone {
    private final TimeManager time;

    public TimeSlowZone(TimeManager time) { this.time = time; }

    @Override
    public void onEnter(Player player) { time.slow(); }

    @Override
    public void onExit(Player player) { time.reset(); }
}
