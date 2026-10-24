package org.sleepwell.app;

import java.util.*;

/** Original visual-search task: exactly one sleeping moon per bounded board. */
final class MoonSearch {
    final int columns,target;private final int[] faces;
    MoonSearch(int columns,int target,int[] faces){if(columns<3||columns>5||faces==null||faces.length!=columns*columns||target<0||target>=faces.length)throw new IllegalArgumentException("Invalid moon board");for(int face:faces)if(face<0||face>3)throw new IllegalArgumentException("Invalid moon face");this.columns=columns;this.target=target;this.faces=faces.clone();}
    static MoonSearch create(Random random,int round){if(random==null||round<0||round>5)throw new IllegalArgumentException("Invalid search round");int columns=3+round/2;int[] faces=new int[columns*columns];for(int n=0;n<faces.length;n++)faces[n]=random.nextInt(4);return new MoonSearch(columns,random.nextInt(faces.length),faces);}
    int size(){return faces.length;}
    int face(int cell){if(cell<0||cell>=size())throw new IllegalArgumentException("Invalid search cell");return faces[cell];}
    boolean sleeping(int cell){return cell>=0&&cell<size()&&cell==target;}
    String encode(){StringBuilder out=new StringBuilder("1:").append(columns).append(':').append(target).append(':');for(int face:faces)out.append(face);return out.toString();}
    static MoonSearch decode(String encoded){if(encoded==null||encoded.length()>40||!encoded.matches("1:[3-5]:[0-9]{1,2}:[0-3]{9,25}"))throw new IllegalArgumentException("Invalid moon board");String[] bits=encoded.split(":",-1);int[] faces=new int[bits[3].length()];for(int n=0;n<faces.length;n++)faces[n]=bits[3].charAt(n)-'0';return new MoonSearch(Integer.parseInt(bits[1]),Integer.parseInt(bits[2]),faces);}
}
