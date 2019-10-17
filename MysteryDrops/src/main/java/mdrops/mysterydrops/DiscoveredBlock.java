package mdrops.mysterydrops;

import java.util.UUID;

import org.bukkit.Material;

class DiscoveredBlock {
    public Material old;
    public UUID discoverer;
    public Material newMat;
    public boolean isFromMob = false;
    public String mobName;

    public DiscoveredBlock(Material old, UUID pl, Material newMat) {
        this.old = old;
        this.discoverer = pl;
        this.newMat = newMat;
    }

    public DiscoveredBlock(String old, UUID pl, Material newMat) {
        mobName = old;
        discoverer = pl;
        this.newMat = newMat;
        isFromMob = true;
    }
}