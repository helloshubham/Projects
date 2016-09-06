package com.bpt.nt8ma.Activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bpt.nt8ma.AppConfig;
import com.bpt.nt8ma.AppController;
import com.bpt.nt8ma.R;
import com.bpt.nt8ma.helper.SQLiteHandler;
import com.bpt.nt8ma.helper.SessionManager;
import com.bpt.nt8ma.util.IabHelper;
import com.bpt.nt8ma.util.IabResult;
import com.bpt.nt8ma.util.Purchase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderSummary extends AppCompatActivity {

    private SQLiteHandler db;
    private SessionManager session;
    ArrayList forexOrderList;
    ArrayList stocksOrderList;
    ArrayList futuresOrderList;
    String allOrderList;
    int forexPrice;
    int stocksPrice;
    int futuresPrice;
    int finalTotalPrice;
    int actualTotalPrice;
    private ArrayList finalOrderSymbols;
    private ProgressDialog pDialog;
    private String subscription;
    private String forexOrderString = "";
    private String stocksOrderString = "";
    private String futuresOrderString = "";
    private String finalOrderedSymbolsString = "";
    private String email;
    private String subType = "monthly_live_acc_35";

    static final String TAG = "OrderSummary";
    // Does the user have an active subscription?
    boolean mSubscribedTo35 = false;
    boolean mSubscribedTo70 = false;
    boolean mSubscribedTo100 = false;
    boolean mSubscribedTo105 = false;
    boolean mSubscribedTo140 = false;
    boolean mSubscribedTo170 = false;
    boolean mSubscribedTo175 = false;
    boolean mSubscribedTo200 = false;

    // SKU for our subscription (Monthly Live Account)
    static final String SKU_LIVE_MONTHLY_35 = "monthly_live_acc_35";
    static final String SKU_LIVE_MONTHLY_70 = "monthly_live_acc_70";
    static final String SKU_LIVE_MONTHLY_100 = "monthly_live_acc_100";
    static final String SKU_LIVE_MONTHLY_105 = "monthly_live_acc_105";
    static final String SKU_LIVE_MONTHLY_140 = "monthly_live_acc_140";
    static final String SKU_LIVE_MONTHLY_170 = "monthly_live_acc_170";
    static final String SKU_LIVE_MONTHLY_175 = "monthly_live_acc_175";
    static final String SKU_LIVE_MONTHLY_200 = "monthly_live_acc_200";

    static final int RC_REQUEST = 10001;

    IabHelper mHelper;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        finalOrderSymbols = new ArrayList();

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqBGWRiaLAK2eK7vVbl8h2JdQ9nggZ/6H/5kABV//rtQ5Ng2QPf+/vfwR3lxwDWaCYmoxUHfwglkGiyzTHCYM8vSQrHtSYSBqkhyatLrZmHSXHiWUFWsoDxcnS6PPKzU4nTNZ4Yg26t8OUmmdqCn6cxk191g6+78rlJ09cRgYCsXZCUSRbmy3w672lq9apd9iDgvUaER5VbqMjHIOyKNSMfccSRJ9OHBDJJ5VAooWNai4V9Ar7glL9XAedgSqvnoLqwABRA7tZ0EbQqxJJgokww6Is0th5QBGw26I2HX+WV4mPk5Ndbx2H7gq6F6VlzktWZYyHYGsD19g5Gg29DUB0QIDAQAB";
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once   setup completes.
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
                //mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });

        db = new SQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
        }
        HashMap<String, String> user = db.getUserDetails();
        email = user.get("email");

        TextView textViewForex = (TextView) findViewById(R.id.forexPrice);
        TextView textViewStocks = (TextView) findViewById(R.id.stocksPrice);
        TextView textViewFutures = (TextView) findViewById(R.id.futuresPrice);
        TextView textViewForexLabel = (TextView) findViewById(R.id.forexLabel);
        TextView textViewStocksLabel = (TextView) findViewById(R.id.stocksLabel);
        TextView textViewFuturesLabel = (TextView) findViewById(R.id.futuresLabel);
        TextView textViewTotalPrice = (TextView) findViewById(R.id.totalPrice);
        TextView textViewFinalPrice = (TextView) findViewById(R.id.finalPrice);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar_order);
        toolbar.setTitle("Subscribe");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Instruments.class);
                startActivity(intent);
            }
        });

        forexPrice = 0;
        stocksPrice = 0;
        futuresPrice = 0;
        finalTotalPrice = 0;
        actualTotalPrice = 0;

        forexOrderList = new ArrayList();
        stocksOrderList = new ArrayList();
        futuresOrderList = new ArrayList();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            forexOrderList = extras.getStringArrayList("OrderedForexSymbols");
            stocksOrderList = extras.getStringArrayList("OrderedStockSymbols");
            futuresOrderList = extras.getStringArrayList("OrderedFuturesSymbols");
            allOrderList = extras.getString("allSymbols");

            finalOrderSymbols.addAll(forexOrderList);
            finalOrderSymbols.addAll(futuresOrderList);
            finalOrderSymbols.addAll(stocksOrderList);
        }

        if (forexOrderList.isEmpty()) {
            forexPrice = 0;
            textViewForexLabel.setText("No Forex Symbols");
            forexOrderString = "No Forex Symbols";
        }
        else if(forexOrderList.contains("Buy all forex")){
            forexPrice = 100;
            textViewForexLabel.setText("All Forex Symbols");
            forexOrderString = "All Forex Symbols";
        }
        else{
            forexPrice = 35 * forexOrderList.size();
            forexOrderString = TextUtils.join(",", forexOrderList);
            textViewForexLabel.setText(forexOrderString);
        }


        if (stocksOrderList.isEmpty()) {
            stocksPrice = 0;
            textViewStocksLabel.setText("No Stock Symbols");
            stocksOrderString = "No Stock Symbols";
        } else if (stocksOrderList.contains("Buy all stocks")){
            stocksPrice = 100;
            textViewStocksLabel.setText("All Stocks Symbols");
            stocksOrderString = "All Stocks Symbols";
        }
        else{
            stocksPrice = 35 * stocksOrderList.size();
            stocksOrderString = TextUtils.join(",", stocksOrderList);
            textViewStocksLabel.setText(stocksOrderString);
        }

        if (futuresOrderList == null){
            futuresPrice = 0;
            textViewFuturesLabel.setText("No Futures Symbols");
            futuresOrderString = "No Futures Symbols";
        }
        else if (futuresOrderList.isEmpty()) {
            futuresPrice = 0;
            textViewFuturesLabel.setText("No Futures Symbols");
            futuresOrderString = "No Futures Symbols";
        } else if (futuresOrderList.contains("Buy all futures")) {
            futuresPrice = 100;
            textViewFuturesLabel.setText("All Futures Symbols");
            futuresOrderString = "All Futures Symbols";
        }
        else{
            futuresPrice = 35 * futuresOrderList.size();
            futuresOrderString = TextUtils.join(",", futuresOrderList);
            textViewFuturesLabel.setText(futuresOrderString);
        }

        if(forexPrice > 100)
            forexPrice = 100;
        if (stocksPrice > 100)
            stocksPrice = 100;
        if (futuresPrice > 100)
            futuresPrice = 100;

        actualTotalPrice = forexPrice + stocksPrice + futuresPrice;

        if (actualTotalPrice > 200) {
            finalTotalPrice = 200;
            textViewTotalPrice.setPaintFlags(textViewTotalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            finalTotalPrice = actualTotalPrice;
        }

        finalOrderedSymbolsString = String.valueOf(finalOrderSymbols);
        Log.v("finalorderlist", finalOrderedSymbolsString);
        finalOrderedSymbolsString = finalOrderedSymbolsString.replace("[", "").replace("]", "");
        Log.v("finalorderlistaltered", finalOrderedSymbolsString);

        //Set value of the price in the textView fields
        String forexPriceDollar = String.valueOf(forexPrice) + "$";
        String stocksPriceDollar = String.valueOf(stocksPrice) + "$";
        String futuresPriceDollar = String.valueOf(futuresPrice) + "$";
        String finalTotalPriceDollar = String.valueOf(finalTotalPrice) + "$";
        String actualTotalPriceDollar = String.valueOf(actualTotalPrice) + "$";

        textViewForex.setText(forexPriceDollar);
        textViewStocks.setText(stocksPriceDollar);
        textViewFutures.setText(futuresPriceDollar);
        textViewTotalPrice.setText(actualTotalPriceDollar);
        textViewFinalPrice.setText(finalTotalPriceDollar);

        //purchaseUpdateinDb(email, subType, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
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
        if (id == R.id.action_logout) {
            logoutUser();
        }
        return super.onOptionsItemSelected(item);
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    /*IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
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
                    + " infinite gas subscription.");
            if (mSubscribedTo35) ;

            // Do we have the monthly_live_acc_70?
            Purchase monthlyLiveAcc70 = inventory.getPurchase(SKU_LIVE_MONTHLY_70);
            mSubscribedTo70 = (monthlyLiveAcc70 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc70));
            Log.d(TAG, "User " + (mSubscribedTo70 ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedTo70) ;

            // Do we have the monthly_live_acc_100?
            Purchase monthlyLiveAcc100 = inventory.getPurchase(SKU_LIVE_MONTHLY_100);
            mSubscribedTo100 = (monthlyLiveAcc100 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc100));
            Log.d(TAG, "User " + (mSubscribedTo100 ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedTo100) ;

            // Do we have the monthly_live_acc_105?
            Purchase monthlyLiveAcc105 = inventory.getPurchase(SKU_LIVE_MONTHLY_105);
            mSubscribedTo105 = (monthlyLiveAcc105 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc105));
            Log.d(TAG, "User " + (mSubscribedTo105 ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedTo105);

            // Do we have the monthly_live_acc_140?
            Purchase monthlyLiveAcc140 = inventory.getPurchase(SKU_LIVE_MONTHLY_140);
            mSubscribedTo140 = (monthlyLiveAcc140 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc140));
            Log.d(TAG, "User " + (mSubscribedTo140 ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedTo140) ;

            // Do we have the monthly_live_acc_170?
            Purchase monthlyLiveAcc170 = inventory.getPurchase(SKU_LIVE_MONTHLY_170);
            mSubscribedTo170 = (monthlyLiveAcc170 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc170));
            Log.d(TAG, "User " + (mSubscribedTo170 ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedTo170) ;

            // Do we have the monthly_live_acc_175?
            Purchase monthlyLiveAcc175 = inventory.getPurchase(SKU_LIVE_MONTHLY_175);
            mSubscribedTo175 = (monthlyLiveAcc175 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc175));
            Log.d(TAG, "User " + (mSubscribedTo175 ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedTo175) ;

            // Do we have the monthly_live_acc_200?
            Purchase monthlyLiveAcc200 = inventory.getPurchase(SKU_LIVE_MONTHLY_200);
            mSubscribedTo200 = (monthlyLiveAcc200 != null &&
                    verifyDeveloperPayload(monthlyLiveAcc200));
            Log.d(TAG, "User " + (mSubscribedTo200 ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedTo200) ;

            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };*/


    public void onSubscribedButtonClicked(View arg0) {
        if (!mHelper.subscriptionsSupported()) {
            complain("Subscriptions not supported on your device yet. Sorry!");
            return;
        }

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        if (finalTotalPrice == 35) {
            Log.d(TAG, "Launching purchase flow for monthly live acc-35.");
            mHelper.launchPurchaseFlow(this,
                    SKU_LIVE_MONTHLY_35, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
        }
        else if (finalTotalPrice == 70){
            Log.d(TAG, "Launching purchase flow for monthly live acc-70.");
            mHelper.launchPurchaseFlow(this,
                    SKU_LIVE_MONTHLY_70, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
        }
        else if (finalTotalPrice == 100){
            Log.d(TAG, "Launching purchase flow for monthly live acc-100.");
            mHelper.launchPurchaseFlow(this,
                    SKU_LIVE_MONTHLY_100, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
        }
        else if (finalTotalPrice == 105){
            Log.d(TAG, "Launching purchase flow for monthly live acc-105.");
            mHelper.launchPurchaseFlow(this,
                    SKU_LIVE_MONTHLY_105, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
        }
        else if (finalTotalPrice == 140){
            Log.d(TAG, "Launching purchase flow for monthly live acc-140.");
            mHelper.launchPurchaseFlow(this,
                    SKU_LIVE_MONTHLY_140, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
        }
        else if (finalTotalPrice == 170){
            Log.d(TAG, "Launching purchase flow for monthly live acc-170.");
            mHelper.launchPurchaseFlow(this,
                    SKU_LIVE_MONTHLY_170, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
        }
        else if (finalTotalPrice == 175){
            Log.d(TAG, "Launching purchase flow for monthly live acc-175.");
            mHelper.launchPurchaseFlow(this,
                    SKU_LIVE_MONTHLY_175, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
        }
        else if (finalTotalPrice == 200){
            Log.d(TAG, "Launching purchase flow for monthly live acc-200.");
            mHelper.launchPurchaseFlow(this,
                    SKU_LIVE_MONTHLY_200, IabHelper.ITEM_TYPE_SUBS,
                    RC_REQUEST, mPurchaseFinishedListener, payload);
        }
    }

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
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
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



    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_LIVE_MONTHLY_35)) {
                // bought the monthly_live_acc_35
                purchaseUpdateinDb(email, SKU_LIVE_MONTHLY_35, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
                Log.d(TAG, "SKU_LIVE_MONTHLY_35 subscription purchased.");
                alert("Thank you for subscribing. Your purchase was successful!");
                mSubscribedTo35 = true;
                subscription = "Live";
                saveData(SKU_LIVE_MONTHLY_35);
                goToHomeIntent();
            }
            else if (purchase.getSku().equals(SKU_LIVE_MONTHLY_70)) {
                // bought the monthly_live_acc_70
                purchaseUpdateinDb(email, SKU_LIVE_MONTHLY_70, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
                Log.d(TAG, "SKU_LIVE_MONTHLY_70 subscription purchased.");
                alert("Thank you for subscribing. Your purchase was successful!");
                mSubscribedTo70 = true;
                subscription = "Live";
                saveData(SKU_LIVE_MONTHLY_70);
                goToHomeIntent();
            }
            else if (purchase.getSku().equals(SKU_LIVE_MONTHLY_100)) {
                // bought the monthly_live_acc_100
                purchaseUpdateinDb(email, SKU_LIVE_MONTHLY_100, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
                Log.d(TAG, "SKU_LIVE_MONTHLY_100 subscription purchased.");
                alert("Thank you for subscribing. Your purchase was successful!");
                mSubscribedTo100 = true;
                subscription = "Live";
                saveData(SKU_LIVE_MONTHLY_100);
                goToHomeIntent();
            }
            else if (purchase.getSku().equals(SKU_LIVE_MONTHLY_105)) {
                // bought the monthly_live_acc_105
                purchaseUpdateinDb(email, SKU_LIVE_MONTHLY_105, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
                Log.d(TAG, "SKU_LIVE_MONTHLY_105 subscription purchased.");
                alert("Thank you for subscribing. Your purchase was successful!");
                mSubscribedTo105 = true;
                subscription = "Live";
                saveData(SKU_LIVE_MONTHLY_105);
                goToHomeIntent();
            }
            else if (purchase.getSku().equals(SKU_LIVE_MONTHLY_140)) {
                // bought the monthly_live_acc_140
                purchaseUpdateinDb(email, SKU_LIVE_MONTHLY_140, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
                Log.d(TAG, "SKU_LIVE_MONTHLY_140 subscription purchased.");
                alert("Thank you for subscribing. Your purchase was successful!");
                mSubscribedTo140 = true;
                subscription = "Live";
                saveData(SKU_LIVE_MONTHLY_140);
                goToHomeIntent();
            }
            else if (purchase.getSku().equals(SKU_LIVE_MONTHLY_170)) {
                // bought the monthly_live_acc_170
                purchaseUpdateinDb(email, SKU_LIVE_MONTHLY_170, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
                Log.d(TAG, "SKU_LIVE_MONTHLY_170 subscription purchased.");
                alert("Thank you for subscribing. Your purchase was successful!");
                mSubscribedTo170 = true;
                subscription = "Live";
                saveData(SKU_LIVE_MONTHLY_170);
                goToHomeIntent();
            }
            else if (purchase.getSku().equals(SKU_LIVE_MONTHLY_175)) {
                // bought the monthly_live_acc_175
                purchaseUpdateinDb(email, SKU_LIVE_MONTHLY_175, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
                Log.d(TAG, "SKU_LIVE_MONTHLY_175 subscription purchased.");
                alert("Thank you for subscribing. Your purchase was successful!");
                mSubscribedTo175 = true;
                subscription = "Live";
                saveData(SKU_LIVE_MONTHLY_175);
                goToHomeIntent();
            }
            else if (purchase.getSku().equals(SKU_LIVE_MONTHLY_200)) {
                // bought the monthly_live_acc_200
                purchaseUpdateinDb(email, SKU_LIVE_MONTHLY_200, forexOrderString, stocksOrderString, futuresOrderString, finalOrderedSymbolsString);
                Log.d(TAG, "SKU_LIVE_MONTHLY_200 subscription purchased.");
                alert("Thank you for subscribing. Your purchase was successful!");
                mSubscribedTo200 = true;
                subscription = "Live";
                saveData(SKU_LIVE_MONTHLY_200);
                goToHomeIntent();
            }
        }
    };

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

    void complain(String message) {
        Log.e(TAG, "**** BestProTrade Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    void saveData(String sku) {
        SharedPreferences.Editor editor = getSharedPreferences(DataTable.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("Subscription", subscription);
        String skuStatus = sku + "_status";
        //String skuSymbol = sku + "_symbols";
        //editor.putString(skuSymbol, allOrderList);
        editor.putString(skuStatus, "Live");
        editor.apply();
        Log.d(TAG, "Saved data: subscribe = " + subscription);
    }


    //Function to store user in MySQL database
    private void purchaseUpdateinDb(final String email, final String subType, final String forex, final String stocks,
                                    final String futures, final String allSymbols) {
        // Tag used to cancel the request
        String tag_string_req = "db_Update";

        pDialog.setMessage("Updating ...");
        showDialog();

        final StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                AppConfig.URL_SYMBOL_UPDATE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Update Response: " + response);
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
                params.put("subType", subType);
                params.put("forex", forex);
                params.put("futures", futures);
                params.put("stocks", stocks);
                params.put("allSymbols", allSymbols);

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

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        // Launching the login activity
        Intent intent = new Intent(OrderSummary.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToHomeIntent(){
        Intent intent = new Intent(this, DataTable.class);
        startActivity(intent);
    }
}