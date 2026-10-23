# Best practices and security alignment update: selected audio documents

Priority: medium. Scope: URI access, bounded provider input, Activity lifetime and stale-result isolation. No nested Intent is forwarded; only the selected content URI and offered read/persistable flags are consumed. No new production component or broad storage permission is added. PendingIntents and alarm behavior are unchanged.

Validation reads a disposable cache snapshot, with an actual 100 MiB cap, a 20-second visible-screen deadline, cancellation, at most two validation workers and two best-effort close workers. An uncooperative provider may keep a worker/temp file alive until it returns or the process dies; late workers never write preferences. Validation of a snapshot does not prevent the provider from changing its original later. Metadata retrieval is on the local snapshot, off the UI thread. Abandoned private snapshots are pruned at the next import after process restart. A process-retained token survives Activity recreation; process death reports interruption without changing the existing mix. Every explicit mix save increments a revision; an obsolete import cannot replace a newer edit.

Persistent READ access is taken only after audio validation and revision checks. WRITE access is never taken. Previous app-wide grants are retained because alarm tones may share them. Remove from mix changes the profile only and never deletes the source. Missing/moved/provider-denied documents can still fail later during playback and need reselection. See the official [Storage Access Framework guide](https://developer.android.com/training/data-storage/shared/documents-files).

The read-only DocumentsProvider below exists only in the test APK, requires system MANAGE_DOCUMENTS, grants access only to selected synthetic documents, exposes no paths or personal files, and advertises roots only on the disposable emulator model/hardware. It is absent from debug/release app APKs.

Verification: seven pure byte-bound/cancellation/error tests pass; full local unit suite 105 passes. Debug/test/release APK builds and lint pass (0 errors, 68 existing warnings). The first CI run passed settings/root/native-sound regressions but stopped on a stale DocumentsUI shortcut selector before imports. Drawer selection is now uniquely scoped; playback snapshots are marshalled to main and CI metadata records import playback. Actual picker/process-restart/native playback evidence is pending a fresh CI run and independent review; no physical phone or alarm tests.

Files changed and exact implementation diffs follow. No dependencies changed.

Timeout while paused, providers that do not offer a persistable grant, failed preference disk writes and interrupted-process import recovery are source-handled limitations, not passing native test claims. Preferences use the existing atomic apply mechanism; normal process restart will be tested, disk-failure reporting is not provided.

## app/src/main/java/org/sleepwell/app/AudioCopy.java

```diff
--- a/app/src/main/java/org/sleepwell/app/AudioCopy.java
+++ b/app/src/main/java/org/sleepwell/app/AudioCopy.java
@@ -0,0 +1,21 @@
+package org.sleepwell.app;
+
+import java.io.*;
+
+/** Bounds actual bytes, including providers that report no size or the wrong size. */
+final class AudioCopy {
+    interface Check { void run() throws IOException; }
+    interface Progress { void bytes(long count); }
+    static long copy(InputStream in,OutputStream out,long limit,Check check,Progress progress)throws IOException{
+        if(limit<1)throw new IllegalArgumentException("Positive byte limit required");
+        byte[] buffer=new byte[32768];long count=0;int empty=0;
+        while(true){
+            check.run();int n=in.read(buffer,0,(int)Math.min(buffer.length,limit-count+1));check.run();
+            if(n<0)break;
+            if(n==0){if(++empty>16)throw new IOException("The document stopped responding");continue;}
+            empty=0;if(n>limit-count)throw new IOException("Audio exceeds the import limit");
+            out.write(buffer,0,n);count+=n;progress.bytes(count);
+        }
+        if(count==0)throw new IOException("Empty audio document");return count;
+    }
+}
```

## app/src/main/java/org/sleepwell/app/SoundImports.java

```diff
--- a/app/src/main/java/org/sleepwell/app/SoundImports.java
+++ b/app/src/main/java/org/sleepwell/app/SoundImports.java
@@ -0,0 +1,85 @@
+package org.sleepwell.app;
+
+import android.content.*;
+import android.content.res.AssetFileDescriptor;
+import android.database.Cursor;
+import android.media.MediaMetadataRetriever;
+import android.net.Uri;
+import android.os.*;
+import android.provider.OpenableColumns;
+import java.io.*;
+import java.util.*;
+import java.util.concurrent.*;
+
+/** Process-retained validation only. No Activity references, playback, or worker preference writes. */
+final class SoundImports {
+    static final long MAX_BYTES=100*1024*1024L,TIMEOUT_MS=20000;
+    private static final Map<String,Job> jobs=new ConcurrentHashMap<>();
+    private static boolean cacheCleaned;
+    private static synchronized void cleanAbandoned(Context app){
+        if(cacheCleaned)return;cacheCleaned=true;
+        // A killed process cannot run finally. Only this importer's disposable private snapshots qualify.
+        File[] files=app.getCacheDir().listFiles(f->f.isFile()&&f.getName().startsWith("sleepwell-audio-")&&f.getName().endsWith(".tmp"));
+        if(files!=null)for(File file:files)if(!file.delete())file.deleteOnExit();
+    }
+    private static final ExecutorService workers=new ThreadPoolExecutor(0,2,30,TimeUnit.SECONDS,new SynchronousQueue<>(),r->{Thread t=new Thread(r,"audio-import");t.setDaemon(true);return t;});
+    private static final ExecutorService closers=new ThreadPoolExecutor(0,2,30,TimeUnit.SECONDS,new SynchronousQueue<>(),r->{Thread t=new Thread(r,"audio-import-close");t.setDaemon(true);return t;});
+    enum State { READING,READY,FAILED,CANCELLED,COMPLETED }
+    static final class Job {
+        final String id=UUID.randomUUID().toString();final Context app;final Uri uri;final int flags;
+        final SharedPreferences preferences;final long revision,started=SystemClock.elapsedRealtime();final SoundMix previous;
+        final CancellationSignal signal=new CancellationSignal();final CountDownLatch exited=new CountDownLatch(1);
+        volatile State state=State.READING;volatile long bytes;volatile String title="My audio",error="";
+        volatile AssetFileDescriptor descriptor;volatile boolean retired;
+        Job(Context c,Uri u,int f){app=c.getApplicationContext();uri=u;flags=f;preferences=SoundPreferences.factory.open(app);synchronized(SoundPreferences.class){revision=SoundPreferences.revision(preferences);previous=SoundPreferences.load(preferences).copy();}}
+        synchronized void fail(String message){if(state==State.READING||state==State.READY){error=message;state=State.FAILED;}}
+        void check()throws IOException{if(state!=State.READING||Thread.currentThread().isInterrupted())throw new IOException("Cancelled");if(SystemClock.elapsedRealtime()-started>TIMEOUT_MS)throw new IOException("Timed out");}
+        void read(){File temporary=null;try{
+            cleanAbandoned(app);
+            check();try(Cursor cursor=app.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE},null,signal)){
+                if(cursor!=null&&cursor.moveToFirst()){
+                    int size=cursor.getColumnIndex(OpenableColumns.SIZE),name=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
+                    if(size>=0&&!cursor.isNull(size)&&(cursor.getLong(size)<=0||cursor.getLong(size)>MAX_BYTES))throw new IOException("Invalid audio size");
+                    if(name>=0&&!cursor.isNull(name)){String clean=cursor.getString(name).replaceAll("[\\p{Cntrl}\\p{Cf}]", " ").trim();if(!clean.isEmpty())title=clean.substring(0,Math.min(clean.length(),80));}
+                }
+            }
+            check();temporary=File.createTempFile("sleepwell-audio-",".tmp",app.getCacheDir());
+            descriptor=app.getContentResolver().openAssetFileDescriptor(uri,"r",signal);if(descriptor==null)throw new IOException("No audio document");
+            try(AssetFileDescriptor fd=descriptor;InputStream in=fd.createInputStream();OutputStream out=new FileOutputStream(temporary)){AudioCopy.copy(in,out,MAX_BYTES,this::check,n->bytes=n);}finally{descriptor=null;}
+            check();try(MediaMetadataRetriever metadata=new MediaMetadataRetriever();FileInputStream input=new FileInputStream(temporary)){
+                metadata.setDataSource(input.getFD());String duration=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
+                if(duration==null||Long.parseLong(duration)<=0||!"yes".equals(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)))throw new IOException("No audio track");
+            }
+            check();synchronized(this){if(state==State.READING)state=State.READY;}
+        }catch(Exception e){fail(SystemClock.elapsedRealtime()-started>=TIMEOUT_MS?"The audio provider took too long. Choose the file again.":"That file could not be read as audio. Choose a readable audio file up to 100 MiB.");}
+        finally{if(temporary!=null&&!temporary.delete())temporary.deleteOnExit();exited.countDown();if(retired)jobs.remove(id,this);}}
+        void cancel(){synchronized(this){if(state!=State.COMPLETED)state=State.CANCELLED;}closeOffMain();}
+        void closeOffMain(){try{closers.execute(()->{signal.cancel();AssetFileDescriptor fd=descriptor;if(fd!=null)try{fd.close();}catch(IOException ignored){}});}catch(RejectedExecutionException ignored){/* A stuck provider never gets an unbounded number of cancellation threads. */}}
+        void poll(){if(state==State.READING&&SystemClock.elapsedRealtime()-started>=TIMEOUT_MS){fail("The audio provider took too long. Choose the file again.");closeOffMain();}}
+        /** Main thread only: no import can commit after another mix edit or after cancellation. */
+        synchronized boolean commit(){
+            if(state!=State.READY)return false;
+            synchronized(SoundPreferences.class){
+                if(SoundPreferences.revision(preferences)!=revision){fail("Your mix changed while the file was checked. Choose the file again to use it.");return false;}
+                try{
+                    int required=Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
+                    if((flags&required)!=required)throw new SecurityException("Persistent read access not offered");
+                    app.getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
+                    SoundMix next=previous.copy();next.uri=uri.toString();next.title=title;next.primary="My audio";
+                    if(!SoundPreferences.saveIfRevision(preferences,revision,next))throw new IllegalStateException("Mix changed");
+                    preferences.edit().putString("soundError","").apply();state=State.COMPLETED;return true;
+                }catch(RuntimeException e){fail("Access to that file could not be kept. Choose it again from a document provider.");return false;}
+            }
+        }
+        void retire(){retired=true;if(exited.getCount()==0)jobs.remove(id,this);}
+    }
+    static Job start(Context c,Uri uri,int flags){
+        if(!"content".equals(uri.getScheme())||uri.getAuthority()==null)throw new IllegalArgumentException("Choose an audio document");
+        Job job=new Job(c,uri,flags);jobs.put(job.id,job);
+        try{workers.execute(job::read);}catch(RejectedExecutionException e){job.fail("Another document is still closing. Try again shortly.");job.exited.countDown();}
+        return job;
+    }
+    static Job find(String id){return id==null?null:jobs.get(id);}
+    static boolean pending(){return !jobs.isEmpty();}
+    static void awaitIdle(long timeout)throws InterruptedException,TimeoutException{long end=SystemClock.elapsedRealtime()+timeout;for(Job job:jobs.values()){long left=end-SystemClock.elapsedRealtime();if(left<=0||!job.exited.await(left,TimeUnit.MILLISECONDS))throw new TimeoutException("Audio provider still reading; retain isolated preferences");if(!job.retired)throw new TimeoutException("Audio import still owned by a screen");}}
+}
```

## app/src/main/java/org/sleepwell/app/SoundPreferences.java

```diff
--- a/app/src/main/java/org/sleepwell/app/SoundPreferences.java
+++ b/app/src/main/java/org/sleepwell/app/SoundPreferences.java
@@ -14,10 +14,12 @@
         m.primaryGain=p.getInt("mixPrimaryGain",70);m.brownGain=p.getInt("mixBrownGain",20);m.timerMinutes=p.getInt("mixMinutes",30);m.fadeMinutes=p.getInt("mixFade",5);m.withTracking=p.getBoolean("mixWithTracking",false);m.validate();return m;
     }
     static void save(Context c,SoundMix mix){save(factory.open(c),mix);}
-    static void save(SharedPreferences p,SoundMix m){
+    static synchronized long revision(SharedPreferences p){return p.getLong("mixRevision",0);}
+    static synchronized boolean saveIfRevision(SharedPreferences p,long expected,SoundMix m){if(revision(p)!=expected)return false;save(p,m);return true;}
+    static synchronized void save(SharedPreferences p,SoundMix m){
         m.validate();
         // Keep legacy consumers readable, while the new engine uses the entire atomic profile.
-        p.edit().putInt("mixVersion",1).putString("mixPrimary",m.primary).putString("mixUri",m.uri).putString("mixTitle",m.title)
+        p.edit().putLong("mixRevision",revision(p)+1).putInt("mixVersion",1).putString("mixPrimary",m.primary).putString("mixUri",m.uri).putString("mixTitle",m.title)
             .putInt("mixPrimaryGain",m.primaryGain).putInt("mixBrownGain",m.brownGain).putInt("mixMinutes",m.timerMinutes).putInt("mixFade",m.fadeMinutes).putBoolean("mixWithTracking",m.withTracking)
             .putString("lullaby",m.withTracking?m.label():"Off").putInt("lullabyMinutes",m.timerMinutes).apply();
     }
```

## app/src/main/java/org/sleepwell/app/SleepSoundsActivity.java

```diff
--- a/app/src/main/java/org/sleepwell/app/SleepSoundsActivity.java
+++ b/app/src/main/java/org/sleepwell/app/SleepSoundsActivity.java
@@ -6,9 +6,7 @@
 import android.graphics.*;
 import android.graphics.drawable.GradientDrawable;
 import android.net.Uri;
-import android.media.MediaMetadataRetriever;
 import android.os.*;
-import android.provider.OpenableColumns;
 import android.view.*;
 import android.widget.*;
 import java.util.*;
@@ -17,26 +15,26 @@
 /** Board 07 / screen 19. Opening, selecting and importing are silent; only Play starts audio. */
 public final class SleepSoundsActivity extends Activity {
     private static final int INK=0xfff7efff,MUTED=0xffd0bce9,PANEL=0xff4b3e60,IMPORT=71;
-    private SoundMix mix;private String localError="";private boolean resumed,importing;
+    private SoundMix mix;private String localError="";private boolean resumed;private SoundImports.Job audioImport;
     private final Handler handler=new Handler(Looper.getMainLooper());private final ExecutorService reader=Executors.newSingleThreadExecutor();
     private final Map<String,Bitmap> artwork=new HashMap<>();private final Map<String,ImageView> tiles=new HashMap<>();
-    private ImageView hero;private LinearLayout playArea;private TextView time,status,alert;private ProgressBar progress;private String lastState="";
-    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);load();if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);render();reader.execute(()->{for(String name:SoundMix.LIBRARY){Bitmap bitmap=null;try(java.io.InputStream stream=getAssets().open("sound-art/"+asset(name)+".png")){BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;bitmap=BitmapFactory.decodeStream(stream,null,options);}catch(Exception ignored){}Bitmap result=bitmap;runOnUiThread(()->{if(!isDestroyed()&&result!=null){artwork.put(name,result);showArt();}});}});}
+    private ImageView hero;private LinearLayout playArea;private TextView time,status,alert,importStatus;private ProgressBar progress;private String lastState="";
+    @Override public void onCreate(Bundle saved){super.onCreate(saved);RecordUi.review(this);load();if(saved!=null&&saved.containsKey("audioImport")){audioImport=SoundImports.find(saved.getString("audioImport"));if(audioImport==null)localError="Audio import was interrupted. Choose the file again.";}if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::finish);render();reader.execute(()->{for(String name:new String[]{"Rain","Ocean","Fireplace","Wind","My audio"}){Bitmap bitmap=null;try(java.io.InputStream stream=getAssets().open("sound-art/"+asset(name)+".png")){BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;bitmap=BitmapFactory.decodeStream(stream,null,options);}catch(Exception ignored){}Bitmap result=bitmap;runOnUiThread(()->{if(!isDestroyed()&&result!=null){artwork.put(name,result);showArt();}});}});}
     private void load(){try{mix=SoundPreferences.load(this);}catch(RuntimeException e){mix=new SoundMix();localError="Your saved mix could not be read. Choose your sounds again to save a new mix.";}}
-    private static String asset(String name){return name.equals("Fireplace")?"fire":name.toLowerCase(Locale.ROOT);}
+    private static String asset(String name){return name.equals("My audio")?"imported":name.equals("Fireplace")?"fire":name.toLowerCase(Locale.ROOT);}
     private void render(){
         LinearLayout body=Design.darkPage(this);RecordUi.label(this,body);LinearLayout heading=Design.row(this);Design.Icon back=(Design.Icon)Design.iconButton(this,"back","Back",this::finish);back.tint(INK);heading.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout copy=Design.col(this);Design.text(copy,"Sleep sounds",25,600,INK);Design.space(copy,5);Design.text(copy,"Create a calming environment",14,400,MUTED);heading.addView(copy,new LinearLayout.LayoutParams(0,-2,1));body.addView(heading);Design.space(body,15);
         hero=new ImageView(this);hero.setScaleType(ImageView.ScaleType.CENTER_CROP);hero.setBackground(Design.shape(this,PANEL,20));hero.setClipToOutline(true);hero.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);body.addView(hero,new LinearLayout.LayoutParams(-1,dp(172)));Design.space(body,8);
         TextView soundTitle=Design.text(body,mix.label(),21,600,MUTED);soundTitle.setMaxLines(2);soundTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);Design.space(body,4);Design.text(body,description(),13,400,MUTED);Design.space(body,8);
-        LinearLayout controls=Design.row(this);playArea=Design.row(this);controls.addView(playArea,new LinearLayout.LayoutParams(dp(58),dp(58)));LinearLayout timeline=Design.col(this);progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(1000);progress.setProgressTintList(ColorStateList.valueOf(Design.VIOLET));progress.setProgressBackgroundTintList(ColorStateList.valueOf(PANEL));SoundSliders.style(progress);timeline.addView(progress,new LinearLayout.LayoutParams(-1,dp(10)));Design.space(timeline,7);time=Design.text(this,"0:00 / "+mix.timerMinutes+":00",12,400,MUTED);time.setGravity(Gravity.END);timeline.addView(time,new LinearLayout.LayoutParams(-1,-2));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1);tp.leftMargin=dp(16);controls.addView(timeline,tp);body.addView(controls);alert=Design.text(body,"",12,500,Design.LILAC);alert.setVisibility(View.GONE);Design.space(body,14);
+        LinearLayout controls=Design.row(this);playArea=Design.row(this);controls.addView(playArea,new LinearLayout.LayoutParams(dp(58),dp(58)));LinearLayout timeline=Design.col(this);progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(1000);progress.setProgressTintList(ColorStateList.valueOf(Design.VIOLET));progress.setProgressBackgroundTintList(ColorStateList.valueOf(PANEL));SoundSliders.style(progress);timeline.addView(progress,new LinearLayout.LayoutParams(-1,dp(10)));Design.space(timeline,7);time=Design.text(this,"0:00 / "+mix.timerMinutes+":00",12,400,MUTED);time.setGravity(Gravity.END);timeline.addView(time,new LinearLayout.LayoutParams(-1,-2));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1);tp.leftMargin=dp(16);controls.addView(timeline,tp);body.addView(controls);alert=Design.text(body,"",12,500,Design.LILAC);alert.setVisibility(View.GONE);importStatus=null;if(audioImport!=null){Design.space(body,8);LinearLayout checking=Design.row(this);importStatus=Design.text(this,"Checking audio…",12,500,MUTED);checking.addView(importStatus,new LinearLayout.LayoutParams(0,-2,1));TextView cancel=Design.text(this,"Cancel",13,600,INK);cancel.setGravity(Gravity.CENTER);cancel.setMinimumHeight(dp(48));cancel.setPadding(dp(16),0,dp(16),0);cancel.setContentDescription("Cancel audio import");cancel.setFocusable(true);cancel.setBackground(Design.touch(this,PANEL,12));cancel.setOnClickListener(v->cancelImport());checking.addView(cancel);body.addView(checking);}Design.space(body,14);
         gain(body,mix.primary.equals("Rain")?"rain":"wave",mix.label(),mix.primaryGain,false);if(!mix.primary.equals("Brown noise")){Design.space(body,8);gain(body,"wave","Brown noise",mix.brownGain,true);}Design.space(body,8);line(body);
         option(body,"clock","Timer",mix.timerMinutes+" min",this::timer);line(body);option(body,"fade","Fade out",mix.fadeMinutes==0?"Off":mix.fadeMinutes+" min",this::fade);line(body);Design.space(body,12);Design.text(body,"Sound library",15,600,INK);Design.space(body,10);
         tiles.clear();LinearLayout library=Design.row(this);for(String name:SoundMix.LIBRARY){LinearLayout tile=Design.col(this);tile.setGravity(Gravity.CENTER);tile.setPadding(dp(3),dp(3),dp(3),dp(5));GradientDrawable surface=Design.shape(this,mix.primary.equals(name)?PANEL:0xff302739,13);surface.setStroke(dp(mix.primary.equals(name)?2:1),mix.primary.equals(name)?Design.LILAC:0xff493c56);tile.setBackground(surface);tile.setSelected(mix.primary.equals(name));tile.setContentDescription(name+" sound");tile.setFocusable(true);tile.setOnClickListener(v->select(name));ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setBackground(Design.shape(this,PANEL,10));image.setClipToOutline(true);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);tile.addView(image,new LinearLayout.LayoutParams(-1,dp(58)));tiles.put(name,image);Design.space(tile,5);TextView label=Design.text(this,name,11,500,INK);label.setGravity(Gravity.CENTER);tile.addView(label);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);if(library.getChildCount()>0)lp.leftMargin=dp(7);library.addView(tile,lp);}body.addView(library);showArt();Design.space(body,12);
-        status=Design.text(body,"",12,400,MUTED);Design.space(body,6);option(body,"file",importing?"Checking audio…":"My audio",mix.uri.isEmpty()?"Import":mix.title,this::myAudio);
+        status=Design.text(body,"",12,400,MUTED);Design.space(body,6);option(body,"file","My audio",audioImport!=null?"Checking…":mix.uri.isEmpty()?"Import":mix.title,this::myAudio);
         LinearLayout auto=Design.row(this);LinearLayout autoCopy=Design.col(this);Design.text(autoCopy,"Play when tracking starts",14,500,INK);Design.space(autoCopy,5);Design.text(autoCopy,"Uses this mix for a new recorded night.",11,400,MUTED);auto.addView(autoCopy,new LinearLayout.LayoutParams(0,-2,1));Switch toggle=new Switch(this);toggle.setContentDescription("Play when tracking starts");Design.styleSwitch(this,toggle,Design.VIOLET,PANEL);toggle.setChecked(mix.withTracking);toggle.setOnCheckedChangeListener((v,on)->{mix.withTracking=on;save();});auto.addView(toggle);body.addView(auto);Design.space(body,6);Design.text(body,"Timer counts playing time; Pause freezes it. Changes save automatically. Phone media volume also affects loudness.",11,400,MUTED);refresh();
     }
     private String description(){return switch(mix.primary){case "Rain"->"A steady rain to help you relax";case "Ocean"->"Slow waves, a quiet shore";case "Fireplace"->"A soft fire for a quiet evening";case "Wind"->"A gentle breeze through the trees";case "Brown noise"->"A low, continuous noise";default->"Your audio, kept with its document provider";};}
-    private void showArt(){if(hero!=null){hero.setImageBitmap(artwork.get(SoundMix.SOURCES.contains(mix.primary)&&!mix.primary.equals("My audio")&&!mix.primary.equals("Brown noise")?mix.primary:"Rain"));}for(Map.Entry<String,ImageView> tile:tiles.entrySet())tile.getValue().setImageBitmap(artwork.get(tile.getKey()));}
+    private void showArt(){if(hero!=null){hero.setImageBitmap(artwork.get(SoundMix.SOURCES.contains(mix.primary)&&!mix.primary.equals("My audio")&&!mix.primary.equals("Brown noise")?mix.primary:"My audio"));}for(Map.Entry<String,ImageView> tile:tiles.entrySet())tile.getValue().setImageBitmap(artwork.get(tile.getKey()));}
     private void gain(LinearLayout body,String icon,String name,int value,boolean brown){LinearLayout row=Design.row(this);Design.Icon glyph=new Design.Icon(this,icon,MUTED);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(27),dp(27));ip.rightMargin=dp(18);row.addView(glyph,ip);LinearLayout slider=Design.col(this),labels=Design.row(this);TextView sourceName=Design.text(this,name,14,500,MUTED);sourceName.setMaxLines(1);sourceName.setEllipsize(android.text.TextUtils.TruncateAt.END);labels.addView(sourceName,new LinearLayout.LayoutParams(0,-2,1));TextView percent=Design.text(this,value+"%",15,600,MUTED);labels.addView(percent);slider.addView(labels);SeekBar seek=new SeekBar(this);seek.setMax(100);seek.setProgress(value);seek.setContentDescription(name+" volume");seek.setProgressTintList(ColorStateList.valueOf(Design.VIOLET));seek.setProgressBackgroundTintList(ColorStateList.valueOf(PANEL));seek.setThumbTintList(ColorStateList.valueOf(INK));seek.setPadding(dp(2),0,dp(2),0);SoundSliders.style(seek);seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){save();}public void onProgressChanged(SeekBar b,int n,boolean user){if(user){if(brown)mix.brownGain=n;else mix.primaryGain=n;percent.setText(getString(R.string.sound_gain_percent,n));SoundPreferences.save(SleepSoundsActivity.this,mix);SleepSoundService.update(SleepSoundsActivity.this);}}});slider.addView(seek,new LinearLayout.LayoutParams(-1,dp(48)));row.addView(slider,new LinearLayout.LayoutParams(0,-2,1));body.addView(row);}
     private void option(LinearLayout body,String icon,String name,String value,Runnable action){LinearLayout row=Design.row(this);row.setMinimumHeight(dp(52));Design.Icon glyph=new Design.Icon(this,icon,MUTED);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(25),dp(25));ip.rightMargin=dp(18);row.addView(glyph,ip);row.addView(Design.text(this,name,14,500,INK),new LinearLayout.LayoutParams(0,-2,1));TextView val=Design.text(this,value,13,400,MUTED);val.setMaxWidth(dp(140));val.setMaxLines(1);val.setEllipsize(android.text.TextUtils.TruncateAt.END);row.addView(val,new LinearLayout.LayoutParams(-2,-2));Design.Icon arrow=new Design.Icon(this,"chevron",MUTED);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(18),dp(18));ap.leftMargin=dp(10);row.addView(arrow,ap);row.setContentDescription(name);row.setBackground(Design.touch(this,android.graphics.Color.TRANSPARENT,12));row.setOnClickListener(v->action.run());body.addView(row);}
     private void line(LinearLayout body){View v=new View(this);v.setBackgroundColor(PANEL);body.addView(v,new LinearLayout.LayoutParams(-1,dp(1)));}
@@ -46,30 +44,35 @@
     private void fade(){int[] values={0,1,2,5,10,15};String[] labels=Arrays.stream(values).mapToObj(n->n==0?"Off":n+" min").toArray(String[]::new);new AlertDialog.Builder(this).setTitle("Fade at the end of the timer").setItems(labels,(d,w)->{mix.fadeMinutes=Math.min(values[w],mix.timerMinutes);save();render();}).setNegativeButton("Cancel",null).show();}
     private void toggle(){SleepSoundService.Snapshot s=SleepSoundService.snapshot();boolean active=s!=null&&(s.state==SoundPlayback.State.PLAYING||s.state==SoundPlayback.State.LOADING);if(!active){localError="";SoundPreferences.error(this,"");save();}SleepSoundService.command(this,active?SleepSoundService.PAUSE:SleepSoundService.PLAY);handler.postDelayed(this::refresh,100);}
     private void refresh(){
-        if(time==null)return;SleepSoundService.Snapshot s=SleepSoundService.snapshot();long elapsed=s==null?0:s.elapsed;boolean active=s!=null&&(s.state==SoundPlayback.State.PLAYING||s.state==SoundPlayback.State.LOADING);String state=active?"Pause": "Play";
+        if(pollImport())return;if(time==null)return;SleepSoundService.Snapshot s=SleepSoundService.snapshot();long elapsed=s==null?0:s.elapsed;boolean active=s!=null&&(s.state==SoundPlayback.State.PLAYING||s.state==SoundPlayback.State.LOADING);String state=active?"Pause": "Play";
         if(!state.equals(lastState)||playArea.getChildCount()==0){lastState=state;playArea.removeAllViews();Design.Icon button=new Design.Icon(this,active?"pause":"play",Design.PLUM);button.setPadding(dp(17),dp(17),dp(17),dp(17));button.setContentDescription(state+" sleep sounds");button.setFocusable(true);button.setBackground(Design.touch(this,Design.LILAC,40));button.setOnClickListener(v->toggle());playArea.addView(button,new LinearLayout.LayoutParams(dp(58),dp(58)));}
         time.setText(String.format(Locale.ROOT,"%d:%02d / %d:00",elapsed/60000,(elapsed/1000)%60,mix.timerMinutes));progress.setProgress((int)Math.min(1000,elapsed*1000/(mix.timerMinutes*60000L)));
-        String error=localError.isEmpty()?SleepSoundService.error(this):localError;alert.setText(error);alert.setVisibility(error.isEmpty()?View.GONE:View.VISIBLE);status.setVisibility(error.isEmpty()?View.VISIBLE:View.GONE);status.setText(s==null?"Ready · tap Play to start":s.state==SoundPlayback.State.LOADING?"Loading your sounds…":s.state==SoundPlayback.State.PAUSED?"Paused · timer is paused":s.state==SoundPlayback.State.FINISHED?"Timer finished · tap Play to start again":"Playing · sounds continue when you leave this screen");
+        String error=audioImport!=null?"":localError.isEmpty()?SleepSoundService.error(this):localError;alert.setText(error);alert.setVisibility(error.isEmpty()?View.GONE:View.VISIBLE);status.setVisibility(error.isEmpty()&&audioImport==null?View.VISIBLE:View.GONE);if(importStatus!=null&&audioImport!=null)importStatus.setText("Checking audio · "+(audioImport.bytes/1024)+" KiB read\nUp to 100 MiB");status.setText(audioImport!=null?"Checking audio · "+(audioImport.bytes/1024)+" KiB read · up to 100 MiB":s==null?"Ready · tap Play to start":s.state==SoundPlayback.State.LOADING?"Loading your sounds…":s.state==SoundPlayback.State.PAUSED?"Paused · timer is paused":s.state==SoundPlayback.State.FINISHED?"Timer finished · tap Play to start again":"Playing · sounds continue when you leave this screen");
         handler.removeCallbacks(refreshTick);if(resumed)handler.postDelayed(refreshTick,500);
     }
     private final Runnable refreshTick=this::refresh;
-    private void myAudio(){if(importing)return;if(mix.uri.isEmpty()){importAudio();return;}new AlertDialog.Builder(this).setTitle("My audio").setItems(new String[]{"Use "+mix.title,"Import another file","Remove from SleepWell"},(d,w)->{if(w==0)select("My audio");else if(w==1)importAudio();else {mix.uri="";mix.title="My audio";if(mix.primary.equals("My audio"))mix.primary="Rain";save();render();}}).setNegativeButton("Cancel",null).show();}
+    private void cancelImport(){if(audioImport!=null){audioImport.cancel();audioImport.retire();audioImport=null;localError="";render();}}
+    private void myAudio(){if(audioImport!=null){cancelImport();return;}if(mix.uri.isEmpty()){importAudio();return;}new AlertDialog.Builder(this).setTitle("My audio").setItems(new String[]{"Use "+mix.title,"Import another file","Remove from mix"},(d,w)->{if(w==0)select("My audio");else if(w==1)importAudio();else {mix.uri="";mix.title="My audio";if(mix.primary.equals("My audio"))mix.primary="Rain";save();render();}}).setNegativeButton("Cancel",null).show();}
     private void importAudio(){try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("audio/*").addCategory(Intent.CATEGORY_OPENABLE).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION),IMPORT);}catch(ActivityNotFoundException e){localError="No audio document picker is available on this phone.";refresh();}}
-    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=IMPORT||result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();if(!"content".equals(uri.getScheme())){localError="Choose an audio document from your phone.";refresh();return;}importing=true;render();Context app=getApplicationContext();reader.execute(()->{String title="My audio",problem="";try{
-            try(android.database.Cursor cursor=app.getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE},null,null,null)){if(cursor!=null&&cursor.moveToFirst()){if(!cursor.isNull(1)&&(cursor.getLong(1)<=0||cursor.getLong(1)>100*1024*1024L))throw new java.io.IOException("Large file");if(!cursor.isNull(0))title=cursor.getString(0);}}
-            MediaMetadataRetriever metadata=new MediaMetadataRetriever();try{metadata.setDataSource(app,uri);String duration=metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);if(duration==null||Long.parseLong(duration)<=0||!"yes".equals(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)))throw new java.io.IOException("No audio");}finally{metadata.release();}
-            // Persist only after validation. Never revoke an app-wide grant shared with an alarm tone.
-            app.getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
-        }catch(Exception e){problem="That file could not be read as audio. Choose a readable audio file under 100 MB.";}String name=title,message=problem;
-        runOnUiThread(()->{importing=false;if(isDestroyed()){return;}
-            if(!message.isEmpty()){localError=message;}
-            else {mix.uri=uri.toString();mix.title=name.length()>80?name.substring(0,80):name;mix.primary="My audio";localError="";SoundPreferences.error(this,"");save();}render();});});}
+    @Override protected void onActivityResult(int request,int result,Intent data){
+        super.onActivityResult(request,result,data);if(request!=IMPORT||result!=RESULT_OK||data==null||data.getData()==null)return;
+        if(audioImport!=null){audioImport.cancel();audioImport.retire();audioImport=null;}
+        try{audioImport=SoundImports.start(this,data.getData(),data.getFlags());localError="";}
+        catch(RuntimeException e){localError="Choose an audio document from your phone.";}render();
+    }
+    private boolean pollImport(){
+        if(audioImport==null)return false;audioImport.poll();if(audioImport.state==SoundImports.State.READING)return false;
+        if(audioImport.state==SoundImports.State.READY&&audioImport.commit()){load();localError="";SleepSoundService.update(this);}
+        else localError=audioImport.error;
+        audioImport.retire();audioImport=null;render();return true;
+    }
+    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);if(audioImport!=null)out.putString("audioImport",audioImport.id);}
     private int dp(int n){return Design.dp(this,n);}
-    boolean artworkReady(){return artwork.size()==SoundMix.LIBRARY.length;}
+    boolean artworkReady(){return artwork.size()==SoundMix.LIBRARY.length+1;}
     void awaitReaders(long timeoutMs)throws Exception{if(reader.isShutdown()){if(!reader.awaitTermination(timeoutMs,TimeUnit.MILLISECONDS))throw new TimeoutException("Sound artwork is still loading");}else reader.submit(()->{}).get(timeoutMs,TimeUnit.MILLISECONDS);}
     @Override protected void onResume(){super.onResume();resumed=true;refresh();}
     @Override protected void onPause(){resumed=false;handler.removeCallbacks(refreshTick);super.onPause();}
-    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);reader.shutdown();super.onDestroy();}
+    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);reader.shutdown();if(audioImport!=null&&!isChangingConfigurations()){audioImport.cancel();audioImport.retire();audioImport=null;}super.onDestroy();}
     @android.annotation.SuppressLint("GestureBackNavigation") @Override public void onBackPressed(){finish();}
     @Override public void onWindowFocusChanged(boolean focus){super.onWindowFocusChanged(focus);if(focus)getWindow().getDecorView().post(()->getWindow().getInsetsController().setSystemBarsAppearance(0,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS));}
 }
```

## app/src/androidTest/AndroidManifest.xml

```diff
--- a/app/src/androidTest/AndroidManifest.xml
+++ b/app/src/androidTest/AndroidManifest.xml
@@ -0,0 +1,9 @@
+<manifest xmlns:android="http://schemas.android.com/apk/res/android">
+    <application>
+        <!-- Test APK only. DocumentsUI holds MANAGE_DOCUMENTS; a selected URI grants read access. -->
+        <provider android:name="org.sleepwell.app.ImportDocuments" android:authorities="org.sleepwell.app.test.documents"
+            android:exported="true" android:grantUriPermissions="true" android:permission="android.permission.MANAGE_DOCUMENTS">
+            <intent-filter><action android:name="android.content.action.DOCUMENTS_PROVIDER" /></intent-filter>
+        </provider>
+    </application>
+</manifest>
```

## app/src/androidTest/java/org/sleepwell/app/ImportDocuments.java

```diff
--- a/app/src/androidTest/java/org/sleepwell/app/ImportDocuments.java
+++ b/app/src/androidTest/java/org/sleepwell/app/ImportDocuments.java
@@ -0,0 +1,33 @@
+package org.sleepwell.app;
+
+import android.database.*;
+import android.os.*;
+import android.provider.*;
+import java.io.*;
+import java.nio.*;
+
+/** Read-only synthetic fixtures, available only in the disposable API36 emulator. Never in the app APK. */
+public final class ImportDocuments extends DocumentsProvider {
+    static final String AUTHORITY="org.sleepwell.app.test.documents",ROOT="SleepWell test audio";
+    private static final String[] IDS={"valid","unknown","broken","large","overflow","denied","delayed"};
+    private static final String[] NAMES={"Quiet sea.wav","Unknown size.wav","Broken audio.wav","Too large.wav","Hidden large.wav","Denied.wav","Delayed.wav"};
+    private boolean enabled(){return Build.MODEL.startsWith("sdk_gphone64")&&Build.HARDWARE.equals("ranchu");}
+    private void guard(){if(!enabled())throw new SecurityException("CI emulator fixture only");}
+    @Override public boolean onCreate(){return true;}
+    private MatrixCursor cursor(String[] projection,String... defaults){return new MatrixCursor(projection==null?defaults:projection);}
+    @Override public Cursor queryRoots(String[] projection){MatrixCursor c=cursor(projection,DocumentsContract.Root.COLUMN_ROOT_ID,DocumentsContract.Root.COLUMN_TITLE,DocumentsContract.Root.COLUMN_DOCUMENT_ID,DocumentsContract.Root.COLUMN_FLAGS,DocumentsContract.Root.COLUMN_MIME_TYPES);if(enabled()){MatrixCursor.RowBuilder row=c.newRow();for(String column:c.getColumnNames())row.add(column,switch(column){case DocumentsContract.Root.COLUMN_ROOT_ID->"audio";case DocumentsContract.Root.COLUMN_TITLE->ROOT;case DocumentsContract.Root.COLUMN_DOCUMENT_ID->"root";case DocumentsContract.Root.COLUMN_FLAGS->DocumentsContract.Root.FLAG_LOCAL_ONLY;case DocumentsContract.Root.COLUMN_MIME_TYPES->"audio/*";default->null;});}return c;}
+    private int index(String id)throws FileNotFoundException{for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;throw new FileNotFoundException("Unknown fixture");}
+    private void document(MatrixCursor c,String id)throws FileNotFoundException{int i=id.equals("root")?-1:index(id);MatrixCursor.RowBuilder row=c.newRow();for(String column:c.getColumnNames())row.add(column,switch(column){case DocumentsContract.Document.COLUMN_DOCUMENT_ID->id;case DocumentsContract.Document.COLUMN_DISPLAY_NAME->i<0?ROOT:NAMES[i];case DocumentsContract.Document.COLUMN_MIME_TYPE->i<0?DocumentsContract.Document.MIME_TYPE_DIR:"audio/wav";case DocumentsContract.Document.COLUMN_FLAGS->0;case DocumentsContract.Document.COLUMN_SIZE->id.equals("unknown")||id.equals("overflow")||id.equals("delayed")?null:id.equals("large")?100*1024*1024L+1:id.equals("broken")?12L:32044L;default->null;});}
+    @Override public Cursor queryDocument(String id,String[] projection)throws FileNotFoundException{guard();MatrixCursor c=cursor(projection,DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_FLAGS,DocumentsContract.Document.COLUMN_SIZE);document(c,id);return c;}
+    @Override public Cursor queryChildDocuments(String parent,String[] projection,String sort)throws FileNotFoundException{guard();if(!parent.equals("root"))throw new FileNotFoundException();MatrixCursor c=cursor(projection,DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_FLAGS,DocumentsContract.Document.COLUMN_SIZE);for(String id:IDS)document(c,id);return c;}
+    @Override public boolean isChildDocument(String parent,String child){return enabled()&&parent.equals("root")&&java.util.Arrays.asList(IDS).contains(child);}
+    private byte[] wave(){ByteBuffer b=ByteBuffer.allocate(32044).order(ByteOrder.LITTLE_ENDIAN);b.put(new byte[]{'R','I','F','F'}).putInt(32036).put(new byte[]{'W','A','V','E','f','m','t',' '}).putInt(16).putShort((short)1).putShort((short)1).putInt(8000).putInt(16000).putShort((short)2).putShort((short)16).put(new byte[]{'d','a','t','a'}).putInt(32000);return b.array();}
+    @Override public ParcelFileDescriptor openDocument(String id,String mode,CancellationSignal signal)throws FileNotFoundException{
+        guard();index(id);if(!"r".equals(mode)||id.equals("denied"))throw new FileNotFoundException("Read unavailable");
+        try{
+            if(id.equals("delayed")){ParcelFileDescriptor[] pipe=ParcelFileDescriptor.createReliablePipe();Thread writer=new Thread(()->{try(OutputStream out=new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])){Thread.sleep(8000);out.write(wave());}catch(Exception ignored){}},"fixture-writer");writer.setDaemon(true);writer.start();return pipe[0];}
+            File f=new File(getContext().getCacheDir(),"fixture-"+id+".wav");synchronized(ImportDocuments.class){if(!f.exists()){if(id.equals("overflow")||id.equals("large")){try(RandomAccessFile out=new RandomAccessFile(f,"rw")){out.setLength(100*1024*1024L+1);}}else try(FileOutputStream out=new FileOutputStream(f)){out.write(id.equals("broken")?new byte[12]:wave());}}}
+            return ParcelFileDescriptor.open(f,ParcelFileDescriptor.MODE_READ_ONLY);
+        }catch(IOException e){throw new FileNotFoundException("Fixture read failed");}
+    }
+}
```
