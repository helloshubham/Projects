package com.bpt.nt8ma.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.bpt.nt8ma.Adapters.ViewPagerAdapter;
import com.bpt.nt8ma.R;
import com.bpt.nt8ma.fragments.Forex;
import com.bpt.nt8ma.fragments.Futures;
import com.bpt.nt8ma.fragments.Stocks;
import com.bpt.nt8ma.helper.SQLiteHandler;
import com.bpt.nt8ma.helper.SessionManager;

import java.util.ArrayList;
import java.util.List;


public class Instruments extends AppCompatActivity {

    private SQLiteHandler db;
    private SessionManager session;
    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> titlesList = new ArrayList<>();
    public static List<String> orderList;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    public static int count;
    public static String executeOption;
    public static ArrayList stocksOrderList;
    public static ArrayList forexOrderList;
    public static ArrayList futuresOrderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruments);
        count =0;
        ViewPagerAdapter viewPagerAdapter;
        db = new SQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());
        orderList = new ArrayList<>();

        forexOrderList = new ArrayList<String>();
        futuresOrderList = new ArrayList<String>();
        stocksOrderList = new ArrayList<String>();

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        initialize();
        prepareDataResourece();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            executeOption = extras.getString("Option");
        }

        Log.v("Extra Value", executeOption);

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentList, titlesList);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void initialize() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("Select Instruments");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DataTable.class);
                startActivity(intent);
            }
        });

        viewPager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
    }

    private void prepareDataResourece() {
        fragmentList.add(new Forex());
        titlesList.add("Forex");

        fragmentList.add(new Stocks());
        titlesList.add("Stocks");

        fragmentList.add(new Futures());
        titlesList.add("Futures");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_instruments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout){
            logoutUser();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        // Launching the login activity
        Intent intent = new Intent(Instruments.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
