package li.zsong.quickstack;

import java.util.Arrays;
import java.util.stream.Stream;

import org.bukkit.Material;

public class MaterialUtils {

    private MaterialUtils() {
    }

    public static String getFriendlyName(Material material) {
        var capitalized = Arrays.stream(material.getKey().getKey().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()).toList();
        return String.join(" ",
                Stream.concat(capitalized.stream().limit(1), capitalized.stream().skip(1).map(word -> word.equals("Of") ? "of" : word)
                        .map(word -> word.equals("The") ? "the" : word)).toList());
    }

    public static String getID(Material material) {
        return "minecraft:" + material.getKey().getKey().toLowerCase();
    }
}
