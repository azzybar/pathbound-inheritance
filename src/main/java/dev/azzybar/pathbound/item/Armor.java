// Copyright 2026 Azrael Baram (azzybar)
// SPDX-License-Identifier: Apache-2.0

package dev.azzybar.pathbound.item;

/*
Armor Guide:
    Piece:
    - Head = 10% DEF increasee
    - Torso = 15% DEF increase
    - Arms = 10% SPD decrease
    - Legs = 15% EVA decrease
    Weight:
    - Light = 10% initiative delay
    - Medium = 15% initiative delay
    - Heavy = 20% initiative delay

ARMOR
"padded" - 11 AC, 8lb, light
"scale" - 14 AC, 45lb, medium
"chainmail" - 16 AC, 55lb, heavy
"splinted" - 17 AC, 60lb, heavy
"plate" - 18 AC, 65lb, heavy

FORMULA
- DEF: AC
- SPD: Weight
- EVA: sqrt(Weight) / 100

0, (AC * 1.1), SPD, (Math.sqrt(EVA) / 100)),
1, (AC * 1.15), SPD, (Math.sqrt(EVA) / 100)),
2, AC, (SPD * 1.1), (Math.sqrt(EVA) / 100)),
3, AC, SPD, (Math.sqrt(EVA) / 100) * 1.15)
*/

/**
 * Stats regarding are immutable for simplicity
 * Represented as percentages
 *
 * DEF is positive, higher value = higher damage reduction
 * SPD is negative, higher value = lower initiative score
 * EVA is negative, higher value = lower dodge chance
 */

public record Armor(Attire attire, Slot slot, double def, double spd, double eva) implements Item {
    // Possible armor sets
    public enum Attire {
        PADDED, SCALE, CHAINMAIL, SPLINTED, PLATE;

        // Helper function when parsing file
        public static Attire fromString(String value) {
            return valueOf(value.trim().toUpperCase());
        }
    }

    // Indicates what part of the body
    public enum Slot { HEAD, BODY, ARMS, LEGS }

    // Canonical constructor
    public Armor(Attire attire, Slot slot, double def, double spd, double eva) {
        this.attire = attire;
        this.slot = slot;

        this.def = switch (slot) {
            case HEAD -> def * 1.1;
            case BODY -> def * 1.15;
            case ARMS, LEGS -> def;
        };

        this.spd = switch (slot) {
            case ARMS -> spd * 1.1;
            case HEAD, BODY, LEGS -> spd;
        };

        this.eva = switch (slot) {
            case LEGS -> (Math.sqrt(eva) / 100) * 1.15;
            case HEAD, BODY, ARMS -> Math.sqrt(eva) / 100;
        };
    }

    // Returns the attire this piece if part of
    @Override
    public String name() {
        return attire.name().toLowerCase();
    }

    // For printing stats in the chest popup
    @Override
    public String[] getStats() {
        String[] stats = new String[4];

        // Creates labels to be presented in chest popup
        stats[0] = (slot.ordinal() <= 1 ? "You found a " : "You found ") + this;
        stats[1] = String.format("DEF: +%.1f%%", (def * 100));
        stats[2] = String.format("SPD: -%.1f%%", (spd * 100));
        stats[3] = String.format("EVA: -%.1f%%", (eva * 100));

        return stats;
    }

    // For printing to the console
    @Override
    public String toString() {
        String descriptor = name() + " ";

        // Checks what armor piece it is
        switch (slot) {
            case HEAD:
                descriptor += ((name().equals("padded")
                    || name().equals("scale")) ? "helmet"
                    : name().equals("chainmail") ? "coif" : "sallet");
                break;
            case BODY:
                descriptor += (name().equals("padded") ? "gambeson"
                    : name().equals("scale") ? "brigandine"
                    : name().equals("chainmail") ? "hauberk" : "cuirass");
                break;
            case ARMS:
                descriptor += (name().equals("padded") ? "bracers"
                    : name().equals("scale") ? "pauldrons"
                    : (name().equals("chainmail")
                    || name().equals("plate")) ? "gauntlets" : "vambraces");
                break;
            case LEGS:
                descriptor += (name().equals("padded") ? "trousers"
                    : name().equals("scale") ? "sabatons"
                    : name().equals("chainmail") ? "chausses" : "greave");
                break;
            default:
                break;
        }

        return descriptor;
    }
}
