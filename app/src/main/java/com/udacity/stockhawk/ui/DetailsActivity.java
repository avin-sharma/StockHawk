package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.graph.HourAxisValueFormatter;
import com.udacity.stockhawk.graph.MyMarkerView;

import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

public class DetailsActivity extends AppCompatActivity {

    long referenceTimeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Initializing Chart
        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setBackgroundColor(ContextCompat.getColor(this,R.color.white));

        String symbol = getIntent().getExtras().getString(MainActivity.SYMBOL_EXTRA);
        String history[] = getIntent().getExtras().getString(MainActivity.HISTORY_EXTRA).split("\n");



        // Adding data to chart
        ArrayList<Entry> entries = new ArrayList<>();
        int i=0;
        boolean first = true;
        for (String h: history){
            String[] split = h.split(", ");
            if (first){
                Timber.d(split[0]);
                referenceTimeStamp = Long.parseLong(split[0]);
                first = false;
            }
            entries.add(new Entry(Float.parseFloat(split[0])-referenceTimeStamp, Float.parseFloat(split[1])));

        }

        Collections.sort(entries, new EntryXComparator());
        LineDataSet dataSet = new LineDataSet(entries, symbol); // add entries to dataset
        dataSet.setColor(R.color.colorAccent);
        dataSet.setValueTextColor(R.color.colorAccent); // styling

        IAxisValueFormatter xAxisFormatter = new HourAxisValueFormatter(referenceTimeStamp);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);

        MyMarkerView myMarkerView = new MyMarkerView(this, R.layout.custom_marker_view, referenceTimeStamp);
        chart.setMarker(myMarkerView);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }
}
