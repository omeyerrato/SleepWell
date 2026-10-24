package org.sleepwell.app;
import org.junit.Test;
import static org.junit.Assert.*;

public class BackupRuntimeTest {
    @Test public void onlyPotentialAlarmDeliveriesNeedAnEarlyDatabaseFence(){assertTrue(BackupRuntime.alarmDelivery(Scheduler.FIRE,4));assertTrue(BackupRuntime.alarmDelivery(Scheduler.SNOOZE,4));assertTrue(BackupRuntime.alarmDelivery(Scheduler.CHECK,4));assertTrue(BackupRuntime.alarmDelivery(Scheduler.TEST,-1));assertFalse(BackupRuntime.alarmDelivery(Scheduler.FIRE,0));assertFalse(BackupRuntime.alarmDelivery(Scheduler.TEST,4));assertFalse(BackupRuntime.alarmDelivery(Scheduler.BED,4));assertFalse(BackupRuntime.alarmDelivery(Scheduler.BACKUP,4));assertFalse(BackupRuntime.alarmDelivery(null,0));}
    @Test public void malformedNapNonceDoesNotAdmitADatabaseDelivery(){assertFalse(BackupRuntime.nonce(null));assertFalse(BackupRuntime.nonce(""));assertFalse(BackupRuntime.nonce("1-1-1-1-1"));assertFalse(BackupRuntime.nonce("not-a-nonce"));assertTrue(BackupRuntime.nonce("9a9379b7-3caf-49a2-b25b-d2d9745ba173"));}
    @Test public void changedAdmissionAndSaturatedGenerationFailBeforeAnyContextAccess(){long previous=BackupRuntime.generation();BackupRuntime.admitted();assertNotEquals(previous,BackupRuntime.generation());assertThrows(IllegalStateException.class,()->BackupRuntime.requireIdle(null,previous));assertThrows(IllegalStateException.class,()->BackupRuntime.requireIdle(null,Long.MAX_VALUE));}
}
