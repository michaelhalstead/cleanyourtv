package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.File;

/**
 * font / clock_font: a typeface from the theme's fonts/ folder (pushed by tools/theme.sh, not bundled in the APK).
 * install() puts a factory on the activity's inflater so every TextView inflated afterwards, adapter rows and
 * dialogs included, uses it. Missing or unreadable fonts leave everything as it was.
 */
final class ThemeFont {
    /** The theme's font for views built in code (UiFont.get), or null when font is off. */
    static volatile Typeface current;

    private ThemeFont() {}

    /** The font file named in the theme's fonts/ folder, or null if the name is unsafe or the file will not load. */
    static Typeface load(Context ctx, String name) {
        File dir = Config.dir(ctx);
        if (dir == null || !ThemeFiles.fontFile(name)) return null;
        File file = new File(new File(dir, ThemeFiles.FONTS), name);
        if (!file.isFile()) return null;
        try {
            Typeface face = Typeface.createFromFile(file);
            return face == Typeface.DEFAULT ? null : face;   // createFromFile answers DEFAULT for a file it cannot read
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Call before setContentView. */
    static void install(Activity activity, Config cfg) {
        current = cfg.font.isEmpty() ? null : load(activity, cfg.font);
        if (current == null) return;
        try {
            activity.getLayoutInflater().setFactory2(new Fonts(current));
        } catch (IllegalStateException alreadySet) {
            // another factory owns this inflater: keep the built-in fonts rather than fight over it
        }
    }

    /** Every layout uses only TextView and TextClock for text; anything else is left to the default inflater. */
    private static final class Fonts implements LayoutInflater.Factory2 {
        private final Typeface face;

        Fonts(Typeface face) {
            this.face = face;
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            TextView view;
            if ("TextView".equals(name)) view = new TextView(context, attrs);
            else if ("TextClock".equals(name)) view = new android.widget.TextClock(context, attrs);
            else return null;
            view.setTypeface(face);
            return view;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return onCreateView(null, name, context, attrs);
        }
    }
}
