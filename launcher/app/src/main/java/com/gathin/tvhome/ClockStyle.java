package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.widget.TextView;

/**
 * clock_style led: the clock as a 7-segment display (digits in the clock_font from the theme's fonts/ folder) on a
 * dark panel with unlit segments behind the lit ones. Done to the existing views, so ids and layouts are untouched;
 * with clock_style off, or clock_font missing or unreadable, nothing changes.
 */
final class ClockStyle {
    private static final int LIT = 0xFFFF5A14;
    private static final int GHOST = 0xFF3C1206;
    private static final int PANEL = 0xFF120806;
    private static final int STROKE = 0xFF46281A;
    private static final int AMPM = 0xFFFF7828;
    /** Every cell lit: the unlit segments, right-aligned so "9:21" lands on the last four cells. */
    private static final String CELLS = "88:88";

    private ClockStyle() {}

    static void apply(TextView clock, TextView ampm, Config cfg) {
        if (!Config.CLOCK_LED.equals(cfg.clockStyle)) return;
        Typeface face = ThemeFont.load(clock.getContext(), cfg.clockFont);
        if (face == null) return;
        float d = clock.getResources().getDisplayMetrics().density;
        int padX = Math.round(12 * d), padY = Math.round(6 * d);

        clock.setTypeface(face);
        clock.setLetterSpacing(0f);
        clock.setTextColor(LIT);
        clock.setShadowLayer(12 * d, 0f, 0f, LIT);
        clock.setGravity(Gravity.RIGHT);   // absolute right: the ghost is drawn from the right edge even in RTL
        Ghost ghost = new Ghost(clock, face);
        clock.setMinWidth(Math.round(ghost.paint.measureText(CELLS)) + 2 * padX);
        GradientDrawable panel = new GradientDrawable();
        panel.setColor(PANEL);
        panel.setCornerRadius(10 * d);
        panel.setStroke(Math.round(1.5f * d), STROKE);
        clock.setBackground(new LayerDrawable(new Drawable[]{panel, ghost}));
        clock.setPadding(padX, padY, padX, padY);   // after the background, which would reset it

        ampm.setTextColor(AMPM);
        Typeface font = ThemeFont.load(ampm.getContext(), cfg.font);
        if (font != null) ampm.setTypeface(font);
    }

    /** "88:88" in the clock's font and size, on the clock's baseline. */
    private static final class Ghost extends Drawable {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final TextView clock;

        Ghost(TextView clock, Typeface face) {
            this.clock = clock;
            paint.setTypeface(face);
            paint.setTextSize(clock.getTextSize());
            paint.setColor(GHOST);
            paint.setTextAlign(Paint.Align.RIGHT);
        }

        @Override
        public void draw(Canvas canvas) {
            int baseline = clock.getBaseline();
            if (baseline < 0) return;   // not laid out yet
            canvas.drawText(CELLS, getBounds().right - clock.getPaddingRight(), baseline, paint);
        }

        @Override public void setAlpha(int alpha) {}
        @Override public void setColorFilter(ColorFilter filter) {}
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
