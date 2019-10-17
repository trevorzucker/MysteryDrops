package mdrops.mysterydrops;

import org.bukkit.Material;
import org.bukkit.loot.LootTables;

public class MaterialHandler {

    public static Material[] GetAllItems() {
        Material[] items = Material.values();
        return items;
    }

    public static LootTables[] GetAllLootTables() {
        LootTables[] table = LootTables.values();
        return table;
    }

    public static Material GetByName(String name) {
        Material[] items = GetAllItems();
        Material m = null;
        for (int i = 0; i < items.length; i++) {
            if (name.equals(items[i].name()))
                m = items[i];
        }
        return m;
    }

    public static LootTables GetLootTableByName(String name) {
        LootTables[] tables = GetAllLootTables();
        LootTables tbl = null;
        for (int i = 0; i < tables.length; i++) {
            if (name.equals(tables[i].name()))
                tbl = tables[i];
        }
        return tbl;
    }

    public static LootTables GetLootTableByKey(String name) {
        LootTables[] tables = GetAllLootTables();
        LootTables tbl = null;
        for (int i = 0; i < tables.length; i++) {
            if (name.equals(tables[i].getKey().getKey()))
                tbl = tables[i];
        }
        return tbl;
    }

    public static boolean isLootTable(Material m) {
        boolean verdict = false;
        if (DropFileHandler.DropTableWithLoot.get(m) != null)
            verdict = true;
            
        return verdict;
    }
}