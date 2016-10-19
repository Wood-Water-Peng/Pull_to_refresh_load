package com.example.pj.onlayout_research;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private PtrClassicContainer mPtrContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        mPtrContainer= (PtrClassicContainer) findViewById(R.id.container);
        mPtrContainer.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin() {
                mPtrContainer.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtrContainer.refreshComplete();
                    }
                },1000);
            }
        });
    }

}
