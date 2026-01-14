package com.mygdx.gravity.levels;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.gravity.mechanics.GravityDirection;
import com.mygdx.gravity.levels.LevelData.*;

public class LevelManager {
    private final LevelData[] levels;
    
    public LevelManager() {
        levels = new LevelData[5];
        levels[0] = createLevel1();
        levels[1] = createLevel2();
        levels[2] = createLevel3();
        levels[3] = createLevel4();
        levels[4] = createLevel5();
    }

    public LevelData get(int ix) { 
        if (ix >= 0 && ix < levels.length) {
            return levels[ix]; 
        }
        return null;
    }
    
    public int count() { return levels.length; }
    
    private Vector2 v(float x, float y) {
        return new Vector2(x, y);
    }
    
    // Level 1: Intro Run - простые прыжки через пустоту между платформами-островами
    private LevelData createLevel1() {
        // Платформы как острова земли - широкие и высокие, уходящие вниз
        PlatformData[] platforms = {
            new PlatformData(v(3f, 2f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL), // Стартовая платформа
            new PlatformData(v(10f, 3f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(17f, 4f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(24f, 5f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(31f, 5.5f), new Vector2(3f, 1.5f), 0.25f, PlatformType.NORMAL) // Финиш
        };
        
        // Шипы выше каждой платформы (удалены с платформ, где есть враги)
        SpikeData[] spikes = {
            new SpikeData(v(3f, 3.8f), new Vector2(0.7f, 0.25f)), // На первой платформе
            new SpikeData(v(10f, 4.8f), new Vector2(0.7f, 0.25f)), // На второй
            // Шип на третьей платформе удален (там враг)
            new SpikeData(v(24f, 6.8f), new Vector2(0.7f, 0.25f))  // На четвертой
            // Шип на финишной платформе удален (там враг)
        };
        
        EnemyData[] enemies = {
            new EnemyData(v(17f, 6.0f), new Vector2(0.4f, 0.5f)) // На третьей платформе
        };
        
        BoxData[] boxes = {
            new BoxData(v(10f, 4.5f), new Vector2(0.5f, 0.5f))
        };
        
        // Spawn on left side of first platform, safe from spikes
        return new LevelData(0, v(1.5f, 3.0f), v(31f, 6.8f),
                           platforms, spikes, new GravityZoneData[0], new TimeSlowZoneData[0], enemies, boxes);
    }
    
    // Level 2: Fragile Crossing - исчезающие платформы и первый импульс между островами
    private LevelData createLevel2() {
        PlatformData[] platforms = {
            new PlatformData(v(3f, 2f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL), // Старт
            new PlatformData(v(10f, 3.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.NORMAL), // Обычная платформа
            new PlatformData(v(17f, 5f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(24f, 6.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.IMPULSE, new Vector2(8f, 7f)), // Импульс
            new PlatformData(v(29f, 8.5f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL), // Приближено
            new PlatformData(v(35f, 9.5f), new Vector2(2f, 1.5f), 0.25f, PlatformType.NORMAL) // Финиш, приближено
        };
        
        // Шипы выше каждой платформы (удалены с платформ, где есть враги)
        SpikeData[] spikes = {
            new SpikeData(v(3f, 3.8f), new Vector2(0.7f, 0.25f))
            // Шипы на платформах с врагами удалены:
            // - на второй платформе (10f, 5.3f) - там враг
            // - на третьей платформе (17f, 6.8f) - там враг
            // - на четвертой платформе (24f, 8.3f) - там враг
            // - на пятой платформе (32f, 11.8f) - там враг
        };
        
        EnemyData[] enemies = {
            new EnemyData(v(10f, 4.5f), new Vector2(0.4f, 0.5f)), // На второй платформе
            new EnemyData(v(17f, 6.5f), new Vector2(0.4f, 0.5f)), // На третьей платформе
            new EnemyData(v(24f, 7.5f), new Vector2(0.4f, 0.5f)), // На четвертой платформе (импульс)
            new EnemyData(v(29f, 10.0f), new Vector2(0.4f, 0.5f))  // На пятой платформе
        };
        
        BoxData[] boxes = {
            new BoxData(v(17f, 6.5f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(35f, 11.0f), new Vector2(0.5f, 0.5f))
        };
        
        // Spawn on left side of first platform, safe from spikes
        return new LevelData(0, v(1.5f, 3.0f), v(35f, 11.3f),
                           platforms, spikes, new GravityZoneData[0], new TimeSlowZoneData[0], enemies, boxes);
    }
    
    // Level 3: Gravity Gallery - смена гравитации между островами
    private LevelData createLevel3() {
        PlatformData[] platforms = {
            new PlatformData(v(3f, 2f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL), // Старт
            new PlatformData(v(11f, 4f), new Vector2(2f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(19f, 9f), new Vector2(2f, 1.5f), 0.25f, PlatformType.NORMAL), // Потолок при UP
            new PlatformData(v(27f, 9f), new Vector2(2f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(35f, 5f), new Vector2(2f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(43f, 7.5f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL) // Финиш
        };
        
        GravityZoneData[] gravityZones = {
            new GravityZoneData(v(7f, 4.5f), new Vector2(1.5f, 3f), GravityDirection.UP), // Между 1 и 2
            new GravityZoneData(v(15f, 7f), new Vector2(1.5f, 3f), GravityDirection.UP), // Между 2 и 3
            new GravityZoneData(v(23f, 7f), new Vector2(1.5f, 3f), GravityDirection.DOWN), // Между 3 и 4
            new GravityZoneData(v(31f, 7f), new Vector2(1.5f, 3f), GravityDirection.RIGHT), // Между 4 и 5
            new GravityZoneData(v(39f, 6.5f), new Vector2(1.5f, 3f), GravityDirection.DOWN) // Между 5 и 6
        };
        
        // Шипы выше каждой платформы
        SpikeData[] spikes = {
            new SpikeData(v(3f, 3.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(11f, 5.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(19f, 10.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(27f, 10.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(35f, 6.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(43f, 9.3f), new Vector2(0.7f, 0.25f))
        };
        
        EnemyData[] enemies = {
            new EnemyData(v(19f, 10.5f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(35f, 6.5f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(43f, 9.0f), new Vector2(0.4f, 0.5f))
        };
        
        BoxData[] boxes = {
            new BoxData(v(11f, 5.5f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(27f, 10.5f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(35f, 6.5f), new Vector2(0.5f, 0.5f))
        };
        
        // Spawn on left side of first platform, safe from spikes
        return new LevelData(0, v(1.5f, 3.0f), v(43f, 9.8f), 
                           platforms, spikes, gravityZones, new TimeSlowZoneData[0], enemies, boxes);
    }
    
    // Level 4: Impulse Control - чередование импульсов, шипов и зоны замедления между островами
    private LevelData createLevel4() {
        PlatformData[] platforms = {
            new PlatformData(v(3f, 2f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL), // Старт
            new PlatformData(v(12f, 3.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.IMPULSE, new Vector2(11f, 8f)), // Импульс
            new PlatformData(v(22f, 9f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(30f, 9.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.IMPULSE, new Vector2(13f, 6f)), // Импульс
            new PlatformData(v(40f, 13.5f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(47f, 14f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.NORMAL), // Обычная платформа
            new PlatformData(v(54f, 13.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.IMPULSE, new Vector2(11f, -8f)), // Импульс вниз
            new PlatformData(v(62f, 7f), new Vector2(2f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(70f, 7.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.IMPULSE, new Vector2(9f, 10f)), // Импульс
            new PlatformData(v(78f, 15.5f), new Vector2(3f, 1.5f), 0.25f, PlatformType.NORMAL) // Финиш
        };
        
        // Шипы выше каждой платформы
        SpikeData[] spikes = {
            new SpikeData(v(3f, 3.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(12f, 5.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(22f, 10.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(30f, 11.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(40f, 15.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(47f, 15.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(54f, 15.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(62f, 9.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(70f, 9.8f), new Vector2(0.7f, 0.25f))
        };
        
        TimeSlowZoneData[] slowZones = new TimeSlowZoneData[0]; // Удалены зоны замедления времени
        
        EnemyData[] enemies = {
            new EnemyData(v(22f, 10.5f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(40f, 14.5f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(62f, 8.5f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(78f, 16.5f), new Vector2(0.4f, 0.5f))
        };
        
        BoxData[] boxes = {
            new BoxData(v(22f, 10.5f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(30f, 11.0f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(62f, 8.5f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(70f, 9.0f), new Vector2(0.5f, 0.5f))
        };
        
        // Spawn on left side of first platform, safe from spikes
        return new LevelData(0, v(1.5f, 3.0f), v(78f, 16.8f), 
                           platforms, spikes, new GravityZoneData[0], slowZones, enemies, boxes);
    }
    
    // Level 5: Final Gauntlet - все механики вместе, высокий риск между островами
    private LevelData createLevel5() {
        PlatformData[] platforms = {
            new PlatformData(v(3f, 2f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL), // Старт
            new PlatformData(v(11f, 3.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.NORMAL), // Обычная платформа
            new PlatformData(v(19f, 4.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.NORMAL), // Обычная платформа
            new PlatformData(v(27f, 6f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(35f, 10.5f), new Vector2(2f, 1.5f), 0.25f, PlatformType.NORMAL), // Потолок при UP
            new PlatformData(v(43f, 10.5f), new Vector2(2f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(51f, 6.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.IMPULSE, new Vector2(15f, 11f)), // Импульс
            new PlatformData(v(62f, 15f), new Vector2(3f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(70f, 15.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.NORMAL), // Обычная платформа
            new PlatformData(v(78f, 16f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.NORMAL), // Обычная платформа
            new PlatformData(v(86f, 15.5f), new Vector2(1.5f, 1.2f), 0.3f, PlatformType.IMPULSE, new Vector2(11f, -9f)), // Импульс вниз
            new PlatformData(v(94f, 8.5f), new Vector2(3f, 1.5f), 0.25f, PlatformType.NORMAL),
            new PlatformData(v(102f, 11f), new Vector2(2.5f, 1.5f), 0.25f, PlatformType.NORMAL) // Финиш
        };
        
        GravityZoneData[] gravityZones = {
            new GravityZoneData(v(31f, 8f), new Vector2(1.5f, 3f), GravityDirection.UP), // Между 4 и 5
            new GravityZoneData(v(39f, 8f), new Vector2(1.5f, 3f), GravityDirection.DOWN), // Между 5 и 6
            new GravityZoneData(v(47f, 8.5f), new Vector2(1.5f, 3f), GravityDirection.RIGHT) // Между 6 и 7
        };
        
        // Шипы выше каждой платформы
        SpikeData[] spikes = {
            new SpikeData(v(3f, 3.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(11f, 5.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(19f, 6.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(27f, 7.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(35f, 12.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(43f, 12.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(51f, 8.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(62f, 16.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(70f, 17.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(78f, 17.8f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(86f, 17.3f), new Vector2(0.7f, 0.25f)),
            new SpikeData(v(94f, 10.3f), new Vector2(0.7f, 0.25f))
        };
        
        TimeSlowZoneData[] slowZones = new TimeSlowZoneData[0]; // Удалены зоны замедления времени
        
        EnemyData[] enemies = {
            new EnemyData(v(27f, 7.5f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(35f, 12.0f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(62f, 16.5f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(94f, 10.0f), new Vector2(0.4f, 0.5f)),
            new EnemyData(v(102f, 12.5f), new Vector2(0.4f, 0.5f))
        };
        
        BoxData[] boxes = {
            new BoxData(v(27f, 7.5f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(35f, 12.0f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(43f, 12.0f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(62f, 16.5f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(70f, 17.0f), new Vector2(0.5f, 0.5f)),
            new BoxData(v(94f, 10.0f), new Vector2(0.5f, 0.5f))
        };
        
        // Spawn on left side of first platform, safe from spikes
        return new LevelData(0, v(1.5f, 3.0f), v(102f, 12.8f), 
                           platforms, spikes, gravityZones, slowZones, enemies, boxes);
    }
}
