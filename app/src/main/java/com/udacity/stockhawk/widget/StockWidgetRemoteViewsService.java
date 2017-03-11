package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import timber.log.Timber;

import static com.udacity.stockhawk.ui.MainActivity.HISTORY_EXTRA;
import static com.udacity.stockhawk.ui.MainActivity.SYMBOL_EXTRA;

/**
 * Created by Avin on 11-03-2017.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {

    String LOG_TAG = StockWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
                //Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null){
                    data.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                // RemoteView with list item layout
                RemoteViews listRV = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                // Assigning values to variables
                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                String history = data.getString(Contract.Quote.POSITION_HISTORY);
                float price = data.getFloat(Contract.Quote.POSITION_PRICE);
                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
                Timber.d(symbol);

                DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                // Setting values to list remote views
                listRV.setTextViewText(R.id.symbol, symbol);
                listRV.setTextViewText(R.id.price, dollarFormat.format(price));
                // Using setInt to change background of textview in remoteviews
                if (rawAbsoluteChange > 0) {
                    listRV.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    listRV.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                if (PrefUtils.getDisplayMode(StockWidgetRemoteViewsService.this)
                        .equals(getString(R.string.pref_display_mode_absolute_key))) {
                    listRV.setTextViewText(R.id.change, change);
                } else {
                    listRV.setTextViewText(R.id.change, percentage);
                }
                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(SYMBOL_EXTRA, symbol);
                fillInIntent.putExtra(HISTORY_EXTRA, history);
                listRV.setOnClickFillInIntent(R.id.list_item, fillInIntent);

                return listRV;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
