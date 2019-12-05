package mdrops.mysterydrops;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

public class DropHandler implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if ((e.getPlayer().getGameMode() == GameMode.CREATIVE) || e.isCancelled() || e.getPlayer().getInventory()
                .getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH))
            return;
        e.setDropItems(false);
        Block b = e.getBlock();
        Material oldMaterial = b.getType();
        if (MaterialHandler.isLootTable(oldMaterial)) {
            LootTable tbl = DropFileHandler.DropTableWithLoot.get(oldMaterial).getLootTable();

            Location blockLoc = e.getBlock().getLocation();
            try {
                String cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " "
                        + blockLoc.getBlockZ() + " loot " + tbl.getKey().getKey();
                Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd);
                /*
                 * cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " "
                 * + blockLoc.getBlockZ() + " kill " + tbl.getKey().getKey();
                 * Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd); cmd =
                 * "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " " +
                 * blockLoc.getBlockZ() + " mine " + tbl.getKey().getKey();
                 * Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd);
                 */} catch (CommandException exc) {
            }
            HashMap<Player, LootTable> entry = new HashMap<>();
            entry.put(e.getPlayer(), tbl);

            DiscoveredLootTable _tbl = new DiscoveredLootTable(e.getBlock().getType(), e.getPlayer().getUniqueId(),
                    DropFileHandler.DropTableWithLoot.get(oldMaterial));
            if (!DropFileHandler.DiscoveredLootTables.contains(_tbl)) {
                if (!App.BlockDictionary_PlayerDependent) {
                    boolean foundEntry = false;
                    for (DiscoveredLootTable block : DropFileHandler.DiscoveredLootTables) {
                        if (tbl.getKey().getKey().equals(block.table.getKey().getKey())) {
                            foundEntry = true;
                            return;
                        }
                    }
                    if (!foundEntry) {
                        DropFileHandler.DiscoveredLootTables.add(_tbl);
                        return;
                    }
                }
                DropFileHandler.DiscoveredLootTables.add(_tbl);
            }
        } else {
            Material newMaterial = DropFileHandler.DropTable.get(oldMaterial);
            try {
                b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(newMaterial));
            } catch (IllegalArgumentException exception) {
                YamlConfiguration drops = YamlConfiguration.loadConfiguration(App.dropTableFile);
                String itemName = App.items[new Random().nextInt(App.items.length)].name();
                drops.set(oldMaterial.name(), itemName);
                ItemStack item = new ItemStack(MaterialHandler.GetByName(itemName));

                try {
                    drops.save(App.dropTableFile);
                } catch (IOException err) {
                }
                try {
                    b.getWorld().dropItemNaturally(b.getLocation(), item);
                } catch (IllegalArgumentException er) {
                }
            }

            DiscoveredBlock bl = new DiscoveredBlock(e.getBlock().getType(), e.getPlayer().getUniqueId(), newMaterial);
            if (!DropFileHandler._DiscoveredBlocks.contains(bl)) {
                if (!App.BlockDictionary_PlayerDependent) {
                    boolean foundEntry = false;
                    for (DiscoveredBlock block : DropFileHandler._DiscoveredBlocks) {
                        if (newMaterial.name() != null && block.newMat.name() != null && newMaterial.name().equals(block.newMat.name())) {
                            foundEntry = true;
                            return;
                        }
                    }
                    if (!foundEntry) {
                        DropFileHandler._DiscoveredBlocks.add(bl);
                        IncrementBlocksDiscovered(e.getPlayer());
                        return;
                    }
                }
                DropFileHandler._DiscoveredBlocks.add(bl);
                IncrementBlocksDiscovered(e.getPlayer());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (!(e.getEntity() instanceof Player) && killer != null && killer instanceof Player) {
            List<ItemStack> drops = e.getDrops();
            for (ItemStack i : drops) {
                Material oldMaterial = i.getType();
                if (MaterialHandler.isLootTable(oldMaterial)) {
                    LootTable tbl = DropFileHandler.DropTableWithLoot.get(oldMaterial).getLootTable();

                    Location blockLoc = e.getEntity().getLocation();
                    try {
                        String cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " "
                                + blockLoc.getBlockZ() + " loot " + tbl.getKey().getKey();
                        Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd);
                        /*
                         * cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " "
                         * + blockLoc.getBlockZ() + " kill " + tbl.getKey().getKey();
                         * Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd); cmd =
                         * "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " " +
                         * blockLoc.getBlockZ() + " mine " + tbl.getKey().getKey();
                         * Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd);
                         */} catch (CommandException exc) {
                    }
                } else {
                    Material newMaterial = DropFileHandler.DropTable.get(oldMaterial);
                    try {
                        e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(),
                                new ItemStack(newMaterial));
                    } catch (IllegalArgumentException exception) {
                        YamlConfiguration dropFile = YamlConfiguration.loadConfiguration(App.dropTableFile);
                        String itemName = App.items[new Random().nextInt(App.items.length)].name();
                        dropFile.set(oldMaterial.name(), itemName);

                        try {
                            dropFile.save(App.dropTableFile);
                        } catch (IOException err) {
                        }
                        try {
                            e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(),
                                    new ItemStack(newMaterial));
                        } catch (IllegalArgumentException exc) {
                        }
                    }

                    String entityName = e.getEntity().getName();

                    if (entityName.contains("x "))
                        entityName = entityName.split("x ")[1];
                    DiscoveredBlock bl = new DiscoveredBlock(entityName, e.getEntity().getKiller().getUniqueId(),
                            newMaterial);
                    if (!DropFileHandler._DiscoveredBlocks.contains(bl)) {
                        if (!App.BlockDictionary_PlayerDependent) {
                            boolean found = false;
                            for (DiscoveredBlock block : DropFileHandler._DiscoveredBlocks) {
                                if (newMaterial.name().equals(block.newMat.name())) {
                                    //DropFileHandler._DiscoveredBlocks.remove(block);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                DropFileHandler._DiscoveredBlocks.add(bl);
                                IncrementBlocksDiscovered(killer);
                            }
                        } else {
                            boolean found = false;
                            for (DiscoveredBlock block : DropFileHandler._DiscoveredBlocks) {
                                if (block.discoverer == killer.getUniqueId() && block.newMat == newMaterial) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                DropFileHandler._DiscoveredBlocks.add(bl);
                            }
                        }
                    }
                }
            }
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        InventoryView inv = player.getOpenInventory();
        if (inv.getTitle().split(" -")[0].equals("Discovered blocks")) {
            event.setCancelled(true);
            int page = Integer.parseInt(inv.getTitle().split("Page ")[1]) - 1;
            if (clicked != null && clicked.getItemMeta() != null
                    && clicked.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Next Page")) {
                player.openInventory(Commands.PlayerBlockInventories.get(player.getUniqueId()).get(page + 1));
            } else if (clicked != null && clicked.getItemMeta() != null
                    && clicked.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Previous Page") && page > 0) {
                player.openInventory(Commands.PlayerBlockInventories.get(player.getUniqueId()).get(page - 1));
            }
        }
        if (inv.getTitle().split(" -")[0].equals("Top Players")) {
            event.setCancelled(true);
            int page = Integer.parseInt(inv.getTitle().split("Page ")[1]) - 1;
            if (clicked != null && clicked.getItemMeta() != null
                    && clicked.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Next Page")) {
                player.openInventory(Commands.PlayerTopInventories.get(player.getUniqueId()).get(page + 1));
            } else if (clicked != null && clicked.getItemMeta() != null
                    && clicked.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Previous Page") && page > 0) {
                player.openInventory(Commands.PlayerTopInventories.get(player.getUniqueId()).get(page - 1));
            }
        }
    }

    
    /* void RemoveIllegals(ItemStack s, Inventory inv) { if (s == null) return;
     
     HashMap<Enchantment, Integer> maxs = new HashMap<>();
     maxs.put(Enchantment.DURABILITY, 3); maxs.put(Enchantment.DIG_SPEED, 5);
     maxs.put(Enchantment.DAMAGE_ARTHROPODS, 5); maxs.put(Enchantment.DAMAGE_ALL,
     5); maxs.put(Enchantment.DAMAGE_UNDEAD, 5);
     maxs.put(Enchantment.LOOT_BONUS_BLOCKS, 3); maxs.put(Enchantment.KNOCKBACK,
     2);
     
     String blacklistResult = "ALLOWED"; if
     (DropFileHandler.Blacklist.get(MaterialHandler.GetByName(s.getType().name()))
     != null) blacklistResult =
     (String)DropFileHandler.Blacklist.get(MaterialHandler.GetByName(s.getType().
     name()));
     
     if (blacklistResult != null && blacklistResult.equalsIgnoreCase("DENIED") &&
     s.getType().name() != "AIR") { inv.remove(s); }
     
     if (s.getEnchantments() != null) { Map<Enchantment, Integer> ench =
     s.getEnchantments();
     
     for (Entry<Enchantment, Integer> enchantment : ench.entrySet()) { if
     ((maxs.get(enchantment.getKey()) != null && maxs.get(enchantment.getKey()) <
     enchantment.getValue()) || (enchantment.getKey() == Enchantment.THORNS &&
     s.getType().name().equals("DIAMOND_SWORD")) || s.getItemMeta().getDisplayName().contains("sword of summer")) { inv.remove(s); } } } }
     
     @EventHandler public void onInventoryOpen(InventoryOpenEvent event) { Player
     p = (Player)event.getPlayer(); InventoryView iView = event.getView();
     
     if (!iView.getTitle().split(" -")[0].equals("Discovered blocks") &&
     p.getGameMode() != GameMode.CREATIVE) { if (iView.getTopInventory() != null)
     { for (ItemStack s : iView.getTopInventory().getContents()) {
     RemoveIllegals(s, iView.getTopInventory()); } }
     
     if (iView.getBottomInventory() != null) { for (ItemStack s :
     iView.getBottomInventory().getContents()) { RemoveIllegals(s,
     iView.getBottomInventory()); } } } }
     
     @EventHandler public void onItemPickup(EntityPickupItemEvent event) { if
     (!(event.getEntity() instanceof Player)) return;
     
     Player p = (Player)event.getEntity(); InventoryView iView =
     p.getOpenInventory();
     
     if (!iView.getTitle().split(" -")[0].equals("Discovered blocks")) {// && !p.hasPermission("MysteryDrops.admin"))
     { if (iView.getTopInventory() !=
     null) { for (ItemStack s : iView.getTopInventory().getContents()) {
     RemoveIllegals(s, iView.getTopInventory()); } }
     
     if (iView.getBottomInventory() != null) { for (ItemStack s :
     iView.getBottomInventory().getContents()) { RemoveIllegals(s,
     iView.getBottomInventory()); } } } } }
     */

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = (Player) event.getPlayer();
        InventoryView inv = player.getOpenInventory();
        if (inv == null)
            return;
        if (inv == null || !inv.getTitle().split(" -")[0].equals("Discovered blocks")) {
            Commands.PlayerBlockInventories.remove(player.getUniqueId());
        }
        if (inv == null || !inv.getTitle().split(" -")[0].equals("Top Players")) {
            Commands.PlayerTopInventories.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //CreatePlayerTableIfNotExists(event.getPlayer());
        if (App.DiscoveredBlockCount.get(event.getPlayer().getUniqueId()) == null) {
            App.DiscoveredBlockCount.put(event.getPlayer().getUniqueId(), 0);
        }
    }

    void CreatePlayerTableIfNotExists(Player p) {
        if (App.sqlConn == null)
        return;

    UUID pUUID = p.getUniqueId();

    try {
        Statement s = App.sqlConn.createStatement();
        ResultSet set = s.executeQuery("SELECT * from " + App.SQL_TABLE + " WHERE uuid='" + pUUID.toString() + "'");
        boolean found = false;
        while(set.next()) {
            found = true;
        }
        if (!found) {
            PreparedStatement stmt = App.sqlConn.prepareStatement("INSERT INTO " + App.SQL_TABLE + "(uuid, username, blocks_discovered) VALUES (?, ?, ?)");
            stmt.setString(1, pUUID.toString());
            stmt.setString(2, p.getName());
            stmt.setInt(3, 0);
            stmt.execute();
        }
    } catch (SQLException e) {Bukkit.getLogger().info("SQL error " + e.getSQLState());}

    }

    void IncrementBlocksDiscovered(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
        int blocks = 0;
        if (App.DiscoveredBlockCount.get(p.getUniqueId()) != null)
            blocks = App.DiscoveredBlockCount.get(p.getUniqueId());
        p.sendMessage(ChatColor.WHITE + "[MysteryDrops] " + ChatColor.GREEN + "You've discovered a new block! Discovered count: " + (blocks + 1));
        App.DiscoveredBlockCount.put(p.getUniqueId(), blocks + 1);
        /*if (App.sqlConn == null) {
            p.sendMessage(ChatColor.WHITE + "[MysteryDrops] " + ChatColor.GREEN + "You've discovered a new block!");
            return;
        }

        CreatePlayerTableIfNotExists(p);

        UUID pUUID = p.getUniqueId();

        try {
            int blocks = 0;
            Statement s = App.sqlConn.createStatement();
            ResultSet set = s.executeQuery("SELECT blocks_discovered from " + App.SQL_TABLE + " WHERE uuid='" + pUUID.toString() + "'");
            while(set.next()) {
                blocks = set.getInt("blocks_discovered");
                PreparedStatement stmt = App.sqlConn.prepareStatement("UPDATE " + App.SQL_TABLE + " SET blocks_discovered = ? WHERE uuid = ?");
                stmt.setInt(1, blocks + 1);
                stmt.setString(2, pUUID.toString());
                stmt.execute();
            }
            p.sendMessage(ChatColor.WHITE + "[MysteryDrops] " + ChatColor.GREEN + "You've discovered a new block! Discovered count: " + (blocks + 1));
        } catch (SQLException e) {Bukkit.getLogger().info("SQL error " + e.getSQLState());}*/

    }
}