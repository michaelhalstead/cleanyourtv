package io.github.michaelhalstead.crt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Restarts the layer after a reboot or an update of this app, if the effect was on. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String a = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(a) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)) {
            CrtService.start(context);
        }
    }
}
