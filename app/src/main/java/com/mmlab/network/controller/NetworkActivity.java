package com.mmlab.network.controller;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.mmlab.network.R;
import com.mmlab.network.model.WifiDoc;
import com.mmlab.network.view.DividerItemDecoration;
import com.mmlab.network.view.HotspotDialog;
import com.mmlab.network.view.NetworkDialog;
import com.mmlab.network.view.WifiDocDialog;

import java.util.ArrayList;
import java.util.List;

public class NetworkActivity extends AppCompatActivity {

    private static final String TAG = NetworkActivity.class.getName();

    Toolbar toolbar;
    NetworkManager networkManager;
    WifiDocAdapter wifiDocAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    List<WifiDoc> wifiDocs = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton imageButton_network = (ImageButton) toolbar.findViewById(R.id.imageButton_network);
        imageButton_network.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NetworkDialog dialog = new NetworkDialog();
                dialog.show(getFragmentManager(), "networkDialog");
            }
        });

        ImageButton imageButton_hotspot = (ImageButton) toolbar.findViewById(R.id.imageButton_hotspot);
        imageButton_hotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HotspotDialog dialog = new HotspotDialog();
                dialog.show(getFragmentManager(), "hotspotDialog");
            }
        });

        networkManager = new NetworkManager(getApplicationContext());
        networkManager.setOnResultsListener(new NetworkManager.OnResultsListener() {
            public void onScanResults(ArrayList<ScanResult> scanResults) {

            }

            public void onWifiDocs(ArrayList<WifiDoc> wifiDocs) {
                Log.d(TAG, "WifiDoc Number : " + wifiDocs.size());
                NetworkActivity.this.wifiDocs.clear();
                NetworkActivity.this.wifiDocs.addAll(wifiDocs);
                wifiDocAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                networkManager.startScan();
            }
        });
        wifiDocAdapter = new WifiDocAdapter(getApplicationContext(), wifiDocs);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
        wifiDocAdapter.setOnItemClickLitener(new WifiDocAdapter.OnItemClickLitener() {
            public void onItemClick(View view, int position) {
                WifiDocDialog dialog = new WifiDocDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable("wifidoc", wifiDocs.get(position));
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "wifidocDialog");
            }

            public void onItemLongClick(View view, int position) {

            }
        });
        recyclerView.setAdapter(wifiDocAdapter);

    }

    protected void onStart() {
        super.onStart();
        networkManager.registerReceiver();
    }

    protected void onStop() {
        super.onStop();
        networkManager.unregisterReceiver();
    }

    protected void onDestroy() {
        super.onDestroy();
        networkManager.release();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_network, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
