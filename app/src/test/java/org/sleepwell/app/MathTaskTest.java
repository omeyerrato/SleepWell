package org.sleepwell.app;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class MathTaskTest {
    @Test public void choicesHaveExactlyOneCorrectAnswer(){Random r=new Random(42);for(int n=0;n<1000;n++){MathTask t=MathTask.create(r);assertEquals(4,new HashSet<>(t.options).size());assertEquals(1,t.options.stream().filter(t::accepts).count());for(int v:t.options)assertTrue(v>=0);}}
    @Test public void persistedTaskPreservesQuestionAndOrder(){MathTask t=MathTask.create(new Random(19));MathTask restored=MathTask.decode(t.encode());assertEquals(t.encode(),restored.encode());assertTrue(restored.accepts(t.left+t.right));assertFalse(restored.accepts(t.left+t.right+1));}
}
