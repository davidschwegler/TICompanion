package com.appenjoyment.ticompanion;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
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

		m_showNotification = getUserPreferences().getBoolean(PREFERENCE_NAME_SHOW_NOTIFICATION, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		m_countdownView = (TextView) rootView.findViewById(R.id.countdown);
		m_countdownCaptionView = (TextView) rootView.findViewById(R.id.countdown_caption);
		updateNotificationVisibility();
		updateTimeUntil();

		return rootView;
	}

	private void onShowNotificationChanged(boolean newValue)
	{
		if (m_showNotification != newValue)
		{
			m_showNotification = newValue;
			getUserPreferences().edit().putBoolean(PREFERENCE_NAME_SHOW_NOTIFICATION, m_showNotification).commit();

			updateNotificationVisibility();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.main, menu);

		MenuItem itemNotification = menu.findItem(R.id.action_show_notification);
		itemNotification.setChecked(m_showNotification);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_show_notification:
			onShowNotificationChanged(!item.isChecked());
			item.setChecked(m_showNotification);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void updateTimeUntil()
	{
		TimeUntil until = TimeUntil.TimeUntilDate(TIInfo.Date2014LocalTime);
		if (until.hasSeconds())
		{
			m_countdownView.setText(TIInfo.createDisplayString(until, getResources()));
		}
		else
		{
			m_countdownView.setVisibility(View.GONE);
			m_countdownCaptionView.setText(TIInfo.createDisplayString(until, getResources()));
		}
	}

	private void updateNotificationVisibility()
	{
		if (m_showNotification)
			showNotification();
		else
			hideNotification();
	}

	private void showNotification()
	{
		Notification notification = new NotificationCompat.Builder(getActivity())
				.setContentTitle("TI4 Countdown")
				// getActivity().getResources().getString("TI4 Countdown"))
				.setContentText(TIInfo.createDisplayString(TimeUntil.TimeUntilDate(TIInfo.Date2014LocalTime), getResources()))
				.setOnlyAlertOnce(true)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
				.setOngoing(true)
				.build();

		NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void hideNotification()
	{
		NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
	}

	private SharedPreferences getUserPreferences()
	{
		return getActivity().getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE);
	}

	private static final int NOTIFICATION_ID = 0;

	private static final String PREFERENCE_NAME_SHOW_NOTIFICATION = "ShowNotification";
	private static final String PREFERENCES_USER = "UserPreferences";
	private TextView m_countdownView;
	private TextView m_countdownCaptionView;
	private boolean m_showNotification;
}