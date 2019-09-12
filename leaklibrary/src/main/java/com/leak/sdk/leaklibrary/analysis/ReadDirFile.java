package com.leak.sdk.leaklibrary.analysis;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static com.leak.sdk.leaklibrary.Constants.HPROF_PATH;

public class ReadDirFile {

    private final Context context;
    private final int maxStoredHeapDumps;
    private boolean writeExternalStorageGranted;
    private static final int DEFAULT_MAX_STORED_HEAP_DUMPS = 7;

    public ReadDirFile(Context context) {
        this(context, DEFAULT_MAX_STORED_HEAP_DUMPS);
    }

    public ReadDirFile(Context context, int maxStoredHeapDumps) {
        if (maxStoredHeapDumps < 1) {
            throw new IllegalArgumentException("maxStoredHeapDumps must be at least 1");
        }
        this.context = context.getApplicationContext();
        this.maxStoredHeapDumps = maxStoredHeapDumps;
    }

    public List<File> listFiles(FilenameFilter filter) {
        if (!hasStoragePermission()) {
            Log.e("hasStoragePermission", " 没有权限");
        }
        List<File> files = new ArrayList<>();

        File[] externalFiles = externalStorageDirectory().listFiles(filter);
        if (externalFiles != null) {
            files.addAll(Arrays.asList(externalFiles));
        }
        return files;
    }

    @TargetApi(M)
    private boolean hasStoragePermission() {
        if (SDK_INT < M) {
            return true;
        }
        if (writeExternalStorageGranted) {
            return true;
        }
        writeExternalStorageGranted =
                context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
        return writeExternalStorageGranted;
    }

    private File externalStorageDirectory() {
        return new File(Environment.getExternalStorageDirectory().getPath() + HPROF_PATH +  context.getPackageName());
    }
    public void clearLeakDirectory() {
        List<File> allFilesExceptPending = listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return !filename.endsWith("leack");
            }
        });
        for (File file : allFilesExceptPending) {
            boolean deleted = file.delete();
            if (!deleted) {
                Log.e("删除不了",file.getAbsolutePath());
            }
        }
    }


}
