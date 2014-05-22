
package com.appenjoyment.ticompanion;

import java.util.Random;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class CountdownService extends Service
{
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(LOG, "onStart");

		// // create some random data
		// AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		//
		// int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
		//
		// ComponentName thisWidget = new ComponentName(getApplicationContext(), CountdownWidgetProvider.class);
		// int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
		// Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
		// Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));
		//
		// for (int widgetId : allWidgetIds)
		// {
		// // create some random data
		// int number = (new Random().nextInt(100));
		//
		// RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);
		// Log.w(LOG, "Setting to " + number);
		//
		// remoteViews.setTextViewText(R.id.update, "Random: " + number);
		//
		// Intent clickIntent = new Intent(getApplicationContext(), CountdownWidgetProvider.class);
		//
		// clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		// clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		//
		// PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		// remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
		// appWidgetManager.updateAppWidget(widgetId, remoteViews);
		// }
		//
		// stopSelf();

		updateClientList();
		if (hasClients())
		{
			if (m_until == null)
				updateTime();
			updateClients();
		}

		if (m_runner == null)
		{
			m_runner = new Runnable()
			{
				@Override
				public void run()
				{
					updateClientList();
					if (hasClients())
					{
						updateTime();
						updateClients();

						if (m_until.hasSeconds())
							s_handler.postDelayed(this,
									// m_until.hasMonths() ? 60 * 60 * 1000 /* 1hr */: m_until.hasDays() ? 60 * 1000 /* 1min */:
									1000 /* 1s */);
					}
					else
					{
						m_until = null;
						m_runner = null;
						stopSelf();
					}
				}
			};
			s_handler.post(m_runner);
		}

		return START_REDELIVER_INTENT;
	}

	// set boot receiver
	// -- starts this service
	// onStartCommand
	// - calls handler to update
	// Handler
	// - obtains widget list, whether notification is enabled, whether is bound (for UI)
	// - updates widgets, notification, bound object
	// - schedules for 1s/etc (min interval)
	// - if none, stopSelf

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private void updateTime()
	{
		m_until = TimeUntil.TimeUntilDate(TIInfo.Date2014LocalTime);
	}

	private void updateClients()
	{
		String rendered = TIInfo.createDisplayString(m_until, getResources());
		Log.i(LOG, "Setting to " + rendered);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		for (int widgetId : m_allWidgetIds)
		{
			RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);
			remoteViews.setTextViewText(R.id.update, rendered);

			Intent clickIntent = new Intent(getApplicationContext(), MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
	}

	private boolean hasClients()
	{
		return m_allWidgetIds != null && m_allWidgetIds.length != 0;
	}

	private void updateClientList()
	{
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(), CountdownWidgetProvider.class);
		m_allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		Log.i(LOG, "Widget count: " + m_allWidgetIds.length);
	}

	private TimeUntil m_until;
	private Runnable m_runner;
	private int[] m_allWidgetIds;
	private static final Handler s_handler = new Handler();
	private static final String LOG = "CountdownService";
}