package com.leak.sdk.leaklibrary.analysis;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.leak.sdk.leaklibrary.activity.HeapActivity;
import com.leak.sdk.leaklibrary.heap.AnalysisResult;
import com.leak.sdk.leaklibrary.heap.HeapDump;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class LoadLeaks implements Runnable {

    private HeapActivity heapActivity;
    private final ReadDirFile readDirFile;
    private final Handler mainHandler;
    private static final List<LoadLeaks> inFlight = new ArrayList<>();

    private static final Executor backgroundExecutor = Executors.newSingleThreadExecutor();


    public static ReadDirFile getLeakDirectoryProvider(Context context) {
        return new ReadDirFile(context);
    }
    public static void DeleteFile ( ReadDirFile readDirFile) {
        readDirFile.clearLeakDirectory();
    }
    public static void DeleteFileOne ( ReadDirFile readDirFile) {
        readDirFile.clearLeakDirectory();
    }
    public static void load(HeapActivity activity, ReadDirFile readDirFile) {
        LoadLeaks loadLeaks = new LoadLeaks(activity, readDirFile);
        inFlight.add(loadLeaks);
        backgroundExecutor.execute(loadLeaks);
    }

    public static void forgetActivity() {
        for (LoadLeaks loadLeaks : inFlight) {
            loadLeaks.heapActivity = null;
        }
        inFlight.clear();
    }


    public LoadLeaks(HeapActivity activity, ReadDirFile readDirFile) {
        this.heapActivity = activity;
        this.readDirFile = readDirFile;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        final List<Leak> leaks = new ArrayList<>();
        List<File> files = readDirFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".result");
            }
        });
        for (File resultFile : files) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(resultFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                HeapDump heapDump = (HeapDump) ois.readObject();
                AnalysisResult result = (AnalysisResult) ois.readObject();
                leaks.add(new Leak(heapDump, result, resultFile));
            } catch (IOException | ClassNotFoundException e) {
                boolean deleted = resultFile.delete();
                if (deleted) {
                    Log.d("Could no.", resultFile.getName());
                } else {
                    Log.d("Could no.", resultFile.getName());

                }
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
        Collections.sort(leaks, new Comparator<Leak>() {
            @Override
            public int compare(Leak lhs, Leak rhs) {
                return Long.valueOf(rhs.resultFile.lastModified())
                        .compareTo(lhs.resultFile.lastModified());
            }
        });
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                inFlight.remove(LoadLeaks.this);
                if (heapActivity != null) {
                    heapActivity.updateUi(leaks);
                }
            }
        });
    }

}