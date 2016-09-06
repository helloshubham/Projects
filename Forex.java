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
public class Forex extends Fragment {

    private List<String> forexInstruments;
    private ListView forexListView;
    //public static ArrayList forexOrderList;
    private ArrayAdapter arrayAdapter;

    public Forex() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout rootView =(RelativeLayout) inflater.inflate(R.layout.fragment_forex, container, false);

        forexListView = (ListView) rootView.findViewById(R.id.forex_listView);

        forexInstruments = new ArrayList<>();
        forexInstruments.add("AUDUSD");
        forexInstruments.add("EURCHF");
        forexInstruments.add("EURGBP");
        forexInstruments.add("EURJPY");
        forexInstruments.add("EURUSD");
        forexInstruments.add("GBPUSD");
        forexInstruments.add("USDCAD");
        forexInstruments.add("USDCHF");
        forexInstruments.add("USDJPY");

        if(Instruments.executeOption.equals("SelectInstruments")) {
            arrayAdapter = new ArrayAdapter(getActivity(), R.layout.listview_item_layout, R.id.list_item_instrument_textview, forexInstruments);
        }
        else if(Instruments.executeOption.equals("BuyInstruments")) {
            forexInstruments.add(0, "Buy all forex");
            arrayAdapter = new ArrayAdapter(getActivity(), R.layout.listview_item_layout, R.id.list_item_instrument_textview, forexInstruments);
        }
        forexListView.setAdapter(arrayAdapter);
        forexListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        forexListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                    String instrumentList;
                    String orderedInstrumentList;

                    if(Instruments.executeOption.equals("SelectInstruments")) {
                        if (b) {

                            instrumentList = "'" + forexListView.getItemAtPosition(i).toString() + "'";
                            Boolean check = false;
                            for (int j = 0; j < DataTable.selectedDemoInstruments.size(); j++) {
                                if (DataTable.selectedDemoInstruments.get(j).equals(instrumentList)) {
                                    Toast.makeText(getActivity(), forexInstruments.get(i) + " data is already included in the table. Tap again to remove the symbol from the table.", Toast.LENGTH_SHORT).show();
                                    check = true;
                                }
                            }

                            if (!check) {
                                Instruments.count = Instruments.count + 1;
                                DataTable.selectedDemoInstruments.add(instrumentList);
                                Log.v("AddedInstrumentsForex", DataTable.selectedDemoInstruments.toString());
                            }
                        }
                        else {
                            Instruments.count = Instruments.count - 1;
                            instrumentList = "'" + forexListView.getItemAtPosition(i).toString() + "'";
                            DataTable.selectedDemoInstruments.remove(instrumentList);
                            Log.v("RemovedInstrumentsForex", DataTable.selectedDemoInstruments.toString());
                        }
                    }

                    else if (Instruments.executeOption.equals("BuyInstruments")){
                        if(b){
                            orderedInstrumentList = forexListView.getItemAtPosition(i).toString();
                            //instrumentList = "'" + forexListView.getItemAtPosition(i).toString() + "'";
                            instrumentList = forexListView.getItemAtPosition(i).toString();
                            Instruments.count = Instruments.count + 1;
                            Instruments.forexOrderList.add(orderedInstrumentList);
                            Log.v("BuyInstrumentsForex", Instruments.forexOrderList.toString());
                        }
                        else{
                            Instruments.count = Instruments.count - 1;
                            //instrumentList = "'" + forexListView.getItemAtPosition(i).toString() + "'";
                            instrumentList = forexListView.getItemAtPosition(i).toString();
                            orderedInstrumentList = forexListView.getItemAtPosition(i).toString();
                            Instruments.forexOrderList.remove(orderedInstrumentList);
                            Log.v("UnBuyInstrumentsForex", Instruments.forexOrderList.toString());
                        }
                    }
                    if(Instruments.count < 0){
                        Instruments.count = 0;
                    }
                    actionMode.setTitle(Instruments.count + " Selected");
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
            }
        );

        return rootView;
    }
}