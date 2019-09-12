package com.leak.sdk.leaklibrary.heap;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public abstract class HeapAnalyzerProsses  extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public HeapAnalyzerProsses(String name) {
        super(name);
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        onHandleIntentInForeground(intent);
    }
    protected abstract void onHandleIntentInForeground(@Nullable Intent intent);

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
    @Override
    public void onCreate() {
        super.onCreate();
/*        showForegroundNotification(100, 0, true,
                getString(R.string.leak_canary_notification_foreground_text));*/
    }

  /*  protected void showForegroundNotification(int max, int progress, boolean indeterminate,
                                              String contentText) {
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(notificationContentTitleResId))
                .setContentText(contentText)
                .setProgress(max, progress, indeterminate);
        Notification notification = LeakCanaryInternals.buildNotification(this, builder);
        startForeground(notificationId, notification);
    }*/

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
