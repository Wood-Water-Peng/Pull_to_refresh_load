package com.example.pj.onlayout_research;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pj.ptr_lib.container.PtrClassicContainer;
import com.example.pj.ptr_lib.container.PtrContainer;
import com.example.pj.ptr_lib.container.PtrHandler;
import com.example.pj.ptr_lib.foot.DefaultFootView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private PtrClassicContainer mPtrContainer;
    private ListView mListView;
    private ArrayAdapter mAdapter;
    private List<String> data = new ArrayList<String>();
    private int count = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        mPtrContainer = (PtrClassicContainer) findViewById(R.id.container);
        mListView = (ListView) findViewById(R.id.content);
        DefaultFootView defaultFootView = new DefaultFootView(this);
        mListView.addFooterView(defaultFootView);
        mPtrContainer.setFootUIHanlder(defaultFootView);
        mPtrContainer.setListView(mListView);


        mAdapter = new ArrayAdapter<String>(this, R.layout.item);

        initData();
        mAdapter.addAll(data);
        mListView.setAdapter(mAdapter);

        mPtrContainer.setOnLoadListener(new PtrContainer.OnLoadListener() {
            @Override
            public void onLoad() {
                mPtrContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.add("Title_0" + count);
                        count++;
                        mAdapter.notifyDataSetChanged();
                        mPtrContainer.loadMoreCompleted();
                    }
                }, 1000);
            }
        });

        mPtrContainer.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin() {
                mPtrContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrContainer.refreshComplete();
                    }
                }, 4000);
            }
        });

        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "list_height:" + mListView.getMeasuredHeight());
                Log.i(TAG, "list_top:" + mListView.getTop());
                Log.i(TAG, "list_bottom:" + mListView.getHeight());
            }
        }, 1000);
    }

    private void initData() {
        data.add("Title_01");
        data.add("Title_02");
        data.add("Title_03");
        data.add("Title_04");
        data.add("Title_05");
        data.add("Title_06");
        data.add("Title_07");
    }

}
