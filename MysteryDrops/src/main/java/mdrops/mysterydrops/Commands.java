package mdrops.mysterydrops;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Commands {

    public static boolean RegenerateCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p = (Player)sender;
        App.getPlugin(App.class).getLogger().warning(p.getDisplayName() + " is regenerating drop tables.");
        p.sendMessage(ChatColor.WHITE + "[SERVER] " + ChatColor.GREEN + "Regenerating droptables..");
        for(Player pl : Bukkit.getServer().getOnlinePlayers()) {
            if (pl.getUniqueId() != p.getUniqueId()) {
                pl.sendMessage(ChatColor.WHITE + "[SERVER] " + ChatColor.GREEN + "Droptables are being regenerated!");
            }
            pl.sendTitle("", p.getDisplayName() + " is re-randomizing drops!", 10, 70, 20);
        }

        File dropTableFile = new File(App.getPlugin(App.class).getDataFolder(), "droptable.yml");
        dropTableFile.delete();
        if (!dropTableFile.exists()) {
            DropFileHandler.CreateDropFile();
        }
        DropFileHandler.LoadDropTables();

        p.sendMessage(ChatColor.WHITE + "[SERVER] " + ChatColor.GREEN + "New droptables loaded!");
        
        return true;
    }

    public static HashMap<UUID, ArrayList<Inventory>> PlayerBlockInventories = new HashMap<>();

    public static boolean BlocksCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!App.BlockDictionary_Enabled) {
            sender.sendMessage("This command has been disabled");
            return true;
        }

        Player ply = (Player)sender;

        ArrayList<Inventory> menus = new ArrayList<>();
        menus.add(Bukkit.createInventory(null, 27, "Discovered blocks - Page 1"));

        // create "next" and "previous" itemstacks
        ItemStack next = new ItemStack(Material.ENCHANTED_BOOK); ItemStack previous = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta nextMeta = next.getItemMeta(); ItemMeta previousMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.BOLD + "Next Page"); previousMeta.setDisplayName(ChatColor.BOLD + "Previous Page");
        next.setItemMeta(nextMeta); previous.setItemMeta(previousMeta);

        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();

        for(DiscoveredBlock block : DropFileHandler._DiscoveredBlocks) {
            if (block == null || (App.BlockDictionary_PlayerDependent && block.discoverer != ply.getUniqueId()) || Bukkit.getPlayer(block.discoverer) == null)
                continue;

            Material newMat = block.newMat; ItemStack stack = new ItemStack(newMat);

            String oldName = "";
            if (!block.isFromMob) {
                String[] nameSplit = block.old.name().replace("_", " ").toLowerCase().split(" ");
                for(int j = 0; j < nameSplit.length; j++) {
                    nameSplit[j] = nameSplit[j].substring(0, 1).toUpperCase() + nameSplit[j].substring(1);
                    if (j != nameSplit.length - 1)
                        oldName += nameSplit[j] + " ";
                    else
                        oldName += nameSplit[j];
                }
            }
            else
                oldName = block.mobName;

            ItemMeta meta = stack.getItemMeta();
            String discovererName = Bukkit.getPlayer(block.discoverer).getDisplayName();
            if (block.discoverer == ply.getUniqueId())
                discovererName = "you";

            meta.setLore(Arrays.asList("Discovered by " + ChatColor.BOLD + discovererName, "Dropped from " + oldName));
            stack.setItemMeta(meta);
            stacks.add(stack);
        }

        if (App.ShowLootTablesInBlocks) {
            for(DiscoveredLootTable table : DropFileHandler.DiscoveredLootTables) {

                if (table == null || (App.BlockDictionary_PlayerDependent && table.discoverer != ply.getUniqueId()) || Bukkit.getPlayer(table.discoverer) == null)
                    continue;

                // make a "user friendly" loot table display name

                String lootTableName = "";
                String[] preSeperator = table.table.getKey().getKey().split("/");
                String seperator = preSeperator[preSeperator.length - 1];
                if (preSeperator.length > 1) {
                    seperator = "";
                    for(int i = preSeperator.length - 1; i > 0; i--) {
                        seperator += preSeperator[i];
                        if (i > 1)
                            seperator += " ";
                    }
                }

                String[] nameSplit = seperator.replace("_", " ").toLowerCase().split(" ");
                for(int j = 0; j < nameSplit.length; j++) {
                    nameSplit[j] = nameSplit[j].substring(0, 1).toUpperCase() + nameSplit[j].substring(1);
                    if (j != nameSplit.length - 1)
                        lootTableName += nameSplit[j] + " ";
                    else
                        lootTableName += nameSplit[j];
                }
                lootTableName += " Loot Table";

                String oldName = "";
                String[] nameSplit2 = table.old.name().replace("_", " ").toLowerCase().split(" ");
                for(int j = 0; j < nameSplit2.length; j++) {
                    nameSplit2[j] = nameSplit2[j].substring(0, 1).toUpperCase() + nameSplit2[j].substring(1);
                    if (j != nameSplit.length - 1)
                        oldName += nameSplit2[j] + " ";
                    else
                        oldName += nameSplit2[j];
                }

                ItemStack skull = new ItemStack(Material.PLAYER_HEAD); SkullMeta meta = (SkullMeta)skull.getItemMeta();
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(table.discoverer)); meta.setDisplayName(lootTableName);
                String discovererName = Bukkit.getOfflinePlayer(table.discoverer).getName();

                if (table.discoverer == ply.getUniqueId())
                    discovererName = "you";
            
                    meta.setLore(Arrays.asList("Discovered by "  + ChatColor.BOLD + discovererName, "Dropped from " + oldName));

                skull.setItemMeta(meta);

                stacks.add(skull);
            }

        }

        stacks.sort(new Comparator<ItemStack>() {
            @Override
            public int compare(ItemStack v1, ItemStack v2) {
                if (v1.getType() == Material.PLAYER_HEAD)
                    return -1;
                else if (v2.getType() == Material.PLAYER_HEAD)
                    return 1;

                String firstName = "";
                String[] nameSplit = v1.getType().name().replace("_", " ").toLowerCase().split(" ");
                for(int j = 0; j < nameSplit.length; j++) {
                    nameSplit[j] = nameSplit[j].substring(0, 1).toUpperCase() + nameSplit[j].substring(1);
                    if (j != nameSplit.length - 1)
                        firstName += nameSplit[j] + " ";
                    else
                        firstName += nameSplit[j];
                }

                String secondName = "";
                String[] nameSplit2 = v2.getType().name().replace("_", " ").toLowerCase().split(" ");
                for(int j = 0; j < nameSplit2.length; j++) {
                    nameSplit2[j] = nameSplit2[j].substring(0, 1).toUpperCase() + nameSplit2[j].substring(1);
                    if (j != nameSplit2.length - 1)
                        secondName += nameSplit2[j] + " ";
                    else
                        secondName += nameSplit2[j];
                }
                return firstName.compareTo(secondName);
            }
        });

        if (stacks.size() >= 27) {
            menus.get(0).setItem(26, next);
        }

        int currentMenu = 0;

        for(int i = 0; i < stacks.size(); i++) {
            menus.get(currentMenu).addItem(stacks.get(i));
            if (menus.get(currentMenu).firstEmpty() == -1) {
                currentMenu++;
                menus.add(Bukkit.createInventory(null, 27, "Discovered blocks - Page " + (currentMenu + 1)));
                if (stacks.size() >= (27 * (currentMenu + 1) - 2)) {
                    menus.get(currentMenu).setItem(26, next);
                }
                menus.get(currentMenu).setItem(18, previous);
            }
        }
        
        Player p = (Player)sender;
        p.openInventory(menus.get(0));
        PlayerBlockInventories.put(p.getUniqueId(), menus);
        return true;
    }
}