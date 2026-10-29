package org.sleepwell.app;

import android.app.Activity;
import android.nfc.*;
import java.util.function.Consumer;

/** A single preview child's reader, never an adapter enable/grant or an alarm result. */
final class PracticeNfcAccess {
    interface Port {boolean available();boolean enabled();boolean start(Consumer<String> found);boolean stop();}
    static Port create(Activity owner){return new Reader(owner);}
    private static final class Reader implements Port {
        final Activity owner;final NfcAdapter adapter;boolean registered;
        Reader(Activity owner){this.owner=owner;adapter=NfcAdapter.getDefaultAdapter(owner);}
        public boolean available(){return adapter!=null;}
        public boolean enabled(){return adapter!=null&&adapter.isEnabled();}
        public boolean start(Consumer<String> found){
            if(!stop()||!enabled())return false;registered=true;
            try{adapter.enableReaderMode(owner,tag->{byte[] bytes=tag.getId();StringBuilder id=new StringBuilder();for(byte value:bytes)id.append(String.format(java.util.Locale.ROOT,"%02X",value));owner.runOnUiThread(()->{if(registered)found.accept(id.toString());});},NfcAdapter.FLAG_READER_NFC_A|NfcAdapter.FLAG_READER_NFC_B|NfcAdapter.FLAG_READER_NFC_F|NfcAdapter.FLAG_READER_NFC_V|NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK|NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,null);return true;}
            catch(RuntimeException failure){stop();throw failure;}
        }
        public boolean stop(){if(!registered)return true;try{adapter.disableReaderMode(owner);registered=false;return true;}catch(RuntimeException failure){return false;}}
    }
    private PracticeNfcAccess(){}
}
