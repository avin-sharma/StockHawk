package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Initializing Chart
        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setBackgroundColor(ContextCompat.getColor(this,R.color.white));

        String symbol = getIntent().getExtras().getString(MainActivity.SYMBOL_EXTRA);
        String history[] = getIntent().getExtras().getString(MainActivity.HISTORY_EXTRA)
                .split("\n");


        //// TODO: 26-02-2017 change i++ to readable date format, figure out how to display date in month/year on X-axis
        // Adding data to chart
        ArrayList<Entry> entries = new ArrayList<>();
        int i=0;
        for (String h: history){
            String[] split = h.split(", ");
            entries.add(new Entry(i++, Float.parseFloat(split[1])));
        }
        LineDataSet dataSet = new LineDataSet(entries, symbol); // add entries to dataset
        dataSet.setColor(R.color.colorAccent);
        dataSet.setValueTextColor(R.color.colorAccent); // styling

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }
}
