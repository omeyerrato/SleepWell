package org.sleepwell.app;

import java.util.*;

/** Ordered, bounded tasks. Completing a sensor task is one unit, never an early dismissal. */
final class ChallengeRoutine {
    static final int LIMIT=8;
    static final List<String> TYPES=List.of("Math","Memory","Sequence","Retype","Shake","Walk","NFC","QR code","Visual search","Counting","Tilt maze","Flags","Mirror text","Dream journal","Speak","Picture match","Jumping","Smile","Pose","Light","Type code","Camera light","Spin around","Musical note");
    static final class Task {
        final String type;final int rounds,targetSteps,journalMinimum;final MathSettings math;final MorningCode code;final PictureReference picture;
        Task(String type,int rounds){this(type,rounds,type.equals("Walk")?10:0);}
        Task(String type,int rounds,int steps){this(type,rounds,steps,MathSettings.LEGACY);}
        Task(String type,int rounds,int steps,MathSettings math){this(type,rounds,steps,math,null);}
        Task(String type,int rounds,int steps,MathSettings math,MorningCode code){this(type,rounds,steps,math,code,JournalRule.DEFAULT);}
        Task(String type,int rounds,int steps,MathSettings math,MorningCode code,int journalMinimum){this(type,rounds,steps,math,code,journalMinimum,null);}
        Task(String type,int rounds,int steps,MathSettings math,MorningCode code,int journalMinimum,PictureReference picture){if(type.equals("Picture match")&&(rounds!=1||picture==null)||!type.equals("Picture match")&&picture!=null)throw new IllegalArgumentException("Enroll a picture reference for one Picture match task");this.picture=picture;if((type.equals("Light")||type.equals("Type code")||type.equals("Camera light")||type.equals("Musical note"))&&(rounds<1||rounds>3))throw new IllegalArgumentException("Choose a supported challenge level");if(type.equals("Spin around")&&rounds!=1)throw new IllegalArgumentException("Spin around is one continuous turn");if(type.equals("Pose")&&rounds!=1)throw new IllegalArgumentException("Pose is one bounded hands-raised camera task");if(type.equals("Smile")&&rounds!=1)throw new IllegalArgumentException("Smile is one bounded camera task");if(type.equals("Speak")&&rounds!=1)throw new IllegalArgumentException("Speak is one bounded listening task");if(type.equals("Dream journal")&&(rounds!=1||!JournalRule.MINIMUMS.contains(journalMinimum))||!type.equals("Dream journal")&&journalMinimum!=JournalRule.DEFAULT)throw new IllegalArgumentException("Unsupported journal settings");if(!TYPES.contains(type)||rounds<1||rounds>6||type.equals("Walk")&&(rounds!=1||steps<5||steps>100)||math==null||!type.equals("Math")&&!math.equals(MathSettings.LEGACY)||type.equals("QR code")&&(rounds!=1||code==null)||!type.equals("QR code")&&code!=null)throw new IllegalArgumentException("Unsupported challenge settings");this.journalMinimum=journalMinimum;this.type=type;this.rounds=rounds;targetSteps=type.equals("Walk")?steps:0;this.math=math;this.code=code;}
        int units(){return type.equals("Musical note")||type.equals("Spin around")||type.equals("Camera light")||type.equals("Light")||type.equals("Type code")||type.equals("Pose")||type.equals("Smile")||type.equals("Picture match")||type.equals("Speak")||type.equals("Dream journal")||type.equals("Shake")||type.equals("NFC")||type.equals("Walk")||type.equals("QR code")?1:rounds;}
        String detail(){return type.equals("Musical note")?"Pitch match · ±"+NotePitch.tolerance(rounds)+" cents":type.equals("Spin around")?"360° turn · two-finger hold":type.equals("Camera light")?CameraLightHold.percent(rounds)+"% image brightness · 1.2 sec":type.equals("Light")?LightHold.target(rounds)+" lx · 2 sec hold":type.equals("Type code")?TypeCodePuzzle.length(rounds)+" characters · case-insensitive":type.equals("Pose")?"Both hands raised · 1.2 sec hold":type.equals("Smile")?"Steady smile · Math alternative": type.equals("Jumping")?"3 hurdles · "+rounds+(rounds==1?" round":" rounds"): type.equals("Picture match")?picture.label+" · similarity estimate":type.equals("Speak")?"2 speech windows · Math fallback":type.equals("Dream journal")?journalMinimum+" letters / digits · 8 words":type.equals("QR code")?(MorningBarcode.QR.equals(code.format)?code.label:MorningBarcode.label(code.format)+" · "+code.label):type.equals("Math")?math.level.label+" · "+rounds+(rounds==1?" round":" rounds"):type.equals("Walk")?targetSteps+" steps":type.equals("Shake")?rounds*5+" shakes":type.equals("NFC")?"Enrolled morning tag":rounds+(rounds==1?" round":" rounds");}
    }
    static final class Position {
        final Task task;final int index,round;
        Position(Task task,int index,int round){this.task=task;this.index=index;this.round=round;}
    }
    final List<Task> tasks;
    ChallengeRoutine(List<Task> tasks){if(tasks.isEmpty()||tasks.size()>LIMIT)throw new IllegalArgumentException("Choose 1–8 tasks");this.tasks=List.copyOf(tasks);}
    static ChallengeRoutine legacy(String type,int rounds){return new ChallengeRoutine(List.of(new Task(TYPES.contains(type)&&!type.equals("Walk")&&!type.equals("QR code")&&!type.equals("Dream journal")&&!type.equals("Speak")&&!type.equals("Picture match")&&!type.equals("Smile")&&!type.equals("Pose")&&!type.equals("Light")&&!type.equals("Type code")&&!type.equals("Camera light")&&!type.equals("Spin around")&&!type.equals("Musical note")?type:"Math",Math.max(1,Math.min(6,rounds)))));}
    int units(){int total=0;for(Task task:tasks)total+=task.units();return total;}
    Position at(int completed){if(completed<0||completed>=units())return null;int offset=0;for(int n=0;n<tasks.size();n++){Task task=tasks.get(n);if(completed<offset+task.units())return new Position(task,n,completed-offset);offset+=task.units();}throw new AssertionError();}
    /** A stale/duplicate task callback must not skip the next task or round. */
    int advance(int completed,int expected){return completed==expected&&at(completed)!=null?completed+1:completed;}
    ChallengeRoutine replace(int index,Task task){List<Task> copy=new ArrayList<>(tasks);copy.set(index,task);return new ChallengeRoutine(copy);}
    ChallengeRoutine add(Task task){List<Task> copy=new ArrayList<>(tasks);copy.add(task);return new ChallengeRoutine(copy);}
    ChallengeRoutine move(int index,int destination){List<Task> copy=new ArrayList<>(tasks);Task task=copy.remove(index);copy.add(destination,task);return new ChallengeRoutine(copy);}
    ChallengeRoutine remove(int index){List<Task> copy=new ArrayList<>(tasks);copy.remove(index);return new ChallengeRoutine(copy);}
    String summary(){return tasks.size()==1?tasks.get(0).type.equals("Walk")?"Walk · "+tasks.get(0).targetSteps+" steps":tasks.get(0).type+" × "+tasks.get(0).rounds:tasks.size()+" tasks";}
}
