
package com.appenjoyment.ticompanion;

import java.util.HashSet;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
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

	public static TimeUntil getCurrentTimeUntil()
	{
		return s_currentTimeUntil;
	}

	public static String getCurrentTimeUntilRendered()
	{
		return s_currentTimeUntilRendered;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(TAG, "onStart");

		if (intent == null)
		{
			Log.d(TAG, "Null intent");

			// recreated -- allow the application's registrations to handle this
			if (m_clients.size() == 0)
				stopSelf();
		}
		else
		{
			String clientId = intent.getStringExtra(EXTRA_CLIENT_ID);
			String requestKind = intent.getStringExtra(EXTRA_REQUEST_KIND);
			if (REQUEST_KIND_REGISTER.equals(requestKind))
			{
				Log.i(TAG, REQUEST_KIND_REGISTER);

				if (m_clients.add(clientId) && m_clients.size() == 1)
					startTrackingTimeUntil();
			}
			else if (REQUEST_KIND_UNREGISTER.equals(requestKind))
			{
				Log.i(TAG, REQUEST_KIND_UNREGISTER);
				if (m_clients.remove(clientId) && m_clients.size() == 0)
					stopTrackingTimeUntil();
			}
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private void startTrackingTimeUntil()
	{
		Log.i(TAG, "startTrackingTimeUntil()");
		m_runner = new Runnable()
		{
			@Override
			public void run()
			{
				Log.d(TAG, "Runner called");

				// sanity check
				if (m_runner != this)
				{
					Log.e(TAG, "Runner was called, but not current, current = " + m_runner);
					return;
				}

				updateTime();

				if (s_currentTimeUntil.hasSeconds())
				{
					long delay = TIInfo.getRefreshFrequency(s_currentTimeUntil);

					Log.d(TAG, "Runner posting again in " + delay + "ms");
					s_handler.postDelayed(this, delay);
				}

				// update clients second in case this broadcast causes them to remove themselves...though this won't happen in the current app code
				updateClients();
			}
		};
		m_runner.run();
	}

	private void stopTrackingTimeUntil()
	{
		Log.i(TAG, "stopTrackingTimeUntil()");
		s_handler.removeCallbacks(m_runner);
		m_runner = null;

		stopSelf();
	}

	private void updateTime()
	{
		s_currentTimeUntil = TimeUntil.TimeUntilDate(TIInfo.Date2014LocalTime);
		s_currentTimeUntilRendered = TIInfo.createDisplayString(s_currentTimeUntil, getResources());

		Log.d(TAG, "Updated time until: " + s_currentTimeUntilRendered);
	}

	private void updateClients()
	{
		Intent intent = new Intent(BROADCAST_ACTION_UPDATE);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}

	private static final String TAG = "CountdownService";
	private static final Handler s_handler = new Handler();
	private static String s_currentTimeUntilRendered;
	private static TimeUntil s_currentTimeUntil;
	private Runnable m_runner;
	private HashSet<String> m_clients;
}