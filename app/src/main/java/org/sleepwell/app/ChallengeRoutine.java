package org.sleepwell.app;

import java.util.*;

/** Ordered, bounded tasks. Completing a sensor task is one unit, never an early dismissal. */
final class ChallengeRoutine {
    static final int LIMIT=8;
    static final List<String> TYPES=List.of("Math","Memory","Sequence","Retype","Shake","NFC");
    static final class Task {
        final String type;final int rounds;
        Task(String type,int rounds){if(!TYPES.contains(type)||rounds<1||rounds>6)throw new IllegalArgumentException("Unsupported challenge settings");this.type=type;this.rounds=rounds;}
        int units(){return type.equals("Shake")||type.equals("NFC")?1:rounds;}
        String detail(){return type.equals("Shake")?rounds*5+" shakes":type.equals("NFC")?"Enrolled morning tag":rounds+(rounds==1?" round":" rounds");}
    }
    static final class Position {
        final Task task;final int index,round;
        Position(Task task,int index,int round){this.task=task;this.index=index;this.round=round;}
    }
    final List<Task> tasks;
    ChallengeRoutine(List<Task> tasks){if(tasks.isEmpty()||tasks.size()>LIMIT)throw new IllegalArgumentException("Choose 1–8 tasks");this.tasks=List.copyOf(tasks);}
    static ChallengeRoutine legacy(String type,int rounds){return new ChallengeRoutine(List.of(new Task(TYPES.contains(type)?type:"Math",Math.max(1,Math.min(6,rounds)))));}
    int units(){int total=0;for(Task task:tasks)total+=task.units();return total;}
    Position at(int completed){if(completed<0||completed>=units())return null;int offset=0;for(int n=0;n<tasks.size();n++){Task task=tasks.get(n);if(completed<offset+task.units())return new Position(task,n,completed-offset);offset+=task.units();}throw new AssertionError();}
    /** A stale/duplicate task callback must not skip the next task or round. */
    int advance(int completed,int expected){return completed==expected&&at(completed)!=null?completed+1:completed;}
    ChallengeRoutine replace(int index,Task task){List<Task> copy=new ArrayList<>(tasks);copy.set(index,task);return new ChallengeRoutine(copy);}
    ChallengeRoutine add(Task task){List<Task> copy=new ArrayList<>(tasks);copy.add(task);return new ChallengeRoutine(copy);}
    ChallengeRoutine move(int index,int destination){List<Task> copy=new ArrayList<>(tasks);Task task=copy.remove(index);copy.add(destination,task);return new ChallengeRoutine(copy);}
    ChallengeRoutine remove(int index){List<Task> copy=new ArrayList<>(tasks);copy.remove(index);return new ChallengeRoutine(copy);}
    String summary(){return tasks.size()==1?tasks.get(0).type+" × "+tasks.get(0).rounds:tasks.size()+" tasks";}
}
