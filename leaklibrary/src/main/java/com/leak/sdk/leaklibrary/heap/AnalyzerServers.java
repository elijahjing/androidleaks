package com.leak.sdk.leaklibrary.heap;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.leak.sdk.leaklibrary.analysis.AnalysisResultSave;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static com.leak.sdk.leaklibrary.Constants.THREAD_NAME;

public final class AnalyzerServers extends HeapAnalyzerProsses implements AnalyzerProgressListener {
    private static String HEAPDUMP_EXTRA = "heap_dump_data";
    public static String TAG = AnalyzerServers.class.getSimpleName();

    public AnalyzerServers() {
        super(AnalyzerServers.class.getSimpleName());
    }

    @Override
    protected void onHandleIntentInForeground(@Nullable Intent intent) {
        if (intent == null) {
            Log.e(TAG, "HeapAnalyzerService received a null intent, ignoring.");
            return;
        }
        boolean isMainProcess = getApplicationContext().getPackageName().equals
                (getCurrentProcessName());
        Log.e(THREAD_NAME, "是否为主进程" + isMainProcess);
        Log.d(THREAD_NAME,"3-" +isMainThread());
        HeapDump heapDump = (HeapDump) intent.getSerializableExtra(HEAPDUMP_EXTRA);
        HeapAnalyzer heapAnalyzer =
                new HeapAnalyzer(heapDump.excludedRefs, this, heapDump.reachabilityInspectorClasses);
        AnalysisResult result = heapAnalyzer.checkForLeak(heapDump.heapDumpFile, heapDump.referenceKey,
                heapDump.computeRetainedHeapSize);
        AnalysisResultSave.analysis(heapDump, result);
    }

    public static void runAnalysis(Context context, HeapDump file) {
        setEnabledBlocking(context, AnalyzerServers.class, true);
        Intent intent = new Intent(context, AnalyzerServers.class);
        intent.putExtra(HEAPDUMP_EXTRA, file);
        ContextCompat.startForegroundService(context, intent);
        Log.d(THREAD_NAME,"2-" +isMainThread());
    }

    public static void setEnabledBlocking(Context appContext, Class<?> componentClass,
                                          boolean enabled) {
        ComponentName component = new ComponentName(appContext, componentClass);
        PackageManager packageManager = appContext.getPackageManager();
        int newState = enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED;
        packageManager.setComponentEnabledSetting(component, newState, DONT_KILL_APP);
    }

    @Override
    public void onProgressUpdate(Step step) {
        Log.d("Analysis in progress", step.name());
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
    /**
     * 获取当前进程名
     */
    private String getCurrentProcessName() {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService
                (Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }
}
