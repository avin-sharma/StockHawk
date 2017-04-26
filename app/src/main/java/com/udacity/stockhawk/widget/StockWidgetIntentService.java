package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Avin on 05-03-2017.
 */

public class StockWidgetIntentService extends IntentService {

    public StockWidgetIntentService() {
        super("StockWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager
                .getAppWidgetIds(new ComponentName(this, StockWidgetProvider.class));



        for (int appWidgetId : appWidgetIds) {
            Cursor cursor = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    "symbol=?", new String[]{"FB"}, null);
            cursor.moveToNext();
            String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
            float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);
            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            Timber.d(symbol);
            cursor.close();

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.stockhawk_appwidget);

            remoteViews.setTextViewText(R.id.symbol, symbol);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(price));
            // Using setInt to change background of textview in remoteviews
            if (rawAbsoluteChange > 0) {
                remoteViews.setInt(R.id.change, "setBackgroundColor", R.drawable.percent_change_pill_green);
            } else {
                remoteViews.setInt(R.id.change, "setBackgroundColor", R.drawable.percent_change_pill_red);
            }
            if (PrefUtils.getDisplayMode(this)
                    .equals(getString(R.string.pref_display_mode_absolute_key))) {
                remoteViews.setTextViewText(R.id.change, change);
            } else {
                remoteViews.setTextViewText(R.id.change, percentage);
            }
            Timber.d(String.valueOf(price));
            Timber.d(String.valueOf(appWidgetId));
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
