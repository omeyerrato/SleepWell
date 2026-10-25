package org.sleepwell.app;

import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Size;
import android.view.View;
import androidx.activity.ComponentActivity;
import androidx.camera.core.*;
import androidx.camera.core.resolutionselector.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.*;

/** CameraX Preview + bounded offline ZXing analysis. No capture files, network, audio or torch. */
final class QrCamera {
    interface Port extends QrScanSession.Port {View preview();default void format(String format){if(!MorningBarcode.QR.equals(format))throw new IllegalStateException("This camera adapter does not support barcode scanning.");}}
    interface Factory {Port create(ComponentActivity activity);default Port create(ComponentActivity activity,String format){Port port=create(activity);port.format(format);return port;}}
    static Factory factory=Real::new;
    private static final class Real implements Port {
    private final ComponentActivity activity;private final PreviewView previewView;private ProcessCameraProvider provider;private Preview preview;private ImageAnalysis analysis;private Camera camera;private ExecutorService decoderWorker;
    private String format=MorningBarcode.QR;private volatile boolean active;private volatile long generation;
    Real(ComponentActivity activity){this.activity=activity;previewView=new PreviewView(activity);previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);}
    public View preview(){return previewView;}
    public void format(String format){if(active||!MorningBarcode.supported(format))throw new IllegalArgumentException("Invalid scanner format");this.format=format;}
    public void start(QrScanSession.Listener listener){
        close();long ticket=++generation;active=true;long[] lastFrame={0};decoderWorker=Executors.newSingleThreadExecutor();ListenableFuture<ProcessCameraProvider> future=ProcessCameraProvider.getInstance(activity);
        future.addListener(()->{if(!live(ticket)||!activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED))return;try{
            if(activity.checkSelfPermission(android.Manifest.permission.CAMERA)!=android.content.pm.PackageManager.PERMISSION_GRANTED)throw new SecurityException("Camera access is off.");
            provider=future.get();CameraSelector selector=provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)?CameraSelector.DEFAULT_BACK_CAMERA:CameraSelector.DEFAULT_FRONT_CAMERA;if(!provider.hasCamera(selector))throw new IllegalStateException("No supported camera is available.");
            preview=new Preview.Builder().build();preview.setSurfaceProvider(previewView.getSurfaceProvider());analysis=new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setResolutionSelector(new ResolutionSelector.Builder().setResolutionStrategy(new ResolutionStrategy(new Size(1280,720),ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)).build()).build();QrDecoder decoder=new QrDecoder(format);
            analysis.setAnalyzer(decoderWorker,image->{String found=null,problem=null;try{if(!live(ticket))return;long now=SystemClock.elapsedRealtime();if(lastFrame[0]!=0&&now-lastFrame[0]<200)return;lastFrame[0]=now;Rect crop=image.getCropRect();ImageProxy.PlaneProxy[] planes=image.getPlanes();if(planes.length==0)throw new IllegalArgumentException("Camera has no luminance frame.");ImageProxy.PlaneProxy y=planes[0];byte[] luma=QrLuma.copy(y.getBuffer(),y.getRowStride(),y.getPixelStride(),crop.left,crop.top,crop.width(),crop.height());found=decoder.digest(luma,crop.width(),crop.height());}catch(RuntimeException invalid){problem=invalid.getMessage()==null?"This camera frame could not be analyzed.":invalid.getMessage();}finally{try{image.close();}catch(RuntimeException unavailable){found=null;problem="Camera frame could not be released. Try again.";}}String hash=found,error=problem;if(hash!=null||error!=null)activity.runOnUiThread(()->{if(!live(ticket))return;if(error!=null)listener.error(error);else listener.captured(hash);});});
            camera=provider.bindToLifecycle(activity,selector,preview,analysis);camera.getCameraInfo().getCameraState().observe(activity,state->{if(live(ticket)&&state.getError()!=null)listener.error("Camera is unavailable or in use. Close other camera apps and try again.");});
        }catch(Exception unavailable){if(live(ticket))listener.error(unavailable instanceof SecurityException?"Camera access is off.":"Camera could not start. It may be unavailable or in use.");}},ContextCompat.getMainExecutor(activity));
    }
    private boolean live(long ticket){return active&&ticket==generation&&!activity.isDestroyed();}
    public void close(){
        active=false;generation++;if(analysis!=null)analysis.clearAnalyzer();if(camera!=null)camera.getCameraInfo().getCameraState().removeObservers(activity);
        try{if(provider!=null){if(preview!=null&&analysis!=null)provider.unbind(preview,analysis);else if(preview!=null)provider.unbind(preview);else if(analysis!=null)provider.unbind(analysis);}preview=null;analysis=null;camera=null;}finally{if(decoderWorker!=null){decoderWorker.shutdown();decoderWorker=null;}}
    }
    }
    private QrCamera(){}
}
