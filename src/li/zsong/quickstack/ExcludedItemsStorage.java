package li.zsong.quickstack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ExcludedItemsStorage {

    private PersistentDataContainer data;
    private Player player;
    private ArrayList<Material> excludedItems;
    private NamespacedKey key;

    public ExcludedItemsStorage(Player p, NamespacedKey key) {
        this.data = p.getPersistentDataContainer();
        this.player = p;
        this.key = key;

        if (!this.data.has(key, PersistentDataType.STRING))
            this.data.set(key, PersistentDataType.STRING, "");

        this.excludedItems = deserialize(this.data.get(key, PersistentDataType.STRING));
    }

    private static String serialize(ArrayList<Material> excludedItems) {
        return String.join(";", excludedItems.stream().map(MaterialUtils::getID).toList());
    }

    private static ArrayList<Material> deserialize(String serialized) {
        return new ArrayList<>(
                Arrays.stream(serialized.split(";")).map(Material::matchMaterial).filter(Objects::nonNull).toList());
    }

    public void add(Material material) {
        if (contains(material))
            return;

        excludedItems.add(material);
        data.set(key, PersistentDataType.STRING, serialize(excludedItems));
    }

    public void remove(Material material) {
        var index = IntStream.range(0, excludedItems.size()).parallel()
                .filter(i -> excludedItems.get(i).name().equals(material.name())).findFirst();
        if (index.isEmpty())
            return;

        excludedItems.remove(index.getAsInt());
        data.set(key, PersistentDataType.STRING, serialize(excludedItems));
    }

    public void clear() {
        excludedItems.clear();
        data.set(key, PersistentDataType.STRING, serialize(excludedItems));
    }

    public boolean contains(Material material) {
        return excludedItems.stream().parallel().anyMatch(m -> m.name().equals(material.name()));
    }

    public Collection<Material> getItems() {
        return excludedItems;
    }

    public Player getPlayer() {
        return player;
    }
}
