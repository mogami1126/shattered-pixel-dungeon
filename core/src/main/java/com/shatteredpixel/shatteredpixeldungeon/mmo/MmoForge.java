package com.shatteredpixel.shatteredpixeldungeon.mmo;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import java.math.BigInteger;
import java.util.Map;

/** Exact forging credits. No rank, combat formula or enemy generation changes. */
public final class MmoForge {
    private MmoForge() {}
    public static final String ACTION = "MMO_FORGE";
    // Common denominator of hundredths and thirds, without floating point rounding.
    public static final BigInteger UNIT = BigInteger.valueOf(300);
    private static int category(Item item) {
        if (item instanceof MissileWeapon) return 1;
        if (item instanceof MeleeWeapon) return 0;
        if (item instanceof Armor) return 2;
        if (item instanceof Ring) return 3;
        if (item instanceof Wand) return 4;
        if (item instanceof Seed) return 5;
        if (item instanceof Runestone) return 6;
        if (item instanceof Potion) return 7;
        if (item instanceof Scroll) return 8;
        return -1;
    }
    public static boolean target(Item item) {
        return MmoRules.on(Challenges.UNLIMITED_GROWTH) && item != null
                && category(item) >= 0 && category(item) <= 4 && category(item) != 1 && item.isUpgradable();
    }
    public static boolean material(Item receiver, Item source, Hero hero) {
        return target(receiver) && source != null && source != receiver && source.quantity() > 0
                && !source.isEquipped(hero) && hero.belongings.backpack.contains(source)
                && units(receiver, source).signum() > 0;
    }
    public static int coefficient(Item receiver, Item source) {
        int a = category(receiver), b = category(source);
        if (a < 0 || b < 0 || a == 1 || b == 1) return 0;
        return a == b ? 90 : 81;
    }
    public static BigInteger units(Item receiver, Item source) {
        if (receiver == source || source == null || receiver instanceof MissileWeapon || source instanceof MissileWeapon) return BigInteger.ZERO;
        if (source instanceof Potion || source instanceof Scroll) return UNIT;
        if (source instanceof Seed || source instanceof Runestone) return UNIT.divide(BigInteger.valueOf(3));
        return source.mmoLevel().add(BigInteger.ONE).max(BigInteger.ZERO)
                .multiply(BigInteger.valueOf(coefficient(receiver,source)*3L));
    }
    public static BigInteger total(Item receiver, Map<Item,Integer> selection) {
        BigInteger sum = BigInteger.ZERO;
        for (Map.Entry<Item,Integer> e : selection.entrySet())
            sum = sum.add(units(receiver,e.getKey()).multiply(BigInteger.valueOf(e.getValue())));
        return sum;
    }
    public static BigInteger levels(Item receiver, Map<Item,Integer> selection) {
        return total(receiver,selection).add(UNIT.divide(BigInteger.TWO)).divide(UNIT);
    }
    /** Validate the complete selection before consuming anything. No turn is spent here. */
    public static boolean apply(Hero hero, Item receiver, Map<Item,Integer> selection) {
        if (!target(receiver) || selection.isEmpty() || hero.belongings.lostInventory()
                || !(receiver.isEquipped(hero) || hero.belongings.backpack.contains(receiver))) return false;
        for (Map.Entry<Item,Integer> e : selection.entrySet())
            if (e.getValue() == null || e.getValue() <= 0 || e.getValue() > e.getKey().quantity()
                    || !material(receiver,e.getKey(),hero)) return false;
        BigInteger result = levels(receiver,selection);
        for (Map.Entry<Item,Integer> e : selection.entrySet()) {
            Item source = e.getKey();
            if (source.quantity() == e.getValue()) source.detachAll(hero.belongings.backpack);
            else source.quantity(source.quantity()-e.getValue());
        }
        MmoRules.addUpgrades(receiver,result);
        MmoRules.revealUpgradeAndCurse(receiver);
        Item.updateQuickslot();
        return true;
    }
}
