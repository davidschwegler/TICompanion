
package com.appenjoyment.ticompanion;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public final class CountdownNotificationManager
{
	public static CountdownNotificationManager getInstance()
	{
		return s_instance != null ? s_instance : (s_instance = new CountdownNotificationManager());
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
		m_showNotification = m_prefs.getBoolean(PREFERENCE_KEY_SHOW_NOTIFICATION, true);
		Log.i(TAG, "Show notification loaded: " + m_showNotification);

		if (m_showNotification)
		{
			startService();
			updateNotification();
		}
	}

	public boolean isShowingNotification()
	{
		return m_showNotification;
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

	public void showNotification()
	{
		Log.i(TAG, "showNotification()");

		if (!m_showNotification)
		{
			m_showNotification = true;

			updateNotificationPrefs();
			startService();
			updateNotification();
		}
	}

	public void hideNotification()
	{
		Log.i(TAG, "hideNotification()");

		if (m_showNotification)
		{
			m_showNotification = false;

			updateNotificationPrefs();
			stopService();
			removeNotification();
		}
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
				Log.d(TAG, "BroadcastReceiver.onReceive()");
				updateNotification();
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

	private void updateNotification()
	{
		String rendered = CountdownService.getCurrentTimeUntilRendered();
		if (rendered == null)
		{
			Log.i(TAG, "Wanted to update, but current time is null");
			return;
		}

		Log.i(TAG, "Setting to " + rendered);

		Notification notification =
				new NotificationCompat.Builder(m_applicationContext)
						.setContentTitle(m_applicationContext.getString(R.string.notification_title))
						.setContentText(rendered)
						.setOnlyAlertOnce(true)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentIntent(
								PendingIntent.getActivity(m_applicationContext, 0, new Intent(m_applicationContext, MainActivity.class),
										PendingIntent.FLAG_UPDATE_CURRENT))
						.setOngoing(true)
						.build();

		NotificationManager notificationManager = (NotificationManager) m_applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void removeNotification()
	{
		NotificationManager notificationManager = (NotificationManager) m_applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
	}

	private void updateNotificationPrefs()
	{
		m_prefs.edit().putBoolean(PREFERENCE_KEY_SHOW_NOTIFICATION, m_showNotification).commit();
	}

	private CountdownNotificationManager()
	{
	}

	private static final String TAG = "CountdownNotificationManager";
	private static final String PREFERENCES_NAME = "CountdownNotificationManager";
	private static final String PREFERENCE_KEY_SHOW_NOTIFICATION = "ShowNotification";
	private static final String CLIENT_ID = "CountdownNotificationManager";
	private static final int NOTIFICATION_ID = 0;

	private static CountdownNotificationManager s_instance;
	private Context m_applicationContext;
	private SharedPreferences m_prefs;
	private boolean m_showNotification;
	private BroadcastReceiver m_countdownReceiver;
}