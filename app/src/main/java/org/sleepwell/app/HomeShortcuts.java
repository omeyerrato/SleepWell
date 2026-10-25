package org.sleepwell.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.*;
import android.graphics.drawable.Icon;
import java.util.*;

final class HomeShortcuts {
    static final List<HomeLaunch.Screen> SCREENS=List.of(HomeLaunch.Screen.ADD_ALARM,HomeLaunch.Screen.TRACKING,HomeLaunch.Screen.HISTORY);
    static String label(HomeLaunch.Screen screen){return switch(screen){case ADD_ALARM->"Add alarm";case TRACKING->"Sleep tracking";case HISTORY->"Sleep history";default->throw new IllegalArgumentException("Not a launcher shortcut");};}
    static ShortcutInfo info(Context context,HomeLaunch.Screen screen){int icon=switch(screen){case ADD_ALARM->R.drawable.ic_phosphor_plus_regular;case TRACKING->R.drawable.ic_phosphor_moon_fill;case HISTORY->R.drawable.ic_phosphor_chart_bar_fill;default->throw new IllegalArgumentException("Unknown shortcut");};return new ShortcutInfo.Builder(context,"sleepwell-"+screen.name().toLowerCase(Locale.ROOT)).setShortLabel(label(screen)).setLongLabel(label(screen)).setIcon(Icon.createWithResource(context,icon)).setIntent(HomeLaunch.intent(context,screen,0)).build();}
    static void publish(Context context){try{ShortcutManager manager=context.getSystemService(ShortcutManager.class);if(manager==null||manager.isRateLimitingActive())return;List<ShortcutInfo> current=manager.getDynamicShortcuts();Set<String> expected=new HashSet<>();for(HomeLaunch.Screen screen:SCREENS)expected.add(info(context,screen).getId());if(current.size()==expected.size()&&current.stream().allMatch(shortcut->expected.contains(shortcut.getId())))return;List<ShortcutInfo> shortcuts=new ArrayList<>();for(HomeLaunch.Screen screen:SCREENS)shortcuts.add(info(context,screen));manager.setDynamicShortcuts(shortcuts);}catch(RuntimeException ignored){}}
    static boolean pin(Activity activity,HomeLaunch.Screen screen){ShortcutManager manager=activity.getSystemService(ShortcutManager.class);return manager!=null&&manager.isRequestPinShortcutSupported()&&manager.requestPinShortcut(info(activity,screen),null);}
}
