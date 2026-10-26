package io.github.michaelhalstead.crt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Off from outside the app:
 * am broadcast -a io.github.michaelhalstead.crt.OFF -n io.github.michaelhalstead.crt/.OffReceiver
 * It only ever switches the effect off; the running service sees the change and removes the layer.
 */
public class OffReceiver extends BroadcastReceiver {
    static final String ACTION = "io.github.michaelhalstead.crt.OFF";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) Prefs.setEnabled(context, false);
    }
}
