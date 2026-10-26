package io.github.michaelhalstead.crt;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

/** A side panel over the live picture, so changes show as they are made: an on/off switch, a preset picker and a strength bar (left/right moves 0.1). Changes reach the running layer live. */
public class MainActivity extends Activity {
    private TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(56, 80, 56, 80);
        root.setBackgroundColor(0xE6120806);

        TextView note = new TextView(this);
        note.setTextColor(0xFFFF8A1E);
        note.setTextSize(20);
        note.setText(R.string.need_overlay);
        note.setVisibility(CrtService.canDraw(this) ? LinearLayout.GONE : LinearLayout.VISIBLE);

        Switch sw = new Switch(this);
        sw.setText(R.string.enabled);
        sw.setTextSize(28);
        sw.setTextColor(Color.WHITE);
        sw.setChecked(Prefs.enabled(this));
        sw.setOnCheckedChangeListener((b, on) -> {
            Prefs.setEnabled(this, on);
            if (on) CrtService.start(this);
        });

        RadioGroup group = new RadioGroup(this);
        group.setPadding(0, 32, 0, 0);
        Mask.Preset cur = Prefs.preset(this);
        for (Mask.Preset pr : Mask.Preset.values()) {
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());
            rb.setTag(pr);
            rb.setText(pr.label);
            rb.setTextSize(24);
            rb.setTextColor(Color.WHITE);
            rb.setFocusable(true);
            group.addView(rb);
            if (pr == cur) group.check(rb.getId());
        }
        group.setOnCheckedChangeListener((g, id) -> {
            Object t = g.findViewById(id).getTag();
            if (t instanceof Mask.Preset) Prefs.setPreset(this, (Mask.Preset) t);
        });

        label = new TextView(this);
        label.setTextSize(24);
        label.setTextColor(Color.WHITE);
        label.setPadding(0, 48, 0, 16);

        SeekBar bar = new SeekBar(this);
        bar.setMax(15);
        bar.setProgress(Prefs.tenths(this));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar b, int p, boolean fromUser) {
                Prefs.setTenths(MainActivity.this, p);
                show(p);
            }

            @Override
            public void onStartTrackingTouch(SeekBar b) {}

            @Override
            public void onStopTrackingTouch(SeekBar b) {}
        });
        show(bar.getProgress());

        root.addView(note);
        root.addView(sw);
        root.addView(group);
        root.addView(label);
        root.addView(bar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        setContentView(root);
        getWindow().setLayout(640, LinearLayout.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.END);
        sw.requestFocus();
    }

    private void show(int tenths) {
        float s = tenths / 10f;
        label.setText(getString(R.string.strength, Mask.percent(s)));
    }
}
