package com.appenjoyment.ticompanion;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class CountdownDisplayManager
{
	public static final String BROADCAST_ACTION_DISPLAY_CHANGED = "DisplayChanged";

	public static CountdownDisplayManager getInstance()
	{
		return s_instance != null ? s_instance : (s_instance = new CountdownDisplayManager());
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

		m_countdownDisplays = new ArrayList<CountdownDisplay>();
		m_countdownDisplays.add(new PartitionedCountdownDisplay(m_applicationContext.getResources(), R.string.app_name));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.Days,
				m_applicationContext.getResources(), R.string.display_title_days, 24 * 60 * 60, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.Hours,
				m_applicationContext.getResources(), R.string.display_title_hours, 60 * 60, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.Minutes,
				m_applicationContext.getResources(), R.string.display_title_minutes, 60, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.Seconds,
				m_applicationContext.getResources(), R.string.display_title_seconds, 1, false));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.TI3GameLengths,
				m_applicationContext.getResources(), R.string.display_title_ti3_games, 33.98f * 60, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.BlackHoles,
				m_applicationContext.getResources(), R.string.display_title_black_holes, 7, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.PGGBlackHoles,
				m_applicationContext.getResources(), R.string.display_title_pgg_black_holes, 0.001f, false));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.DeathWards,
				m_applicationContext.getResources(), R.string.display_title_death_wards, 8, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.DreamCoils,
				m_applicationContext.getResources(), R.string.display_title_dream_coils, 6, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.NagaSleeps,
				m_applicationContext.getResources(), R.string.display_title_naga_songs, 7, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.RoshanTimers,
				m_applicationContext.getResources(), R.string.display_title_roshan_respawns, 9.5f * 60, true));
		m_countdownDisplays.add(new PeriodCountdownDisplay(CountdownDisplayKind.Teleports,
				m_applicationContext.getResources(), R.string.display_title_teleports, 3, true));

		m_prefs = applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
		String kindString = m_prefs.getString(PREFERENCE_KEY_DISPLAY_KIND, null);
		CountdownDisplayKind kind = CountdownDisplayKind.Partitioned;
		if (kindString != null)
		{
			try
			{
				kind = Enum.valueOf(CountdownDisplayKind.class, kindString);
			}
			catch (IllegalArgumentException e)
			{
				Log.e(TAG, "Couldn't reload countdown kind " + kindString);
			}
		}

		Log.i(TAG, "Initializing to: " + kind);

		for (CountdownDisplay display : m_countdownDisplays)
		{
			if (display.getKind() == kind)
			{
				m_currentDisplay = display;
				break;
			}
		}

		// sanity check
		if (m_currentDisplay == null)
			throw new IllegalStateException();
	}

	public final List<CountdownDisplay> getCountdownDisplays()
	{
		return m_countdownDisplays;
	}

	public CountdownDisplay getCurrentDisplay()
	{
		return m_currentDisplay;
	}

	public void setCurrentDisplay(CountdownDisplay countdownDisplay)
	{
		if (countdownDisplay != m_currentDisplay)
		{
			m_currentDisplay = countdownDisplay;
			m_prefs.edit().putString(PREFERENCE_KEY_DISPLAY_KIND, countdownDisplay.getKind().name()).commit();
			Intent intent = new Intent(BROADCAST_ACTION_DISPLAY_CHANGED);
			LocalBroadcastManager.getInstance(m_applicationContext).sendBroadcast(intent);
		}
	}

	private CountdownDisplayManager()
	{
	}

	private static final String TAG = "CountdownDisplayManager";
	private static final String PREFERENCES_NAME = "CountdownDisplayManager";
	private static final String PREFERENCE_KEY_DISPLAY_KIND = "DisplayKind";
	private static CountdownDisplayManager s_instance;

	private List<CountdownDisplay> m_countdownDisplays;
	private Context m_applicationContext;
	private SharedPreferences m_prefs;
	private CountdownDisplay m_currentDisplay;
}
