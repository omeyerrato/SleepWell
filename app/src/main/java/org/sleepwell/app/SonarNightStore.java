package org.sleepwell.app;
import android.content.Context;
/** All methods are called only by SonarNight's one ordered storage worker. */
final class SonarNightStore implements SonarNight.Sink {
 private final Context app;private final TrackingService.StoreFactory factory;private final SonarNight.Clock clock;private final AwakeRuntime awake;private Store store;private SonarNight.Session session;private boolean clockChanged,backupProduction;
 SonarNightStore(Context app,TrackingService.StoreFactory factory,SonarNight.Clock clock){this(app,factory,clock,null);}
 SonarNightStore(Context app,TrackingService.StoreFactory factory,SonarNight.Clock clock,AwakeRuntime awake){this.app=app.getApplicationContext();this.factory=factory;this.clock=clock;this.awake=awake;}
 public SonarNight.Session begin(SonarConfig config,String capture)throws Exception{return begin(config,capture,false);}
 public SonarNight.Session begin(SonarConfig config,String capture,boolean analysis)throws Exception{store=factory.open(app);backupProduction=factory==TrackingService.PRODUCTION_STORE&&Records.factory==Records.PRODUCTION&&"sleepwell.db".equals(store.getDatabaseName());long beforeWall=clock.wall(),beforeElapsed=clock.millis();long id=store.begin(SensorProvenance.SONAR,SensorProvenance.sonar(config,capture),SoundAnalysisProvenance.encode(SoundAnalysisStatus.initial(analysis)));long wall=store.rows("SELECT start FROM sessions WHERE id=?",Long.toString(id)).get(0).getLong("start");long elapsed=Math.addExact(beforeElapsed,wall-beforeWall);if(elapsed<0||Math.abs((clock.millis()-beforeElapsed)-(clock.wall()-beforeWall))>2000){store.end(id,true,0,wall,SoundAnalysisProvenance.encode(analysis?new SoundAnalysisStatus(true,"error",0,"capture"):SoundAnalysisStatus.initial(false)));throw new IllegalStateException("The clock changed while starting. Start contactless tracking again.");}if(awake!=null)awake.begin(id,wall,elapsed,false);return session=new SonarNight.Session(id,wall,elapsed);}
 private void checkClock(){if(!clockChanged&&session!=null&&Math.abs(clock.wall()-(session.wall+clock.millis()-session.elapsed))>2000){store.clockChanged(session.id);clockChanged=true;}}
 public void checkpoint(long id,long from,long to,Double movement,String quality,int samples,long active){checkClock();store.sonarCheckpoint(id,from,to,movement,quality,samples,active);}
 public void pause(long id,long at){if(awake!=null)awake.pause();checkClock();store.pause(id,at);}
 public void resume(long id,long at){if(awake!=null)awake.resume();checkClock();store.resume(id,at);}
 public void end(long id,long at,long active,boolean interrupted){if(awake!=null)awake.finishAndWait(at,interrupted);checkClock();store.end(id,interrupted,active,at);backupEnded(id,at,interrupted);}
 public void soundEvent(long id,long at,long duration,String category,double score){checkClock();store.soundEvent(id,at,duration,category,score,null);}
 public void soundStatus(long id,SoundAnalysisStatus analysis){checkClock();store.soundAnalysisCheckpoint(id,SoundAnalysisProvenance.encode(analysis));}
 public void end(long id,long at,long active,boolean interrupted,SoundAnalysisStatus analysis){if(awake!=null)awake.finishAndWait(at,interrupted);checkClock();store.end(id,interrupted,active,at,SoundAnalysisProvenance.encode(analysis));backupEnded(id,at,interrupted);}
 private void backupEnded(long id,long at,boolean interrupted){if(!interrupted&&session!=null&&session.id==id)AutoCloudBackups.ended(app,id,session.wall,at,backupProduction);}
 public void close(){if(store!=null){Store owned=store;store=null;owned.close();}}
}
