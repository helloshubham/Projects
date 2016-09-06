package com.bpt.nt8ma.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bpt.nt8ma.Activity.DataTable;
import com.bpt.nt8ma.Activity.Instruments;
import com.bpt.nt8ma.Activity.OrderSummary;
import com.bpt.nt8ma.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Stocks extends Fragment {

    private List<String> stockInstruments;
    private ListView stocksListView;
    //public static ArrayList stocksOrderList;
    ArrayAdapter arrayAdapter;

    public Stocks() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RelativeLayout rootView =  (RelativeLayout) inflater.inflate(R.layout.fragment_stocks, container, false);

        //stocksOrderList = new ArrayList<String>();

        //Log.v("OnCreateViewStocks",DataTable.selectedDemoInstruments.toString());
        stocksListView = (ListView) rootView.findViewById(R.id.listView_stocks);

        stockInstruments = new ArrayList<>();
        stockInstruments.add("AAPL");
        stockInstruments.add("BA");
        stockInstruments.add("CAT");
        stockInstruments.add("CSCO");
        stockInstruments.add("IBM");
        stockInstruments.add("MSFT");
        stockInstruments.add("NKE");
        stockInstruments.add("V");
        stockInstruments.add("XOM");

        if(Instruments.executeOption.equals("SelectInstruments")) {
            arrayAdapter = new ArrayAdapter(getActivity(), R.layout.listview_item_layout, R.id.list_item_instrument_textview, stockInstruments);
        }
        else if(Instruments.executeOption.equals("BuyInstruments")) {
            stockInstruments.add(0, "Buy all stocks");
            arrayAdapter = new ArrayAdapter(getActivity(), R.layout.listview_item_layout, R.id.list_item_instrument_textview, stockInstruments);
        }
        stocksListView.setAdapter(arrayAdapter);

        stocksListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        stocksListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                String instrumentList;
                String orderedInstrumentList;

                if(Instruments.executeOption.equals("SelectInstruments")) {
                    if (b) {
                        instrumentList = "'" + stocksListView.getItemAtPosition(i).toString() + "'";
                        Boolean check = false;
                        for (int j = 0; j < DataTable.selectedDemoInstruments.size(); j++) {

                            if (DataTable.selectedDemoInstruments.get(j).equals(instrumentList)) {
                                Toast.makeText(getActivity(), stockInstruments.get(i) + " data is already included in the table. Tap again to remove the symbol from the table.", Toast.LENGTH_SHORT).show();
                                check = true;
                            }
                        }

                        if (!check) {
                            Instruments.count = Instruments.count + 1;
                            DataTable.selectedDemoInstruments.add(instrumentList);
                            Log.v("AddedInstrumentsStocks", DataTable.selectedDemoInstruments.toString());
                        }
                    } else {
                        Instruments.count = Instruments.count - 1;
                        instrumentList = "'" + stocksListView.getItemAtPosition(i).toString() + "'";
                        DataTable.selectedDemoInstruments.remove(instrumentList);
                        Log.v("RemoveInstrumentsStocks", DataTable.selectedDemoInstruments.toString());
                    }
                }
                else if (Instruments.executeOption.equals("BuyInstruments")){
                    if(b){
                        Instruments.count = Instruments.count + 1;
                        instrumentList = stocksListView.getItemAtPosition(i).toString();
                        orderedInstrumentList = stocksListView.getItemAtPosition(i).toString();
                        Instruments.stocksOrderList.add(orderedInstrumentList);
                        Log.v("BuyInstrumentsStocks", Instruments.stocksOrderList.toString());
                    }
                    else{
                        Instruments.count = Instruments.count - 1;
                        instrumentList = stocksListView.getItemAtPosition(i).toString();
                        orderedInstrumentList = stocksListView.getItemAtPosition(i).toString();
                        Instruments.stocksOrderList.remove(orderedInstrumentList);
                        Log.v("UnBuyInstrumentsStocks", Instruments.stocksOrderList.toString());
                    }
                }
                if(Instruments.count < 0){
                    Instruments.count = 0;
                }
                actionMode.setTitle(Instruments.count+" Selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater menuInflater = actionMode.getMenuInflater();
                if(Instruments.executeOption.equals("SelectInstruments"))
                    menuInflater.inflate(R.menu.select_context_menu, menu);
                else if (Instruments.executeOption.equals("BuyInstruments"))
                    menuInflater.inflate(R.menu.buy_context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == R.id.fetch) {
                    String selectedInstruments = String.valueOf(DataTable.selectedDemoInstruments);
                    Intent intent = new Intent(getActivity(), DataTable.class);
                    intent.putExtra("UrlValue", selectedInstruments);
                    startActivity(intent);
                }

                if (id == R.id.buy) {
                    Intent intent = new Intent(getActivity(), OrderSummary.class);
                    intent.putStringArrayListExtra("OrderedStockSymbols", Instruments.stocksOrderList);
                    intent.putStringArrayListExtra("OrderedFuturesSymbols", Instruments.futuresOrderList);
                    intent.putStringArrayListExtra("OrderedForexSymbols", Instruments.forexOrderList);
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
        return rootView;
    }
}