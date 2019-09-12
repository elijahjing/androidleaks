package com.leak.sdk.leaklibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.leak.sdk.leaklibrary.R;
import com.leak.sdk.leaklibrary.heap.AnalysisResult;

import static com.leak.sdk.leaklibrary.activity.HeapActivity.SHOW_DETAIL_EXTRA;


public class HeapMainActivity extends AppCompatActivity {

    public AnalysisResult analysisResult;
    public String referenceName="";
    MainAdapter myAdapter;
    ListView listView;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heap_activity_main);
        View view=findViewById(R.id.root);
        Intent intent = getIntent();
        if (intent.hasExtra(SHOW_DETAIL_EXTRA)) {
            referenceName = intent.getStringExtra(SHOW_DETAIL_EXTRA);
        }
        listView = findViewById(R.id.listview_main);
        analysisResult= HeapActivity.result;
        updateUi();
    }



    public void updateUi() {

        if (analysisResult == null||analysisResult.leakTrace==null) {
            return;
        }

        if (myAdapter == null) {
            myAdapter = new MainAdapter(analysisResult.leakTrace, this,referenceName);
            listView.setAdapter(myAdapter);
        } else {
            myAdapter.setData(analysisResult.leakTrace,referenceName);
        }
    }

}
