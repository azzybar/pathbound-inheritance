// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;

// Meant to be able to return either a weapon or piece
// of a armor from a chest. Also for any future item
// subclasses if developed.
import javafx.scene.control.Label;
public abstract class Item {
    private final String name;
    
    // Coonstructor
    protected Item(String name) {
        this.name = name;
    }
    
    // Override based on item
    public abstract Label[] getStats();
    
    // Getters
    public String getName() { return name; }
    
    // Items have fixed stats and are copied when created
    public boolean equals(Weapon that) {
        return this.name.equals(that.getName());
    }
}
