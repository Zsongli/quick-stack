// this is one ugly ass file

package li.zsong.quickstack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class QuickStackCommandExecutor implements CommandExecutor {

    private Plugin plugin;

    public QuickStackCommandExecutor(Plugin instance) {
        this.plugin = instance;
    }

    private void excludeItem(ExcludedItemsStorage excludedItems, Material item) {
        if (excludedItems.contains(item)) {
            excludedItems.remove(item);
            excludedItems.getPlayer().sendMessage(Plugin.prefixed(String.format("Removed %s%s %sfrom excluded items.",
                    ChatColor.GREEN, MaterialUtils.getFriendlyName(item),
                    ChatColor.GRAY)));
        } else {
            excludedItems.add(item);
            excludedItems.getPlayer().sendMessage(Plugin.prefixed(String.format("Added %s%s %sto excluded items.",
                    ChatColor.GREEN,
                    MaterialUtils.getFriendlyName(item),
                    ChatColor.GRAY)));
        }
    }

    private boolean canPlayerSeeBlock(Player player, Block block, int maxDistance) {
        var boxes = block.getCollisionShape().getBoundingBoxes();
        var points = new ArrayList<Vector>();
        for (var box : boxes) {
            points.add(box.getMin());
            points.add(box.getMax());
            points.add(box.getCenter());
            points.add(new Vector(box.getMin().getX(), box.getMin().getY(), box.getMax().getZ()));
            points.add(new Vector(box.getMin().getX(), box.getMax().getY(), box.getMin().getZ()));
            points.add(new Vector(box.getMax().getX(), box.getMin().getY(), box.getMin().getZ()));
            points.add(new Vector(box.getMax().getX(), box.getMax().getY(), box.getMin().getZ()));
            points.add(new Vector(box.getMax().getX(), box.getMin().getY(), box.getMax().getZ()));
            points.add(new Vector(box.getMin().getX(), box.getMax().getY(), box.getMax().getZ()));
        }

        for (var point : points) {
            var result = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
                    block.getLocation().toVector().add(point).subtract(player.getEyeLocation().toVector()), maxDistance,
                    FluidCollisionMode.NEVER, true);
            if (result == null)
                continue;
            if (result.getHitBlock().getLocation().equals(block.getLocation()))
                return true;
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0) {
            switch (args[0]) {
                case "range": // /quickstack range <range>
                    if (!sender.hasPermission("quickstack.range")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                        return true;
                    }

                    if (args.length < 2) {
                        sender.sendMessage(Plugin.prefixed(String.format("Range is currently set to %s%s%s.",
                                ChatColor.GREEN,
                                Optional.ofNullable(plugin.getConfig().getString("range"))
                                        .orElse("8"),
                                ChatColor.GRAY)));
                        return true;
                    }

                    boolean invalid = false;
                    try {
                        int range = Integer.parseInt(args[1]);
                        if (range < 0)
                            invalid = true;

                    } catch (NumberFormatException e) {
                        invalid = true;
                    }

                    if (invalid) {
                        sender.sendMessage(ChatColor.RED + "Range must be a positive integer!");
                        return true;
                    }

                    var range = args[1];

                    plugin.getConfig().set("range", range);
                    plugin.saveConfig();

                    sender.sendMessage(
                            Plugin.prefixed(String.format("Range set to %s%s%s.", ChatColor.GREEN, range,
                                    ChatColor.GRAY)));
                    return true;
                case "exclude":
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
                        return true;
                    }

                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "Option or item is required!");
                        return true;
                    }

                    var excludedItems = ExcludedItemsStorageInstanceManager.getInstance(player);

                    if (args[1].equals("list")) { // /quickstack exclude list
                        final var nl = "\n"; // to supress warning
                        player.sendMessage(Plugin.prefixed(String.format("Excluded items:%s%s%s",
                                nl, ChatColor.GREEN,
                                excludedItems.getItems().isEmpty() ? "None"
                                        : String.join("\n", excludedItems.getItems().stream()
                                                .map(MaterialUtils::getFriendlyName).toList()))));
                        return true;
                    } else if (args[1].equals("this")) { // /quickstack exclude this
                        var item = player.getInventory().getItemInMainHand().getType();

                        if (item == Material.AIR) {
                            player.sendMessage(ChatColor.RED + "You must be holding an item!");
                            return true;
                        }

                        excludeItem(excludedItems, item);
                        return true;

                    } else if (args[1].equals("clear")) {
                        excludedItems.clear();
                        player.sendMessage(Plugin.prefixed("Cleared excluded items."));
                        return true;
                    } else { // /quickstack exclude <item>
                        var itemId = args[1].startsWith("minecraft:") ? args[1] : "minecraft:" + args[1];
                        var item = Material.matchMaterial(itemId);

                        if (item == null) {
                            player.sendMessage(ChatColor.RED + "Unknown item!");
                            return true;
                        }

                        excludeItem(excludedItems, item);
                        return true;
                    }

                default:
                    return false;
            }
        }

        if (!sender.hasPermission("quickstack.use")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        if (!(sender instanceof Player player))
            return true;

        if (!plugin.getConfig().contains("range"))
            plugin.getConfig().set("range", 8);

        int radius;
        try {
            radius = Integer.parseInt(plugin.getConfig().getString("range"));
        } catch (NumberFormatException e) {
            radius = 8;
        }

        var chests = new HashMap<Vector, Inventory>();

        // get blocks around player in radius
        for (int x = radius; x >= -radius; x--) {
            for (int y = radius; y >= -radius; y--) {
                for (int z = radius; z >= -radius; z--) {
                    var block = player.getWorld().getBlockAt(player.getLocation().add(x, y, z));
                    if (block.getType() != Material.CHEST)
                        continue;

                    var someChestBlock = (Chest) block.getState();
                    var someChestInventoryHolder = someChestBlock.getInventory().getHolder();

                    if (!canPlayerSeeBlock(player, block, radius))
                        continue;

                    if (someChestInventoryHolder instanceof DoubleChest) {
                        var doubleChest = (DoubleChest) someChestInventoryHolder; // here we cast to DoubleChest so
                                                                                  // getLocation returns
                        // the average of the two locations
                        if (chests.containsKey(doubleChest.getLocation().toVector()))
                            continue;
                        chests.put(doubleChest.getLocation().toVector(), doubleChest.getInventory());
                    } else {
                        var chest = (Chest) someChestInventoryHolder;
                        if (chests.containsKey(chest.getLocation().toVector()))
                            continue;
                        chests.put(chest.getLocation().toVector(), chest.getInventory());
                    }
                }
            }
        }

        var sortedChests = chests.values().stream()
                .sorted((a, b) -> Double.compare(a.getLocation().distance(player.getLocation()),
                        b.getLocation().distance(player.getLocation())))
                .toList(); // prioritize chests closer to player in case of multiple options

        var excludedItems = ExcludedItemsStorageInstanceManager.getInstance(player);
        var inventory = player.getInventory();
        int count = 0; // number of item stacks that quick stack had an effect on

        for (int i = 9; i < inventory.getSize(); i++) { // start from 9 to ignore hotbar
            var stack = inventory.getItem(i);

            if (stack == null || stack.getType() == Material.AIR)
                continue;

            // ignore armor or items in offhand
            if (stack.equals(inventory.getItemInOffHand())
                    || Arrays.asList(inventory.getArmorContents()).contains(stack))
                continue;

            if (excludedItems.contains(stack.getType())) // ignore this stack if it's excluded
                continue;

            var itemsToStore = stack.getAmount();
            var originalAmount = stack.getAmount();
            for (var chest : sortedChests) {

                boolean containsItem = false;
                for (var stackInChest : chest) { // try to fit the item into existing stacks
                    if (stackInChest == null || stackInChest.getType() != stack.getType())
                        continue;

                    containsItem = true;
                    var fitsInStack = Math.min(stack.getMaxStackSize(), chest.getMaxStackSize())
                            - stackInChest.getAmount();
                    var amountToStore = Math.min(fitsInStack, itemsToStore);

                    stackInChest.setAmount(stackInChest.getAmount() + amountToStore);
                    itemsToStore -= amountToStore;
                }

                if (!containsItem || itemsToStore == 0) // if we've stored all the items into existing stacks or the
                                                        // chest doesn't contain this type of item, we can go onto the
                                                        // next chest
                    continue;

                for (int j = 0; j < chest.getSize(); j++) { // try to fit the item into empty slots
                    var stackInChest = chest.getItem(j);
                    if (stackInChest != null && stackInChest.getType() != Material.AIR)
                        continue;

                    var fitsInStack = Math.min(stack.getMaxStackSize(), chest.getMaxStackSize());
                    var amountToStore = Math.min(fitsInStack, itemsToStore);

                    var newStack = new ItemStack(stack.getType(), amountToStore);
                    chest.setItem(j, newStack);

                    itemsToStore -= amountToStore;
                }
            }

            stack.setAmount(itemsToStore);

            if (stack.getAmount() != originalAmount)
                count++;
        }

        if (count > 0)
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);

        player.sendMessage(
                Plugin.prefixed(String.format("Quick stacked %s%s%s item %s to nearby chests.", ChatColor.GREEN,
                        count, ChatColor.GRAY, count == 1 ? "stack" : "stacks")));

        return true;
    }
}
