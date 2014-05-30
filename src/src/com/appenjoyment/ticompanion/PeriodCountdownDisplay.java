package com.appenjoyment.ticompanion;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import android.content.res.Resources;

public final class PeriodCountdownDisplay extends CountdownDisplay
{
	public PeriodCountdownDisplay(CountdownDisplayKind kind, Resources resources, int titleResourceId, float periodSeconds, boolean showFractionDigits)
	{
		super(kind, resources, titleResourceId);

		m_periodSeconds = periodSeconds;
		m_numberFormat = DecimalFormat.getNumberInstance();

		if (showFractionDigits)
		{
			m_numberFormat.setMinimumFractionDigits(2);
			m_numberFormat.setMaximumFractionDigits(2);
		}
	}

	@Override
	public String render()
	{
		long seconds = TimeUntil.getSecondsUntilDate(getCurrentTime(), TIInfo.Date2014LocalTime);
		if (seconds <= 0)
			return getResources().getString(R.string.date_display_past);

		return m_numberFormat.format(seconds / m_periodSeconds) + " " + getTitle();
	}

	private final float m_periodSeconds;
	private final NumberFormat m_numberFormat;
}