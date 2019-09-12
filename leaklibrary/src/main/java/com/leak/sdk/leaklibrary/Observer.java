package com.leak.sdk.leaklibrary;

import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.leak.sdk.leaklibrary.heap.AnalyzerServers;
import com.leak.sdk.leaklibrary.heap.ExcludedRefs;
import com.leak.sdk.leaklibrary.heap.HeapDump;
import com.leak.sdk.leaklibrary.heap.Reachability;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.leak.sdk.leaklibrary.Constants.HPROF_PATH;
import static com.leak.sdk.leaklibrary.Constants.THREAD_NAME;
import static com.leak.sdk.leaklibrary.Retryable.Result.RETRY;
import static com.leak.sdk.leaklibrary.Utils.checkNotNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

class Observer {
    public static final Observer DISABLED = new Observer();

    private Set<String> retainedKeys;
    private ReferenceQueue<Object> queue;
    private GcTrigger gcTrigger;
    private ObserverExecutor observerExecutor;
    private Context context;

    Observer init(Context context) {
        this.context = context;
        queue = new ReferenceQueue<>();
        retainedKeys = new CopyOnWriteArraySet<>();
        gcTrigger = GcTrigger.DEFAULT;
        observerExecutor = new AndroidExecutor();
        return this;
    }

    void addObserver(Object observer, String referenceName) {
        if (this == DISABLED) {
            return;
        }
        checkNotNull(observer, "activity");
        checkNotNull(referenceName, "referenceName");
        final long watchStartNanoTime = System.nanoTime();
        String key = UUID.randomUUID().toString();
        retainedKeys.add(key);
        final LeakWeakReference reference =
                new LeakWeakReference(observer, key, referenceName, queue);
        observerExecutor.execute(new Retryable() {
            @Override
            public Result run() {
                return checkRecycle(reference, watchStartNanoTime);
            }
        });
    }


    private Retryable.Result checkRecycle(final LeakWeakReference reference, final long watchStartNanoTime) {
        long gcStartNanoTime = System.nanoTime();
        long watchDurationMs = NANOSECONDS.toMillis(gcStartNanoTime - watchStartNanoTime);
        Log.d(THREAD_NAME, "0-" + isMainThread());
        removeWeaklyReachableReferences();
        if (recycle(reference)) {
            return Retryable.Result.DONE;
        }
        gcTrigger.runGc();
        removeWeaklyReachableReferences();
        if (!recycle(reference)) {
            long startDumpHeap = System.nanoTime();
            long gcDurationMs = NANOSECONDS.toMillis(startDumpHeap - gcStartNanoTime);

            //把文件保存在new  file的路径 方便后续分析取出
            String heapDumpFile = createDumpFile();
            if (null == heapDumpFile) {
                return RETRY;
            }
            long heapDumpDurationMs = NANOSECONDS.toMillis(System.nanoTime() - startDumpHeap);
            HeapDump.Builder heapDumpBuilder = new HeapDump.Builder();
            HeapDump heapDump = heapDumpBuilder.heapDumpFile(new File(heapDumpFile)).referenceKey(reference.key)
                    .referenceName(reference.name)
                    .watchDurationMs(watchDurationMs)
                    .gcDurationMs(gcDurationMs)
                    .heapDumpDurationMs(heapDumpDurationMs)
                    .excludedRefs(defaultExcludedRefs())
                    .computeRetainedHeapSize(true)
                    .reachabilityInspectorClasses(defaultReachabilityInspectorClasses())
                    .build();

            //向子进程发送dump文件  让子进程处理文件
            Log.d(THREAD_NAME, "1-" + isMainThread());
            AnalyzerServers.runAnalysis(context, heapDump);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, reference.name + "  有内存泄漏！开始分析... ", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return Retryable.Result.DONE;
    }

    private List<Class<? extends Reachability.Inspector>> defaultReachabilityInspectorClasses() {
        return Collections.emptyList();
    }

    private ExcludedRefs defaultExcludedRefs() {
        return ExcludedRefs.builder().build();
    }

    private String createDumpFile() {
        String state = android.os.Environment.getExternalStorageState();
        // 判断SdCard是否存在并且是可用的
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
            String hprofPath;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ssss", Locale.CHINA);
            String createTime = sdf.format(new Date(System.currentTimeMillis()));
            // 判断SdCard是否存在并且是可用的
            Log.d("保存", "保存成功!111" + isMainThread());

            File file = new File(Environment.getExternalStorageDirectory().getPath() + HPROF_PATH + context.getPackageName());
            if (!file.exists()) {
                file.mkdirs();
                Log.d("保存", "保存成功!222" + isMainThread());

            }
            hprofPath = file.getPath() + "/" + createTime + ".hprof";
            try {
                Log.d("保存", "保存成功!333" + isMainThread());

                Debug.dumpHprofData(hprofPath);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("保存", "保存失败");
                return hprofPath;
            }
            Log.d("保存", "保存成功!" + isMainThread());
            return hprofPath;
        } else {
            Log.d("保存", "no sdcard!");
            return null;
        }
    }

    private boolean recycle(LeakWeakReference reference) {
        return !retainedKeys.contains(reference.key);
    }

    private void removeWeaklyReachableReferences() {
        LeakWeakReference ref;
        while ((ref = (LeakWeakReference) queue.poll()) != null) {
            retainedKeys.remove(ref.key);
        }
    }

    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

}
