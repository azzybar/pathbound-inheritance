// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;

import java.util.List;
import java.util.ArrayList;

public final class ItemRegistry {
    private static List<Item> items = new ArrayList<Item>();

    // Prevents other classes from instantiating this
    private ItemRegistry() {}

    // Returns all the list of items
    public static List<Item> getAll() { return items; }

    // Returns all items of a specfic subtype
    public static <T extends Item> List<T> getAllOfType(Class<T> type) {
        return items.stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .toList();
    }

    // Adds item to list if not already added
    public static void register(Item item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }
}
