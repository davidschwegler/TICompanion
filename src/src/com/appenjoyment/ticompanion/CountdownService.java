package com.appenjoyment.ticompanion;

import java.util.HashSet;
import org.joda.time.DateTime;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class CountdownService extends Service
{
	public static final String EXTRA_CLIENT_ID = "ClientId";
	public static final String EXTRA_REQUEST_KIND = "RequestKind";
	public static final String REQUEST_KIND_REGISTER = "RequestKindRegister";
	public static final String REQUEST_KIND_UNREGISTER = "RequestKindUnregister";
	public static final String BROADCAST_ACTION_UPDATE = "ActionUpdate";

	public CountdownService()
	{
		m_clients = new HashSet<String>();
	}

	public static CountdownDisplay getCurrentDisplay()
	{
		return s_currentDisplay;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "onStart");

		if (intent == null)
		{
			Log.i(TAG, "Null intent");
		}
		else
		{
			String clientId = intent.getStringExtra(EXTRA_CLIENT_ID);
			String requestKind = intent.getStringExtra(EXTRA_REQUEST_KIND);
			if (REQUEST_KIND_REGISTER.equals(requestKind))
			{
				if (LOG_DEBUG)
					Log.i(TAG, REQUEST_KIND_REGISTER);

				if (m_clients.add(clientId) && m_clients.size() == 1)
				{
					Log.i(TAG, "Registered first");
					startTrackingTimeUntil();
				}
			}
			else if (REQUEST_KIND_UNREGISTER.equals(requestKind))
			{
				if (LOG_DEBUG)
					Log.i(TAG, REQUEST_KIND_UNREGISTER);
				if (m_clients.remove(clientId) && m_clients.size() == 0)
				{
					Log.i(TAG, "Unregistered all");
					stopTrackingTimeUntil();
					stopSelf();
				}
			}
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		Log.i(TAG, "onDestroy()");
		super.onDestroy();

		stopTrackingTimeUntil();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent)
	{
		Log.i(TAG, "onTaskRemoved()");
		super.onTaskRemoved(rootIntent);

		// when the app is swiped out of the recents list, the service is killed
		// pre-kitkat, the service is restarted after 5s
		// in KitKat, the service isn't restarted automatically at all
		// schedule 2 alarms -- a greedy alarm that fires after 1s, and a fallback that fires after 5 (some forums report that 1s is too fast for some devices)
		Intent restartServiceIntent = new Intent(getApplicationContext(), CountdownService.class);
		restartServiceIntent.setPackage(getPackageName());
		PendingIntent restartServicePendingIntentGreedy = PendingIntent.getService(getApplicationContext(), 0, restartServiceIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent restartServicePendingIntentFallback = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntentGreedy);
		alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5000, restartServicePendingIntentFallback);
	}

	private void startTrackingTimeUntil()
	{
		Log.i(TAG, "startTrackingTimeUntil()");

		m_runner = new Runnable()
		{
			@Override
			public void run()
			{
				if (LOG_DEBUG)
					Log.d(TAG, "Runner called");

				// sanity check
				if (m_runner != this)
				{
					Log.e(TAG, "Runner was called, but not current, current = " + m_runner);
					return;
				}

				updateTime();

				if (!s_currentDisplay.isInPast())
				{
					long delay = 1000;

					if (LOG_DEBUG)
						Log.d(TAG, "Runner posting again in " + delay + "ms");
					s_handler.postDelayed(this, delay);
				}

				// update clients second in case this broadcast causes them to remove themselves...though this won't happen in the current app code
				broadcastTime();
			}
		};

		// register screen on/off receiver to save battery by not running while the screen is off
		IntentFilter screenOnOffFilter = new IntentFilter();
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
		m_screenOnOffReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
					pauseTrackingTimeUntil();
				else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
					resumeTrackingTimeUntil();
			}
		};
		registerReceiver(m_screenOnOffReceiver, screenOnOffFilter);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (pm.isScreenOn())
			m_runner.run();

		// register display changed receiver
		IntentFilter displayChangedFilter = new IntentFilter();
		displayChangedFilter.addAction(CountdownDisplayManager.BROADCAST_ACTION_DISPLAY_CHANGED);
		m_displayChangedReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if (intent.getAction().equals(CountdownDisplayManager.BROADCAST_ACTION_DISPLAY_CHANGED))
				{
					s_handler.removeCallbacks(m_runner);
					m_runner.run();
				}
			}
		};
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(m_displayChangedReceiver, displayChangedFilter);
	}

	private void stopTrackingTimeUntil()
	{
		if (m_runner != null)
		{
			Log.i(TAG, "stopTrackingTimeUntil()");
			s_handler.removeCallbacks(m_runner);
			m_runner = null;

			unregisterReceiver(m_screenOnOffReceiver);
			m_screenOnOffReceiver = null;

			LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(m_displayChangedReceiver);
			m_displayChangedReceiver = null;
		}
	}

	private void pauseTrackingTimeUntil()
	{
		s_handler.removeCallbacks(m_runner);
	}

	private void resumeTrackingTimeUntil()
	{
		m_runner.run();
	}

	private void updateTime()
	{
		s_currentDisplay = CountdownDisplayManager.getInstance().getCurrentDisplay();
		s_currentDisplay.setCurrentTime(DateTime.now());
	}

	private void broadcastTime()
	{
		Intent intent = new Intent(BROADCAST_ACTION_UPDATE);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}

	private static final boolean LOG_DEBUG = BuildConfig.DEBUG;
	private static final String TAG = "CountdownService";
	private static final Handler s_handler = new Handler();
	private static CountdownDisplay s_currentDisplay;
	private Runnable m_runner;
	private HashSet<String> m_clients;
	private BroadcastReceiver m_screenOnOffReceiver;
	private BroadcastReceiver m_displayChangedReceiver;
}