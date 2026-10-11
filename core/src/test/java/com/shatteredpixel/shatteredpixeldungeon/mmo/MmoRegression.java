package com.shatteredpixel.shatteredpixeldungeon.mmo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.watabou.utils.*;
import com.watabou.utils.Random;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.util.*;

/** Headless regression main. No test powers or debug menus are included in the game. */
public class MmoRegression {
    private static int checks;
    private static void check(boolean ok,String why) { checks++; if(!ok)throw new AssertionError(why); }
    private static class Target extends Char {
        int received; int calls;
        Target() { HP=HT=100; mmoInitialized=true; }
        protected boolean act(){return false;}
        public void damageBase(int damage,Object src){received=damage;calls++;HP=Math.max(0,HP-Math.max(0,damage));}
        void advance(float t){spend(t);}
    }
    private static class TestLevel extends Level {
        java.util.List<Item> drops=new ArrayList<>();
        protected boolean build(){return true;}
        protected void createMobs(){}
        protected void createItems(){}
        public Heap drop(Item item,int pos){ drops.add(item);Heap heap=new Heap();heap.sprite=new ItemSprite(){public void drop(){}};return heap; }
    }
    private static class QuietSprite extends CharSprite {
        public void showStatus(int color,String text,Object... args){}
        public void showStatusWithIcon(int color,String text,int icon,Object... args){}
        public void idle(){}
        public void showAlert(){}
    }
    private static class PackedFloor extends SewerLevel {
        PackedFloor() {
            setSize(12,12);mobs=new HashSet<>();rooms=new ArrayList<>();heaps=new SparseArray<>();traps=new SparseArray<>();
            com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EmptyRoom room=new com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.EmptyRoom();
            room.set(1,1,10,10);rooms.add(room);
            for(int y=2;y<10;y++)for(int x=2;x<10;x++) {
                int p=y*12+x;passable[p]=true;map[p]=Terrain.EMPTY;
                Rat rat=new Rat();rat.pos=p;mobs.add(rat);
            }
        }
        public int exit(){return 0;}
        int itemCell(){return randomDropCell();}
    }
    private static class TestKing extends DwarfKing {
        boolean died;
        public void die(Object cause){died=true;}
    }
    private static class TestPrison extends PrisonBossLevel {
        int progressed;
        public void progress(){progressed++;}
    }
    private static class TestTengu extends Tengu {
        public void yell(String text){}
    }
    private static void setup(String assets) {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        com.watabou.noosa.Game.version="4.0.2-MMO.1-INDEV";
        Map<String,Object> prefs=new HashMap<>();
        GameSettings.set((Preferences)Proxy.newProxyInstance(Preferences.class.getClassLoader(),new Class[]{Preferences.class},(o,m,a)->{
            if(m.getName().equals("contains"))return prefs.containsKey(a[0]);
            if(m.getName().startsWith("get") && a!=null && a.length==2)return prefs.getOrDefault(a[0].toString(),a[1]);
            if(m.getName().equals("get"))return prefs;
            if(m.getName().startsWith("put")){prefs.put(a[0].toString(),a[1]);return o;}
            if(m.getReturnType()==boolean.class)return false;
            return null;
        }));
        Gdx.files=(Files)Proxy.newProxyInstance(Files.class.getClassLoader(),new Class[]{Files.class},(o,m,a)->new FileHandle(assets+"/"+a[0]));
        Dungeon.hero=new Hero();Dungeon.hero.heroClass=HeroClass.WARRIOR;
        Dungeon.challenges=Challenges.MMO_PRESET;
        Dungeon.level=new TestLevel();Dungeon.depth=1;
        Ring.initGems();
    }
    public static void main(String[] args)throws Exception {
        setup(args[0]);
        BigInteger million=BigInteger.valueOf(1000000);
        GameNumber giant=GameNumber.power(1.035,million);
        check(giant.compareTo(GameNumber.of(Double.MAX_VALUE))>0,"growth must exceed double range");
        check(giant.divide(giant).bounded(10)==1,"large-number cancellation");
        check(GameNumber.decode(giant.encode()).compareTo(giant)==0,"large-number round trip");
        check(GameNumber.power(1.035,BigInteger.TEN.pow(80)).toString().length()<110,"unbounded exponent formatting");
        check(GameNumber.of(1e-320).bounded(1)>0,"subnormal magnitude");
        check(GameNumber.of(2).minus(GameNumber.of(3)).compareTo(GameNumber.ZERO)==0,"unsigned subtraction");

        Sword sword=new Sword();sword.level(20);sword.mmoExtra=million;sword.levelKnown=true;
        Dungeon.hero.belongings.weapon=sword;
        check(MmoRules.itemFactor(sword).compareTo(giant)>0,"linear plus exponential weapon growth");
        sword.upgrade();check(sword.trueLevel()==20 && sword.mmoLevel().equals(million.add(BigInteger.valueOf(21))),"upgrade is unbounded with bounded formula coordinate");
        Bundle saved=new Bundle();sword.storeInBundle(saved);Sword restored=new Sword();restored.restoreFromBundle(saved);
        check(restored.mmoLevel().equals(sword.mmoLevel()),"item upgrade persistence");
        check(restored.title().contains(sword.mmoLevel().toString()),"true upgrade title");
        PlateArmor armor=new PlateArmor();armor.level(20);armor.mmoExtra=million;
        check(MmoRules.itemFactor(armor).compareTo(giant)>0,"armor keeps growing");
        Dungeon.hero.belongings.armor=armor;
        Target armorAttacker=new Target();armorAttacker.alignment=Char.Alignment.ENEMY;
        Dungeon.level.mmoFloorArmor=MmoRules.itemFactor(armor).times(8).encode();
        check(MmoRules.physical(armorAttacker,Dungeon.hero,6,8)==0,"monster damage no longer gains player armor compensation");
        Dungeon.hero.belongings.armor=null;Dungeon.level.mmoFloorArmor=GameNumber.ZERO.encode();
        Sword direct=new Sword();direct.level(Integer.MAX_VALUE);
        check(direct.trueLevel()==20 && direct.mmoLevel().equals(BigInteger.valueOf(Integer.MAX_VALUE)),"direct level setter keeps native formulas bounded");
        MagesStaff staff=new MagesStaff(new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile());
        staff.level(20);staff.mmoExtra=million;staff.updateWand(false);
        check(staff.mmoWand().mmoLevel().equals(staff.mmoLevel()),"embedded mage wand keeps full upgrade level");
        check(MmoRules.itemFactor(staff.mmoWand()).compareTo(giant)>0,"embedded mage magic has large-number damage");

        Target target=new Target();target.mmoHealth=giant.encode();
        check(MmoRules.normalized(target,giant.times(.25))==0,"fractional hit is retained");
        check(MmoRules.normalized(target,giant.times(.25))==0,"second fractional hit retained");
        check(MmoRules.normalized(target,giant.times(.25))==0,"third fractional hit retained");
        check(MmoRules.normalized(target,giant.times(.25))==1,"four fractional hits deal exactly one unit");
        check(MmoRules.normalized(target,giant.times(1000000000))==1000000,"oversized hit saturates safely");
        Target dispatch=new Target();dispatch.damage(10,sword);
        check(dispatch.calls==1 && dispatch.received==1000000,"scaled packet dispatched once before subclass processing");
        dispatch=new Target();dispatch.mmoHealth=giant.encode();dispatch.damage(10,dispatch);
        check(dispatch.received==10,"self percentage damage is not multiplied twice");
        dispatch=new Target();dispatch.mmoHealth=giant.encode();dispatch.damage(10,new Object());
        check(dispatch.received==10,"delayed damage stays in local coordinates");
        Target caster=new Target();caster.mmoAttack=giant.encode();dispatch=new Target();dispatch.mmoHealth=giant.encode();
        MmoRules.magic(caster,dispatch,12,new Shaman.EarthenBolt());
        check(dispatch.received==12 && dispatch.calls==1,"enemy magic uses caster growth exactly once");

        RingOfMight root=new RingOfMight();root.level(20);root.mmoExtra=million;Dungeon.hero.belongings.ring=root;
        check(MmoRules.health(Dungeon.hero).compareTo(GameNumber.of(Double.MAX_VALUE))>0,"root ring health has no float overflow");
        check(MmoRules.health(Dungeon.hero).times(Dungeon.hero.HP).compareTo(GameNumber.ZERO)>0,"positive actual HP");
        GameNumber hp=MmoRules.health(Dungeon.hero).times(Dungeon.hero.HT);
        check(hp.times(.8).plus(hp.times(.2)).compareTo(hp)==0,"healing sum remains finite and positive");
        root.cursed=true;check(MmoRules.health(Dungeon.hero).compareTo(GameNumber.ONE)==0,"cursed ring does not grant positive excess");root.cursed=false;
        Dungeon.hero.belongings.ring=null;
        Dungeon.hero.lvl=30;MmoRules.earn(Dungeon.hero,155);
        check(MmoRules.heroLevel(Dungeon.hero).equals(BigInteger.valueOf(31)),"hero level 31");
        check(Dungeon.hero.mmoExp.signum()==0,"XP threshold consumed exactly");
        MmoRules.earn(Dungeon.hero,160);check(MmoRules.heroLevel(Dungeon.hero).equals(BigInteger.valueOf(32)),"XP threshold rises by five");

        Target attacker=new Target(), defender=new Target();int hits=0;
        for(int i=0;i<1000;i++)if(MmoRules.hit(attacker,defender,10,10))hits++;
        check(hits==500,"deterministic half chance gives 500 of 1000 hits");
        attacker=new Target();hits=0;
        for(int i=0;i<1000;i++)if(MmoRules.hit(attacker,defender,10,20))hits++;
        check(hits==250,"quarter chance keeps expected hit frequency");
        check(MmoRules.range(5,11)==8,"deterministic midpoint");
        PackedFloor packed=new PackedFloor();
        check(packed.itemCell()>=0,"packed floor can still place its initial items");
        Dungeon.challenges=0;check(packed.itemCell()==-1,"normal placement still excludes occupied tiles");
        Dungeon.challenges=Challenges.MMO_PRESET;
        for(Mob mob:packed.mobs){Heap heap=new Heap();heap.type=Heap.Type.HEAP;heap.pos=mob.pos;packed.heaps.put(mob.pos,heap);}
        check(packed.itemCell()>=0,"saturated loot merges onto safe loose heaps");

        MmoRules.floor(Dungeon.level);String floorHP=Dungeon.level.mmoFloorHealth;
        Dungeon.hero.belongings.weapon=null;MmoRules.floor(Dungeon.level);
        check(Dungeon.level.mmoFloorHealth.equals(floorHP),"fixed depth growth ignores player loadout");
        Mob first=new Rat();MmoRules.initialize(first);String firstHP=first.mmoHealth;
        Dungeon.hero.belongings.weapon=sword;sword.mmoExtra=million.multiply(BigInteger.TEN);
        check(first.mmoHealth.equals(firstHP),"live floor does not chase player upgrades");
        Mob summon=new Rat();MmoRules.initialize(summon);check(summon.mmoHealth.equals(firstHP),"summons share fixed depth growth");

        Hero h=Dungeon.hero;Sword low=new Sword();low.level(2);BigInteger before=sword.mmoLevel();
        check(MmoRules.absorb(low,h) && sword.mmoLevel().equals(before.add(BigInteger.valueOf(3))),"duplicate absorption sums real levels");
        Sword high=new Sword();high.level(4);check(MmoRules.absorb(high,h),"high-level gear can merge");
        Sword cursed=new Sword();cursed.cursed=true;check(MmoRules.absorb(cursed,h),"unidentified cursed gear can merge");
        for(int i=0;i<100;i++)h.belongings.backpack.items.add(new Sword());
        check(h.belongings.backpack.canHold(new Sword()),"large bag capacity");
        check(!MmoRules.ordinaryPickup(new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey()),"automatic pickup preserves key journal flow");
        check(!MmoRules.ordinaryPickup(new Dewdrop()),"automatic pickup preserves dew healing flow");
        check(MmoRules.ordinaryPickup(new Sword()),"ordinary equipment can be collected automatically");

        Target clock=new Target();for(int i=0;i<65536;i++)clock.advance(Float.MIN_VALUE);
        check(clock.cooldown()==256,"tiny positive actions advance time exactly");
        saved=new Bundle();clock.storeInBundle(saved);Target clock2=new Target();clock2.restoreFromBundle(saved);
        check(clock2.cooldown()==256,"double scheduler persistence");
        saved=new Bundle();clock.storeInBundle(saved);saved.put("time",1.25f);clock2.restoreFromBundle(saved);
        check(clock2.cooldown()==1.25f,"legacy numeric time loads");

        Dungeon.challenges=0;dispatch=new Target();dispatch.damage(7,sword);
        check(dispatch.received==7,"disabled mods preserve damage");
        check(!h.belongings.backpack.canHold(new Sword()),"disabled bag preserves capacity");
        check(Challenges.MAX_CHALS==Challenges.NAME_IDS.length && Challenges.MASKS.length==Challenges.NAME_IDS.length,"modifier registry length");

        Dungeon.challenges=Challenges.MMO_PRESET; Dungeon.hero.belongings.ring=null;
        Dungeon.hero.belongings.weapon=null;Dungeon.hero.HP=Dungeon.hero.HT=100;
        Generator.fullReset();Random.pushGenerator(901);
        TestLevel drops=new TestLevel();Dungeon.level=drops;
        int drawn=0;
        for(int i=0;i<20;i++){int beforeCount=drops.drops.size();MmoRules.loot(new Rat());int added=drops.drops.size()-beforeCount;drawn+=added;check(added>=0 && added<=4,"depth one drops include null rolls and at most four draws");}
        check(drawn>0,"extra loot still drops items");
        int beforeCount=drops.drops.size();MmoRules.loot(new DwarfKing());check(drops.drops.size()-beforeCount<=4,"boss no longer has a thirty-item award");
        Mob unrewarded=new Rat();unrewarded.EXP=0;boolean unrewardedDrop=false;
        for(int i=0;i<30;i++){beforeCount=drops.drops.size();MmoRules.loot(unrewarded);unrewardedDrop|=drops.drops.size()>beforeCount;}
        check(unrewardedDrop,"original extra-loot logic does not gate on experience");
        Dungeon.depth=5;MmoRules.floor(Dungeon.level);
        check(MmoRules.mobGrowth().compareTo(GameNumber.of(5.0625))==0,"original depth five growth is 1.5 to the fourth");
        Rat legacy=new Rat();legacy.mmoInitialized=true;legacy.mmoHealth=giant.encode();MmoRules.initialize(legacy);
        check(GameNumber.decode(legacy.mmoHealth).compareTo(GameNumber.of(5.0625))==0,"old player-scaled monsters migrate to fixed depth growth");
        Dungeon.depth=25;
        check(MmoRules.mobGrowth().divide(GameNumber.of(Math.pow(1.5,24))).bounded(2)>.999999,"original depth twenty-five growth");
        Dungeon.depth=1;Dungeon.challenges &= ~Challenges.MMO;
        Random.popGenerator();

        TestKing king=new TestKing();king.sprite=new QuietSprite();king.mmoInitialized=true;king.mmoHealth=giant.encode();
        java.lang.reflect.Field phase=DwarfKing.class.getDeclaredField("phase");phase.setAccessible(true);phase.setInt(king,3);
        king.HP=100;king.damage(1000000,sword);
        Viscosity.DeferedDamage deferred=king.buff(Viscosity.DeferedDamage.class);
        check(deferred!=null && !king.died,"king phase three still defers hits");
        deferred.extend(Float.MAX_VALUE);deferred.act();
        check(king.died && king.HP==0 && !king.isAlive(),"king dies after deferred damage even with huge magnitudes");

        TestPrison prison=new TestPrison();prison.setSize(32,32);prison.mobs=new HashSet<>();Dungeon.level=prison;
        java.lang.reflect.Field state=PrisonBossLevel.class.getDeclaredField("state");state.setAccessible(true);state.set(prison,PrisonBossLevel.State.FIGHT_START);
        TestTengu tengu=new TestTengu();tengu.sprite=new QuietSprite();tengu.mmoInitialized=true;tengu.mmoHealth=giant.encode();prison.mobs.add(tengu);
        for(int i=0;i<10 && prison.progressed==0;i++)tengu.damage(1000000,sword);
        check(prison.progressed==1 && tengu.HP==tengu.HT/2,"huge Tengu hits preserve the first phase transition");
        Dungeon.hero=new Hero();Dungeon.hero.heroClass=HeroClass.WARRIOR;
        Dungeon.challenges=0;
        Dungeon.customSeedText="category-bag-regression"; // avoid rendering achievement popups in the headless fixture
        MmoRules.ensureCategoryBags();
        check(Dungeon.hero.belongings.getBags().size()==1,"native saves receive no extra bags");
        com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch originalPouch=new com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch();originalPouch.collect();
        Waterskin water=new Waterskin();water.collect();
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart dart=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart();dart.collect();
        Dungeon.challenges=Challenges.LARGE_BAGS;
        MmoRules.ensureCategoryBags();
        check(Dungeon.hero.belongings.getBags().size()==7,"large-bag saves receive all six category bags");
        check(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch.class)==originalPouch,"existing category bags preserved");
        check(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier.class).contains(water),"existing waterskin sorted into potion bag");
        check(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster.class).contains(dart) && dart.holster,"existing thrown weapon gains holster mechanics");
        MmoRules.ensureCategoryBags();
        check(Dungeon.hero.belongings.getBags().size()==7,"loading again does not duplicate bags");
        check(Dungeon.LimitedDrops.SCROLL_HOLDER.dropped() && Dungeon.LimitedDrops.POTION_BANDOLIER.dropped() && Dungeon.LimitedDrops.MAGICAL_HOLSTER.dropped(),"shop bag flags match supplied bags");
        Dungeon.challenges=Challenges.MMO_PRESET;
        Sword lower=new Sword();lower.level(3);lower.enchantment=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing();lower.collect();
        Sword upper=new Sword();upper.level(100);upper.enchantment=null;upper.cursed=false;upper.levelKnown=true;upper.cursedKnown=true;
        check(MmoRules.absorb(upper,Dungeon.hero) && lower.mmoLevel().equals(BigInteger.valueOf(104)),"merge directly sums real high levels");
        check(lower.enchantment==null && !lower.cursed && lower.levelKnown,"higher cleansed item clears lower enchantment and curse");
        Sword zero=new Sword();BigInteger prior=lower.mmoLevel();check(MmoRules.absorb(zero,Dungeon.hero) && lower.mmoLevel().equals(prior.add(BigInteger.ONE)),"white item contributes one real level");
        MagesStaff uniqueStaff=new MagesStaff(new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile());Dungeon.hero.belongings.weapon=uniqueStaff;
        Sword ordinarySword=new Sword();ordinarySword.level(2);BigInteger staffBefore=uniqueStaff.mmoLevel();MmoRules.absorb(ordinarySword,Dungeon.hero);
        check(uniqueStaff.mmoLevel().equals(staffBefore),"ordinary melee weapons cannot upgrade mage staff");
        Sword duplicate=new Sword();duplicate.level(5);duplicate.collect();MmoRules.consolidateEquipment();
        check(Dungeon.hero.belongings.getAllItems(Sword.class).size()==1 && lower.mmoLevel().equals(BigInteger.valueOf(114)),"existing duplicate inventory consolidates once without item-level loss");
        MmoRules.consolidateEquipment();check(lower.mmoLevel().equals(BigInteger.valueOf(114)),"consolidation is idempotent");
        lower.level(0);Sword white=new Sword();check(MmoRules.absorb(white,Dungeon.hero) && lower.mmoLevel().equals(BigInteger.ONE),"two white items merge to plus one");
        lower.level(1);Sword plusOne=new Sword();plusOne.level(1);check(MmoRules.absorb(plusOne,Dungeon.hero) && lower.mmoLevel().equals(BigInteger.valueOf(3)),"two plus-one items merge to plus three");
        lower.level(1);Sword plusEighteen=new Sword();plusEighteen.level(18);check(MmoRules.absorb(plusEighteen,Dungeon.hero) && lower.mmoLevel().equals(BigInteger.valueOf(20)),"plus one and plus eighteen merge to plus twenty");
        check(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.WeaponBag.class).contains(lower),"ordinary weapon is held by weapon bag");
        RingOfMight spareRing=new RingOfMight();spareRing.collect();
        check(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.RingBag.class).contains(spareRing),"ring is held by ring bag");
        Dungeon.depth=5;Rat fixedArmor=new Rat();
        check(MmoRules.physical(new Target(),fixedArmor,10,2)==1,"monster armor stays native while its health scales");
        Dungeon.depth=1;
        lower.levelKnown=false;lower.cursedKnown=false;
        Sword hiddenHigher=new Sword();hiddenHigher.level(30);hiddenHigher.cursed=true;
        check(MmoRules.absorb(hiddenHigher,Dungeon.hero) && lower.levelKnown && lower.cursedKnown && lower.cursed,"merge reveals facts without removing the winning curse");
        RingOfHaste hiddenRing=new RingOfHaste();
        check(!hiddenRing.isKnown(),"ring type begins unknown in fixture");
        check(!MmoRules.absorb(hiddenRing,Dungeon.hero) && hiddenRing.levelKnown && hiddenRing.cursedKnown && !hiddenRing.isKnown() && !hiddenRing.isIdentified(),"unmerged ring reveals level and curse but not type");
        hiddenRing.collect();RingOfHaste donorRing=new RingOfHaste();donorRing.level(5);
        check(MmoRules.absorb(donorRing,Dungeon.hero) && hiddenRing.levelKnown && hiddenRing.cursedKnown && !hiddenRing.isKnown() && !hiddenRing.isIdentified(),"merged ring still does not reveal type");
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost hiddenWand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost();
        check(!MmoRules.absorb(hiddenWand,Dungeon.hero) && hiddenWand.levelKnown && hiddenWand.cursedKnown && !hiddenWand.curChargeKnown && !hiddenWand.isIdentified(),"unmerged wand retains unknown charge");
        hiddenWand.collect();com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost donorWand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost();donorWand.level(5);
        check(MmoRules.absorb(donorWand,Dungeon.hero) && hiddenWand.levelKnown && hiddenWand.cursedKnown && !hiddenWand.curChargeKnown && !hiddenWand.isIdentified(),"merged wand retains unknown charge");
        lower.levelKnown=false;lower.cursedKnown=false;MmoRules.consolidateEquipment();
        check(lower.levelKnown && lower.cursedKnown,"existing inventory gains level and curse information");
        Dungeon.challenges=0;Sword nativeUnknown=new Sword();MmoRules.absorb(nativeUnknown,Dungeon.hero);
        check(!nativeUnknown.levelKnown && !nativeUnknown.cursedKnown,"disabled absorption retains native identification");
        System.out.println("PASS: "+checks+" MMO regression checks");
    }
}
