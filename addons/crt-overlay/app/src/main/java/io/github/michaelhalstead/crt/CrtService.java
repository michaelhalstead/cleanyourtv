package io.github.michaelhalstead.crt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Holds the CRT layer: one full-screen window over every app that never takes focus, keys or touches. The view
 * draws one prebuilt bitmap and is invalidated only when the preset or strength changes.
 */
public class CrtService extends Service {
    private static final String CHANNEL = "crt";

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private WindowManager wm;
    private MaskView layer;
    private int generation;
    /** Kept in a field: SharedPreferences holds its listeners weakly. */
    private final SharedPreferences.OnSharedPreferenceChangeListener prefsListener = (p, key) -> apply();

    /** Draws the mask. Not invalidated after setBitmap. */
    private static final class MaskView extends View {
        private Bitmap bitmap;

        MaskView(Context c) {
            super(c);
        }

        void setBitmap(Bitmap b) {
            bitmap = b;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (bitmap != null) canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    static void start(Context c) {
        if (!Prefs.enabled(c) || !canDraw(c)) return;
        try {
            c.startForegroundService(new Intent(c, CrtService.class));
        } catch (RuntimeException e) {
            // Android 12+ refuses a foreground start from the background; the next open of the app starts it.
            Log.w(Prefs.TAG, "service not started: " + e.getMessage());
        }
    }

    static boolean canDraw(Context c) {
        return Settings.canDrawOverlays(c);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = getSystemService(WindowManager.class);
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL, getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_MIN));
        Notification n = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_foreground)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, n);
        }
        Prefs.of(this).registerOnSharedPreferenceChangeListener(prefsListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        apply();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Prefs.of(this).unregisterOnSharedPreferenceChangeListener(prefsListener);
        generation++;
        removeLayer();
        worker.shutdownNow();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void apply() {
        if (!Prefs.enabled(this) || !canDraw(this)) {
            removeLayer();
            stopSelf();
            return;
        }
        if (layer == null) {
            MaskView v = new MaskView(this);
            try {
                wm.addView(v, params());
                layer = v;
            } catch (RuntimeException e) {
                Log.w(Prefs.TAG, "layer not shown: " + e.getMessage());
                stopSelf();
                return;
            }
        }
        DisplayMetrics m = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(m);
        int w = m.widthPixels, h = m.heightPixels;
        float s = Prefs.strength(this);
        Mask.Preset pr = Prefs.preset(this);
        int gen = ++generation;
        worker.execute(() -> {
            Bitmap b = Bitmap.createBitmap(Mask.build(w, h, pr, s), w, h, Bitmap.Config.ARGB_8888);
            main.post(() -> {
                if (gen == generation && layer != null) layer.setBitmap(b);
            });
        });
    }

    private void removeLayer() {
        if (layer == null) return;
        try {
            wm.removeView(layer);
        } catch (RuntimeException ignored) {
            // Already gone with the window manager.
        }
        layer = null;
    }

    private static WindowManager.LayoutParams params() {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        if (Build.VERSION.SDK_INT >= 28) {
            p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        return p;
    }
}
