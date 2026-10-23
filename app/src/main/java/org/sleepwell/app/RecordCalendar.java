package org.sleepwell.app;

import android.app.*;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.Consumer;

/** SleepWell-owned calendar. Only Use date invokes the result; dismissing cancels. */
final class RecordCalendar implements AutoCloseable {
    private final Activity activity;private final String title;private final Consumer<LocalDate> result;private final Runnable clear;private final Dialog dialog;private final Locale locale;
    private LocalDate selected;private YearMonth month;
    RecordCalendar(Activity activity,String title,LocalDate initial,Bundle saved,Consumer<LocalDate> result,Runnable clear){this.activity=activity;this.title=title;this.result=result;this.clear=clear;locale=activity.getResources().getConfiguration().getLocales().get(0);selected=saved==null?initial:LocalDate.parse(saved.getString("selected",initial.toString()));month=saved==null?YearMonth.from(selected):YearMonth.parse(saved.getString("month",YearMonth.from(selected).toString()));dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);render();dialog.show();dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));dialog.getWindow().setLayout(activity.getResources().getDisplayMetrics().widthPixels-Design.dp(activity,24),-2);}
    boolean showing(){return dialog.isShowing();}
    Bundle state(){Bundle saved=new Bundle();saved.putString("selected",selected.toString());saved.putString("month",month.toString());return saved;}
    private void browse(int amount){try{month=month.plusMonths(amount);render();}catch(DateTimeException ignored){}}
    private void render(){LinearLayout root=Design.col(activity);Design.pad(root,16,16);root.setBackground(Design.shape(activity,Design.CREAM,24));Design.text(root,title,22,600,Design.INK);Design.space(root,8);TextView selection=Design.text(root,selected.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)),12,500,Design.MUTED);selection.setContentDescription("Pending date "+selected);Design.space(root,10);
        LinearLayout nav=Design.row(activity);nav.addView(Design.iconButton(activity,"back","Previous month",()->browse(-1)),new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));TextView name=Design.text(activity,month.format(DateTimeFormatter.ofPattern("LLLL uuuu",locale)),18,600,Design.INK);name.setGravity(Gravity.CENTER);nav.addView(name,new LinearLayout.LayoutParams(0,-2,1));nav.addView(Design.iconButton(activity,"chevron","Next month",()->browse(1)),new LinearLayout.LayoutParams(Design.dp(activity,48),Design.dp(activity,48)));root.addView(nav);
        LinearLayout labels=Design.row(activity);for(DayOfWeek day:CalendarMonth.weekdays(locale)){TextView text=Design.text(activity,day.getDisplayName(TextStyle.NARROW_STANDALONE,locale),11,500,Design.MUTED);text.setGravity(Gravity.CENTER);text.setContentDescription(day.getDisplayName(TextStyle.FULL,locale));labels.addView(text,new LinearLayout.LayoutParams(0,Design.dp(activity,32),1));}root.addView(labels);
        NumberFormat numbers=NumberFormat.getIntegerInstance(locale);numbers.setGroupingUsed(false);List<LocalDate> cells=CalendarMonth.cells(month,locale);for(int n=0;n<cells.size();n+=7){LinearLayout row=Design.row(activity);for(int col=0;col<7;col++){LocalDate date=cells.get(n+col);FrameLayout hit=new FrameLayout(activity);if(date!=null){boolean chosen=date.equals(selected);TextView face=Design.text(activity,numbers.format(date.getDayOfMonth()),15,chosen?600:400,chosen?Design.CREAM:Design.INK);face.setGravity(Gravity.CENTER);android.graphics.drawable.GradientDrawable shape=Design.shape(activity,chosen?Design.BROWN:Color.TRANSPARENT,24);if(date.equals(LocalDate.now())&&!chosen)shape.setStroke(Design.dp(activity,1),Design.LINE);face.setBackground(shape);hit.addView(face,new FrameLayout.LayoutParams(Design.dp(activity,40),Design.dp(activity,40),Gravity.CENTER));hit.setContentDescription("Choose date "+date);hit.setSelected(chosen);hit.setFocusable(true);hit.setOnClickListener(v->{selected=date;render();});}row.addView(hit,new LinearLayout.LayoutParams(0,Design.dp(activity,48),1));}root.addView(row);}
        Design.space(root,6);Design.button(root,"Use date",true,()->{LocalDate value=selected;dialog.dismiss();result.accept(value);});Design.button(root,"Cancel",false,dialog::dismiss);if(clear!=null){TextView all=Design.text(root,"All dates",14,600,Design.BROWN);all.setGravity(Gravity.CENTER);all.setMinHeight(Design.dp(activity,48));all.setContentDescription("All dates");all.setOnClickListener(v->{dialog.dismiss();clear.run();});}ScrollView scroll=new ScrollView(activity);scroll.setFillViewport(true);scroll.addView(root);dialog.setContentView(scroll);}
    @Override public void close(){dialog.dismiss();}
}
