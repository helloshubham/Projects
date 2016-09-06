package com.bpt.nt8ma.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class BuyInstruments extends AppCompatActivity {

    private SQLiteHandler db;
    private SessionManager session;
    private List<Fragment> fragmentList = new ArrayList<>();
    private List<String> titlesList = new ArrayList<>();
    private ViewPager viewPager;
    private TabLayout tabLayout;
    public static int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_instruments);

        count =0;
        ViewPagerAdapter viewPagerAdapter;
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        initialize();
        prepareDataResourece();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentList, titlesList);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }
    private void initialize() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("Select instruments to buy");
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
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
