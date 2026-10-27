package org.sleepwell.app;

import android.app.*;
import android.nfc.NfcAdapter;
import android.os.*;
import java.util.function.Consumer;

/** One visible enrollment owns its reader callback. Closing never changes the saved tag. */
final class NfcEnrollment {
    interface Reader {boolean ready();void start(Consumer<byte[]> found);void stop();}
    private final Activity activity;private final Reader reader;private final Consumer<String> enroll,notice;
    private final Handler main=new Handler(Looper.getMainLooper());private AlertDialog dialog;private long generation;private boolean reading;
    NfcEnrollment(Activity activity,Reader reader,Consumer<String> enroll,Consumer<String> notice){this.activity=activity;this.reader=reader;this.enroll=enroll;this.notice=notice;}
    static NfcEnrollment platform(Activity activity,Consumer<String> enroll,Consumer<String> notice){NfcAdapter adapter=NfcAdapter.getDefaultAdapter(activity);return new NfcEnrollment(activity,new Reader(){
        public boolean ready(){return adapter!=null&&adapter.isEnabled();}
        public void start(Consumer<byte[]> found){adapter.enableReaderMode(activity,tag->found.accept(tag==null?null:tag.getId()),NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_B|NfcAdapter.FLAG_READER_NFC_F|NfcAdapter.FLAG_READER_NFC_V|NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null);}
        public void stop(){if(adapter!=null)adapter.disableReaderMode(activity);}
    },enroll,notice);}
    void open(){close();if(activity.isFinishing()||activity.isDestroyed()||!activity.hasWindowFocus())return;try{if(!reader.ready()){notice.accept("Enable NFC on your phone first.");return;}long ticket=generation;AlertDialog review=new AlertDialog.Builder(activity).setTitle("Hold your phone over the tag").setMessage("This tag will be required for NFC alarms.").setNegativeButton("Cancel",null).create();dialog=review;review.setOnDismissListener(value->close());review.show();reading=true;reader.start(bytes->{byte[] id=bytes==null?null:bytes.clone();main.post(()->{if(!owned(ticket,review))return;try{if(!reader.ready()){close();notice.accept("NFC is unavailable. Your enrolled tag was kept.");return;}if(id==null||id.length==0){notice.accept("This tag has no identifier. Try another tag.");return;}String value=AlarmActivity.hex(id);close();enroll.accept(value);notice.accept("Tag enrolled");}catch(RuntimeException unavailable){close();notice.accept("The tag could not be enrolled. Try again.");}});});}catch(RuntimeException unavailable){close();notice.accept("NFC reader could not start. Your enrolled tag was kept.");}}
    private boolean owned(long ticket,AlertDialog review){return ticket==generation&&dialog==review&&review.isShowing()&&!activity.isFinishing()&&!activity.isDestroyed()&&review.getWindow()!=null&&review.getWindow().getDecorView().hasWindowFocus();}
    boolean active(){return dialog!=null&&owned(generation,dialog);}
    void close(){generation++;AlertDialog old=dialog;dialog=null;if(old!=null){old.setOnDismissListener(null);old.dismiss();}if(reading){reading=false;try{reader.stop();}catch(RuntimeException unavailable){android.util.Log.w("SleepWell","NFC reader cleanup was not confirmed",unavailable);}}}
}
