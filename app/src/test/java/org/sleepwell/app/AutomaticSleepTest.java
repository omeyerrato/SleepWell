package org.sleepwell.app;

import org.junit.Test;
import static org.junit.Assert.*;

public class AutomaticSleepTest {
    private AutomaticSleep still(){return new AutomaticSleep(AutomaticSleep.Mode.STILLNESS,0,0);}
    private void feed(AutomaticSleep model,long from,long to,double motion,boolean interactive,long spacing){for(long at=from;at<=to;at+=spacing)model.sample(at,motion,interactive);}
    @Test public void fifteenMinutesOfContinuousScreenOffLowMotionStarts(){AutomaticSleep m=still();feed(m,0,AutomaticSleep.QUIET_REQUIRED,.01,false,200);assertEquals(AutomaticSleep.State.START,m.state());}
    @Test public void timerAloneNeverInventsMotionEvidence(){AutomaticSleep m=still();assertEquals(AutomaticSleep.State.COLLECTING,m.tick(AutomaticSleep.QUIET_REQUIRED,false));assertEquals(0,m.quietMillis(AutomaticSleep.QUIET_REQUIRED));}
    @Test public void screenUseResetsTheEntireQuietPeriod(){AutomaticSleep m=still();feed(m,0,800000,.01,false,200);m.tick(800100,true);feed(m,800200,1500000,.01,false,200);assertNotEquals(AutomaticSleep.State.START,m.state());assertEquals(699800,m.quietMillis(1500000));}
    @Test public void movementRestartsThePeriod(){AutomaticSleep m=still();feed(m,0,800000,.01,false,200);m.sample(800200,.5,false);assertEquals(0,m.quietMillis(800200));feed(m,800400,1600000,.01,false,200);assertEquals(AutomaticSleep.State.QUIET,m.state());}
    @Test public void missingSensorDataCannotCountAsStillness(){AutomaticSleep m=still();feed(m,0,800000,.01,false,200);assertEquals(AutomaticSleep.State.COLLECTING,m.tick(803000,false));feed(m,804000,1000000,.01,false,200);assertNotEquals(AutomaticSleep.State.START,m.state());}
    @Test public void sparseEventsFailCoverageEvenWithoutLargeGaps(){AutomaticSleep m=still();feed(m,0,AutomaticSleep.QUIET_REQUIRED,.01,false,1000);assertEquals(AutomaticSleep.State.QUIET,m.state());}
    @Test public void invalidEnergyNeverCountsAsQuiet(){for(double energy:new double[]{Double.NaN,Double.POSITIVE_INFINITY,-1}){AutomaticSleep m=still();feed(m,0,800000,.01,false,200);m.sample(800200,energy,false);assertEquals(0,m.quietMillis(800200));}}
    @Test public void fourHourExpiryWinsEvenWithNoEvents(){AutomaticSleep m=still();assertEquals(AutomaticSleep.State.EXPIRED,m.tick(AutomaticSleep.MAX_WAIT,false));feed(m,AutomaticSleep.MAX_WAIT,AutomaticSleep.MAX_WAIT+1000,.01,false,200);assertEquals(AutomaticSleep.State.EXPIRED,m.state());}
    @Test public void cancelIsTerminalAndCannotStartLater(){AutomaticSleep m=still();m.cancel();feed(m,0,AutomaticSleep.QUIET_REQUIRED,.01,false,200);assertEquals(AutomaticSleep.State.CANCELLED,m.tick(AutomaticSleep.QUIET_REQUIRED,false));}
    @Test public void timeTriggerDoesNotPretendToInferSleep(){AutomaticSleep m=new AutomaticSleep(AutomaticSleep.Mode.BEDTIME,1000,61000);assertEquals(AutomaticSleep.State.WAITING,m.tick(60000,true));assertEquals(AutomaticSleep.State.START,m.tick(61000,true));}
    @Test public void elapsedRealtimeTriggerDoesNotConsultChangingWallClock(){AutomaticSleep m=new AutomaticSleep(AutomaticSleep.Mode.SMART_WINDOW,10,9010);assertEquals(AutomaticSleep.State.WAITING,m.tick(9009,false));assertEquals(AutomaticSleep.State.START,m.tick(9010,false));}
    @Test public void disallowsManualAndOutOfBoundSchedules(){for(AutomaticSleep.Mode mode:new AutomaticSleep.Mode[]{null,AutomaticSleep.Mode.MANUAL})try{new AutomaticSleep(mode,0,0);fail();}catch(IllegalArgumentException expected){}try{new AutomaticSleep(AutomaticSleep.Mode.BEDTIME,0,AutomaticSleep.MAX_WAIT+1);fail();}catch(IllegalArgumentException expected){}}
    @Test public void reversedSensorTimeIsRejected(){AutomaticSleep m=still();m.sample(500,.01,false);try{m.sample(400,.01,false);fail();}catch(IllegalArgumentException expected){}}
}
