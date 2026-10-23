package org.sleepwell.app;

import java.util.*;

/** Independent arithmetic complexity; round count keeps its original routine meaning. */
final class MathSettings {
    enum Level {EASY("Easy",2,9,2,5),MEDIUM("Medium",2,19,2,12),HARD("Hard",20,99,10,19);
        final String label;final int minimum,maximum,factorMinimum,factorMaximum;
        Level(String label,int minimum,int maximum,int factorMinimum,int factorMaximum){this.label=label;this.minimum=minimum;this.maximum=maximum;this.factorMinimum=factorMinimum;this.factorMaximum=factorMaximum;}
        String key(){return name().toLowerCase(Locale.ROOT);}
    }
    enum Operator {ADD("add","Addition","+","plus",1),SUBTRACT("subtract","Subtraction","−","minus",2),MULTIPLY("multiply","Multiplication","×","times",4),DIVIDE("divide","Division","÷","divided by",8);
        final String key,label,symbol,spoken;final int bit;
        Operator(String key,String label,String symbol,String spoken,int bit){this.key=key;this.label=label;this.symbol=symbol;this.spoken=spoken;this.bit=bit;}
        int answer(int left,int right){return switch(this){case ADD->left+right;case SUBTRACT->left-right;case MULTIPLY->left*right;case DIVIDE->left/right;};}
    }
    static final MathSettings LEGACY=new MathSettings(Level.MEDIUM,1,false);
    final Level level;final int operators;final boolean typed;
    MathSettings(Level level,int operators,boolean typed){if(level==null||operators<1||(operators&~15)!=0)throw new IllegalArgumentException("Choose at least one supported arithmetic operator.");this.level=level;this.operators=operators;this.typed=typed;}
    List<Operator> enabled(){List<Operator> result=new ArrayList<>();for(Operator op:Operator.values())if((operators&op.bit)!=0)result.add(op);return List.copyOf(result);}
    boolean permits(Operator operator){return (operators&operator.bit)!=0;}
    MathSettings toggle(Operator op,boolean on){return new MathSettings(level,on?operators|op.bit:operators&~op.bit,typed);}
    String explanation(){return "Add/subtract: "+level.minimum+"–"+level.maximum+". Multiply/divide: "+level.factorMinimum+"–"+level.factorMaximum+" factors.";}
    boolean operands(Operator op,int left,int right){
        if(op==null||!permits(op))return false;
        if(op==Operator.ADD||op==Operator.SUBTRACT)return left>=level.minimum&&left<=level.maximum&&right>=level.minimum&&right<=level.maximum&&(op!=Operator.SUBTRACT||left>=right);
        if(op==Operator.MULTIPLY)return left>=level.factorMinimum&&left<=level.factorMaximum&&right>=level.factorMinimum&&right<=level.factorMaximum;
        return right>=level.factorMinimum&&right<=level.factorMaximum&&left>0&&left%right==0&&left/right>=level.factorMinimum&&left/right<=level.factorMaximum;
    }
    @Override public boolean equals(Object other){return other instanceof MathSettings s&&level==s.level&&operators==s.operators&&typed==s.typed;}
    @Override public int hashCode(){return Objects.hash(level,operators,typed);}
}
