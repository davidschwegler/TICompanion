package com.appenjoyment.ticompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainFragment extends Fragment
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		m_countdownView = (TextView) rootView.findViewById(R.id.countdown);
		m_countdownCaptionView = (TextView) rootView.findViewById(R.id.countdown_caption);
		updateTimeUntil();

		return rootView;
	}

	private void onShowNotificationChanged(boolean newValue)
	{
		if (newValue)
			CountdownNotificationManager.getInstance().showNotification();
		else
			CountdownNotificationManager.getInstance().hideNotification();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.main, menu);

		MenuItem itemNotification = menu.findItem(R.id.action_show_notification);
		itemNotification.setChecked(CountdownNotificationManager.getInstance().isShowingNotification());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_show_notification:
			onShowNotificationChanged(!item.isChecked());
			item.setChecked(CountdownNotificationManager.getInstance().isShowingNotification());
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		stopService();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		startService();
	}

	private void updateTimeUntil()
	{
		TimeUntil until = CountdownService.getCurrentTimeUntil();
		String rendered = CountdownService.getCurrentTimeUntilRendered();
		if (rendered == null)
		{
			if (LOG_DEBUG)
				Log.d(TAG, "Wanted to update, but current time is null");
			return;
		}

		if (LOG_DEBUG)
			Log.d(TAG, "Setting to " + rendered);
		if (until.hasSeconds())
		{
			m_countdownView.setVisibility(View.VISIBLE);
			m_countdownView.setText(rendered);
		}
		else
		{
			m_countdownView.setVisibility(View.GONE);
			m_countdownCaptionView.setText(rendered);
		}
	}

	private void startService()
	{
		Log.i(TAG, "startService()");
		Intent intent = new Intent(getActivity(), CountdownService.class);
		intent.putExtra(CountdownService.EXTRA_CLIENT_ID, CLIENT_ID);
		intent.putExtra(CountdownService.EXTRA_REQUEST_KIND, CountdownService.REQUEST_KIND_REGISTER);
		getActivity().startService(intent);

		m_countdownReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if (LOG_DEBUG)
					Log.d(TAG, "BroadcastReceiver.onReceive()");
				updateTimeUntil();
			}
		};

		IntentFilter filter = new IntentFilter(CountdownService.BROADCAST_ACTION_UPDATE);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(m_countdownReceiver, filter);
	}

	private void stopService()
	{
		Log.i(TAG, "stopService()");

		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(m_countdownReceiver);
		m_countdownReceiver = null;

		Intent intent = new Intent(getActivity(), CountdownService.class);
		intent.putExtra(CountdownService.EXTRA_CLIENT_ID, CLIENT_ID);
		intent.putExtra(CountdownService.EXTRA_REQUEST_KIND, CountdownService.REQUEST_KIND_UNREGISTER);
		getActivity().startService(intent);
	}

	private static final boolean LOG_DEBUG = BuildConfig.DEBUG;
	private static final String TAG = "MainFragment";
	private static final String CLIENT_ID = "MainFragment";

	private BroadcastReceiver m_countdownReceiver;
	private TextView m_countdownView;
	private TextView m_countdownCaptionView;
}