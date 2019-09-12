package com.leak.sdk.leaklibrary.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


import com.leak.sdk.leaklibrary.R;
import com.leak.sdk.leaklibrary.analysis.Leak;
import com.leak.sdk.leaklibrary.analysis.LoadLeaks;
import com.leak.sdk.leaklibrary.heap.AnalysisResult;

import java.util.ArrayList;
import java.util.List;

import static com.leak.sdk.leaklibrary.analysis.LoadLeaks.getLeakDirectoryProvider;


public class HeapActivity extends Activity implements MyAdapter.OnClick {

    public List<Leak> leaks;
    public static AnalysisResult result;

    MyAdapter myAdapter;
    ListView listView;
    Button delete;
    public static final String SHOW_DETAIL_EXTRA = "show_detail";


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoadLeaks.load(this, getLeakDirectoryProvider(this));
        setContentView(R.layout.heap_activity);
        delete = findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        LoadLeaks.DeleteFile(getLeakDirectoryProvider(HeapActivity.this));
                    }
                });
                leaks = new ArrayList<>();
                updateUi(leaks);
            }
        });
        listView = findViewById(R.id.listView);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadLeaks.forgetActivity();
    }

    public void updateUi(List<Leak> leaks) {
        this.leaks = leaks;
        if (leaks == null) {
            return;
        }
        if (myAdapter == null) {
            myAdapter = new MyAdapter(leaks, this);
            listView.setAdapter(myAdapter);
        } else {
            myAdapter.setData(leaks);
        }
        myAdapter.setOnDelete(this);
    }

    @Override
    public void delete(int pos) {
        deleteVisibleLeak(pos);
    }

    @Override
    public void onClick(int position) {
        Intent intent1 = new Intent(HeapActivity.this, HeapMainActivity.class);
        result = leaks.get(position).result;
        intent1.putExtra(SHOW_DETAIL_EXTRA, leaks.get(position).heapDump.referenceName);
        startActivity(intent1);
    }

    void deleteVisibleLeak(final int pos) {
        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                leaks.get(pos).heapDump.heapDumpFile.delete();
                leaks.get(pos).resultFile.delete();
                delete.post(new Runnable() {
                    @Override
                    public void run() {
                        leaks.remove(pos);
                        updateUi(leaks);
                    }
                });
            }
        });

    }
}


