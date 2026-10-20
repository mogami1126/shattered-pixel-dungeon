import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.watabou.noosa.Game;
import com.watabou.utils.GameSettings;
import java.lang.reflect.Proxy;
import java.util.*;

/** Analysis-only sampler. Does not read or write player saves or launch a game. */
public class InitialMobSample {
    static class Floor extends SewerLevel {
        // Item placement does not affect createMobs, which runs first.
        protected void createItems() {}
    }
    static class PrisonFloor extends PrisonLevel {
        protected void createItems() {}
    }
    public static void main(String[] args) {
        com.badlogic.gdx.utils.GdxNativesLoader.load();
        Game.version="4.0.2-MMO.1.6";
        Game.instance=new Game(com.watabou.noosa.Scene.class,null);
        Map<String,Object> prefs=new HashMap<>();
        prefs.put("challenges",Challenges.MMO_PRESET);
        GameSettings.set((Preferences)Proxy.newProxyInstance(Preferences.class.getClassLoader(),new Class[]{Preferences.class},(o,m,a)->{
            if(m.getName().equals("contains"))return prefs.containsKey(a[0]);
            if(m.getName().startsWith("get") && a!=null && a.length==2)return prefs.getOrDefault(a[0].toString(),a[1]);
            if(m.getName().equals("get"))return prefs;
            if(m.getName().startsWith("put")){prefs.put(a[0].toString(),a[1]);return o;}
            if(m.getReturnType()==boolean.class)return false;
            return null;
        }));
        Gdx.app=(Application)Proxy.newProxyInstance(Application.class.getClassLoader(),new Class[]{Application.class},(o,m,a)->{
            if(m.getName().equals("getType"))return Application.ApplicationType.Desktop;
            if(m.getReturnType()==int.class)return 0;
            if(m.getReturnType()==boolean.class)return false;
            return null;
        });
        Gdx.files=(Files)Proxy.newProxyInstance(Files.class.getClassLoader(),new Class[]{Files.class},(o,m,a)->new FileHandle(args[0]+"/"+a[0]));
        com.watabou.utils.FileUtils.setDefaultFileProperties(Files.FileType.Local,"analysis-only/");
        GamesInProgress.selectedClass=HeroClass.WARRIOR;
        Dungeon.customSeedText="analysis-only";
        System.out.println("seed,depth,initial_enemies");
        for(int seed=1;seed<=30;seed++) {
            Dungeon.seed=100000L+seed;Dungeon.init();
            for(int depth=1;depth<=(args.length>1?Integer.parseInt(args[1]):4);depth++) {
                if(depth%5==0)continue;
                Dungeon.depth=depth;Level floor=depth<5?new Floor():new PrisonFloor();Dungeon.level=floor;floor.create();
                int count=0;for(Mob mob:floor.mobs)if(mob.alignment==Char.Alignment.ENEMY)count++;
                System.out.println(seed+","+depth+","+count);
            }
        }
    }
}
