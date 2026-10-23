package org.sleepwell.app;

import java.util.*;

/** A task's operator, operands and answer order stay stable until solved, including recreation. */
final class MathTask {
    final int left,right;final List<Integer> options;final MathSettings settings;final MathSettings.Operator operator;
    MathTask(int left,int right,List<Integer> options){this(left,right,options,MathSettings.LEGACY,MathSettings.Operator.ADD);}
    MathTask(int left,int right,List<Integer> options,MathSettings settings,MathSettings.Operator operator){
        if(!settings.operands(operator,left,right)||options.size()!=4||new HashSet<>(options).size()!=4)throw new IllegalArgumentException("Invalid arithmetic task.");
        int correct=operator.answer(left,right);if(!options.contains(correct))throw new IllegalArgumentException("Missing arithmetic answer.");for(int value:options)if(value<0||Math.abs((long)value-correct)>4)throw new IllegalArgumentException("Invalid arithmetic choice.");
        this.left=left;this.right=right;this.options=List.copyOf(options);this.settings=settings;this.operator=operator;
    }
    static MathTask create(Random random){return create(random,MathSettings.LEGACY);}
    static MathTask create(Random random,MathSettings settings){
        List<MathSettings.Operator> enabled=settings.enabled();MathSettings.Operator operator=enabled.get(enabled.size()==1?0:random.nextInt(enabled.size()));MathSettings.Level level=settings.level;
        boolean factors=operator==MathSettings.Operator.MULTIPLY||operator==MathSettings.Operator.DIVIDE;int min=factors?level.factorMinimum:level.minimum,max=factors?level.factorMaximum:level.maximum;
        int left=min+random.nextInt(max-min+1),right=min+random.nextInt(max-min+1);
        if(operator==MathSettings.Operator.SUBTRACT&&left<right){int swap=left;left=right;right=swap;}
        if(operator==MathSettings.Operator.DIVIDE)left*=right;
        int correct=operator.answer(left,right);Set<Integer> choices=new LinkedHashSet<>();choices.add(correct);
        for(int n=0;n<64&&choices.size()<4;n++)choices.add(Math.max(0,correct-4+random.nextInt(9)));
        for(int delta=1;choices.size()<4;delta++)choices.add(correct+delta);
        List<Integer> options=new ArrayList<>(choices);Collections.shuffle(options,random);return new MathTask(left,right,options,settings,operator);
    }
    int answer(){return operator.answer(left,right);}
    boolean accepts(int option){return option==answer();}
    boolean acceptsTyped(String text){if(text==null)return false;String clean=text.trim();if(!clean.matches("[0-9]{1,5}"))return false;return accepts(Integer.parseInt(clean));}
    boolean matches(MathSettings configuration){return settings.equals(configuration);}
    String question(){return left+" "+operator.symbol+" "+right+" = ?";}
    String spoken(){return "Math question "+left+" "+operator.spoken+" "+right;}
    String encode(){String values=left+","+right+","+options.get(0)+","+options.get(1)+","+options.get(2)+","+options.get(3);return settings.equals(MathSettings.LEGACY)&&operator==MathSettings.Operator.ADD?values:"m2,"+settings.level.name()+","+settings.operators+","+(settings.typed?"typed":"choices")+","+operator.name()+","+values;}
    static MathTask decode(String raw){
        if(raw==null||raw.length()>160)throw new IllegalArgumentException("Invalid arithmetic cache.");String[] p=raw.split(",",-1);MathSettings settings=MathSettings.LEGACY;MathSettings.Operator operator=MathSettings.Operator.ADD;int offset=0;
        if(p.length==11&&p[0].equals("m2")){if(!p[3].equals("typed")&&!p[3].equals("choices"))throw new IllegalArgumentException("Invalid arithmetic answer mode.");settings=new MathSettings(MathSettings.Level.valueOf(p[1]),number(p[2]),p[3].equals("typed"));operator=MathSettings.Operator.valueOf(p[4]);offset=5;}else if(p.length!=6)throw new IllegalArgumentException("Invalid arithmetic task.");
        List<Integer> values=new ArrayList<>();for(int n=2;n<6;n++)values.add(number(p[offset+n]));return new MathTask(number(p[offset]),number(p[offset+1]),values,settings,operator);
    }
    private static int number(String raw){if(!raw.matches("0|[1-9][0-9]{0,4}"))throw new IllegalArgumentException("Invalid arithmetic integer.");return Integer.parseInt(raw);}
}
