package com.shatteredpixel.shatteredpixeldungeon.mmo;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.*;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;
import java.math.BigInteger;

/** Optional first-edition rules. Native health remains a stage coordinate, not a magnitude. */
public final class MmoRules {
    private MmoRules() {}
    private static final ThreadLocal<Char> procTarget = new ThreadLocal<>();
    public static boolean on(int flag) { return Dungeon.isChallenged(flag); }
    /** Supply category bags once, including when an existing large-bag save is loaded. */
    public static void ensureCategoryBags() {
        if (!on(Challenges.LARGE_BAGS) || Dungeon.hero == null || Dungeon.hero.belongings.lostInventory()) return;
        ensureCategoryBag(VelvetPouch.class, Dungeon.LimitedDrops.VELVET_POUCH);
        ensureCategoryBag(ScrollHolder.class, Dungeon.LimitedDrops.SCROLL_HOLDER);
        ensureCategoryBag(PotionBandolier.class, Dungeon.LimitedDrops.POTION_BANDOLIER);
        ensureCategoryBag(MagicalHolster.class, Dungeon.LimitedDrops.MAGICAL_HOLSTER);
        ensureEquipmentBag(WeaponBag.class);
        ensureEquipmentBag(RingBag.class);
    }
    private static void ensureCategoryBag(Class<? extends Bag> type, Dungeon.LimitedDrops drop) {
        for (Bag bag : Dungeon.hero.belongings.getBags()) {
            if (type.isInstance(bag)) {
                drop.drop();
                return;
            }
        }
        Bag bag = com.watabou.utils.Reflection.newInstance(type);
        if (bag != null && bag.collect(Dungeon.hero.belongings.backpack)) drop.drop();
    }
    private static void ensureEquipmentBag(Class<? extends Bag> type) {
        for (Bag bag : Dungeon.hero.belongings.getBags()) if (type.isInstance(bag)) return;
        Bag bag=com.watabou.utils.Reflection.newInstance(type);
        if (bag != null) bag.collect(Dungeon.hero.belongings.backpack);
    }
    public static GameNumber mobGrowth() {
        return on(Challenges.MMO) ? GameNumber.power(1.5, BigInteger.valueOf(Math.max(0,Dungeon.depth-1))) : GameNumber.ONE;
    }
    public static boolean scaled() { return on(Challenges.UNLIMITED_GROWTH) || on(Challenges.MMO); }
    public static boolean growing(Item item) {
        return on(Challenges.UNLIMITED_GROWTH) && (item instanceof Weapon || item instanceof Armor || item instanceof Wand || item instanceof Ring);
    }
    public static int range(int min, int max) {
        return on(Challenges.BIOCHIP) ? min+(max-min)/2 : Random.NormalIntRange(min,max);
    }
    public static BigInteger heroLevel(Hero h) { return BigInteger.valueOf(h.lvl).add(h.mmoLevels); }
    public static String expText(Hero h) {
        return on(Challenges.UNLIMITED_GROWTH) && h.lvl >= Hero.MAX_LEVEL ? h.mmoExp+"/"+heroLevel(h).add(BigInteger.ONE).multiply(BigInteger.valueOf(5)) : h.exp+"/"+h.maxExp();
    }
    public static float expFraction(Hero h) {
        return on(Challenges.UNLIMITED_GROWTH) && h.lvl >= Hero.MAX_LEVEL ? (float)GameNumber.of(h.mmoExp).divide(GameNumber.of(heroLevel(h).add(BigInteger.ONE).multiply(BigInteger.valueOf(5)))).bounded(1) : h.exp/(float)h.maxExp();
    }
    private static GameNumber growth(BigInteger level) {
        return GameNumber.power(1.035, level.subtract(BigInteger.valueOf(20)).max(BigInteger.ZERO));
    }
    public static GameNumber itemFactor(Item item) {
        if (item == null || !scaled() || item.mmoExtra.signum() == 0) return GameNumber.ONE;
        double low, step, nativeAverage;
        BigInteger effective = item.mmoLevel().add(BigInteger.valueOf(item.buffedLvl()-item.trueLevel()));
        if (item instanceof KindOfWeapon) {
            KindOfWeapon w = (KindOfWeapon)item;
            low = (w.min(0)+w.max(0))/2.0; step = (w.min(1)+w.max(1))/2.0-low;
            if (w instanceof SpiritBow) { step=1.5; low-=1.5*w.trueLevel(); }
            nativeAverage = Math.max(1, (w.min()+w.max())/2.0);
        } else if (item instanceof DamageWand) {
            DamageWand w = (DamageWand)item;
            low = (w.min(0)+w.max(0))/2.0; step = (w.min(1)+w.max(1))/2.0-low;
            nativeAverage = Math.max(1, (w.min()+w.max())/2.0);
        } else if (item instanceof Armor) {
            Armor a = (Armor)item; low = (a.DRMin(0)+a.DRMax(0))/2.0;
            step = (a.DRMin(1)+a.DRMax(1))/2.0-low;
            nativeAverage = Math.max(1, (a.DRMin()+a.DRMax())/2.0);
        } else if (item instanceof Wand) {
            low = 1; step = 1; nativeAverage = Math.max(1,item.buffedLvl()+1);
        } else return GameNumber.ONE;
        GameNumber linear = GameNumber.of(effective.max(BigInteger.ZERO)).times(Math.max(0,step)).plus(GameNumber.of(Math.max(0,low)));
        return linear.divide(GameNumber.of(nativeAverage)).times(growth(item.mmoLevel()));
    }
    public static BigInteger ringExtra(Hero hero, Class<? extends Ring> type) {
        if(hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune.class)!=null)return BigInteger.ZERO;
        BigInteger n = BigInteger.ZERO;
        Item[] items = {hero.belongings.ring(), hero.belongings.misc()};
        for (Item i : items) if (type.isInstance(i) && !i.cursed) n = n.add(i.mmoExtra);
        return n;
    }
    public static float ringMultiplier(Char target,Class<? extends Ring> type,double base,int nativeBonus,double limit) {
        if(!scaled() || !(target instanceof Hero))return (float)Math.pow(base,nativeBonus);
        GameNumber full=GameNumber.power(base,ringExtra((Hero)target,type).add(BigInteger.valueOf(nativeBonus)));
        return (float)full.bounded(limit);
    }
    public static GameNumber health(Char ch) {
        if (!scaled()) return GameNumber.ONE;
        if (ch instanceof Hero) {
            Hero h = (Hero)ch;
            GameNumber levels = GameNumber.of(heroLevel(h).subtract(BigInteger.ONE)).times(5).plus(GameNumber.of(20));
            GameNumber ratio = levels.divide(GameNumber.of(20+5*(h.lvl-1)));
            return ratio.times(GameNumber.power(1.035, ringExtra(h, RingOfMight.class)));
        }
        initialize(ch);
        return GameNumber.decode(ch.mmoHealth);
    }
    public static GameNumber attack(Char ch) {
        if (!scaled()) return GameNumber.ONE;
        if (ch instanceof Hero) {
            Hero h=(Hero)ch; Item weapon=h.belongings.attackingWeapon();
            if (weapon != null && weapon.getClass().getName().endsWith("SpiritBow$SpiritArrow")) weapon=h.belongings.getItem(SpiritBow.class);
            GameNumber factor=itemFactor(weapon);
            if (weapon == null) {
                BigInteger extra=ringExtra(h,RingOfForce.class);
                factor = GameNumber.of(extra).plus(GameNumber.of(1)).times(growth(extra));
            }
            // Beyond 256 attacks per tick, represent attack-rate growth in the packet.
            GameNumber speed=GameNumber.power(1.09051,ringExtra(h,RingOfFuror.class));
            double nativeSpeed=Math.pow(1.09051,Ring.getBuffedBonus(h,RingOfFuror.Furor.class));
            GameNumber actualSpeed=speed.times(nativeSpeed);
            factor=factor.times(actualSpeed.divide(GameNumber.of(Math.max(1,actualSpeed.bounded(256)))).max(GameNumber.ONE));
            BigInteger force=ringExtra(h,RingOfForce.class);
            if(weapon instanceof KindOfWeapon && force.signum()>0) {
                double base=Math.max(1,(((KindOfWeapon)weapon).min()+((KindOfWeapon)weapon).max())/2.0);
                factor=factor.times(GameNumber.of(force).plus(GameNumber.of(base)).divide(GameNumber.of(base)));
            }
            if(weapon instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon || weapon instanceof SpiritBow) {
                BigInteger sharp=ringExtra(h,RingOfSharpshooting.class);
                double average=weapon==null?4:Math.max(1,(((KindOfWeapon)weapon).min()+((KindOfWeapon)weapon).max())/2.0);
                factor=factor.times(GameNumber.of(sharp).times(1.5).plus(GameNumber.of(average)).divide(GameNumber.of(average)));
            }
            return factor;
        }
        initialize(ch); return GameNumber.decode(ch.mmoAttack);
    }
    public static void initialize(Char ch) {
        if (!scaled() || ch instanceof Hero || (ch.mmoInitialized && (!(ch instanceof Mob) || !on(Challenges.MMO)))) return;
        // Recompute fixed depth scaling for old saves too; never retain player-derived snapshots.
        GameNumber factor=ch instanceof Mob && !(ch instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC) ? mobGrowth() : GameNumber.ONE;
        ch.mmoHealth=factor.encode();ch.mmoAttack=factor.encode();ch.mmoInitialized=true;
    }
    public static void floor(Level level) {
        level.mmoFloorHealth=mobGrowth().encode();
        level.mmoFloorAttack=mobGrowth().encode();
        level.mmoFloorArmor=GameNumber.ZERO.encode();
    }
    public static int normalized(Char target, GameNumber actual) {
        GameNumber units=actual.divide(health(target)).plus(target.mmoRemainder);
        double value=units.bounded(1000000); int whole=(int)Math.floor(value);
        target.mmoRemainder=units.minus(GameNumber.of(whole));
        // Overflowing hits kill normally; never turn a huge packet into a 1-point sentinel.
        if (value >= 1000000) { target.mmoRemainder=GameNumber.ZERO; return 1000000; }
        return whole;
    }
    public static int physical(Char attacker, Char target, int damage, int dr) {
        GameNumber hit=attack(attacker).times(Math.max(0,damage));
        GameNumber armor=target instanceof Hero ? itemFactor(((Hero)target).belongings.armor()) : GameNumber.ONE;
        GameNumber defense=armor.times(Math.max(0,dr));
        GameNumber net=hit.minus(defense);
        return normalized(target, extraResistance(target,net,false));
    }
    private static GameNumber extraResistance(Char target,GameNumber hit,boolean elemental) {
        if(target instanceof Hero) {
            Hero h=(Hero)target;
            // Tenacity follows missing health, not an unconditional reduction at full HP.
            BigInteger extra=ringExtra(h,RingOfTenacity.class);
            double missing=Math.max(0,(h.HT-h.HP)/(double)Math.max(1,h.HT));
            hit=hit.times(GameNumber.power(Math.pow(.85,missing),extra));
            if(elemental)hit=hit.times(GameNumber.power(.825,ringExtra(h,RingOfElements.class)));
        }
        return hit;
    }
    public static int defenseProc(Char target,Char attacker,int damage) {
        if(!scaled())return target.defenseProc(attacker,damage);
        Char previous=procTarget.get();procTarget.set(target);
        try { return target.defenseProc(attacker,damage); } finally { procTarget.set(previous); }
    }
    public static int attackProc(Char attacker,Char target,int damage) {
        if(!scaled())return attacker.attackProc(target,damage);
        Char previous=procTarget.get();procTarget.set(target);
        try { return attacker.attackProc(target,damage); } finally { procTarget.set(previous); }
    }
    public static int damage(Char target, int damage, Object src) {
        if (!scaled() || damage <= 0) return damage;
        GameNumber factor;
        if(procTarget.get()==target) return damage;
        if (src instanceof Char) factor=src == target ? health(target) : attack((Char)src);
        else if (src instanceof Item) factor=itemFactor((Item)src);
        else {
            // DOT, hunger, fall, delayed damage are in local health coordinates.
            // They preserve native percentage mechanics rather than being multiplied twice.
            factor=health(target);
        }
        GameNumber actual=factor.times(damage);
        actual=extraResistance(target,actual,com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic.RESISTS.contains(src.getClass()));
        return normalized(target, actual);
    }
    public static void magic(Char attacker,Char target,int amount,Object src) {
        if(!scaled()){target.damage(amount,src);return;}
        GameNumber actual=attack(attacker).times(Math.max(0,amount));
        target.damageBase(normalized(target,extraResistance(target,actual,true)),src);
    }
    public static boolean hit(Char attacker, Char defender, double accuracy, double evasion) {
        GameNumber a=GameNumber.of(Math.max(0,accuracy)), d=GameNumber.of(Math.max(0,evasion));
        if (attacker instanceof Hero) a=a.times(GameNumber.power(1.3,ringExtra((Hero)attacker,RingOfAccuracy.class)));
        if (defender instanceof Hero) d=d.times(GameNumber.power(1.125,ringExtra((Hero)defender,RingOfEvasion.class)));
        if (on(Challenges.MMO)) {
            if (attacker instanceof Mob && !(attacker instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC)) a=a.times(Math.max(1,Dungeon.depth));
            if (defender instanceof Mob && !(defender instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC)) d=d.times(Math.max(1,Dungeon.depth));
        }
        double p;
        if (a.compareTo(GameNumber.ZERO)==0) p=0;
        else if (d.compareTo(GameNumber.ZERO)==0) p=1;
        else p=a.compareTo(d)>=0 ? 1-d.divide(a).bounded(1)/2 : a.divide(d).bounded(1)/2;
        if(!on(Challenges.BIOCHIP))return Random.Float()<p;
        attacker.mmoHitCredit+=p;
        if (attacker.mmoHitCredit>=1) { attacker.mmoHitCredit-=1; return true; }
        return false;
    }
    public static void earn(Hero h,int amount) {
        if (amount<=0) return;
        h.mmoExp=h.mmoExp.add(BigInteger.valueOf(amount));
        boolean leveled=false;
        BigInteger cost=heroLevel(h).add(BigInteger.ONE).multiply(BigInteger.valueOf(5));
        while(h.mmoExp.compareTo(cost)>=0) {
            h.mmoExp=h.mmoExp.subtract(cost); h.mmoLevels=h.mmoLevels.add(BigInteger.ONE); leveled=true;
            cost=cost.add(BigInteger.valueOf(5));
        }
        if (leveled && h.sprite!=null) h.sprite.showStatus(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE, "Lv "+heroLevel(h));
    }
    private static void revealUpgradeAndCurse(Item item) {
        if (item.isUpgradable()) {
            // TooCruel reveals these two facts without learning ring types or wand charges.
            item.levelKnown=true;
            item.cursedKnown=true;
        }
    }
    public static boolean absorb(Item item,Hero h) {
        if (!on(Challenges.ABSORPTION)) return false;
        revealUpgradeAndCurse(item);
        if (!mergeable(item) || item.isEquipped(h)) return false;
        Item target=null;
        for (Item candidate:h.belongings) {
            if(candidate==item || candidate.getClass()!=item.getClass() || !mergeable(candidate)) continue;
            if(target==null || candidate.isEquipped(h) || (!target.isEquipped(h) && candidate.mmoLevel().compareTo(target.mmoLevel())>0)) target=candidate;
            if(target.isEquipped(h)) break;
        }
        if(target==null) return false;
        BigInteger total=target.mmoLevel().add(item.mmoLevel()).add(BigInteger.ONE);
        if(item.mmoLevel().compareTo(target.mmoLevel())>0) copyMergeState(item,target);
        int nativeLevel=total.min(BigInteger.valueOf(20)).max(BigInteger.valueOf(Integer.MIN_VALUE)).intValue();
        target.level(nativeLevel);target.mmoExtra=total.subtract(BigInteger.valueOf(nativeLevel));
        if(target instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff)
            ((com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff)target).updateWand(false);
        revealUpgradeAndCurse(target);
        Item.updateQuickslot();return true;
    }
    private static boolean mergeable(Item item) {
        return !item.unique && (item instanceof Armor || item instanceof Ring || item instanceof Wand ||
                item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon);
    }
    private static void copyMergeState(Item source,Item target) {
        target.cursed=source.cursed;
        if(source instanceof Weapon) {
            Weapon a=(Weapon)source,b=(Weapon)target;
            b.enchantment=a.enchantment;b.curseInfusionBonus=a.curseInfusionBonus;b.masteryPotionBonus=a.masteryPotionBonus;
        } else if(source instanceof Armor) {
            Armor a=(Armor)source,b=(Armor)target;
            b.glyph=a.glyph;b.curseInfusionBonus=a.curseInfusionBonus;b.masteryPotionBonus=a.masteryPotionBonus;
        } else if(source instanceof Wand) {
            Wand a=(Wand)source,b=(Wand)target;
            b.curseInfusionBonus=a.curseInfusionBonus;b.resinBonus=a.resinBonus;
        }
    }
    public static void consolidateEquipment() {
        if (!on(Challenges.ABSORPTION) || Dungeon.hero==null || Dungeon.hero.belongings.lostInventory()) return;
        java.util.ArrayList<Item> inventory=new java.util.ArrayList<>();
        for(Item item:Dungeon.hero.belongings) {
            revealUpgradeAndCurse(item);
            if(!item.isEquipped(Dungeon.hero) && mergeable(item)) inventory.add(item);
        }
        inventory.sort((a,b)->a.mmoLevel().compareTo(b.mmoLevel()));
        for(Item item:inventory) if(Dungeon.hero.belongings.backpack.contains(item) && absorb(item,Dungeon.hero))
            item.detachAll(Dungeon.hero.belongings.backpack);
    }
    public static void pickup(Hero h) {
        if(!on(Challenges.AUTO_PICKUP) || Dungeon.level==null) return;
        int x=h.pos%Dungeon.level.width(),y=h.pos/Dungeon.level.width();
        for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++) {
            int nx=x+dx,ny=y+dy;
            if(nx<0 || ny<0 || nx>=Dungeon.level.width() || ny>=Dungeon.level.height()) continue;
            int pos=ny*Dungeon.level.width()+nx;
            // Pick up only visible, reachable loose heaps. Never open chests or cross walls.
            if (!Dungeon.level.heroFOV[pos] || !Dungeon.level.passable[pos] || (dx!=0 && dy!=0 && !Dungeon.level.passable[y*Dungeon.level.width()+nx] && !Dungeon.level.passable[ny*Dungeon.level.width()+x])) continue;
            Heap heap=Dungeon.level.heaps.get(pos);
            if(heap==null || heap.hidden || heap.type!=Heap.Type.HEAP)continue;
            for(Item item:heap.items.toArray(new Item[0])) {
                if(item instanceof Gold) {
                    Dungeon.gold = (int)Math.min(Integer.MAX_VALUE, (long)Dungeon.gold+item.quantity());
                    com.shatteredpixel.shatteredpixeldungeon.Statistics.goldCollected=(int)Math.min(Integer.MAX_VALUE,(long)com.shatteredpixel.shatteredpixeldungeon.Statistics.goldCollected+item.quantity());
                    com.shatteredpixel.shatteredpixeldungeon.Badges.validateGoldCollected();
                    com.shatteredpixel.shatteredpixeldungeon.journal.Catalog.setSeen(item.getClass());
                    com.shatteredpixel.shatteredpixeldungeon.Statistics.itemTypesDiscovered.add(item.getClass());heap.items.remove(item);
                } else if(item instanceof EnergyCrystal) {
                    Dungeon.energy = (int)Math.min(Integer.MAX_VALUE, (long)Dungeon.energy+item.quantity());
                    com.shatteredpixel.shatteredpixeldungeon.journal.Catalog.setSeen(item.getClass());
                    com.shatteredpixel.shatteredpixeldungeon.Statistics.itemTypesDiscovered.add(item.getClass());heap.items.remove(item);
                } else if(ordinaryPickup(item) && (absorb(item,h) || item.collect(h.belongings.backpack))) heap.items.remove(item);
            }
            if(heap.items.isEmpty()) heap.destroy(); else if(heap.sprite!=null) heap.sprite.view(heap);
        }
    }
    public static boolean ordinaryPickup(Item item) {
        // Keys, quest rewards, dew, active bombs and journal entries have special pickup hooks.
        // Preserve their manual flow rather than putting them into the ordinary inventory.
        try {
            Class<?> owner=item.getClass().getMethod("doPickUp",Hero.class,int.class).getDeclaringClass();
            return owner==Item.class || owner==EquipableItem.class || owner==com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon.class;
        } catch(ReflectiveOperationException error) {return false;}
    }
    public static int extraLootDraws() {
        int amount=Random.Int(3)+1;
        if(on(Challenges.MMO)) amount+=(int)Math.round(Math.max(Math.sqrt(mobGrowth().bounded(Integer.MAX_VALUE)),Dungeon.depth));
        return amount;
    }
    public static Item extraLoot() {
        int max,min;
        if(Dungeon.depth<=2){max=10;min=0;}
        else if(Dungeon.depth<=6){max=8;min=1;}
        else if(Dungeon.depth<=10){max=6;min=2;}
        else {max=5;min=3;}
        int roll=Random.Int(max)+min;
        if(roll<=2)return Generator.random();
        if(roll<=7)return RingOfWealth.genConsumableDrop((roll-3)*5);
        return null;
    }
    public static void loot(Mob mob) {
        if(!on(Challenges.MASS_LOOT) || mob.alignment!=Char.Alignment.ENEMY || Dungeon.level==null) return;
        // TooCruel GRINDING_2 + GRINDING_3: depth-dependent draws, consumables, and null rolls.
        // It has neither an EXP filter nor a special 30-item boss award.
        int draws=extraLootDraws();
        for(int i=0;i<draws;i++) {
            Item item=extraLoot();
            if(item!=null) Dungeon.level.drop(item,mob.pos).sprite.drop();
        }
    }
    public static String bio(Char ch) {
        if(!on(Challenges.BIOCHIP))return "";
        return "\n\n_BIOCHIP_\n"+Messages.get(MmoRules.class,"health",health(ch).times(Math.max(0,ch.HP)),health(ch).times(ch.HT))
                +"\n"+Messages.get(MmoRules.class,"scales",attack(ch),health(ch));
    }
    public static String itemInfo(Item item) {
        if(!scaled() || !item.levelKnown || !growing(item))return "";
        return "\n\n"+Messages.get(MmoRules.class,"item_stats",item.mmoLevel(),itemFactor(item));
    }
    public static void upgradeMenu(ScrollOfUpgrade scroll, Item item, boolean detached) {
        GameScene.show(new WndOptions(Messages.get(MmoRules.class,"batch_title"),Messages.get(MmoRules.class,"batch_desc",item.title()),"1","10","100",Messages.get(MmoRules.class,"all")) {
            protected void onSelect(int index) {
                int wanted=index==0?1:index==1?10:index==2?100:Integer.MAX_VALUE;
                int available=detached?1:0;
                ScrollOfUpgrade stack=Dungeon.hero.belongings.getItem(ScrollOfUpgrade.class);
                if(stack!=null)available+=stack.quantity();
                int count=Math.min(wanted,available);
                if(count<=0)return;
                // First upgrade runs normal curse/enchantment/resin semantics. Later upgrades
                // use the same semantics, with a single turn/animation for the batch.
                int fromStack=count-(detached?1:0);
                if(fromStack>0) {
                    if(fromStack==stack.quantity())stack.detachAll(Dungeon.hero.belongings.backpack);
                    else stack.quantity(stack.quantity()-fromStack);
                }
                // Execute utility transitions up to +32, then change only the magnitude.
                // Avoid thousands of repeated particles, curse messages and quickslot refreshes.
                int transitions=growing(item) ? Math.min(count,Math.max(0,32-item.mmoLevel().min(BigInteger.valueOf(32)).intValue())) : count;
                for(int n=0;n<transitions;n++) item.upgrade();
                int extra=count-transitions;
                if(extra>0) item.mmoExtra=item.mmoExtra.add(BigInteger.valueOf(extra));
                if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff) ((com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff)item).updateWand(false);
                com.shatteredpixel.shatteredpixeldungeon.Statistics.upgradesUsed=(int)Math.min(Integer.MAX_VALUE,(long)com.shatteredpixel.shatteredpixeldungeon.Statistics.upgradesUsed+count);
                com.shatteredpixel.shatteredpixeldungeon.Badges.validateItemLevelAquired(item);
                scroll.readAnimation();
                Item.updateQuickslot();
            }
        });
    }
}
