package com.appenjoyment.ticompanion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

public final class CountdownWidgetManager
{
	public static CountdownWidgetManager getInstance()
	{
		return s_instance != null ? s_instance : (s_instance = new CountdownWidgetManager());
	}

	public void init(Context applicationContext)
	{
		Log.i(TAG, "Init()");
		if (m_applicationContext != null)
		{
			Log.e(TAG, "Init already called!", new RuntimeException());
			return;
		}

		m_applicationContext = applicationContext;
		m_prefs = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		m_widgetIds = new HashSet<Integer>();

		String widgetIdsSaved = m_prefs.getString(PREFERENCE_KEY_WIDGET_IDS, "");
		Log.i(TAG, "Widget Ids loaded: " + widgetIdsSaved);

		if (widgetIdsSaved.length() != 0)
		{
			String[] widgetIdStrings = widgetIdsSaved.split(",");

			for (String widgetIdString : widgetIdStrings)
				m_widgetIds.add(Integer.valueOf(widgetIdString));

			if (m_widgetIds.size() != 0)
			{
				startService();
				updateWidgets();
			}
		}
	}

	public void verifyInit(Context applicationContext, int[] appWidgetIds)
	{
		// sanity check in case the provider's update/delete methods are called before the app's onCreate
		if (m_applicationContext == null)
		{
			Log.e(TAG, "Init was not previously called! Id count = " + appWidgetIds.length, new RuntimeException());
			init(applicationContext);
		}
	}

	// called when widgets were added, the system booted, or a requested update interval elapsed
	// we use this to check for added widgets
	public void onWidgetsUpdate(int[] ids)
	{
		Log.i(TAG, "onWidgetsUpdate()");
		List<Integer> added = new ArrayList<Integer>();
		for (int idToUpdate : ids)
		{
			if (m_widgetIds.add(idToUpdate))
				added.add(idToUpdate);
		}

		// sanity check
		if (added.size() != 0)
		{
			updateWidgetPrefs();

			if (added.size() == m_widgetIds.size())
				startService();

			for (Integer id : added)
				updateWidget(id.intValue());
		}
	}

	// called when widgets are removed
	public void onWidgetsDeleted(int[] ids)
	{
		Log.i(TAG, "onWidgetsDeleted()");
		// sanity check
		if (m_widgetIds.size() == 0)
			return;

		for (int idToDelete : ids)
			m_widgetIds.remove(idToDelete);

		updateWidgetPrefs();

		if (m_widgetIds.size() == 0)
			stopService();
	}

	private void startService()
	{
		Log.i(TAG, "startService()");
		Intent intent = new Intent(m_applicationContext, CountdownService.class);
		intent.putExtra(CountdownService.EXTRA_CLIENT_ID, CLIENT_ID);
		intent.putExtra(CountdownService.EXTRA_REQUEST_KIND, CountdownService.REQUEST_KIND_REGISTER);
		m_applicationContext.startService(intent);

		m_countdownReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if (LOG_DEBUG)
					Log.d(TAG, "BroadcastReceiver.onReceive()");
				updateWidgets();
			}
		};

		IntentFilter filter = new IntentFilter(CountdownService.BROADCAST_ACTION_UPDATE);
		LocalBroadcastManager.getInstance(m_applicationContext).registerReceiver(m_countdownReceiver, filter);
	}

	private void stopService()
	{
		Log.i(TAG, "stopService()");

		LocalBroadcastManager.getInstance(m_applicationContext).unregisterReceiver(m_countdownReceiver);
		m_countdownReceiver = null;

		Intent intent = new Intent(m_applicationContext, CountdownService.class);
		intent.putExtra(CountdownService.EXTRA_CLIENT_ID, CLIENT_ID);
		intent.putExtra(CountdownService.EXTRA_REQUEST_KIND, CountdownService.REQUEST_KIND_UNREGISTER);
		m_applicationContext.startService(intent);
	}

	private void updateWidgets()
	{
		for (Integer id : m_widgetIds)
			updateWidget(id.intValue());
	}

	private void updateWidget(int id)
	{
		CountdownDisplay display = CountdownService.getCurrentDisplay();
		if (display == null)
		{
			if (LOG_DEBUG)
				Log.d(TAG, "Wanted to update, but current countdown is null");
			return;
		}

		String rendered = display.getCurrentTimeRendered();
		if (LOG_DEBUG)
			Log.d(TAG, "Setting to " + rendered);

		RemoteViews remoteViews = new RemoteViews(m_applicationContext.getPackageName(), R.layout.widget_layout);
		remoteViews.setTextViewText(R.id.update, rendered);

		Intent clickIntent = IntentUtility.getBringToFrontIntent(m_applicationContext, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(m_applicationContext, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(m_applicationContext);
		appWidgetManager.updateAppWidget(id, remoteViews);
	}

	private void updateWidgetPrefs()
	{
		StringBuilder widgetIdsStringBuilder = new StringBuilder();
		for (Integer id : m_widgetIds)
		{
			if (widgetIdsStringBuilder.length() != 0)
				widgetIdsStringBuilder.append(',');

			widgetIdsStringBuilder.append(id.intValue());
		}

		String widgetIdsString = widgetIdsStringBuilder.toString();
		Log.i(TAG, "Widget Ids saved: " + widgetIdsString);
		m_prefs.edit().putString(PREFERENCE_KEY_WIDGET_IDS, widgetIdsString).commit();
	}

	private CountdownWidgetManager()
	{
	}

	private static final boolean LOG_DEBUG = BuildConfig.DEBUG;
	private static final String TAG = "CountdownWidgetProvider";
	private static final String PREFERENCES_NAME = "CountdownWidgetManager";
	private static final String PREFERENCE_KEY_WIDGET_IDS = "WidgetIds";
	private static final String CLIENT_ID = "CountdownWidgetManager";

	private static CountdownWidgetManager s_instance;
	private Context m_applicationContext;
	private SharedPreferences m_prefs;
	private HashSet<Integer> m_widgetIds;
	private BroadcastReceiver m_countdownReceiver;
}