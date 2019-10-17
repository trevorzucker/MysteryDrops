package mdrops.mysterydrops;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {

    public static File dropTableFile;
    public static File dropTableBlacklistFile;
    public static File blockDictionaryFile;
    public static FileConfiguration config;
    public static Material[] items;

    public static boolean BlockDictionary_Enabled = false;
    public static boolean BlockDictionary_PlayerDependent = true;
    public static boolean ShowLootTablesInBlocks = false;

    @Override
    public void onEnable() {
        items = MaterialHandler.GetAllItems();
        config = this.getConfig();
        config.addDefault("autoRegen", false);
        config.addDefault("autoRegenTime", "00:00");
        config.addDefault("enableBlockDictionary", true);
        config.addDefault("showLootTablesInBlocksCommand", false);
        config.addDefault("blockDictionaryPlayerDependent", true);
        config.options().copyDefaults(true);
        this.saveDefaultConfig();
        dropTableFile = new File(getDataFolder(), "droptable.yml");
        dropTableBlacklistFile = new File(getDataFolder(), "blacklist.yml");
        blockDictionaryFile = new File(getDataFolder(), "blockdictionary.yml");

        if (!dropTableBlacklistFile.exists()) {
            DropFileHandler.CreateBlacklist();
        }

        if (!blockDictionaryFile.exists()) {
            DropFileHandler.CreateBlacklist();
        }

        if (!dropTableFile.exists()) {
            DropFileHandler.CreateDropFile();
        }

        DropFileHandler.LoadDropTables();
        getLogger().info("Drop tables loaded!");
        getServer().getPluginManager().registerEvents(new DropHandler(), this);
        CommandHelper.RegisterCommand("regenerate", "RegenerateCommand");
        CommandHelper.RegisterCommand("blocks", "BlocksCommand");

        config = this.getConfig();
        BlockDictionary_Enabled = config.getBoolean("enableBlockDictionary");
        BlockDictionary_PlayerDependent = config.getBoolean("blockDictionaryPlayerDependent");
        ShowLootTablesInBlocks = config.getBoolean("blockDictionaryPlayerDependent");
        String time = config.getString("autoRegenTime");

        if (config.getBoolean("autoRegen"))
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    String pattern = "HH:mm";
                    Date d = Calendar.getInstance().getTime();
                    String date = new SimpleDateFormat(pattern).format(d);
                    if (date.equals(time)) {
                        App.getPlugin(App.class).getLogger().info("Regenerating drop tables as per config..");
                        for (Player pl : Bukkit.getServer().getOnlinePlayers()) {
                            pl.sendMessage(ChatColor.WHITE + "[SERVER] " + ChatColor.GREEN
                                    + "It's " + time + "! Time to re-randomize drops!");
                            pl.sendTitle("It's " + time + "!", "Time to re-randomize drops!", 10, 70, 20);
                        }

                        File dropTableFile = new File(App.getPlugin(App.class).getDataFolder(), "droptable.yml");
                        dropTableFile.delete();
                        if (!dropTableFile.exists()) {
                            DropFileHandler.CreateDropFile();
                        }
                        DropFileHandler.LoadDropTables();
                    }
                }
            }, 20L * 55, 20L * 55);

        YamlConfiguration blockDictionary = YamlConfiguration.loadConfiguration(blockDictionaryFile);
        for (String s : blockDictionary.getKeys(false)) {
            boolean isMat = s.contains("MAT_");
            boolean isLootTbl = s.contains("TBL_");

            String[] splitter = blockDictionary.getString(s).split("_iBsrJka5aX_");
            UUID uuid = UUID.fromString(splitter[0]);
            Material newMat = null; LootTables tbl = null;
            
            if (isMat)
                newMat = MaterialHandler.GetByName(s.replace("MAT_", ""));

            if (isMat && newMat != null && uuid != null) {
                DiscoveredBlock blk;
                if (MaterialHandler.GetByName(splitter[1]) == null)
                    blk = new DiscoveredBlock(splitter[1], uuid, newMat);
                else
                    blk = new DiscoveredBlock(MaterialHandler.GetByName(splitter[1]), uuid, newMat);
                DropFileHandler._DiscoveredBlocks.add(blk);
            }

            if (isLootTbl) {
                tbl = MaterialHandler.GetLootTableByKey(s.replace("TBL_", ""));
                if (tbl != null) {
                    DiscoveredLootTable _tbl = new DiscoveredLootTable(MaterialHandler.GetByName(splitter[1]), uuid, tbl);
                    DropFileHandler.DiscoveredLootTables.add(_tbl);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        for (Map.Entry<String, HashMap<Method, Integer>> map : CommandHelper.commands.entrySet()) {
            if (cmd.getName().equalsIgnoreCase(map.getKey())) {
                try {
                    HashMap<Method, Integer> m = map.getValue();
                    Method meth = (Method) m.keySet().iterator().next();
                    int requiredEnergy = (int) m.get(meth);
                    if (requiredEnergy == 0) {
                        meth.invoke(null, sender, cmd, label, args);
                        return true;
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDisable() {
        YamlConfiguration blockDictionary = YamlConfiguration.loadConfiguration(blockDictionaryFile);
        for (DiscoveredBlock b : DropFileHandler._DiscoveredBlocks) {
            if (!b.isFromMob)
                blockDictionary.set("MAT_" + b.newMat.name(), b.discoverer + "_iBsrJka5aX_" + b.old.name());
            else
                blockDictionary.set("MAT_" + b.newMat.name(), b.discoverer + "_iBsrJka5aX_" + b.mobName);
        }

        for (DiscoveredLootTable b : DropFileHandler.DiscoveredLootTables) {
            blockDictionary.set("TBL_" + b.table.getKey().getKey(), b.discoverer + "_iBsrJka5aX_" + b.old.name());
        }
        try { blockDictionary.save(blockDictionaryFile); } catch (IOException e) {}
    }
}