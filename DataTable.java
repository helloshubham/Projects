package com.bpt.nt8ma.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bpt.nt8ma.AppConfig;
import com.bpt.nt8ma.AppController;
import com.bpt.nt8ma.AsyncTask.FetchDemoDataTask;
import com.bpt.nt8ma.AsyncTask.FetchLiveDataTask;
import com.bpt.nt8ma.R;
import com.bpt.nt8ma.helper.SQLiteHandler;
import com.bpt.nt8ma.helper.SessionManager;
import com.bpt.nt8ma.util.IabHelper;
import com.bpt.nt8ma.util.IabResult;
import com.bpt.nt8ma.util.Inventory;
import com.bpt.nt8ma.util.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DataTable extends AppCompatActivity {
    public static TableLayout tv;
    public static String jsonString;
    public static ProgressBar progressBar;
    private String demoUrlParameters;
    private String liveUrlParameters;
    private SQLiteHandler db;
    private SessionManager session;
    private String initialInstrumentList;
    public static List<String> selectedDemoInstruments;
    //public static List<String> dbUpdateInstruments;
    private String subscription;
    private Timer timer;
    private TimerTask doAsynchronousTask;
    private static final String TAG = "DataTable";
    public static final String MY_PREFS_NAME = "BestTradePro";
    private List<String> tempUrlList;
    public static MediaPlayer mp;
    private LinearLayout linearLayout;

    boolean mSubscribed = false;
    boolean mSubscribedTo35 = false;
    boolean mSubscribedTo70 = false;
    boolean mSubscribedTo100 = false;
    boolean mSubscribedTo105 = false;
    boolean mSubscribedTo140 = false;
    boolean mSubscribedTo170 = false;
    boolean mSubscribedTo175 = false;
    boolean mSubscribedTo200 = false;

    //SKU for our subscription (Monthly Live Account)
    static final String SKU_LIVE_MONTHLY = "monthly_live_acc";
    static final String SKU_LIVE_MONTHLY_35 = "monthly_live_acc_35";
    static final String SKU_LIVE_MONTHLY_70 = "monthly_live_acc_70";
    static final String SKU_LIVE_MONTHLY_100 = "monthly_live_acc_100";
    static final String SKU_LIVE_MONTHLY_105 = "monthly_live_acc_105";
    static final String SKU_LIVE_MONTHLY_140 = "monthly_live_acc_140";
    static final String SKU_LIVE_MONTHLY_170 = "monthly_live_acc_170";
    static final String SKU_LIVE_MONTHLY_175 = "monthly_live_acc_175";
    static final String SKU_LIVE_MONTHLY_200 = "monthly_live_acc_200";


    private IabHelper mHelper;
    private ProgressDialog pDialog;
    private String email;
    private TextView wifiTextView;
    public static Hashtable tempSoundFlag = new Hashtable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_table);

        initializeHashTable();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        linearLayout = (LinearLayout) findViewById(R.id.root_layout);
        subscription = loadData();
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqBGWRiaLAK2eK7vVbl8h2JdQ9nggZ/6H/5kABV//rtQ5Ng2QPf+/vfwR3lxwDWaCYmoxUHfwglkGiyzTHCYM8vSQrHtSYSBqkhyatLrZmHSXHiWUFWsoDxcnS6PPKzU4nTNZ4Yg26t8OUmmdqCn6cxk191g6+78rlJ09cRgYCsXZCUSRbmy3w672lq9apd9iDgvUaER5VbqMjHIOyKNSMfccSRJ9OHBDJJ5VAooWNai4V9Ar7glL9XAedgSqvnoLqwABRA7tZ0EbQqxJJgokww6Is0th5QBGw26I2HX+WV4mPk5Ndbx2H7gq6F6VlzktWZYyHYGsD19g5Gg29DUB0QIDAQAB";

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        tv = (TableLayout) findViewById(R.id.table);
        //wifiTextView = (TextView) findViewById(R.id.textView2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar_dataTable);
        toolbar.setTitle("Best Pro Trade Signal");
        setSupportActionBar(toolbar);

        db = new SQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
        }
        HashMap<String, String> user = db.getUserDetails();
        email = user.get("email");
        //expiryUpdateInDb(email, "monthly_live_acc_35");
        //getUrlList(email);

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progressBar.setMax(100);

        Bundle extras = getIntent().getExtras();
        //DemoDataSymbols
        if (extras != null) {
            demoUrlParameters = extras.getString("UrlValue");
            SharedPreferences.Editor editor = SessionManager.pref.edit();
            editor.putString("SelectedInstrument", demoUrlParameters);
            editor.commit();
        } else {
            String[] temp = new String[]{
                    "'" + "AUDUSD" + "'",
                    "'" + "EURCHF" + "'",
                    "'" + "EURGBP" + "'",
                    "'" + "EURJPY" + "'",
                    "'" + "EURUSD" + "'",
                    "'" + "GBPUSD" + "'",
                    "'" + "USDCAD" + "'",
                    "'" + "USDCHF" + "'",
                    "'" + "USDJPY" + "'",
                    "'" + "6E 06-16" + "'",
                    "'" + "CL 04-16" + "'",
                    "'" + "ES 09-16" + "'",
                    "'" + "FDXM 09-16" + "'",
                    "'" + "GC 12-16" + "'",
                    "'" + "NQ 09-16" + "'",
                    "'" + "YM 09-16" + "'",
                    "'" + "ZB 06-16" + "'",
                    "'" + "ZN 09-16" + "'",
                    "'" + "AAPL" + "'",
                    "'" + "BA" + "'",
                    "'" + "CAT" + "'",
                    "'" + "MSFT" + "'",
                    "'" + "XOM" + "'"
            };
            selectedDemoInstruments = new ArrayList<>(Arrays.asList(temp));
            //demoUrlParameters = String.valueOf(selectedDemoInstruments);
            //Log.v("Whether Else",demoUrlParameters);
            initialInstrumentList = Arrays.toString(temp);
            demoUrlParameters = SessionManager.pref.getString("SelectedInstrument", initialInstrumentList);
        }

        //Log.v("Data from Preference", SessionManager.pref.getString("SelectedInstrument", initialInstrumentList));
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_data_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
        } else if (id == R.id.addInstruments) {
            Intent intent = new Intent(this, Instruments.class);
            intent.putExtra("Option", "SelectInstruments");
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLiveDataButtonClicked(View arg0) {
        Intent intent = new Intent(this,Instruments.class);
        intent.putExtra("Option", "BuyInstruments");
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkAvailable()) {
            int message = R.string.empty_data_list;
            TextView rowTextView = new TextView(this);
            rowTextView.setText(message);
            rowTextView.setGravity(Gravity.CENTER);
            rowTextView.setTextSize(22);
            if (!rowTextView.equals(message))
            linearLayout.addView(rowTextView, 1);
            //wifiTextView.setText(message);
            progressBar.setVisibility(!isNetworkAvailable() ?
                    View.GONE : View.VISIBLE);
        } else {
            if (subscription.equals("Live") || email.equals("hellosatyam.in@gmail.com")) {
                Log.v(TAG, "After Live IAB");
                //getUrlList(email);
                getUrlList(email, new VolleyCallback(){
                    @Override
                    public void onSuccess(String result){
                        FetchLiveDataTask fetchLiveDataTask = new FetchLiveDataTask(getApplicationContext());
                        fetchLiveDataTask.execute(result);
                        callLiveAsynchronousTask();

                    }
                });


            } else if (subscription.equals("Demo")) {
                Log.v(TAG, "After Demo IAB");
                FetchDemoDataTask fetchDemoDataTask = new FetchDemoDataTask(this);
                fetchDemoDataTask.execute(demoUrlParameters);
                callDemoAsynchronousTask();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(timer != null) {
            doAsynchronousTask.cancel();
            timer.cancel();
            doAsynchronousTask = null;
            timer.purge();
            timer = null;
        }
    }

    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        // Launching the login activity
        Intent intent = new Intent(DataTable.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void callLiveAsynchronousTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            FetchLiveDataTask performBackgroundTask = new FetchLiveDataTask();
                            performBackgroundTask.execute(liveUrlParameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1000);
    }

    private void callDemoAsynchronousTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            FetchDemoDataTask fetchDemoDataTask = new FetchDemoDataTask();
                            fetchDemoDataTask.execute(demoUrlParameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1000);
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            // Do we have the monthly_live_acc_35?
                Purchase monthlyLiveAcc35 = inventory.getPurchase(SKU_LIVE_MONTHLY_35);
            mSubscribedTo35 = (monthlyLiveAcc35 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc35));
            Log.d(TAG, "User " + (mSubscribedTo35 ? "HAS" : "DOES NOT HAVE")
                    + " monthly live account_35.");
            if (mSubscribedTo35){
                subscription = "Live";
                saveData();
            }
            else{

                if(getSkuSpStatus(SKU_LIVE_MONTHLY_35).equals("Live"))
                {
                    setSkuSpStatus(SKU_LIVE_MONTHLY_35);
                    expiryUpdateInDb(email, SKU_LIVE_MONTHLY_35);
                }
            }

            // Do we have the monthly_live_acc_70?
            Purchase monthlyLiveAcc70 = inventory.getPurchase(SKU_LIVE_MONTHLY_70);
            mSubscribedTo70 = (monthlyLiveAcc70 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc70));
            Log.d(TAG, "User " + (mSubscribedTo70 ? "HAS" : "DOES NOT HAVE")
                    + " monthly live account_70.");
            if (mSubscribedTo70){
                subscription = "Live";
                saveData();
            }
            else{
                //Update in db that this subscription has expired/cancelled.
                if(getSkuSpStatus(SKU_LIVE_MONTHLY_70).equals("Live"))
                {
                    setSkuSpStatus(SKU_LIVE_MONTHLY_70);
                    expiryUpdateInDb(email, SKU_LIVE_MONTHLY_70);
                }
            }

            // Do we have the monthly_live_acc_100?
            Purchase monthlyLiveAcc100 = inventory.getPurchase(SKU_LIVE_MONTHLY_100);
            mSubscribedTo100 = (monthlyLiveAcc100 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc100));
            Log.d(TAG, "User " + (mSubscribedTo100 ? "HAS" : "DOES NOT HAVE")
                    + " monthly live account_100.");
            if (mSubscribedTo100){
                subscription = "Live";
                saveData();
            }
            else{
                //Update in db that this subscription has expired/cancelled.
                if(getSkuSpStatus(SKU_LIVE_MONTHLY_100).equals("Live"))
                {
                    setSkuSpStatus(SKU_LIVE_MONTHLY_100);
                    expiryUpdateInDb(email, SKU_LIVE_MONTHLY_100);
                }
            }

            // Do we have the monthly_live_acc_105?
            Purchase monthlyLiveAcc105 = inventory.getPurchase(SKU_LIVE_MONTHLY_105);
            mSubscribedTo105 = (monthlyLiveAcc105 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc105));
            Log.d(TAG, "User " + (mSubscribedTo105 ? "HAS" : "DOES NOT HAVE")
                    + " monthly live account_105.");
            if (mSubscribedTo105){
                subscription = "Live";
                saveData();
            }
            else{
                //Update in db that this subscription has expired/cancelled.
                if(getSkuSpStatus(SKU_LIVE_MONTHLY_105).equals("Live"))
                {
                    setSkuSpStatus(SKU_LIVE_MONTHLY_105);
                    expiryUpdateInDb(email, SKU_LIVE_MONTHLY_105);
                }
            }

            // Do we have the monthly_live_acc_140?
            Purchase monthlyLiveAcc140 = inventory.getPurchase(SKU_LIVE_MONTHLY_140);
            mSubscribedTo140 = (monthlyLiveAcc140 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc140));
            Log.d(TAG, "User " + (mSubscribedTo140 ? "HAS" : "DOES NOT HAVE")
                    + " monthly live account_140.");
            if (mSubscribedTo140){
                subscription = "Live";
                saveData();
            }
            else{
                //Update in db that this subscription has expired/cancelled.
                if(getSkuSpStatus(SKU_LIVE_MONTHLY_140).equals("Live"))
                {
                    setSkuSpStatus(SKU_LIVE_MONTHLY_140);
                    expiryUpdateInDb(email, SKU_LIVE_MONTHLY_140);
                }
            }

            // Do we have the monthly_live_acc_170?
            Purchase monthlyLiveAcc170 = inventory.getPurchase(SKU_LIVE_MONTHLY_170);
            mSubscribedTo170 = (monthlyLiveAcc170 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc170));
            Log.d(TAG, "User " + (mSubscribedTo170 ? "HAS" : "DOES NOT HAVE")
                    + " monthly live account_170.");
            if (mSubscribedTo170){
                subscription = "Live";
                saveData();
            }
            else{
                //Update in db that this subscription has expired/cancelled.
                if(getSkuSpStatus(SKU_LIVE_MONTHLY_170).equals("Live"))
                {
                    setSkuSpStatus(SKU_LIVE_MONTHLY_170);
                    expiryUpdateInDb(email, SKU_LIVE_MONTHLY_170);
                }
            }

            // Do we have the monthly_live_acc_175?
            Purchase monthlyLiveAcc175 = inventory.getPurchase(SKU_LIVE_MONTHLY_175);
            mSubscribedTo175 = (monthlyLiveAcc175 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc175));
            Log.d(TAG, "User " + (mSubscribedTo175 ? "HAS" : "DOES NOT HAVE")
                    + " monthly live account_175.");
            if (mSubscribedTo175){
                subscription = "Live";
                saveData();
            }
            else{
                //Update in db that this subscription has expired/cancelled.
               if(getSkuSpStatus(SKU_LIVE_MONTHLY_175).equals("Live"))
                {
                    setSkuSpStatus(SKU_LIVE_MONTHLY_175);
                    expiryUpdateInDb(email, SKU_LIVE_MONTHLY_175);
                }
            }

            // Do we have the monthly_live_acc_200?
            Purchase monthlyLiveAcc200 = inventory.getPurchase(SKU_LIVE_MONTHLY_200);
            mSubscribedTo200 = (monthlyLiveAcc200 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc200));
            Log.d(TAG, "User " + (mSubscribedTo200 ? "HAS" : "DOES NOT HAVE")
                    + " monthly live account_200.");
            if (mSubscribedTo200) {
                subscription = "Live";
                saveData();
            }
            else{
                //Update in db that this subscription has expired/cancelled.
                if(getSkuSpStatus(SKU_LIVE_MONTHLY_200).equals("Live"))
                {
                    setSkuSpStatus(SKU_LIVE_MONTHLY_200);
                    expiryUpdateInDb(email, SKU_LIVE_MONTHLY_200);
                }

            }

            if(!mSubscribedTo35 && !mSubscribedTo70 && !mSubscribedTo100 && !mSubscribedTo105 && !mSubscribedTo140 && !mSubscribedTo170 && !mSubscribedTo175 && !mSubscribedTo200)
            {
                Log.v("NoSubscriptionPurchased", "TRUE");
                subscription = "Demo";
                saveData();
            }

            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    private boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    private void complain(String message) {
        Log.e(TAG, "****  Error: " + message);
        alert("Error: " + message);
    }

    private void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    private void saveData() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("Subscription", subscription);
        editor.apply();
        Log.d(TAG, "Saved data: subscribe = " + subscription);
    }

    private String loadData() {
        //subscription = SessionManager.pref.getString("Subscription", "Demo");
        /*SharedPreferences sp = getPreferences(MODE_PRIVATE);
        subscription = sp.getString("Subscription", "Demo");*/

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String subPref = prefs.getString("Subscription", "Demo");

        Log.d(TAG, "Loaded data: = " + subPref);
        return subPref;
    }

    private void expiryUpdateInDb(final String email, final String SKU){

        // Tag used to cancel the request
        String tag_string_req = "db_expiry_Update";

        pDialog.setMessage("Updating ...");
        showDialog();

        final StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                AppConfig.URL_EXPIRY_UPDATE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Expiry Update Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        Toast.makeText(getApplicationContext(), "Successful!", Toast.LENGTH_LONG).show();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Updation Error: " + error.getMessage());
                if(error.getMessage().equals(null))
                {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                    hideDialog();
                }
                else if(error.getMessage().contains("ENETUNREACH")){
                    Toast.makeText(getApplicationContext(),
                            "Please check your internet connection. Internet connection not available", Toast.LENGTH_LONG).show();
                    hideDialog();
                }
                else if(error.getMessage().contains("ETIMEDOUT")){
                    Toast.makeText(getApplicationContext(),
                            "Connection time out. Please restart the app.", Toast.LENGTH_LONG).show();
                    hideDialog();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            }
        })

        {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("subType", SKU);
                return params;
            }
        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void getUrlList(final String email, final VolleyCallback callback){
        // Tag used to cancel the request
        String tag_string_req = "req_symbols";

        pDialog.setMessage("Fetching Symbols ...");
        showDialog();

        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                AppConfig.URL_GET_SYMBOLS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Symbol Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        JSONObject user = jObj.getJSONObject("user");
                        liveUrlParameters = user.getString("all");
                        liveUrlParameters = liveUrlParameters.trim();

                        Log.v("LIVEString",liveUrlParameters);

                        tempUrlList = new ArrayList<String>(Arrays.asList(liveUrlParameters.split(", ")));

                        Log.v("LIVEArrayList",tempUrlList.toString());

                        if (tempUrlList.contains("Buy all forex")){
                            addAllForex();
                        }
                        if (tempUrlList.contains("Buy all stocks")) {
                            addAllStocks();
                        }
                        if (tempUrlList.contains("Buy all futures")) {
                            addAllFutures();
                        }

                        String str = "";
                        for (int i = 0; i < tempUrlList.size(); i++)
                            str += "\'" + tempUrlList.get(i).toString() + "\'" + ",";

                        if (str != null && str.length() > 0 && str.charAt(str.length()-1)==',') {
                            str = str.substring(0, str.length()-1);
                        }

                        List<String> urlList = new ArrayList<String>(Arrays.asList(str.split(",")));
                        liveUrlParameters = urlList.toString();
                        callback.onSuccess(liveUrlParameters);
                        Log.v("Live URL",liveUrlParameters);
                    } else {
                        // Error in activity_login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Get symbol Error: " + error.getMessage());
                if(error.getMessage().equals(null))
                {
                    Toast.makeText(getApplicationContext(),
                            "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                    hideDialog();
                }
                else if(error.getMessage().contains("ENETUNREACH")){
                    Toast.makeText(getApplicationContext(),
                            "Please check your internet connection. Internet connection not available", Toast.LENGTH_LONG).show();
                    hideDialog();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to activity_login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                return params;
            }

        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private String getSkuSpStatus(String sku){
        String skuStatusLabel = sku + "_status";
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String skuStatus = prefs.getString(skuStatusLabel, "NotSet");
        return skuStatus;
    }

    private void setSkuSpStatus(String sku){
        SharedPreferences.Editor editor = getSharedPreferences(DataTable.MY_PREFS_NAME, MODE_PRIVATE).edit();
        String skuStatusLabel = sku + "_status";
        editor.putString(skuStatusLabel, "NotSet");
        editor.apply();
        Log.d(TAG, "SetSkuStatus " + skuStatusLabel);
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void addAllForex(){
        tempUrlList.remove("Buy all forex");
        tempUrlList.add("AUDUSD");
        tempUrlList.add("EURCHF");
        tempUrlList.add("EURGBP");
        tempUrlList.add("EURJPY");
        tempUrlList.add("EURUSD");
        tempUrlList.add("GBPUSD");
        tempUrlList.add("USDCAD");
        tempUrlList.add("USDCHF");
        tempUrlList.add("USDJPY");
    }

    private void addAllStocks(){
        tempUrlList.remove("Buy all stocks");
        tempUrlList.add("AAPL");
        tempUrlList.add("BA");
        tempUrlList.add("CAT");
        tempUrlList.add("CSCO");
        tempUrlList.add("IBM");
        tempUrlList.add("MSFT");
        tempUrlList.add("NKE");
        tempUrlList.add("V");
        tempUrlList.add("XOM");
    }

    private void addAllFutures(){
        tempUrlList.remove("Buy all futures");
        tempUrlList.add("6B 09-16");
        tempUrlList.add("6E 09-16");
        tempUrlList.add("CL 09-16");
        tempUrlList.add("ES 06-16");
        tempUrlList.add("FDAX 09-16");
        tempUrlList.add("GC 12-16");
        tempUrlList.add("M6E 09-16");
        tempUrlList.add("NQ 09-16");
        tempUrlList.add("TF 09-16");
        tempUrlList.add("YM 09-16");
        tempUrlList.add("ZB 09-16");
        tempUrlList.add("ZN 09-16");
        tempUrlList.add("ZS 09-16");
        tempUrlList.add("ZW 09-16");
    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }


    private void initializeHashTable(){
        tempSoundFlag.put("AUDUSD", "false");
        tempSoundFlag.put("EURCHF", "false");
        tempSoundFlag.put("EURGBP", "false");
        tempSoundFlag.put("EURJPY", "false");
        tempSoundFlag.put("AUDUSD", "false");
        tempSoundFlag.put("EURUSD", "false");
        tempSoundFlag.put("GBPUSD", "false");
        tempSoundFlag.put("AUDUSD", "false");
        tempSoundFlag.put("USDCAD", "false");
        tempSoundFlag.put("USDCHF", "false");
        tempSoundFlag.put("USDJPY", "false");
        tempSoundFlag.put("AAPL", "false");
        tempSoundFlag.put("BA", "false");
        tempSoundFlag.put("CAT", "false");
        tempSoundFlag.put("CSCO", "false");
        tempSoundFlag.put("IBM", "false");
        tempSoundFlag.put("MSFT", "false");
        tempSoundFlag.put("NKE", "false");
        tempSoundFlag.put("V", "false");
        tempSoundFlag.put("XOM", "false");
        tempSoundFlag.put("6B 09-16", "false");
        tempSoundFlag.put("6E 09-16", "false");
        tempSoundFlag.put("CL 09-16", "false");
        tempSoundFlag.put("ES 09-16", "false");
        tempSoundFlag.put("FDAX 09-16", "false");
        tempSoundFlag.put("GC 09-16", "false");
        tempSoundFlag.put("M6E 09-16", "false");
        tempSoundFlag.put("NQ 09-16", "false");
        tempSoundFlag.put("TF 09-16", "false");
        tempSoundFlag.put("YM 09-16", "false");
        tempSoundFlag.put("ZB 09-16", "false");
        tempSoundFlag.put("ZN 09-16", "false");
        tempSoundFlag.put("ZS 09-16", "false");
        tempSoundFlag.put("ZW 09-16", "false");
    }
}