package org.sleepwell.app;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class RecordSensorTest {
 private RecordEntry record(String sensor){return new RecordEntry(1,1000,11000,10000L,"tracked","local","UTC",0,0,new RecordDraft(1000,11000),false,0,List.of(),sensor,"immutable-config");}
 @Test public void sonarProjectionRetainsSnapshotAcrossEdits(){RecordEntry raw=record("sonar_fmcw_v1");RecordDraft edit=new RecordDraft(2000,10000);RecordEntry edited=raw.draft(edit);assertTrue(edited.sonar());assertEquals(raw.sensor,edited.sensor);assertEquals(raw.sensorConfig,edited.sensorConfig);assertEquals("Contactless recording · edited",edited.provenance());assertEquals(8000,edited.duration());}
 @Test public void motionIsNeverMistakenForContactless(){RecordEntry raw=record("accelerometer");assertFalse(raw.sonar());assertEquals("Phone recording",raw.provenance());}
 @Test public void legacyConstructorDoesNotInventASensorMeasurement(){RecordEntry legacy=new RecordEntry(1,1000,11000,null,"legacy","legacy","",0,0,new RecordDraft(1000,11000),false,0,List.of());assertEquals("unspecified",legacy.sensor);assertEquals("",legacy.sensorConfig);assertFalse(legacy.sonar());}
 @Test public void manualConstructorHasNoSensor(){RecordEntry manual=new RecordEntry(1,1000,11000,null,"manual","local","UTC",0,0,new RecordDraft(1000,11000),false,0,List.of());assertEquals("none",manual.sensor);assertFalse(manual.sonar());}
}
