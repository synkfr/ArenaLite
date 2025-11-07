package org.ayosynk.models;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    
    private final UUID uuid;
    private int kills;
    private int deaths;
    private int streak;
    private String currentKit;
    private String currentArena;
    private long joinTime;
    private boolean inArena;
    private final Map<String, KitStats> kitStats = new HashMap<>();
    
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.kills = 0;
        this.deaths = 0;
        this.streak = 0;
        this.inArena = false;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public void addKill() {
        this.kills++;
        this.streak++;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public void addDeath() {
        this.deaths++;
        this.streak = 0;
    }
    
    public int getStreak() {
        return streak;
    }
    
    public void setStreak(int streak) {
        this.streak = streak;
    }
    
    public double getKDR() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }
    
    public String getCurrentKit() {
        return currentKit;
    }
    
    public void setCurrentKit(String currentKit) {
        this.currentKit = currentKit;
    }
    
    public String getCurrentArena() {
        return currentArena;
    }
    
    public void setCurrentArena(String currentArena) {
        this.currentArena = currentArena;
    }
    
    public long getJoinTime() {
        return joinTime;
    }
    
    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }
    
    public boolean isInArena() {
        return inArena;
    }
    
    public void setInArena(boolean inArena) {
        this.inArena = inArena;
    }
    
    public KitStats getKitStats(String kitName) {
        if (kitName == null) {
            return null;
        }
        return kitStats.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> new KitStats());
    }
    
    public Map<String, KitStats> getKitStats() {
        return kitStats;
    }
    
    public void addKillForKit(String kitName) {
        if (kitName != null) {
            getKitStats(kitName).addKill();
        }
    }
    
    public void addDeathForKit(String kitName) {
        if (kitName != null) {
            getKitStats(kitName).addDeath();
        }
    }
}

