package org.ayosynk.setup;

import org.ayosynk.models.Arena;
import org.bukkit.Location;

public class SetupSession {
    
    private final Arena arena;
    private Location pos1;
    private Location pos2;
    
    public SetupSession(Arena arena) {
        this.arena = arena;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public Location getPos1() {
        return pos1;
    }
    
    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }
    
    public Location getPos2() {
        return pos2;
    }
    
    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }
}

