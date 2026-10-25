package org.sleepwell.app;

import java.util.*;

/** Small explicitly named pack; flag patterns are drawn originally, without third-party artwork. */
final class FlagPuzzle {
    static final List<String> COUNTRIES=List.of("Japan","France","Italy","Germany","Poland","Ukraine","Sweden","Finland");
    final int target;final List<Integer> choices;
    private FlagPuzzle(int target,List<Integer> choices){this.target=target;this.choices=List.copyOf(choices);}
    static FlagPuzzle create(String run,int unit,int round){Random random=new Random(CountingPuzzle.seed(run,unit,round,"flag-v1"));int target=random.nextInt(COUNTRIES.size());List<Integer> options=new ArrayList<>();for(int n=0;n<COUNTRIES.size();n++)if(n!=target)options.add(n);Collections.shuffle(options,random);options=new ArrayList<>(options.subList(0,3));options.add(target);Collections.shuffle(options,random);return new FlagPuzzle(target,options);}
    boolean accepts(int country){return country==target&&choices.contains(country);}
}
