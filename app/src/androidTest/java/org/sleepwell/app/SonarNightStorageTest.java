package org.sleepwell.app;

import android.content.Context;
import android.os.SystemClock;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONObject;
import org.junit.Test;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.Assert.*;

/** Isolated SQLite and generated echo PCM. Never creates a Service, AudioTrack, AudioRecord or sensor. */
public final class SonarNightStorageTest {
 final Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
 /** Signal time advances with generated samples, never with emulator/JIT scheduling latency. */
 static final class Clock implements SonarNight.Clock {
  final long baseElapsed=SystemClock.elapsedRealtime(),baseWall=System.currentTimeMillis();final AtomicLong now=new AtomicLong(baseElapsed);boolean admitted;
  public long millis(){return now.get();}
  public long wall(){return Math.addExact(baseWall,millis()-baseElapsed);}
  public void idle()throws InterruptedException{Thread.sleep(1);advance(10);}
  void advance(long milliseconds){if(milliseconds<0)throw new IllegalArgumentException("Synthetic time cannot move backwards");now.addAndGet(milliseconds);}
  synchronized void admitted(SonarNight.Session session){if(admitted||session.elapsed<now.get())throw new IllegalStateException("Invalid fixture admission anchor");if(session.wall!=Math.addExact(baseWall,session.elapsed-baseElapsed))throw new IllegalStateException("Real Store admission lost its exact paired wall time");now.set(session.elapsed);admitted=true;}
 }
 /** The unmodified real adapter creates the real SQLite row; only its returned test-clock pair is admitted. */
 static final class PairedStore implements SonarNight.Sink {
  final SonarNightStore actual;final Clock clock;PairedStore(Context context,TrackingService.StoreFactory factory,Clock clock){this.clock=clock;actual=new SonarNightStore(context,factory,clock);}
  public SonarNight.Session begin(SonarConfig config,String capture)throws Exception{SonarNight.Session session=actual.begin(config,capture);clock.admitted(session);return session;}
  public void checkpoint(long id,long from,long to,Double value,String quality,int samples,long active)throws Exception{actual.checkpoint(id,from,to,value,quality,samples,active);}
  public void pause(long id,long at){actual.pause(id,at);}public void resume(long id,long at){actual.resume(id,at);}public void end(long id,long at,long active,boolean interrupted){actual.end(id,at,active,interrupted);}public void close(){actual.close();}
 }
 static final class Echo implements SonarPump.Io {
  final Clock clock;long boot,written,read,origin=-1;volatile boolean closed,silence;long startupCpuDelay;boolean delayed;final AtomicReference<SonarFrame.Quality> unavailable=new AtomicReference<>();
  Echo(Clock clock){this.clock=clock;}
  public String captureSource(){return "UNPROCESSED";}
  public void startMuted(){boot=clock.millis();}
  public void check()throws Exception{if(unavailable.get()!=null){Thread.sleep(5);throw new SonarPump.Failure(unavailable.get(),"Injected charging loss");}}
  public boolean routed(){return true;}public void confirmRoutes(){}
  public SonarPump.Timing timing(){return new SonarPump.Timing(0,boot*1000000L);}
  public void gain(float gain){assertTrue(gain<=.10f);origin=written;}
  public int write(short[] p,int at,int count){int n=(int)Math.min(Math.min(480,count),Math.max(0,(clock.millis()-boot)*48-written));written+=n;return n;}
  public int read(short[] p,int at,int count)throws Exception{int n=(int)Math.min(Math.min(480,count),written-read);if(!delayed&&startupCpuDelay>0&&origin>=0&&read+n>=origin+1920){delayed=true;long before=clock.millis();Thread.sleep(startupCpuDelay);if(before!=clock.millis())throw new IllegalStateException("Host CPU stall aged synthetic PCM timestamps");}for(int k=0;k<n;k++){long t=read+k-origin;int frame=(int)(t/1920);double delay=240+5*Math.sin(frame*.3);p[at+k]=silence||origin<0?0:(short)Math.round(32767*(.5*SonarChirp.sample(t-8)+.16*SonarChirp.sample(t-delay)));}read+=n;return n;}
  public void close(){closed=true;}
 }
 static void waitFor(java.util.function.BooleanSupplier predicate)throws Exception{long until=SystemClock.elapsedRealtime()+20000;while(!predicate.getAsBoolean()&&SystemClock.elapsedRealtime()<until)Thread.sleep(20);assertTrue("Actual asynchronous Night boundary",predicate.getAsBoolean());}
 final class Fixture implements AutoCloseable {
  final String database="sonar-night-"+UUID.randomUUID()+".db";final Clock clock=new Clock();final List<Echo> audio=new CopyOnWriteArrayList<>();final SonarNight night;
  Fixture(){night=new SonarNight(new SonarConfig(.05f),(config,cancel)->{Echo port=new Echo(clock);if(audio.isEmpty())port.startupCpuDelay=1500;audio.add(port);return port;},new PairedStore(c,ctx->new Store(ctx,database),clock),clock,Runnable::run,state->{});}
  public void close()throws Exception{night.stop(true);assertTrue("Ordered storage closed",night.awaitClosed(10000));for(Echo port:audio)assertTrue("Fake audio cleanup confirmed",port.closed);assertTrue(c.deleteDatabase(database));}
  Store open(){return new Store(c,database);}
  void measured()throws Exception{
   long until=SystemClock.elapsedRealtime()+20000;
   while(SystemClock.elapsedRealtime()<until){SonarNight.State state=night.state();if(state.readings.stream().anyMatch(x->x.movement!=null))return;if(state.phase.equals("CLOSED")||state.phase.equals("BLOCKED"))fail(diagnostic(state));Thread.sleep(20);}
   fail(diagnostic(night.state()));
  }
  String diagnostic(SonarNight.State state){StringBuilder details=new StringBuilder("No measured interval: phase=").append(state.phase).append("; message=").append(state.message).append("; error=").append(state.error).append("; active=").append(state.active).append("; readings=").append(state.readings.size());for(SonarFrame frame:state.readings.subList(Math.max(0,state.readings.size()-8),state.readings.size()))details.append("; ").append(frame.quality).append('@').append(frame.from).append('-').append(frame.to);for(Echo port:audio)details.append("; capture written=").append(port.written).append(" read=").append(port.read).append(" closed=").append(port.closed).append(" virtualNow=").append(clock.millis()).append(" boot=").append(port.boot).append(" injectedCpuDelay=").append(port.startupCpuDelay);return details.toString();}
 }
 @Test public void actualPcmToSqliteNightPausesResumesAndRetainsSignalGaps()throws Exception{
  try(Fixture f=new Fixture()){
   f.night.start();f.measured();
   f.night.pause();waitFor(()->f.night.state().phase.equals("PAUSED"));long active=f.night.state().active,id=f.night.state().id;
   try(Store store=f.open()){JSONObject row=store.rows("SELECT * FROM sessions WHERE id=?",""+id).get(0);assertEquals(SensorProvenance.SONAR,row.getString("sensor"));assertEquals("UNPROCESSED",new JSONObject(row.getString("sensor_config")).getString("capture"));assertEquals(.05,new JSONObject(row.getString("sensor_config")).getDouble("gain"),.000001);assertTrue(row.isNull("end"));assertEquals(1,store.rows("SELECT * FROM tracking_pauses WHERE end IS NULL").size());assertTrue(store.rows("SELECT * FROM clips").isEmpty());assertTrue(store.rows("SELECT * FROM sound_events").isEmpty());}
   f.clock.advance(250);assertEquals(active,f.night.state().active);
   assertTrue(f.night.resume());waitFor(()->f.audio.size()==2&&f.night.state().readings.stream().anyMatch(x->x.from>active+250&&x.movement!=null));
   f.audio.get(1).silence=true;waitFor(()->f.night.state().readings.get(f.night.state().readings.size()-1).quality==SonarFrame.Quality.NO_ECHO);
   f.night.stop(false);assertTrue(f.night.awaitClosed(10000));assertEquals("",f.night.state().error);
   try(Store store=f.open()){JSONObject session=store.rows("SELECT * FROM sessions WHERE id=?",""+id).get(0);assertEquals(0,session.getInt("interrupted"));assertTrue(session.getLong("end")>session.getLong("start"));assertEquals(f.night.state().active,session.getLong("active_ms"));assertEquals(1,store.rows("SELECT * FROM tracking_pauses WHERE end IS NOT NULL").size());boolean measured=false,missing=false;for(JSONObject row:store.rows("SELECT * FROM epochs ORDER BY time")){assertTrue(row.getLong("time")-row.getLong("start")<=5000);String q=row.getString("signal_quality");if(q.equals("VALID")){measured=true;assertFalse(row.isNull("movement"));assertTrue(row.getInt("samples")>0);}else{missing=true;assertTrue(row.isNull("movement"));}assertTrue(row.isNull("noise"));assertTrue(row.isNull("light"));}assertTrue(measured);assertTrue(missing);Backup.validate(store.backup());}
  }
 }
 @Test public void chargingFailurePersistsInterruptedNullTailAndClosesPorts()throws Exception{
  try(Fixture f=new Fixture()){f.night.start();f.measured();f.audio.get(0).unavailable.set(SonarFrame.Quality.CHARGING_LOST);assertTrue(f.night.awaitClosed(10000));assertTrue(f.night.state().error.contains("charging loss"));try(Store store=f.open()){JSONObject row=store.rows("SELECT * FROM sessions").get(0);assertEquals(1,row.getInt("interrupted"));List<JSONObject> epochs=store.rows("SELECT * FROM epochs ORDER BY time");JSONObject tail=epochs.get(epochs.size()-1);assertEquals("CHARGING_LOST",tail.getString("signal_quality"));assertTrue(tail.isNull("movement"));assertEquals(row.getLong("end"),tail.getLong("time"));Backup.validate(store.backup());}}
 }
}
