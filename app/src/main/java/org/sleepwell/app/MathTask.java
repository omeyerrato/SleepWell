package org.sleepwell.app;

import java.util.*;

/** A task's answer order stays stable until it is solved, including activity recreation. */
final class MathTask {
    final int left,right;final List<Integer> options;
    MathTask(int left,int right,List<Integer> options){this.left=left;this.right=right;this.options=List.copyOf(options);}
    static MathTask create(Random random){
        int left=2+random.nextInt(18),right=2+random.nextInt(18),correct=left+right;
        Set<Integer> choices=new LinkedHashSet<>();choices.add(correct);
        while(choices.size()<4)choices.add(Math.max(0,correct-4+random.nextInt(9)));
        List<Integer> options=new ArrayList<>(choices);Collections.shuffle(options,random);return new MathTask(left,right,options);
    }
    boolean accepts(int option){return option==left+right;}
    String encode(){return left+","+right+","+options.get(0)+","+options.get(1)+","+options.get(2)+","+options.get(3);}
    static MathTask decode(String raw){String[] p=raw.split(",");if(p.length!=6)throw new IllegalArgumentException("Invalid task");List<Integer> values=new ArrayList<>();for(int i=2;i<6;i++)values.add(Integer.parseInt(p[i]));return new MathTask(Integer.parseInt(p[0]),Integer.parseInt(p[1]),values);}
}
