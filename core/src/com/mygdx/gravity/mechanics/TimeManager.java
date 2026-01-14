package com.mygdx.gravity.mechanics;

import com.mygdx.gravity.utils.Constants;

public class TimeManager {
    private float scale = 1f;
    public void slow() { scale = Constants.TIME_SLOW_SCALE; }
    public void reset() { scale = 1f; }
    public void setSlow(boolean slow) { 
        scale = slow ? Constants.TIME_SLOW_SCALE : 1f; 
    }
    public float get() { return scale; }
}
