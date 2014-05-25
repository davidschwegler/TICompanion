
package com.appenjoyment.ticompanion;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;

public class CountdownWidgetProvider extends AppWidgetProvider
{
	public CountdownWidgetProvider()
	{
		Log.i(TAG, "ctor");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		Log.i(TAG, "onUpdate");

		CountdownWidgetManager.getInstance().verifyInit(context.getApplicationContext(), appWidgetIds);
		CountdownWidgetManager.getInstance().onWidgetsUpdate(appWidgetIds);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		Log.i(TAG, "onDeleted");

		CountdownWidgetManager.getInstance().verifyInit(context.getApplicationContext(), appWidgetIds);
		CountdownWidgetManager.getInstance().onWidgetsDeleted(appWidgetIds);
	}

	private static final String TAG = "CountdownWidgetProvider";
}