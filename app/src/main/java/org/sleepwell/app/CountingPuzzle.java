package org.sleepwell.app;

import java.util.*;

/** Original geometric counting board, deterministic for one occurrence/unit/round. */
final class CountingPuzzle {
    final int columns,target;private final int[] shapes;
    private CountingPuzzle(int columns,int target,int[] shapes){this.columns=columns;this.target=target;this.shapes=shapes;}
    static long seed(String run,int unit,int round,String kind){if(run==null||run.isEmpty()||run.length()>128||unit<0||round<0||round>5)throw new IllegalArgumentException("Invalid challenge owner");long seed=0xcbf29ce484222325L;String key=kind+":"+run+":"+unit+":"+round;for(int n=0;n<key.length();n++){seed^=key.charAt(n);seed*=0x100000001b3L;}return seed;}
    static CountingPuzzle create(String run,int unit,int round){Random random=new Random(seed(run,unit,round,"count-v1"));int columns=round<3?4:5;int[] cells=new int[columns*columns];for(int n=0;n<cells.length;n++)cells[n]=random.nextInt(3);int target=random.nextInt(3);cells[random.nextInt(cells.length)]=target;return new CountingPuzzle(columns,target,cells);}
    int size(){return shapes.length;}int shape(int cell){return shapes[cell];}
    int answer(){int count=0;for(int shape:shapes)if(shape==target)count++;return count;}
    String targetName(){return name(target);}static String name(int shape){return switch(shape){case 0->"circles";case 1->"diamonds";case 2->"triangles";default->throw new IllegalArgumentException("Unknown shape");};}
    boolean accepts(String value){return value!=null&&value.trim().matches("[0-9]{1,2}")&&Integer.parseInt(value.trim())==answer();}
}
