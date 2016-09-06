package com.bpt.nt8ma.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bpt.nt8ma.AppConfig;
import com.bpt.nt8ma.AppController;
import com.bpt.nt8ma.R;
import com.bpt.nt8ma.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputConfirmPassword;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Button btnReset;
        Button btnLinkToLogin;
        SessionManager session;
        TextView title;

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputConfirmPassword = (EditText) findViewById(R.id.cnf_password);

        btnReset = (Button) findViewById(R.id.btnResetPwd);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);


        title =(TextView) findViewById(R.id.title);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/CreteRound-Regular.ttf");
        title.setTypeface(typeface);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());


        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(ResetPasswordActivity.this,
                    Instruments.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        }

        // Register Button Click event
        btnReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String passsword = inputPassword.getText().toString().trim();
                String cnfPassword = inputConfirmPassword.getText().toString().trim();


                if (!email.isEmpty() && !passsword.isEmpty() && !cnfPassword.isEmpty() && isValidEmail(email)) {
                    registerUser(email, passsword, cnfPassword);
                } else {
                    if(email.isEmpty()){
                        Toast.makeText(getApplicationContext(),
                                "Please enter an email id!", Toast.LENGTH_LONG)
                                .show();
                    }
                    else if(!isValidEmail(email)){
                        Toast.makeText(getApplicationContext(),
                                "Please enter a valid email id!!", Toast.LENGTH_LONG)
                                .show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),
                                "Please confirm your password!", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                finish();
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String email,
                              final String password, final String cnfPassword) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Updating ...");
        showDialog();

        StringRequest strReq = new StringRequest(com.android.volley.Request.Method.POST,
                AppConfig.URL_RESET, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response);
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        Toast.makeText(getApplicationContext(), "Password changed successfully. Please login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                ResetPasswordActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
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
                if(error.getMessage().equals(""))
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
                            "Connection time out. Please try again.", Toast.LENGTH_LONG).show();
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
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);
                params.put("cnfPassword", cnfPassword);
                return params;
            }

        };

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

    private static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}