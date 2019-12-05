package mdrops.mysterydrops;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

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
    public static YamlConfiguration blacklist;
    public static File blockDictionaryFile;
    public static FileConfiguration config;
    public static Material[] items;

    public static boolean BlockDictionary_Enabled = false;
    public static boolean BlockDictionary_PlayerDependent = true;
    public static boolean ShowLootTablesInBlocks = false;
    private static String[] sqlInfo = null;
    public static Connection sqlConn = null;
    public static final String SQL_TABLE = "MysteryDrops";

    public static HashMap<UUID, Integer> DiscoveredBlockCount = new HashMap<UUID, Integer>();

    @Override
    public void onEnable() {
        items = MaterialHandler.GetAllItems();
        config = this.getConfig();
        config.addDefault("autoRegen", false);
        config.addDefault("autoRegenTime", "00:00");
        config.addDefault("enableBlockDictionary", true);
        config.addDefault("showLootTablesInBlocksCommand", false);
        config.addDefault("blockDictionaryPlayerDependent", true);
        config.addDefault("mysql_enable", false);
        config.addDefault("mysql_host", "host");
        config.addDefault("mysql_user", "user");
        config.addDefault("mysql_pass", "pass");
        config.addDefault("mysql_db", "db");
        config.options().copyDefaults(true);
        this.saveDefaultConfig();
        dropTableFile = new File(getDataFolder(), "droptable.yml");
        dropTableBlacklistFile = new File(getDataFolder(), "blacklist.yml");
        blockDictionaryFile = new File(getDataFolder(), "blockdictionary.yml");

        if (!dropTableBlacklistFile.exists()) {
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
        CommandHelper.RegisterCommand("mdtop", "BlocksTopCommand");

        config = this.getConfig();
        if (config.getBoolean("mysql_enable")) {
            sqlInfo = new String[] { config.getString("mysql_host"), config.getString("mysql_user"),
                    config.getString("mysql_pass"), config.getString("mysql_db") };
            try {
                sqlConn = DriverManager.getConnection("jdbc:mysql://" + sqlInfo[0] + "/" + sqlInfo[3], sqlInfo[1], sqlInfo[2]);
                Statement s = sqlConn.createStatement();
                s.executeUpdate("CREATE TABLE IF NOT EXISTS " + SQL_TABLE + " (uuid VARCHAR(36), username TINYTEXT, blocks_discovered INT(255) DEFAULT 0) ENGINE=InnoDB;");
                Statement stmt = sqlConn.createStatement();
                ResultSet set = stmt.executeQuery("SELECT uuid, blocks_discovered from " + App.SQL_TABLE);
                while(set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));
                    int blocks = set.getInt("blocks_discovered");
                    DiscoveredBlockCount.put(uuid, blocks);
                }
                getLogger().info(ChatColor.GREEN + "Successfully connected to MySQL");
            } catch (SQLException e) {
                getLogger().info(ChatColor.RED + "Error connecting to MySQL, error " + e.getSQLState());
                sqlConn = null;
            }
        }

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
            try {
                boolean isMat = s.contains("MAT_");
                boolean isLootTbl = s.contains("TBL_");
                if (!isMat && !isLootTbl)
                    continue;

                String[] splitter = blockDictionary.getString(s).split("_iBsrJka5aX_");
                if (splitter.length <= 1)
                    continue;
                    
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
            } catch(Exception e) {}
        }

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                SaveToSQL();
            }
        }, 1200, 1200);
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
        if (sqlConn != null)
            try { sqlConn.close(); } catch (SQLException e1) {}
        SaveToSQL();
        YamlConfiguration blockDictionary = YamlConfiguration.loadConfiguration(blockDictionaryFile);
        for (DiscoveredBlock b : DropFileHandler._DiscoveredBlocks) {
            if (!b.isFromMob)
                blockDictionary.set("MAT_" + b.newMat.name(), b.discoverer + "_iBsrJka5aX_" + b.old.name());
            else {
                blockDictionary.set("MAT_" + b.newMat.name(), b.discoverer + "_iBsrJka5aX_" + b.mobName.replace("\n", "").replace(" ", "_"));
            }
        }

        for (DiscoveredLootTable b : DropFileHandler.DiscoveredLootTables) {
            blockDictionary.set("TBL_" + b.table.getKey().getKey(), b.discoverer + "_iBsrJka5aX_" + b.old.name());
        }
        try { blockDictionary.save(blockDictionaryFile); } catch (IOException e) {}
    }

    static void SaveToSQL() {
        if (sqlConn == null)
            return;
            
        try {
            for(Entry<UUID, Integer> set : DiscoveredBlockCount.entrySet()) {
                Statement s = App.sqlConn.createStatement();
                ResultSet rSet = s.executeQuery("SELECT blocks_discovered from " + SQL_TABLE + " WHERE uuid='" + set.getKey().toString() + "'");
                boolean found = false;
                while(rSet.next()) {
                    PreparedStatement stmt = App.sqlConn.prepareStatement("UPDATE " + App.SQL_TABLE + " SET blocks_discovered = ? WHERE uuid = ?");
                    stmt.setInt(1, set.getValue());
                    stmt.setString(2, set.getKey().toString());
                    stmt.execute();
                    found = true;
                }
                if (!found) {
                    PreparedStatement stmt = App.sqlConn.prepareStatement("INSERT INTO " + SQL_TABLE + "(uuid, username, blocks_discovered) VALUES (?, ?, ?)");
                    stmt.setString(1, set.getKey().toString());
                    stmt.setString(2, Bukkit.getOfflinePlayer(set.getKey()).getName());
                    stmt.setInt(3, set.getValue());
                    stmt.execute();
                }  
            }
        } catch(SQLException e) {}

    }
}