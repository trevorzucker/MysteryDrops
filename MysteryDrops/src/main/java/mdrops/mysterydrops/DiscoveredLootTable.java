package mdrops.mysterydrops;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.loot.LootTables;

class DiscoveredLootTable {
    public Material old;
    public UUID discoverer;
    public LootTables table;

    public DiscoveredLootTable(Material old, UUID pl, LootTables table) {
        this.old = old;
        this.discoverer = pl;
        this.table = table;
    }
}