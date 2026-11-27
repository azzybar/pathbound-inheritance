// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.internal;

/*
Weight Guide:
- Light = 1-3 lbs (10% initiative delay)
- Heavy = 4+ lbs (25% initiative delay)

BLUDGEONS
"battleaxe" - 1d8, 4lb
"club" - 1d4, 2lb
"flail" - 1d8, 2lb
"mace" - 1d6, 4lb
"quarterstaff" - 1d6, 4lb
"warhammer" - 1d8, 5lb

BLADED
"dagger" - 1d4, 1lb
"longsword" - 1d8, 3lb
"rapier" - 1d8, 2lb
"shortsword" - 1d6, 2lb
"scimitar" - 1d6, 3lb

POLEARMS
"spear" - 1d6, 3lb
"glaive" - 1d10, 6lb
"pike" - 1d10, 18lb
"halberd" - 1d10, 6lb
"lance" - 1d10, 6lb
*/

public record Weapon(String name, int dmg, double spd) implements Item {
    // For printing stats in the chest popup
    @Override
    public String[] getStats() {
        String[] stats = new String[3];

        // Creates labels to be presented in chest popup
        stats[0] = "You found a " + name;
        stats[1] = String.format("DMG: 1d%d", dmg);
        stats[2] = String.format("SPD: -%d%%", ((int) (spd * 100)));

        return stats;
    }

    // For printing to the console
    @Override
    public String toString() {
        return String.format("%s (1d%d), -%d%% speed", name, dmg, ((int) (spd * 100)));
    }
}
