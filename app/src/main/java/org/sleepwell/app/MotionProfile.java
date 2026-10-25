package org.sleepwell.app;

/** Configurable heuristic gain. Raw recorded acceleration remains in its original units. */
final class MotionProfile {
    final int level;final double gain;final boolean calibrated;private final String config;
    MotionProfile(int level){if(level<0||level>2)throw new IllegalArgumentException("Choose Low, Medium or High sensitivity.");this.level=level;gain=level==0?.5:level==2?2:1;calibrated=false;config="";}
    MotionProfile(MotionCalibration calibration){if(calibration==null)throw new IllegalArgumentException("Missing surface calibration.");level=1;gain=calibration.gain;calibrated=true;config=calibration.config();}
    String label(){return calibrated?String.format(java.util.Locale.ROOT,"Calibrated · %.2f×",gain):level==0?"Low":level==2?"High":"Medium";}
    String config(){return config;}
    static void validateConfig(String config){if(config==null)throw new IllegalArgumentException("Missing motion configuration.");if(!config.isEmpty()){try{if(BatterySampling.ALGORITHM.equals(new org.json.JSONObject(config).optString("algorithm")))BatterySampling.validate(config);else MotionCalibration.read(config);}catch(org.json.JSONException invalid){throw new IllegalArgumentException("Invalid motion configuration",invalid);}}}
    double inference(double raw){return !Double.isFinite(raw)||raw<0?Double.NaN:raw*gain;}
    int activeMinutes(double raw,int previous){return inference(raw)>=.12?Math.min(5,previous+1):0;}
    boolean smartWake(long now,long deadline,int window,double raw,int recent,int samples){return SleepMath.smartWake(now,deadline,window,inference(raw),recent,samples);}
}
