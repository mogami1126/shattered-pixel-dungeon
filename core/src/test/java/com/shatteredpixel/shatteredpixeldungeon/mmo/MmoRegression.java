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
    private static class FastSprite extends QuietSprite {
        public void frame(RectF frame){}
        void start(Callback callback) {
            animCallback=callback;
            play(new com.watabou.noosa.MovieClip.Animation(1,false).frames(new RectF(),new RectF()));
        }
        void tick(){updateAnimation();}
    }
    private static class FastMob extends Mob {
        int attacks;
        public boolean attack(Char target,float damage,float bonus,float accuracy){attacks++;return true;}
        boolean strike(Char target){return doAttack(target);}
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
    private static class MortalRat extends Rat {
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
        Gdx.app=(com.badlogic.gdx.Application)Proxy.newProxyInstance(com.badlogic.gdx.Application.class.getClassLoader(),new Class[]{com.badlogic.gdx.Application.class},(o,m,a)->{
            if(m.getName().equals("getType"))return com.badlogic.gdx.Application.ApplicationType.Desktop;
            if(m.getReturnType()==int.class)return 0;
            if(m.getReturnType()==boolean.class)return false;
            return null;
        });
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
        check(!SPDSettings.fastTurns(),"fast turns default off");
        SPDSettings.fastTurns(true);
        FastMob fastMob=new FastMob();fastMob.sprite=new QuietSprite();fastMob.sprite.visible=true;
        Target fastTarget=new Target();fastTarget.sprite=new QuietSprite();fastTarget.sprite.visible=true;
        float beforeAttack=fastMob.cooldown();
        check(fastMob.strike(fastTarget) && fastMob.attacks==1,"visible fast attack resolves once without waiting for animation");
        check(fastMob.cooldown()-beforeAttack==fastMob.attackDelay(),"fast attack retains its turn cost");
        int[] completions={0};FastSprite fastSprite=new FastSprite();fastSprite.ch=fastMob;
        fastSprite.start(()->completions[0]++);
        check(completions[0]==0,"fast callbacks do not run inside play");
        fastSprite.tick();fastSprite.tick();
        check(completions[0]==1,"fast render completion runs exactly once");
        fastSprite.ch=Dungeon.hero;fastSprite.start(()->completions[0]++);fastSprite.tick();
        check(completions[0]==1,"hero animation retains native timing");
        SPDSettings.fastTurns(false);fastSprite.ch=fastMob;fastSprite.start(()->completions[0]++);fastSprite.tick();
        check(completions[0]==1,"disabled fast mode retains monster animation timing");
        Dungeon.challenges=Challenges.AUTO_PICKUP | Challenges.LARGE_BAGS;
        TestLevel pickupFloor=new TestLevel();pickupFloor.setSize(5,5);pickupFloor.heaps=new SparseArray<>();
        Arrays.fill(pickupFloor.passable,true);Arrays.fill(pickupFloor.heroFOV,true);
        Arrays.fill(pickupFloor.map,Terrain.EMPTY);Dungeon.level=pickupFloor;
        Hero collector=new Hero();collector.heroClass=HeroClass.WARRIOR;collector.sprite=new QuietSprite();
        collector.pos=12;collector.HP=10;collector.HT=20;Dungeon.hero=collector;
        com.shatteredpixel.shatteredpixeldungeon.journal.Notes.reset();
        com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb bomb=new com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb();
        bomb.fuse=new com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb.Fuse().ignite(bomb);
        com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey key=new com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey(1);
        Heap loose=new Heap();loose.pos=12;loose.type=Heap.Type.HEAP;
        loose.items.add(bomb);loose.items.add(key);loose.items.add(new Dewdrop());
        Item refusing=new Item(){public boolean doPickUp(Hero user,int pos){return false;}};loose.items.add(refusing);
        pickupFloor.heaps.put(12,loose);
        Heap chest=new Heap();chest.pos=13;chest.type=Heap.Type.CHEST;chest.items.add(new com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb());pickupFloor.heaps.put(13,chest);
        float beforePickup=collector.cooldown();MmoRules.pickup(collector);
        check(collector.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb.class)!=null && bomb.fuse==null,"automatic pickup collects and defuses bombs");
        check(com.shatteredpixel.shatteredpixeldungeon.journal.Notes.keyCount(key)==1 && !collector.belongings.backpack.contains(key),"automatic keys go to journal rather than backpack");
        check(collector.HP==11,"automatic dew retains healing hook");
        check(loose.items.size()==1 && loose.items.contains(refusing),"refused items stay on ground while other items are collected");
        check(chest.items.size()==1,"automatic collection does not open chests");
        check(collector.cooldown()==beforePickup,"bulk hooks do not spend per-item turns");
        try {collector.pickUpAutomatically(new Item(){public boolean doPickUp(Hero user,int pos){throw new IllegalStateException("fixture");}},12);} catch(IllegalStateException expected){}
        collector.spendAndNext(1);
        check(collector.cooldown()==beforePickup+1,"pickup scope clears even after a hook throws");
        setup(args[0]);
        // Manual recovery must not depend on the densely populated challenge.
        Dungeon.challenges=Challenges.UNLIMITED_GROWTH;
        Hero forger=Dungeon.hero;
        Sword receiver=new Sword();receiver.level(9);forger.belongings.weapon=receiver;
        Dagger donor=new Dagger();donor.level(2);forger.belongings.backpack.items.add(donor);
        check(MmoForge.coefficient(receiver,donor)==90,"same category is exactly 0.9");
        Sword sameName=new Sword();sameName.level(2);
        check(MmoForge.coefficient(receiver,sameName)==90,"manual recovery has no same-name exemption");
        PlateArmor plate=new PlateArmor();plate.level(2);
        ClothArmor cloth=new ClothArmor();cloth.level(2);
        check(MmoForge.units(receiver,plate).equals(MmoForge.units(receiver,cloth)),"native tier has no recovery bonus");
        com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();
        check(MmoForge.coefficient(receiver,wand)==81 && MmoForge.coefficient(wand,receiver)==81,"different category is 0.81 in both directions, without group classification");
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife knife=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingKnife();
        check(MmoForge.coefficient(receiver,knife)==0 && MmoForge.units(receiver,knife).signum()==0,"throwing weapons cannot be recovered");
        check(!MmoForge.target(knife) && MmoForge.units(knife,donor).signum()==0,"throwing weapons have no recovery target action");
        com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll.initLabels();
        com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.initColors();
        com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade upgradeScroll=new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade();
        com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing potion=new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing();
        check(MmoForge.units(receiver,upgradeScroll).equals(MmoForge.UNIT) && MmoForge.units(receiver,potion).equals(MmoForge.UNIT),"all potions and scrolls grant exactly one level");
        LinkedHashMap<Item,Integer> selection=new LinkedHashMap<>();selection.put(donor,1);
        check(MmoForge.apply(forger,receiver,selection),"recovery works without Omnipresent or MMO enemy growth");
        check(receiver.mmoLevel().equals(BigInteger.valueOf(12)),"+9 plus +2 same-category material rounds 2.7 to 3 levels");
        check(!forger.belongings.backpack.contains(donor) && !MmoForge.apply(forger,receiver,selection),"consumed material cannot be used twice");
        potion.quantity(10);forger.belongings.backpack.items.add(potion);
        selection.clear();selection.put(potion,5);
        check(MmoForge.apply(forger,receiver,selection) && potion.quantity()==5 && receiver.mmoLevel().equals(BigInteger.valueOf(17)),"partial stack adds each unit and consumes only requested quantity");
        selection.clear();selection.put(potion,6);
        check(!MmoForge.apply(forger,receiver,selection) && potion.quantity()==5,"invalid batch leaves all quantities unchanged");
        Bundle forgedSave=new Bundle();receiver.storeInBundle(forgedSave);
        check(!forgedSave.contains("mmo_forge_units"),"no fractional recovery credit is saved");
        selection.clear();selection.put(plate,1);
        check(MmoForge.levels(receiver,selection).equals(BigInteger.valueOf(2)),"different-category 2.43 rounds down to 2");
        selection.clear();selection.put(donor,1);selection.put(sameName,1);
        check(MmoForge.levels(receiver,selection).equals(BigInteger.valueOf(5)),"round the combined batch rather than each material individually");
        plate.level(49);selection.clear();selection.put(plate,1);
        check(MmoForge.levels(receiver,selection).equals(BigInteger.valueOf(41)),"exact half-level 40.5 rounds up without floating point error");
        com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Seed seed=new com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Seed();
        com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink stone=new com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink();
        check(MmoForge.units(receiver,seed).multiply(BigInteger.valueOf(3)).equals(MmoForge.UNIT),"three seeds contribute exactly one level");
        check(MmoForge.units(receiver,stone).multiply(BigInteger.valueOf(3)).equals(MmoForge.UNIT),"three runestones contribute exactly one level");
        selection.clear();selection.put(seed,1);
        check(MmoForge.levels(receiver,selection).equals(BigInteger.ZERO),"one seed rounds to zero without saving a remainder");
        selection.put(seed,2);
        check(MmoForge.levels(receiver,selection).equals(BigInteger.ONE),"two seeds round their combined two thirds to one");
        selection.put(seed,3);
        check(MmoForge.total(receiver,selection).equals(MmoForge.UNIT),"three seeds are exact before rounding");
        selection.clear();selection.put(seed,1);selection.put(stone,2);
        check(MmoForge.total(receiver,selection).equals(MmoForge.UNIT),"mixed seeds and stones combine into an exact level");
        selection.put(potion,1);
        check(MmoForge.levels(receiver,selection).equals(BigInteger.TWO),"thirds combine with whole potion credits");
        selection.clear();selection.put(plate,1);selection.put(stone,1);
        check(MmoForge.levels(receiver,selection).equals(BigInteger.valueOf(41)),"40.5 equipment plus one third still rounds once to 41");
        Dagger hugeDonor=new Dagger();hugeDonor.level(20);hugeDonor.mmoExtra=BigInteger.TEN.pow(80);forger.belongings.backpack.items.add(hugeDonor);
        selection.clear();selection.put(hugeDonor,1);
        BigInteger expected=MmoForge.levels(receiver,selection).add(receiver.mmoLevel());
        check(MmoForge.apply(forger,receiver,selection) && receiver.mmoLevel().equals(expected),"huge recovery uses exact large integers without a per-level loop");
        Dungeon.challenges=0;
        check(!MmoForge.target(receiver),"normal mode exposes no forging action");
        setup(args[0]);
        Dungeon.challenges=Challenges.MMO | Challenges.BIOCHIP;
        for(int depth:new int[]{1,5,10,15,20,25}) {
            Dungeon.depth=depth;Rat reference=new Rat();double g=Math.pow(1.5,depth-1);
            check(Math.abs(MmoRules.health(reference).times(reference.HT).bounded(Double.MAX_VALUE)/(reference.HT*g)-1)<1e-12,"original MMO health formula at floor "+depth);
            check(Math.abs(MmoRules.attack(reference).times(2).bounded(Double.MAX_VALUE)/(2*g)-1)<1e-12,"original MMO damage formula at floor "+depth);
            check(MmoRules.mobExperience(10)==(int)(10*g),"original MMO experience formula at floor "+depth);
            check(MmoRules.mobSkillGrowth()==depth,"original MMO skill multiplier at floor "+depth);
            reference.mmoHitCredit=0;MmoRules.hit(reference,Dungeon.hero,10,1000);
            check(Math.abs(reference.mmoHitCredit-depth/200.0)<1e-12,"MMO accuracy actually reaches the hit calculation at floor "+depth);
            Dungeon.hero.mmoHitCredit=0;MmoRules.hit(Dungeon.hero,reference,100,100);
            check(Math.abs(Dungeon.hero.mmoHitCredit-(depth==1?.5:1.0/(2*depth)))<1e-12,"MMO evasion actually reaches the hit calculation at floor "+depth);
            for(double ratio:new double[]{1,1.5,1.99,2,1000000}) {
                MortalRat mortal=new MortalRat();mortal.sprite=new QuietSprite();
                int hit=MmoRules.normalized(mortal,MmoRules.health(mortal).times(mortal.HP*ratio));
                mortal.damageBase(hit,Dungeon.hero);
                check(mortal.died && mortal.HP==0,"no MMO anti-one-shot protection at floor "+depth+", damage/HP="+ratio);
            }
        }
        Dungeon.challenges=0;Dungeon.depth=25;
        check(MmoRules.mobGrowth().compareTo(GameNumber.ONE)==0 && MmoRules.mobSkillGrowth()==1 && MmoRules.mobExperience(10)==10,"disabled MMO preserves native monsters and experience");
        Dungeon.challenges=Challenges.MMO;Dungeon.depth=5;
        Rat justBelow=new Rat();
        check(MmoRules.normalized(justBelow,MmoRules.health(justBelow).times(1-1e-8))==0,"real fractional damage is not rounded into an extra point");
        check(MmoRules.normalized(justBelow,MmoRules.health(justBelow).times(1e-8))==1,"real fractional remainder still accumulates correctly");
        System.out.println("PASS: "+checks+" MMO regression checks");
    }
}
