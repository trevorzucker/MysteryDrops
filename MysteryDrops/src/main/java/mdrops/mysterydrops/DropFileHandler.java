package mdrops.mysterydrops;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTables;

public class DropFileHandler {

    static App plugin = App.getPlugin(App.class);

    public static HashMap<Material, Material> DropTable;
    public static HashMap<Material, LootTables> DropTableWithLoot;
    public static HashMap<Material, String> Blacklist;

    // Original item, Discoverer, New item
    static ArrayList<DiscoveredBlock> _DiscoveredBlocks = new ArrayList<DiscoveredBlock>();
    // Original item, Discoverer, Loot table
    static ArrayList<DiscoveredLootTable> DiscoveredLootTables = new ArrayList<DiscoveredLootTable>();

    public static void CreateDropFile() {
        plugin.getLogger().info("Creating drop tables..");
        App.dropTableFile.getParentFile().mkdirs();
        try { App.dropTableFile.createNewFile(); } catch (IOException e) { plugin.getLogger().info(e.toString()); }

        // make drop table here
        YamlConfiguration drops = YamlConfiguration.loadConfiguration(App.dropTableFile);
        YamlConfiguration blacklist = YamlConfiguration.loadConfiguration(App.dropTableBlacklistFile);
        YamlConfiguration blockDictionary = new YamlConfiguration();
        try { blockDictionary.save(App.blockDictionaryFile); } catch (IOException e1) {}

        LootTables[] lootTables = MaterialHandler.GetAllLootTables();
        Material[] items = App.items;

        Random r = new Random();

        for (int i = 0; i < items.length; i++) {
            drops.createSection(items[i].name());
            int random = r.nextInt(items.length + lootTables.length);
            if (random > items.length - 1)
                drops.set(items[i].name(), "LOOT_TABLE." + lootTables[random - items.length].name());
            else {
                /* get rid of giving potted plants, because it's not a valid item */
                while (items[random].name().split("POTTED_").length > 1) {
                    random = r.nextInt(items.length);
                }
                String itemName = items[random].name();
                itemName = itemName.replace("_WALL", "").replace("CARROTS", "CARROT").replace("SWEET_BERRY_BUSH", "SWEET_BERRY");
                // do a check to make sure itemName is not blacklisted

                String blacklistResult = (String)blacklist.get(itemName);

                while (blacklistResult != null && blacklistResult.equalsIgnoreCase("DENIED")) {
                    //String oldItem = itemName;
                    random = r.nextInt(items.length);
                    while (items[random].name().split("POTTED_").length > 1) {
                        random = r.nextInt(items.length);
                    }
                    itemName = items[random].name();
                    //Bukkit.getLogger().info("Tried to assign a blacklisted item to the drop table (" + oldItem + "), replaced  with " + itemName);
                    blacklistResult = (String)blacklist.get(itemName);
                }
                
                drops.set(items[i].name(), itemName);
            }
        }

        try {
            drops.save(App.dropTableFile);
        } catch (IOException e) {
        }

        _DiscoveredBlocks.clear();
        DiscoveredLootTables.clear();
        plugin.getLogger().info("Drop table created!");

        for(Player pl : Bukkit.getServer().getOnlinePlayers()) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
        }
    }

    public static void CreateBlacklist() {
        App.dropTableBlacklistFile.getParentFile().mkdirs();
        try { App.dropTableBlacklistFile.createNewFile(); } catch (IOException e) { plugin.getLogger().info(e.toString()); }
        try {
            FileWriter writer = new FileWriter(App.dropTableBlacklistFile);
            writer.append("# This configuration file lets you prevent specific items to be propagated into the drop file configuration.\n");
            writer.append("# Possible options: ALLOWED or DENIED\n");
            writer.append("# By default, AIR and SPAWNER are blacklisted");
            writer.close();
        } catch (IOException e) { }
        YamlConfiguration blacklist = YamlConfiguration.loadConfiguration(App.dropTableBlacklistFile);

        Material[] items = App.items;

        String[] defaultBlacklist = new String[] {"AIR", "SPAWNER", "BARRIER"};

        for (int i = 0; i < items.length; i++) {
            blacklist.createSection(items[i].name());
            blacklist.set(items[i].name(), "ALLOWED");
            for(String def : defaultBlacklist) {
                if (items[i].name().equalsIgnoreCase(def)) {
                    blacklist.set(items[i].name(), "DENIED");
                }
            }
        }
        try { blacklist.save(App.dropTableBlacklistFile); } catch (IOException e) {}
    }

    public static void LoadDropTables() {
        DropTable = new HashMap<>();
        DropTableWithLoot = new HashMap<>();
        Blacklist = new HashMap<>();
        App.dropTableFile = new File(plugin.getDataFolder(), "droptable.yml");
        YamlConfiguration drops = YamlConfiguration.loadConfiguration(App.dropTableFile);
        YamlConfiguration blacklist = YamlConfiguration.loadConfiguration(App.dropTableBlacklistFile);
        String[] dropString = drops.saveToString().split("\\r?\\n");
        for(int i = 0; i < MaterialHandler.GetAllItems().length; i++) {
            String[] dropLine = dropString[i].split(": ");
            boolean containsLootTable = dropLine[1].split("\\.").length > 1;
            if (containsLootTable) {
                DropTableWithLoot.put(MaterialHandler.GetByName(dropLine[0]), MaterialHandler.GetLootTableByName(dropString[i].split("\\.")[1]));
            }
            else
                DropTable.put(MaterialHandler.GetByName(dropLine[0]), MaterialHandler.GetByName(dropLine[1]));
            //plugin.getLogger().info("Breaking " + dropLine[0] + " will drop " + dropLine[1]);
        }

        for(String s : blacklist.getKeys(false)) {
            Blacklist.put(MaterialHandler.GetByName(s), blacklist.getString(s));
        }
    }
}