package mdrops.mysterydrops;

import java.util.UUID;

import org.bukkit.Material;

class DiscoveredBlock {
    public Material old;
    public UUID discoverer;
    public Material newMat;

    public DiscoveredBlock(Material old, UUID pl, Material newMat) {
        this.old = old;
        this.discoverer = pl;
        this.newMat = newMat;
    }
}