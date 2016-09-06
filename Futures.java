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
public class Futures extends Fragment {

    public static List<String> futuresInstruments = new ArrayList<>();
    private ListView futuresListView;
    //public static ArrayList futuresOrderList;

    ArrayAdapter arrayAdapter;

    public Futures() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RelativeLayout rootView =  (RelativeLayout) inflater.inflate(R.layout.fragment_futures, container, false);

        //futuresOrderList = new ArrayList<String>();
        //Log.v("OnCreateViewFutures",DataTable.selectedDemoInstruments.toString());

        futuresInstruments = new ArrayList<>();
        futuresInstruments.add("6B 09-16");
        futuresInstruments.add("6E 09-16");
        futuresInstruments.add("CL 09-16");
        futuresInstruments.add("ES 06-16");
        futuresInstruments.add("FDAX 09-16");
        futuresInstruments.add("GC 12-16");
        futuresInstruments.add("M6E 09-16");
        futuresInstruments.add("NQ 09-16");
        futuresInstruments.add("TF 09-16");
        futuresInstruments.add("YM 09-16");
        futuresInstruments.add("ZB 09-16");
        futuresInstruments.add("ZN 09-16");
        futuresInstruments.add("ZS 09-16");
        futuresInstruments.add("ZW 09-16");


        futuresListView = (ListView) rootView.findViewById(R.id.listView_futures);
        if(Instruments.executeOption.equals("SelectInstruments")) {

            arrayAdapter = new ArrayAdapter(getActivity(), R.layout.listview_item_layout, R.id.list_item_instrument_textview, futuresInstruments);
        }
            else if(Instruments.executeOption.equals("BuyInstruments")) {
            futuresInstruments.add(0, "Buy all futures");
            arrayAdapter = new ArrayAdapter(getActivity(), R.layout.listview_item_layout, R.id.list_item_instrument_textview, futuresInstruments);
        }
        futuresListView.setAdapter(arrayAdapter);

        futuresListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        futuresListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                String instrumentList;
                String orderedInstrumentList;

                if(Instruments.executeOption.equals("SelectInstruments")) {
                    if (b) {
                        instrumentList = "'" + futuresListView.getItemAtPosition(i).toString() + "'";
                        Boolean check = false;
                        for (int j = 0; j < DataTable.selectedDemoInstruments.size(); j++) {
                            if (DataTable.selectedDemoInstruments.get(j).equals(instrumentList)) {
                                Toast.makeText(getActivity(), futuresInstruments.get(i) + " data is already included in the table.  Tap again to remove the symbol from the table.", Toast.LENGTH_SHORT).show();
                                check = true;
                            }
                        }

                        if (!check) {
                            Instruments.count = Instruments.count + 1;
                            DataTable.selectedDemoInstruments.add(instrumentList);
                            Log.v("AddedInstrumentsFutures", DataTable.selectedDemoInstruments.toString());
                        }
                    } else {
                        Instruments.count = Instruments.count - 1;
                        instrumentList = "'" + futuresListView.getItemAtPosition(i).toString() + "'";
                        DataTable.selectedDemoInstruments.remove(instrumentList);
                        Log.v("RemoveInstrumentFutures", DataTable.selectedDemoInstruments.toString());
                    }
                }
                else if (Instruments.executeOption.equals("BuyInstruments")){
                    if(b){
                        orderedInstrumentList = futuresListView.getItemAtPosition(i).toString();
                        //instrumentList = "'" + futuresListView.getItemAtPosition(i).toString() + "'";
                        instrumentList = futuresListView.getItemAtPosition(i).toString();
                        Instruments.count = Instruments.count + 1;
                        Instruments.futuresOrderList.add(orderedInstrumentList);
                        Log.v("BuyInstrumentsFutures", Instruments.futuresOrderList.toString());
                    }
                    else{
                        Instruments.count = Instruments.count - 1;
                        //instrumentList = "'" + futuresListView.getItemAtPosition(i).toString() + "'";
                        instrumentList = futuresListView.getItemAtPosition(i).toString();
                        orderedInstrumentList = futuresListView.getItemAtPosition(i).toString();
                        Instruments.futuresOrderList.remove(orderedInstrumentList);
                        Log.v("UnBuyInstrumentsFutures", Instruments.futuresOrderList.toString());
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