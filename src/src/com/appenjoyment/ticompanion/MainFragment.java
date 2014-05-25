
package com.appenjoyment.ticompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

	private TextView m_countdownView;
	private TextView m_countdownCaptionView;
}