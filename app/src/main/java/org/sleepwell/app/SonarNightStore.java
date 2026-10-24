package org.sleepwell.app;
import android.content.Context;
/** All methods are called only by SonarNight's one ordered storage worker. */
final class SonarNightStore implements SonarNight.Sink {
 private final Context app;private final TrackingService.StoreFactory factory;private final SonarNight.Clock clock;private Store store;private SonarNight.Session session;private boolean clockChanged;
 SonarNightStore(Context app,TrackingService.StoreFactory factory,SonarNight.Clock clock){this.app=app.getApplicationContext();this.factory=factory;this.clock=clock;}
 public SonarNight.Session begin(SonarConfig config,String capture)throws Exception{store=factory.open(app);long beforeWall=clock.wall(),beforeElapsed=clock.millis();long id=store.begin(SensorProvenance.SONAR,SensorProvenance.sonar(config,capture));long wall=store.rows("SELECT start FROM sessions WHERE id=?",Long.toString(id)).get(0).getLong("start");long elapsed=Math.addExact(beforeElapsed,wall-beforeWall);if(elapsed<0||Math.abs((clock.millis()-beforeElapsed)-(clock.wall()-beforeWall))>2000){store.end(id,true,0,wall);throw new IllegalStateException("The clock changed while starting. Start contactless tracking again.");}return session=new SonarNight.Session(id,wall,elapsed);}
 private void checkClock(){if(!clockChanged&&session!=null&&Math.abs(clock.wall()-(session.wall+clock.millis()-session.elapsed))>2000){store.clockChanged(session.id);clockChanged=true;}}
 public void checkpoint(long id,long from,long to,Double movement,String quality,int samples,long active){checkClock();store.sonarCheckpoint(id,from,to,movement,quality,samples,active);}
 public void pause(long id,long at){checkClock();store.pause(id,at);}
 public void resume(long id,long at){checkClock();store.resume(id,at);}
 public void end(long id,long at,long active,boolean interrupted){checkClock();store.end(id,interrupted,active,at);}
 public void close(){if(store!=null){Store owned=store;store=null;owned.close();}}
}
