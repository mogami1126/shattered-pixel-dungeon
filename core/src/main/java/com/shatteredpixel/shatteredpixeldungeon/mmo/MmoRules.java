package com.shatteredpixel.shatteredpixeldungeon.mmo;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
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
        if (!scaled() || ch.mmoInitialized || Dungeon.level == null || ch instanceof Hero) return;
        ch.mmoInitialized=true;
        if (ch.alignment == Char.Alignment.ENEMY && on(Challenges.MMO)) {
            ch.mmoHealth=Dungeon.level.mmoFloorHealth; ch.mmoAttack=Dungeon.level.mmoFloorAttack;
        } else if (ch.alignment == Char.Alignment.ALLY && Dungeon.hero != null) {
            ch.mmoHealth=health(Dungeon.hero).encode();
            ch.mmoAttack=attack(Dungeon.hero).encode();
        }
    }
    public static void floor(Level level) {
        if (!on(Challenges.MMO) || Dungeon.hero == null) return;
        Hero h=Dungeon.hero;
        GameNumber output=attack(h).times(h.belongings.weapon() == null ? 4 : Math.max(1,(h.belongings.weapon().min()+h.belongings.weapon().max())/2.0));
        // Compare permanent weapon and wand outputs; exclude temporary combat buffs.
        for (Item item:h.belongings) if (item instanceof DamageWand) {
            DamageWand w=(DamageWand)item;
            output=output.max(itemFactor(w).times(Math.max(1,(w.min()+w.max())/2.0)));
        }
        if(h.belongings.weapon() instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff) {
            Wand embedded=((com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff)h.belongings.weapon()).mmoWand();
            if(embedded instanceof DamageWand) {
                DamageWand w=(DamageWand)embedded;
                output=output.max(itemFactor(w).times(Math.max(1,(w.min()+w.max())/2.0)));
            }
        }
        GameNumber hp=health(h).times(h.HT);
        output=output.max(GameNumber.decode(h.mmoBestOutput)); hp=hp.max(GameNumber.decode(h.mmoBestHealth));
        h.mmoBestOutput=output.encode(); h.mmoBestHealth=hp.encode();
        double pressure=Math.pow(1.01, Math.max(0,Dungeon.depth-1));
        // Regional reference HP/damage preserves the relative strength and stages of every species.
        double referenceHP = 10 + 4*Math.max(0,Dungeon.depth-1);
        double referenceDamage = 4 + 1.2*Math.max(0,Dungeon.depth-1);
        level.mmoFloorHealth=output.times(1.5*pressure/referenceHP).max(GameNumber.ONE).encode();
        Armor armor=h.belongings.armor();
        GameNumber armorPower=armor==null ? GameNumber.ZERO : itemFactor(armor).times(Math.max(0,(armor.DRMin()+armor.DRMax())/2.0));
        level.mmoFloorAttack=hp.times(.06*pressure/referenceDamage).max(GameNumber.ONE).encode();
        level.mmoFloorArmor=armorPower.encode();
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
        GameNumber armor=target instanceof Hero ? itemFactor(((Hero)target).belongings.armor()) : health(target);
        GameNumber defense=armor.times(Math.max(0,dr));
        GameNumber flat=on(Challenges.MMO) && attacker.alignment==Char.Alignment.ENEMY && target instanceof Hero && Dungeon.level!=null ? GameNumber.decode(Dungeon.level.mmoFloorArmor) : GameNumber.ZERO;
        // Cancel the shared armor magnitude before adding the much smaller health-sized hit.
        // (1e100 + 6) - 1e100 must still deal 6, not become zero through precision loss.
        GameNumber net=flat.compareTo(defense)>=0 ? hit.plus(flat.minus(defense)) : hit.minus(defense.minus(flat));
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
    public static boolean absorb(Item item,Hero h) {
        if (!on(Challenges.ABSORPTION) || item.unique || item.trueLevel()<0 || item.trueLevel()>3 || item.mmoExtra.signum()>0 || item.cursed || item.isEquipped(h)) return false;
        if (item instanceof Weapon && ((Weapon)item).enchantment!=null || item instanceof Armor && ((Armor)item).glyph!=null) return false;
        Item target=null;
        if (item instanceof Armor) target=h.belongings.armor();
        else if (item instanceof Ring) {
            if (h.belongings.ring()!=null && h.belongings.ring().getClass()==item.getClass()) target=h.belongings.ring();
            else if(h.belongings.misc()!=null && h.belongings.misc().getClass()==item.getClass()) target=h.belongings.misc();
        } else if(item instanceof Weapon && item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon) target=h.belongings.weapon();
        else if(item instanceof Wand) for(Item i:h.belongings) if(i instanceof Wand && i!=item && i.getClass()==item.getClass()) { target=i; break; }
        if(target==null || target==item || !target.isUpgradable()) return false;
        target.upgrade(); Item.updateQuickslot(); return true;
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
    public static void loot(Mob mob) {
        if(!on(Challenges.MASS_LOOT) || mob.alignment!=Char.Alignment.ENEMY || mob.EXP<=0 || Dungeon.level==null) return;
        int draws=mob.properties().contains(Char.Property.BOSS) ? 30 : Random.IntRange(3,6);
        for(int i=0;i<draws;i++) {
            int roll=Random.Int(100); Item item;
            if(roll<18)item=Generator.random(Generator.Category.WEAPON);
            else if(roll<30)item=Generator.random(Generator.Category.ARMOR);
            else if(roll<40)item=Generator.random(Generator.Category.RING);
            else if(roll<50)item=Generator.random(Generator.Category.WAND);
            else if(roll<62)item=Generator.random(Generator.Category.POTION);
            else if(roll<74)item=Generator.random(Generator.Category.SCROLL);
            else if(roll<82)item=new ScrollOfUpgrade();
            else if(roll<88)item=Generator.random(Generator.Category.FOOD);
            else if(roll<94)item=Generator.random(Random.Int(2)==0 ? Generator.Category.SEED : Generator.Category.STONE);
            else item=Random.Int(2)==0 ? new Gold().quantity(25) : new EnergyCrystal().quantity(2);
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
