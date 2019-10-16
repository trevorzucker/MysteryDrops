package mdrops.mysterydrops;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHelper {
    
    public static Map<String, HashMap<Method, Integer>> commands = new HashMap<>();

    public static void RegisterCommand(String command, String methodName) {
        Method cmd = (Method)null;
        try {
            cmd = Commands.class.getMethod(methodName, CommandSender.class, Command.class, String.class, String[].class);
        } catch (NoSuchMethodException | SecurityException e) {}
        commands.put(command, new HashMap<Method, Integer>());
        commands.get(command).put(cmd, 0);
    }

    public static Player AutoCompleteName(String name, Player caller) {
        Player p = null;

        for(Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getDisplayName().contains(name)) {
                if (caller == null) {
                    p = pl;
                    break;
                } else if (!pl.getUniqueId().equals(caller.getUniqueId())) {
                    p = pl;
                    break;
                }
            }
        }
        return p;
    }
}