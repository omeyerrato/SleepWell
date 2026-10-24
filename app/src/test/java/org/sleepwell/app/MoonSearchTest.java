package org.sleepwell.app;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public final class MoonSearchTest {
    @Test public void everyBoundedRoundHasExactlyOneSleepingTarget(){for(int round=0;round<6;round++)for(int seed=0;seed<300;seed++){MoonSearch b=MoonSearch.create(new Random(seed),round);assertEquals(3+round/2,b.columns);int matches=0;for(int n=0;n<b.size();n++)if(b.sleeping(n))matches++;assertEquals(1,matches);assertFalse(b.sleeping(-1));assertFalse(b.sleeping(b.size()));}}
    @Test public void codecRetainsExactBoardAndOwnsItsArray(){int[] faces=new int[9];MoonSearch b=new MoonSearch(3,2,faces);faces[0]=3;assertEquals(0,b.face(0));MoonSearch reopened=MoonSearch.decode(b.encode());assertEquals(b.encode(),reopened.encode());assertEquals(2,reopened.target);for(int n=0;n<b.size();n++)assertEquals(b.face(n),reopened.face(n));}
    @Test public void malformedBoardsCannotIntroduceMultipleOrMissingTargets(){for(String value:new String[]{"","2:3:0:000000000","1:2:0:0000","1:3:9:000000000","1:3:-1:000000000","1:4:1:000000000","1:3:1:000000004","1:3:0:000000000:extra"})assertThrows(value,IllegalArgumentException.class,()->MoonSearch.decode(value));assertThrows(IllegalArgumentException.class,()->MoonSearch.create(new Random(),6));assertThrows(IllegalArgumentException.class,()->MoonSearch.create(new Random(),-1));}
    @Test public void allCellsCanBeTargetsAndEveryDecoyStaysAwake(){Set<Integer> targets=new HashSet<>();for(int seed=0;seed<1000;seed++){MoonSearch b=MoonSearch.create(new Random(seed),0);targets.add(b.target);for(int n=0;n<b.size();n++)assertEquals(n==b.target,b.sleeping(n));}assertEquals(9,targets.size());}
}
