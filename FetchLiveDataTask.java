package com.bpt.nt8ma.AsyncTask;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.bpt.nt8ma.Activity.DataTable;
import com.bpt.nt8ma.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

/**
 * Created by Satyam on 7/14/2016.
 */
public class FetchLiveDataTask extends AsyncTask<String,Integer,String> {

    private static Context ctx;
    public FetchLiveDataTask(Context ctx)
    {
        FetchLiveDataTask.ctx =ctx;
    }
    public FetchLiveDataTask()
    {

    }

    private final String LOG_TAG = FetchLiveDataTask.class.getSimpleName();
    private Hashtable soundFlag = new Hashtable();


    //To parse JSON String recieved from the server
    private void getInstrumentDataFromJson(String instrumentJsonStr) throws JSONException {
        final String OWM_INSTRUMENT = "Instrument";
        final String OWM_NEW_SIGNAL = "NewSignal";
        final String OWM_ENTRY_TYPE = "EntryType";
        final String OWM_ENTRY_PRICE = "EntryPrice";
        final String OWM_TRAILING_STOP_1 = "TrailingStop1";
        final String OWM_TRAILING_STOP_2 = "TrailingStop2";
        final String OWM_TGT = "TGT";
        final String OWM_TGT_HIT = "TGTHit";
        final String OWM_P_LTicks = "PandL_Ticks";
        final String OWM_P_L = "PandL";
        final String OWM_STOP_LOSS = "StopLoss";

        try {
            JSONArray instrumentArray = new JSONArray(instrumentJsonStr);
            DataTable.tv.removeAllViewsInLayout();
            int flag = 1;
            int tableHeaderTextSize = 16;
            int tableHeaderTextColor = Color.parseColor("#FFFFFF");
            int tableHeaderBackgroundColor = Color.parseColor("#7986CB");

            int tableBodyTextSize = 14;
            int tableBodyTextColor = Color.parseColor("#000000");
            int tableBodyBackgroundColor = Color.parseColor("#FFFFFF");
            int greenShade = Color.parseColor("#0ACF98");
            int redShade = Color.parseColor("#F6562A");
            int instrumentBackgroundColor = Color.parseColor("#E8EAF6");
            int bodyPaddingLeft = 20;
            int bodyPaddingtop = 10;
            int bodyPaddingright = 20;
            int bodyPaddingbottom = 10;

            int headPaddingLeft = 30;
            int headPaddingtop = 20;
            int headPaddingright = 30;
            int headPaddingbottom = 20;

            //String[] resultStrs = new String[instrumentArray.length()];
            for (int i = -1; i <= instrumentArray.length() - 1; i++) {
                TableRow tr = new TableRow(ctx);
                tr.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                if (flag == 1) {
                    TextView b09 = new TextView(ctx);
                    b09.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b09.setText("Instrument");
                    b09.setTextColor(tableHeaderTextColor);
                    b09.setTextSize(tableHeaderTextSize);
                    b09.setBackgroundColor(tableHeaderBackgroundColor);
                    b09.setGravity(Gravity.CENTER);
                    tr.addView(b09);

                    TextView b19 = new TextView(ctx);
                    b19.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b19.setTextSize(tableHeaderTextSize);
                    b19.setText("New Signal");
                    b19.setTextColor(tableHeaderTextColor);
                    b19.setGravity(Gravity.CENTER);
                    b19.setBackgroundColor(tableHeaderBackgroundColor);
                    tr.addView(b19);

                    TextView b29 = new TextView(ctx);
                    b29.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b29.setText("Entry Type");
                    b29.setTextColor(tableHeaderTextColor);
                    b29.setGravity(Gravity.CENTER);
                    b29.setBackgroundColor(tableHeaderBackgroundColor);
                    b29.setTextSize(tableHeaderTextSize);
                    tr.addView(b29);

                    TextView b39 = new TextView(ctx);
                    b39.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b39.setText("Entry Price");
                    b39.setGravity(Gravity.CENTER);
                    b39.setTextColor(tableHeaderTextColor);
                    b39.setTextSize(tableHeaderTextSize);
                    b39.setBackgroundColor(tableHeaderBackgroundColor);
                    tr.addView(b39);

                    TextView b69 = new TextView(ctx);
                    b69.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b69.setText("TGT");
                    b69.setBackgroundColor(tableHeaderBackgroundColor);
                    b69.setGravity(Gravity.CENTER);
                    b69.setTextColor(tableHeaderTextColor);
                    b69.setTextSize(tableHeaderTextSize);
                    tr.addView(b69);

                    TextView b79 = new TextView(ctx);
                    b79.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b79.setGravity(Gravity.CENTER);
                    b79.setText("TGT Hit");
                    b79.setBackgroundColor(tableHeaderBackgroundColor);
                    b79.setTextColor(tableHeaderTextColor);
                    b79.setTextSize(tableHeaderTextSize);
                    tr.addView(b79);

                    TextView b11 = new TextView(ctx);
                    b11.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b11.setText("P&L(pips)");
                    b11.setGravity(Gravity.CENTER);
                    b11.setBackgroundColor(tableHeaderBackgroundColor);
                    b11.setTextColor(tableHeaderTextColor);
                    b11.setTextSize(tableHeaderTextSize);
                    tr.addView(b11);

                    TextView b89 = new TextView(ctx);
                    b89.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b89.setText("P&L");
                    b89.setGravity(Gravity.CENTER);
                    b89.setBackgroundColor(tableHeaderBackgroundColor);
                    b89.setTextColor(tableHeaderTextColor);
                    b89.setTextSize(tableHeaderTextSize);
                    tr.addView(b89);

                    TextView b49 = new TextView(ctx);
                    b49.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b49.setText("TrailingStop1");
                    b49.setGravity(Gravity.CENTER);
                    b49.setTextColor(tableHeaderTextColor);
                    b49.setBackgroundColor(tableHeaderBackgroundColor);
                    b49.setTextSize(tableHeaderTextSize);
                    tr.addView(b49);

                    TextView b59 = new TextView(ctx);
                    b59.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b59.setText("TrailingStop2");
                    b59.setGravity(Gravity.CENTER);
                    b59.setBackgroundColor(tableHeaderBackgroundColor);
                    b59.setTextColor(tableHeaderTextColor);
                    b59.setTextSize(tableHeaderTextSize);
                    tr.addView(b59);


                    /*TextView b99 = new TextView(ctx);
                    b99.setPadding(headPaddingLeft, headPaddingtop, headPaddingright, headPaddingbottom);
                    b99.setGravity(Gravity.CENTER);
                    b99.setText("Stop Loss");
                    b99.setBackgroundColor(tableHeaderBackgroundColor);
                    b99.setTextColor(tableHeaderTextColor);
                    b99.setTextSize(tableHeaderTextSize);
                    tr.addView(b99);*/

                    DataTable.tv.addView(tr);

                    final View vline = new View(ctx);
                    vline.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 2));
                    vline.setBackgroundColor(Color.parseColor("#283593"));
                    DataTable.tv.addView(vline);
                    flag = 0;
                } else {
                    JSONObject instrumentObject = instrumentArray.getJSONObject(i);

                    String instrumentName = instrumentObject.getString(OWM_INSTRUMENT);
                    TextView b = new TextView(ctx);
                    b.setText(instrumentName);
                    b.setPadding(20, 5, 20, 5);
                    b.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b.setGravity(Gravity.CENTER);
                    b.setTypeface(Typeface.DEFAULT_BOLD);
                    b.setTextColor(tableBodyTextColor);
                    b.setTextSize(tableBodyTextSize);
                    b.setBackgroundColor(instrumentBackgroundColor);
                    tr.addView(b);
                    soundFlag.put(instrumentName, "false");

                    String newSignal = instrumentObject.getString(OWM_NEW_SIGNAL);
                    TextView b1 = new TextView(ctx);
                    b1.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b1.setGravity(Gravity.CENTER);
                    b1.setTextSize(tableBodyTextSize);
                    b1.setText(newSignal);
                    b1.setTextColor(tableBodyTextColor);
                    b1.setBackgroundColor(tableBodyBackgroundColor);
                    if (newSignal.equals("New Signal")){
                        b1.setBackgroundColor(greenShade);
                        DataTable.mp = MediaPlayer.create(ctx, R.raw.noti);
                        if (DataTable.tempSoundFlag.get(instrumentName).equals("false")) {
                            if (DataTable.mp.isPlaying()){
                                DataTable.mp.stop();
                                DataTable.mp.release();
                            }
                            else DataTable.mp.start();
                            DataTable.tempSoundFlag.put(instrumentName, "true");
                        }
                    }
                    tr.addView(b1);

                    String entryType = instrumentObject.getString(OWM_ENTRY_TYPE);
                    TextView b2 = new TextView(ctx);
                    b2.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b2.setGravity(Gravity.CENTER);
                    b2.setTextSize(tableBodyTextSize);
                    b2.setText(entryType);
                    b2.setTextColor(tableHeaderTextColor);
                    if (entryType.equals("Buy"))
                        b2.setBackgroundColor(greenShade);
                    else
                        b2.setBackgroundColor(redShade);
                    tr.addView(b2);


                    String entryPrice = instrumentObject.getString(OWM_ENTRY_PRICE);
                    TextView b9 = new TextView(ctx);
                    b9.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b9.setGravity(Gravity.CENTER);
                    b9.setTextSize(tableBodyTextSize);
                    b9.setText(entryPrice);
                    b9.setTextColor(tableBodyTextColor);
                    b9.setBackgroundColor(tableBodyBackgroundColor);
                    tr.addView(b9);



                    String tgt = instrumentObject.getString(OWM_TGT);
                    TextView b5 = new TextView(ctx);
                    b5.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b5.setGravity(Gravity.CENTER);
                    b5.setTextSize(tableBodyTextSize);
                    b5.setText(tgt);
                    b5.setTextColor(tableBodyTextColor);
                    b5.setBackgroundColor(tableBodyBackgroundColor);
                    tr.addView(b5);

                    String tgtHit = instrumentObject.getString(OWM_TGT_HIT);
                    TextView b6 = new TextView(ctx);
                    b6.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b6.setGravity(Gravity.CENTER);
                    b6.setTextSize(tableBodyTextSize);
                    b6.setText(tgtHit);
                    b6.setTypeface(Typeface.DEFAULT_BOLD);
                    if (tgtHit.equals("HIT"))
                        b6.setTextColor(redShade);
                    else
                        b6.setTextColor(tableBodyTextColor);
                    b6.setBackgroundColor(tableBodyBackgroundColor);
                    tr.addView(b6);

                    String pl_ticks = instrumentObject.getString(OWM_P_LTicks);
                    TextView b10 = new TextView(ctx);
                    b10.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b10.setGravity(Gravity.CENTER);
                    b10.setTextSize(tableBodyTextSize);
                    b10.setText(pl_ticks);
                    if (pl_ticks.contains("-"))
                        b10.setTextColor(redShade);
                    else
                        b10.setTextColor(greenShade);
                    b10.setBackgroundColor(tableBodyBackgroundColor);
                    tr.addView(b10);

                    String pl = instrumentObject.getString(OWM_P_L);
                    TextView b7 = new TextView(ctx);
                    b7.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b7.setGravity(Gravity.CENTER);
                    b7.setTextSize(tableBodyTextSize);
                    b7.setText(pl);
                    if (pl_ticks.contains("-"))
                        b7.setTextColor(redShade);
                    else
                        b7.setTextColor(greenShade);
                    b7.setBackgroundColor(tableBodyBackgroundColor);
                    tr.addView(b7);

                    String trailingStop1 = instrumentObject.getString(OWM_TRAILING_STOP_1);
                    TextView b3 = new TextView(ctx);
                    b3.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b3.setGravity(Gravity.CENTER);
                    b3.setTextSize(tableBodyTextSize);
                    b3.setText(trailingStop1);
                    b3.setTextColor(tableBodyTextColor);
                    b3.setBackgroundColor(tableBodyBackgroundColor);
                    tr.addView(b3);

                    String trailingStop2 = instrumentObject.getString(OWM_TRAILING_STOP_2);
                    TextView b4 = new TextView(ctx);
                    b4.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b4.setGravity(Gravity.CENTER);
                    b4.setTextSize(tableBodyTextSize);
                    b4.setText(trailingStop2);
                    b4.setTextColor(tableBodyTextColor);
                    b4.setBackgroundColor(tableBodyBackgroundColor);
                    tr.addView(b4);

                    /*String stopLoss = instrumentObject.getString(OWM_STOP_LOSS);
                    TextView b8 = new TextView(ctx);
                    b8.setPadding(bodyPaddingLeft, bodyPaddingtop, bodyPaddingright, bodyPaddingbottom);
                    b8.setGravity(Gravity.CENTER);
                    b8.setTextSize(tableBodyTextSize);
                    b8.setText(stopLoss);
                    if (stopLoss.equals("Candle should close above Trailing Stop 2\u0000"))
                        b8.setTextColor(greenShade);
                    else
                        b8.setTextColor(redShade);
                    b8.setBackgroundColor(tableBodyBackgroundColor);
                    tr.a    ddView(b8);*/

                    DataTable.tv.addView(tr);
                    final View vline1 = new View(ctx);
                    vline1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                    vline1.setBackgroundColor(Color.parseColor("#283593"));
                    DataTable.tv.addView(vline1);

                }
            }
        }catch (Exception e){
            Log.e("JSON Exception", "MINE" , e);
        }
    }

    @Override
    protected String doInBackground(String...result) {

        publishProgress(100);
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String instrumentJsonStr;

        try {
            final String FORECAST_BASE_URL = "http://198.57.174.253/plesk-site-preview/bestprotrade.com/198.57.174.253/nt8_ma/FetchLiveData.php?";
            final String QUERY_PARAM = "id";
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, result[0])
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Live URL is:" + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            instrumentJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e("IOEXCEPTIONLive", "Error ", e);
            return null;
        } catch (Exception e){
            Log.e("ExceptionGeneral", "Ex", e);
            return null;
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {

                    Log.e("Fetch", "Error closing stream", e);
                }
            }
        }
        //Log.v(LOG_TAG, "Live Data from VPS is: " + instrumentJsonStr);
            return instrumentJsonStr;
    }

    @Override
    protected void onPostExecute(String s) {
        DataTable.progressBar.setVisibility(View.GONE);
        DataTable.jsonString = s;
        try {
            getInstrumentDataFromJson(s);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        DataTable.progressBar.setProgress(values[0]);
    }
}