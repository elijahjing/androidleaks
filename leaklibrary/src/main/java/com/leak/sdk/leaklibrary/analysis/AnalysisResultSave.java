package com.leak.sdk.leaklibrary.analysis;

import android.os.Looper;
import android.util.Log;


import com.leak.sdk.leaklibrary.heap.AnalysisResult;
import com.leak.sdk.leaklibrary.heap.HeapDump;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.leak.sdk.leaklibrary.Constants.THREAD_NAME;


public class AnalysisResultSave {
    //保留解析数据
    public static void analysis(HeapDump heapDump, AnalysisResult result) {
        Log.d(THREAD_NAME, "4-" + isMainThread());
       // String leakInfo =  AndroidLeak.leakInfo(application, heapDump, result, true);
      //  Log.d("%s", leakInfo);

        boolean shouldSaveResult = result.leakFound || result.failure != null;
        Log.d(THREAD_NAME, "4---" + result.leakFound+"==="+(result.failure != null));

        if (shouldSaveResult) {
            heapDump = renameHeapdump(heapDump);
            saveResult(heapDump, result);
        }

    }

    private static boolean saveResult(HeapDump heapDump, AnalysisResult result) {
        File resultFile = new File(heapDump.heapDumpFile.getParentFile(),
                heapDump.heapDumpFile.getName() + ".result");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(resultFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(heapDump);
            oos.writeObject(result);
            return true;
        } catch (IOException e) {
            Log.e("AnalysisResultSave", "Could not save leak analysis result to disk.");
        }
        return false;
    }


    /**
     * 保留解析的结果
     *
     * @param heapDump
     * @return
     */
    private static HeapDump renameHeapdump(HeapDump heapDump) {
        String fileName =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS'.hprof'", Locale.CHINA).format(new Date());
        File newFile = new File(heapDump.heapDumpFile.getParent(), fileName);
        heapDump.heapDumpFile.renameTo(newFile);
        return heapDump.buildUpon().heapDumpFile(newFile).build();
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
