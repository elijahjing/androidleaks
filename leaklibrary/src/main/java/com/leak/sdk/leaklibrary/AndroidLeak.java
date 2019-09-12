package com.leak.sdk.leaklibrary;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.util.Log;

import com.leak.sdk.leaklibrary.heap.AnalysisResult;
import com.leak.sdk.leaklibrary.heap.AnalyzerServers;
import com.leak.sdk.leaklibrary.heap.HeapDump;

import java.util.List;

import static android.content.pm.PackageManager.GET_SERVICES;
import static android.text.format.Formatter.formatShortFileSize;

public class AndroidLeak {
    public static Application application;
    public static Observer observer;
    public static void init(Context context) {
         application = (Application) context.getApplicationContext();
           observer = new Observer().init(application);
        application.registerActivityLifecycleCallbacks(new LeakLifecycle() {
            @Override
            public void onActivityDestroyed(Activity activity) {
                super.onActivityDestroyed(activity);
                observer.addObserver(activity, activity.getLocalClassName());
            }
        });
    }
    public  static void addObserver(Object o){
        observer.addObserver(o, o.getClass().getSimpleName());
    }
    /** Returns a string representation of the result of a heap analysis. */
    public static String leakInfo(Context context, HeapDump heapDump, AnalysisResult result,
                                  boolean detailed) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        String versionName = packageInfo.versionName;
        int versionCode = packageInfo.versionCode;
        String info = "In " + packageName + ":" + versionName + ":" + versionCode + ".\n";
        String detailedString = "";
        if (result.leakFound) {
            if (result.excludedLeak) {
                info += "* EXCLUDED LEAK.\n";
            }
            info += "* " + result.className;
            if (!heapDump.referenceName.equals("")) {
                info += " (" + heapDump.referenceName + ")";
            }
            info += " has leaked:\n" + result.leakTrace.toString() + "\n";
            if (result.retainedHeapSize != AnalysisResult.RETAINED_HEAP_SKIPPED) {
                info += "* Retaining: " + formatShortFileSize(context, result.retainedHeapSize) + ".\n";
            }
            if (detailed) {
                detailedString = "\n* Details:\n" + result.leakTrace.toDetailedString();
            }
        } else if (result.failure != null) {
            // We duplicate the library version & Sha information because bug reports often only contain
            // the stacktrace.
            info += "* FAILURE in " + 2 + " " + 2 + ":" + Log.getStackTraceString(
                    result.failure) + "\n";
        } else {
            info += "* NO LEAK FOUND.\n\n";
        }
        if (detailed) {
            detailedString += "* Excluded Refs:\n" + heapDump.excludedRefs;
        }

        info += "* Reference Key: "
                + heapDump.referenceKey
                + "\n"
                + "* Device: "
                + Build.MANUFACTURER
                + " "
                + Build.BRAND
                + " "
                + Build.MODEL
                + " "
                + Build.PRODUCT
                + "\n"
                + "* Android Version: "
                + Build.VERSION.RELEASE
                + " API: "
                + Build.VERSION.SDK_INT
                + " LeakCanary: "
                + "\n"
                + "* Durations: watch="
                + heapDump.watchDurationMs
                + "ms, gc="
                + heapDump.gcDurationMs
                + "ms, heap dump="
                + heapDump.heapDumpDurationMs
                + "ms, analysis="
                + result.analysisDurationMs
                + "ms"
                + "\n"
                + detailedString;

        return info;
    }
    public static volatile Boolean isInAnalyzerProcess;
    /**
     * Whether the current process is the process running the {@link AnalyzerServers}, which is
     * a different process than the normal app process.
     */
    public static boolean isInAnalyzerProcess(Context context) {

        return  isInServiceProcess(context, AnalyzerServers.class);
    }

    public static boolean isInServiceProcess(Context context, Class<? extends Service> serviceClass) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), GET_SERVICES);
        } catch (Exception e) {
            return false;
        }
        String mainProcess = packageInfo.applicationInfo.processName;

        ComponentName component = new ComponentName(context, serviceClass);
        ServiceInfo serviceInfo;
        try {
            serviceInfo = packageManager.getServiceInfo(component, 0);
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }

        if (serviceInfo.processName.equals(mainProcess)) {
            return false;
        }

        int myPid = android.os.Process.myPid();
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningAppProcessInfo myProcess = null;
        List<ActivityManager.RunningAppProcessInfo> runningProcesses;
        try {
            runningProcesses = activityManager.getRunningAppProcesses();
        } catch (SecurityException exception) {
            return false;
        }
        if (runningProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo process : runningProcesses) {
                if (process.pid == myPid) {
                    myProcess = process;
                    break;
                }
            }
        }
        if (myProcess == null) {
            return false;
        }

        return myProcess.processName.equals(serviceInfo.processName);
    }


}
