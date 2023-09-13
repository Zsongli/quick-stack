package li.zsong.quickstack;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    public static String getMessagePrefix() {
        return String.format("%s[%sQuick Stack%s] %s", ChatColor.DARK_GRAY, ChatColor.DARK_BLUE, ChatColor.DARK_GRAY,
                ChatColor.GRAY);
    }

    public static String prefixed(String message) {
        return getMessagePrefix() + message;
    }

    @Override
    public void onEnable() {
        getCommand("quickstack").setExecutor(new QuickStackCommandExecutor(this));

        ExcludedItemsStorage.setKey(new NamespacedKey(this, "excludedItems"));
        getServer().getPluginManager().registerEvents(new ExcludedItemsStorageInstanceManager(), this);

        getServer().getConsoleSender()
                .sendMessage(getMessagePrefix() + "Plugin loaded!");
    }
}
