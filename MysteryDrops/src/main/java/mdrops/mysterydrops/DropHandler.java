package mdrops.mysterydrops;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

public class DropHandler implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if ((e.getPlayer().getGameMode() == GameMode.CREATIVE) || e.isCancelled() || e.getPlayer().getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH))
            return;
        e.setDropItems(false);
        Block b = e.getBlock();
        Material oldMaterial = b.getType();
        if (MaterialHandler.isLootTable(oldMaterial)) {
            LootTable tbl = DropFileHandler.DropTableWithLoot.get(oldMaterial).getLootTable();

            Location blockLoc = e.getBlock().getLocation();
            try {
            String cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " " + blockLoc.getBlockZ() + " loot " + tbl.getKey().getKey();
            Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd);
            /*cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " " + blockLoc.getBlockZ() + " kill " + tbl.getKey().getKey();
            Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd);
            cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " " + blockLoc.getBlockZ() + " mine " + tbl.getKey().getKey();
            Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd); */} catch (CommandException exc) {}
            HashMap<Player, LootTable> entry = new HashMap<>();
            entry.put(e.getPlayer(), tbl);

            DiscoveredLootTable _tbl = new DiscoveredLootTable(e.getBlock().getType(), e.getPlayer().getUniqueId(), DropFileHandler.DropTableWithLoot.get(oldMaterial));
            if (!DropFileHandler.DiscoveredLootTables.contains(_tbl)) {
                if (!App.BlockDictionary_PlayerDependent) {
                    boolean foundEntry = false;
                    for(DiscoveredLootTable block : DropFileHandler.DiscoveredLootTables) {
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
                
                try { drops.save(App.dropTableFile); } catch (IOException err) {}
                try { b.getWorld().dropItemNaturally(b.getLocation(), item); } catch(IllegalArgumentException er) {}
            }

            DiscoveredBlock bl = new DiscoveredBlock(e.getBlock().getType(), e.getPlayer().getUniqueId(), newMaterial);
            if (!DropFileHandler._DiscoveredBlocks.contains(bl)) {
                if (!App.BlockDictionary_PlayerDependent) {
                    boolean foundEntry = false;
                    for(DiscoveredBlock block : DropFileHandler._DiscoveredBlocks) {
                        if (newMaterial.name().equals(block.newMat.name())) {
                            foundEntry = true;
                            return;
                        }
                    }
                    if (!foundEntry) {
                        DropFileHandler._DiscoveredBlocks.add(bl);
                        return;
                    }
                }
                DropFileHandler._DiscoveredBlocks.add(bl);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (!(e.getEntity() instanceof Player) && killer != null && killer instanceof Player) {
            List<ItemStack> drops = e.getDrops();
            for(ItemStack i : drops) {
                Material oldMaterial = i.getType();
                if (MaterialHandler.isLootTable(oldMaterial)) {
                    LootTable tbl = DropFileHandler.DropTableWithLoot.get(oldMaterial).getLootTable();

                    Location blockLoc = e.getEntity().getLocation();
                    try {
                        String cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " " + blockLoc.getBlockZ() + " loot " + tbl.getKey().getKey();
                        Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd);
                        /*cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " " + blockLoc.getBlockZ() + " kill " + tbl.getKey().getKey();
                        Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd);
                        cmd = "loot spawn " + blockLoc.getBlockX() + " " + blockLoc.getBlockY() + " " + blockLoc.getBlockZ() + " mine " + tbl.getKey().getKey();
                        Bukkit.getServer().dispatchCommand(new CustomCommandSender(), cmd); */} catch (CommandException exc) {}
                } else {
                    Material newMaterial = DropFileHandler.DropTable.get(oldMaterial);    
                    try {
                        e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack(newMaterial));
                    } catch (IllegalArgumentException exception) {
                        YamlConfiguration dropFile = YamlConfiguration.loadConfiguration(App.dropTableFile);
                        String itemName = App.items[new Random().nextInt(App.items.length)].name();
                        dropFile.set(oldMaterial.name(), itemName);
                        
                        try { dropFile.save(App.dropTableFile); } catch (IOException err) {}
                        try { e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack(newMaterial)); } catch(IllegalArgumentException exc) {}
                    }

                    String entityName = e.getEntity().getName();

                    if (entityName.contains("x "))
                        entityName = entityName.split("x ")[1];
                    DiscoveredBlock bl = new DiscoveredBlock(entityName, e.getEntity().getKiller().getUniqueId(), newMaterial);
                    if (!DropFileHandler._DiscoveredBlocks.contains(bl)) {
                        if (!App.BlockDictionary_PlayerDependent) {
                            for(DiscoveredBlock block : DropFileHandler._DiscoveredBlocks) {
                                if (newMaterial.name().equals(block.newMat.name())) {
                                    DropFileHandler._DiscoveredBlocks.remove(block);
                                    break;
                                }
                            }
                            DropFileHandler._DiscoveredBlocks.add(bl);
                        } else {
                            boolean found = false;
                            for(DiscoveredBlock block : DropFileHandler._DiscoveredBlocks) {
                                if (block.discoverer == killer.getUniqueId() && block.newMat == newMaterial) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                DropFileHandler._DiscoveredBlocks.add(bl);
                        }
                    }
                }
            }
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        InventoryView inv = player.getOpenInventory();
        if (inv.getTitle().split(" -")[0].equals("Discovered blocks")) {            
            event.setCancelled(true);
            int page = Integer.parseInt(inv.getTitle().split("Page ")[1]) - 1;
            if (clicked != null && clicked.getItemMeta() != null && clicked.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Next Page")) {
                player.openInventory(Commands.PlayerBlockInventories.get(player.getUniqueId()).get(page + 1));
            }
            else if (clicked != null && clicked.getItemMeta() != null && clicked.getItemMeta().getDisplayName().equals(ChatColor.BOLD + "Previous Page") && page > 0) {
                player.openInventory(Commands.PlayerBlockInventories.get(player.getUniqueId()).get(page - 1));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = (Player)event.getPlayer();
        InventoryView inv = player.getOpenInventory();
        if (inv == null)
            return; 
        if (inv == null || !inv.getTitle().split(" -")[0].equals("Discovered blocks")) {
            Commands.PlayerBlockInventories.remove(player.getUniqueId());
        }
    }
}