// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;

// Meant to be able to return either a weapon or piece
// of a armor from a chest. Also for any future item
// subclasses if developed.
public sealed interface Item permits Armor, Weapon {
    String name();
    String[] getStats();
}
