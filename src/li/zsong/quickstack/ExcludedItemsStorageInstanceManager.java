package li.zsong.quickstack;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ExcludedItemsStorageInstanceManager implements Listener {
    static HashMap<String, ExcludedItemsStorage> instances = new HashMap<>();

    public static ExcludedItemsStorage getInstance(Player p) {
        if (instances.containsKey(p.getName()))
            return instances.get(p.getName());
        else
            throw new IllegalStateException(
                    "Player " + p.getName() + " does not have an ExcludedItemsStorage instance!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        instances.put(e.getPlayer().getName(), new ExcludedItemsStorage(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        instances.remove(e.getPlayer().getName());
    }
}
