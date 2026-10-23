package org.sleepwell.app;

import java.util.Arrays;

/** Shared tracking/preview gravity removal, preserving the existing .85/.15 low-pass filter. */
final class MotionFilter {
    private final float[] gravity=new float[3];private boolean ready;
    Double sample(float x,float y,float z){if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))return null;if(!ready){gravity[0]=x;gravity[1]=y;gravity[2]=z;ready=true;return null;}double energy=0;for(int k=0;k<3;k++){float value=k==0?x:k==1?y:z;gravity[k]=.85f*gravity[k]+.15f*value;double delta=value-gravity[k];energy+=delta*delta;}return Math.sqrt(energy);}
    void reset(){ready=false;Arrays.fill(gravity,0);}
}
