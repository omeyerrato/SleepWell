package org.sleepwell.app;

import android.app.*;
import android.os.Bundle;
import android.widget.*;

public class FocusActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); LinearLayout body=Ui.page(this,"Make room for tomorrow","Your bedtime guard is keeping this distraction for the morning.");
        Ui.label(body,"LET THE DAY END",12,Ui.MINT); Ui.heading(body,"The feed can wait.",36);
        Ui.text(body,"Rest is the plan you made for yourself. Put your phone on the mattress with the microphone uncovered, then start your night.");
        Ui.button(body,"Start my night",()->{ startActivity(new android.content.Intent(this,MainActivity.class));finish(); });
        Ui.button(body,"Pause guard for 5 minutes",()->new AlertDialog.Builder(this).setTitle("Take an intentional break?").setMessage("The guard will resume in five minutes. You can always disable it in Settings.")
            .setPositiveButton("Pause",(d,w)->{ Prefs.put(this,"focusPauseUntil",System.currentTimeMillis()+5*60000L);finish(); }).setNegativeButton("Keep resting",null).show());
        Ui.button(body,"Return to home screen",()->{ startActivity(new android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME));finish(); });
    }
}
