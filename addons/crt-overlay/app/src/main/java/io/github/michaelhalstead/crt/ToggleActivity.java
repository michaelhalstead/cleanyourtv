package io.github.michaelhalstead.crt;

import android.app.Activity;
import android.os.Bundle;

/** Flips the effect on or off with no window, for a remote button (io.github.michaelhalstead.crt/.ToggleActivity). */
public class ToggleActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs.setEnabled(this, !Prefs.enabled(this));
        CrtService.start(this);
        finish();
    }
}
