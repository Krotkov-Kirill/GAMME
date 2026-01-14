package com.mygdx.gravity.levels;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.gravity.mechanics.GravityDirection;

public class LevelData {
    public final float timeLimit;
    public final Vector2 playerSpawn;
    public final Vector2 finishPosition;
    public final PlatformData[] platforms;
    public final SpikeData[] spikes;
    public final GravityZoneData[] gravityZones;
    public final TimeSlowZoneData[] timeSlowZones;
    public final EnemyData[] enemies;
    public final BoxData[] boxes;
    
    public LevelData(float timeLimit, Vector2 playerSpawn, Vector2 finishPosition,
                    PlatformData[] platforms, SpikeData[] spikes, 
                    GravityZoneData[] gravityZones, TimeSlowZoneData[] timeSlowZones,
                    EnemyData[] enemies, BoxData[] boxes) {
        this.timeLimit = timeLimit;
        this.playerSpawn = playerSpawn;
        this.finishPosition = finishPosition;
        this.platforms = platforms;
        this.spikes = spikes;
        this.gravityZones = gravityZones;
        this.timeSlowZones = timeSlowZones;
        this.enemies = enemies != null ? enemies : new EnemyData[0];
        this.boxes = boxes != null ? boxes : new BoxData[0];
    }
    
    public static class PlatformData {
        public final Vector2 position;
        public final Vector2 halfSize;
        public final float friction;
        public final PlatformType type;
        public final Vector2 impulse; // for ImpulsePlatform
        
        public PlatformData(Vector2 position, Vector2 halfSize, float friction, PlatformType type) {
            this(position, halfSize, friction, type, null);
        }
        
        public PlatformData(Vector2 position, Vector2 halfSize, float friction, PlatformType type, Vector2 impulse) {
            this.position = position;
            this.halfSize = halfSize;
            this.friction = friction;
            this.type = type;
            this.impulse = impulse;
        }
    }
    
    public static class SpikeData {
        public final Vector2 position;
        public final Vector2 halfSize;
        
        public SpikeData(Vector2 position, Vector2 halfSize) {
            this.position = position;
            this.halfSize = halfSize;
        }
    }
    
    public static class GravityZoneData {
        public final Vector2 position;
        public final Vector2 halfSize;
        public final GravityDirection direction;
        
        public GravityZoneData(Vector2 position, Vector2 halfSize, GravityDirection direction) {
            this.position = position;
            this.halfSize = halfSize;
            this.direction = direction;
        }
    }
    
    public static class TimeSlowZoneData {
        public final Vector2 position;
        public final Vector2 halfSize;
        
        public TimeSlowZoneData(Vector2 position, Vector2 halfSize) {
            this.position = position;
            this.halfSize = halfSize;
        }
    }
    
    public static class EnemyData {
        public final Vector2 position;
        public final Vector2 halfSize;
        
        public EnemyData(Vector2 position, Vector2 halfSize) {
            this.position = position;
            this.halfSize = halfSize;
        }
    }
    
    public static class BoxData {
        public final Vector2 position;
        public final Vector2 halfSize;
        
        public BoxData(Vector2 position, Vector2 halfSize) {
            this.position = position;
            this.halfSize = halfSize;
        }
    }
    
    public enum PlatformType {
        NORMAL, VANISHING, IMPULSE
    }
}
