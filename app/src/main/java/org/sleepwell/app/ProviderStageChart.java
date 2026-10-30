package org.sleepwell.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Saved provider intervals on their actual time axis; gaps and local edits never create stages. */
@android.annotation.SuppressLint("ViewConstructor")
final class ProviderStageChart extends View {
    private final List<HealthSleep.Stage> stages = new ArrayList<>();
    private final long start, end;
    private final ZoneId zone;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int selected = -1;
    private boolean gapSelected;

    ProviderStageChart(Context context, HealthSleep source, long start, long end, ZoneId zone) {
        super(context);
        if (end <= start) throw new IllegalArgumentException("A stage view needs a valid period.");
        this.start = start;
        this.end = end;
        this.zone = zone;
        for (HealthSleep.Stage stage : source.stages) {
            if (stage.end > start && stage.start < end) stages.add(stage);
        }
        setFocusable(true);
        setClickable(true);
        setDefaultFocusHighlightEnabled(false);
        describe();
    }

    static String name(int kind) {
        return HealthSleepSourceActivity.NAMES[kind];
    }

    static int color(int kind) {
        switch (kind) {
            case 1: case 3: case 7: return 0xffffaa25;
            case 4: return 0xffb18cf0;
            case 5: return 0xff7746ca;
            case 6: return 0xff9954d4;
            case 2: return 0xffd2bbee;
            default: return 0xff9c949e;
        }
    }

    private float dp(float value) { return Design.dp(getContext(), value); }
    private float x(long time) { return dp(2) + (getWidth() - dp(4)) * (time - start) / (float) (end - start); }
    private long visibleStart(HealthSleep.Stage stage) { return Math.max(start, stage.start); }
    private long visibleEnd(HealthSleep.Stage stage) { return Math.min(end, stage.end); }
    private String clock(long time) {
        return ChartTicks.clock(time, start, end, zone,
                android.text.format.DateFormat.is24HourFormat(getContext()), Locale.getDefault(), false, false);
    }
    private String selection() {
        if (selected < 0) return gapSelected ? "No stage reported for this interval" : "";
        HealthSleep.Stage stage = stages.get(selected);
        return name(stage.kind) + " · " + clock(visibleStart(stage)) + " – " + clock(visibleEnd(stage));
    }
    private void describe() {
        setContentDescription("Health Connect source-reported sleep stages. " + stages.size()
                + " intervals in this view. Blank time is unreported. " + selection());
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setTypeface(Design.font(getContext(), 400));
        paint.setTextSize(Design.sp(getContext(), 11 * Appearance.scale(getContext())));
        paint.setStyle(Paint.Style.FILL);
        Paint.FontMetrics font = paint.getFontMetrics();
        float lineHeight = font.descent - font.ascent + dp(2);
        float top = Math.max(dp(20), lineHeight + dp(6)), stripHeight = dp(14), baseline = top + dp(25);
        paint.setStrokeWidth(dp(.6f));
        paint.setColor(Design.LINE);
        canvas.drawLine(dp(2), baseline, getWidth() - dp(2), baseline, paint);
        List<ChartTicks.Placed> ticks = ChartTicks.layout(
                ChartTicks.candidates(start, end, zone,
                        android.text.format.DateFormat.is24HourFormat(getContext()), Locale.getDefault(), 5),
                start, end, dp(2), getWidth() - dp(2), dp(8), paint::measureText);
        for (ChartTicks.Placed tick : ticks) {
            float at = x(tick.tick.time);
            canvas.drawLine(at, top - dp(3), at, baseline, paint);
        }
        for (int n = 0; n < stages.size(); n++) {
            HealthSleep.Stage stage = stages.get(n);
            float y = stage.kind == 5 ? top + dp(10) : top;
            paint.setColor(color(stage.kind));
            canvas.drawRoundRect(x(visibleStart(stage)), y, x(visibleEnd(stage)), y + stripHeight,
                    dp(3), dp(3), paint);
            if (n == selected) {
                paint.setColor(Design.BROWN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(1.5f));
                canvas.drawRoundRect(x(visibleStart(stage)), y, x(visibleEnd(stage)), y + stripHeight,
                        dp(3), dp(3), paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }
        paint.setColor(Design.MUTED);
        paint.setTextAlign(Paint.Align.CENTER);
        for (ChartTicks.Placed tick : ticks) {
            String[] lines = tick.tick.label.split("\n");
            for (int n = 0; n < lines.length; n++) {
                canvas.drawText(lines[n], (float) tick.center, baseline + dp(14) + n * lineHeight, paint);
            }
        }
        String caption = selection();
        if (!caption.isEmpty()) {
            paint.setColor(Design.BROWN);
            paint.setTextAlign(Paint.Align.LEFT);
            CharSequence fitted = android.text.TextUtils.ellipsize(caption, textPaint(),
                    Math.max(0, getWidth() - dp(4)), android.text.TextUtils.TruncateAt.END);
            canvas.drawText(fitted.toString(), dp(2), top - dp(8), paint);
        }
        if (hasFocus()) {
            paint.setColor(Design.BROWN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            canvas.drawRoundRect(dp(1), top - dp(4), getWidth() - dp(1), baseline + dp(2), dp(5), dp(5), paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private android.text.TextPaint textPaint() { return new android.text.TextPaint(paint); }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            float ratio = Math.max(0, Math.min(1, (event.getX() - dp(2)) / Math.max(1, getWidth() - dp(4))));
            long time = start + Math.min(end - start - 1, (long) ((end - start) * (double) ratio));
            selected = -1;
            for (int n = 0; n < stages.size(); n++) {
                HealthSleep.Stage stage = stages.get(n);
                if (time >= stage.start && time < stage.end) { selected = n; break; }
            }
            gapSelected = selected < 0;
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean move(int step) {
        int next = selected < 0 ? (step > 0 ? 0 : stages.size() - 1) : selected + step;
        if (next < 0 || next >= stages.size()) return false;
        selected = next;
        gapSelected = false;
        performClick();
        return true;
    }

    @Override public boolean performClick() { super.performClick(); describe(); invalidate(); return true; }
    @Override protected void onFocusChanged(boolean focused, int direction, Rect previous) {
        super.onFocusChanged(focused, direction, previous); invalidate();
    }
    @Override public boolean onKeyDown(int key, KeyEvent event) {
        if (key == KeyEvent.KEYCODE_DPAD_RIGHT) return move(1);
        if (key == KeyEvent.KEYCODE_DPAD_LEFT) return move(-1);
        return super.onKeyDown(key, event);
    }
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
    }
    @Override public boolean performAccessibilityAction(int action, android.os.Bundle args) {
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) return move(1);
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) return move(-1);
        return super.performAccessibilityAction(action, args);
    }
}
