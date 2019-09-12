package com.leak.sdk.leaklibrary;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;

import static com.leak.sdk.leaklibrary.Retryable.Result.RETRY;
import static java.util.concurrent.TimeUnit.SECONDS;

public class AndroidExecutor implements ObserverExecutor {
    static final String LEAK_CANARY_THREAD_NAME = "androidLeak-dump";
    private Handler mainHandler;
    private Handler backgroundHandler;

    private  long initialDelayMillis;
    private  long maxBackoffFactor;
    private static final long DEFAULT_WATCH_DELAY_MILLIS = SECONDS.toMillis(5);

    public AndroidExecutor() {
        mainHandler = new Handler(Looper.getMainLooper());
        HandlerThread handlerThread = new HandlerThread(LEAK_CANARY_THREAD_NAME);
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
        this.initialDelayMillis = DEFAULT_WATCH_DELAY_MILLIS;
        maxBackoffFactor = Long.MAX_VALUE / DEFAULT_WATCH_DELAY_MILLIS;
    }
    @Override
    public void execute(Retryable retryable) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            waitForIdle(retryable, 0);
        } else {
            postWaitForIdle(retryable, 0);
        }
    }
    private void postWaitForIdle(final Retryable retryable, final int failedAttempts) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                waitForIdle(retryable, failedAttempts);
            }
        });
    }

    private void waitForIdle(final Retryable retryable, final int failedAttempts) {
        // This needs to be called from the main thread.
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                postToBackgroundWithDelay(retryable, failedAttempts);
                return false;
            }
        });
    }
    private void postToBackgroundWithDelay(final Retryable retryable, final int failedAttempts) {
        long exponentialBackoffFactor = (long) Math.min(Math.pow(2, failedAttempts), maxBackoffFactor);
        long delayMillis = initialDelayMillis * exponentialBackoffFactor;
        backgroundHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Retryable.Result result = retryable.run();
                if (result == RETRY) {
                    postWaitForIdle(retryable, failedAttempts + 1);
                }
            }
        }, delayMillis);
    }
}
