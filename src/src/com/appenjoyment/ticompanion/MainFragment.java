package com.appenjoyment.ticompanion;

import java.util.ArrayList;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
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
		ActionBarActivity activity = ((ActionBarActivity) getActivity());
		activity.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

		final CountdownDisplayManager countdownDisplayManager = CountdownDisplayManager.getInstance();
		List<String> displayTitles = new ArrayList<String>();
		for (CountdownDisplay display : countdownDisplayManager.getCountdownDisplays())
			displayTitles.add(display.getTitle());

		SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, displayTitles)
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
					convertView = getActivity().getLayoutInflater().inflate(R.layout.title_spinner_item, null);

				return super.getView(position, convertView, parent);
			}
		};

		OnNavigationListener onNavigationListener = new OnNavigationListener()
		{
			@Override
			public boolean onNavigationItemSelected(int position, long itemId)
			{
				countdownDisplayManager.setCurrentDisplay(countdownDisplayManager.getCountdownDisplays().get(position));
				return false;
			}
		};
		activity.getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, onNavigationListener);
		activity.getSupportActionBar().setSelectedNavigationItem(
				countdownDisplayManager.getCountdownDisplays().indexOf(countdownDisplayManager.getCurrentDisplay()));

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

		MenuItem shareItem = menu.findItem(R.id.action_share);
		m_shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
		updateShareIntent();
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
		m_countdownDisplay = CountdownService.getCurrentDisplay();
		if (m_countdownDisplay == null)
		{
			if (LOG_DEBUG)
				Log.d(TAG, "Wanted to update, but current countdown is null");
			return;
		}

		String rendered = m_countdownDisplay.getCurrentTimeRendered();
		if (LOG_DEBUG)
			Log.d(TAG, "Setting to " + rendered);

		if (!m_countdownDisplay.isInPast())
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

	private void updateShareIntent()
	{
		if (m_shareActionProvider != null && m_countdownDisplay != null)
		{
			String rendered = m_countdownDisplay.getCurrentTimeRendered();
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, !m_countdownDisplay.isInPast() ?
					getString(R.string.share_format, rendered, SHARE_URL) :
					getString(R.string.share_format_past, rendered, SHARE_URL));
			m_shareActionProvider.setShareIntent(intent);
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
				updateShareIntent();
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
	private static final String SHARE_URL = "https://play.google.com/store/apps/details?id=com.appenjoyment.ticompanion";

	private BroadcastReceiver m_countdownReceiver;
	private ShareActionProvider m_shareActionProvider;
	private TextView m_countdownView;
	private TextView m_countdownCaptionView;
	private CountdownDisplay m_countdownDisplay;
}