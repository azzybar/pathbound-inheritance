// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.item;
import dev.azzybar.pathbound.Config;

// For handling and reading files
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.List;

final class ItemLoader {
    // Paths leading to each item config file
    private static final Path armorPath =  Config.APP_DIR.resolve("items", "armor" + Config.ITEM_EXT);
    private static final Path weaponsPath =  Config.APP_DIR.resolve("items", "weapons" + Config.ITEM_EXT);

    // Prevents other classes from instantiating this
    private ItemLoader() {}

    // Is called by the registry to populate it
    static void loadAll() throws IOException {
        load(armorPath, ItemLoader::parseArmor);
        load(weaponsPath, ItemLoader::parseWeapons);
    }

    // Loads file and calls corresponding parser
    private static void load(Path path, Function<String[], List<Item>> parser) throws IOException {
        // Figuring what type of item is being loaded
        String separator = path.getFileSystem().getSeparator();
        String type = path.toString();
        type = type.substring(type.lastIndexOf(separator) + 1, type.indexOf(Config.ITEM_EXT));
        System.out.println("Adding " + type + " from: \u001B[36m" + path + "\u001B[0m");

        // Configs and adds each item to the item registry
        try (Stream<String> stream = Files.lines(path)) {
            stream.map(line -> line.trim())
                  .map(line -> line.split("-"))
                  .map(parser)
                  .flatMap(List::stream)
                  .forEach(ItemRegistry::register);
        }

        System.out.println("\u001B[92mSuccessfully added " + type + "\u001B[0m");
    }

    // Parser specifically for armor
    private static List<Item> parseArmor(String[] data) {
        // Sorts data
        Armor.Attire attire = Armor.Attire.fromString(data[0]);
        double def = Double.parseDouble(data[1]);
        double spd = Double.parseDouble(data[2]);
        int eva = Integer.parseInt(data[3]);

        // Creates armor set
        return List.of(
            new Armor(attire, Armor.Slot.HEAD, def, spd, eva),
            new Armor(attire, Armor.Slot.BODY, def, spd, eva),
            new Armor(attire, Armor.Slot.ARMS, def, spd, eva),
            new Armor(attire, Armor.Slot.LEGS, def, spd, eva)
        );
    }

    // Parser specifically for weapons
    private static List<Item> parseWeapons(String[] data) {
        // Sorts data
        String type = data[0];
        String name = data[1];
        int dmg = Integer.parseInt(data[2]);
        int weight = Integer.parseInt(data[3]);

        // Creates and sorts weapon into list
        double spd = (weight < 4) ? 0.1 : 0.25;
        return List.of(new Weapon(name, dmg, spd));
    }
}
