package com.appenjoyment.ticompanion;

import org.joda.time.DateTime;
import android.content.res.Resources;

public abstract class CountdownDisplay
{
	public CountdownDisplay(CountdownDisplayKind kind, Resources resources, int titleResourceId)
	{
		m_kind = kind;
		m_resources = resources;
		m_titleResourceId = titleResourceId;
	}

	public final CountdownDisplayKind getKind()
	{
		return m_kind;
	}

	public final String getTitle()
	{
		if (m_titleString == null)
			m_titleString = m_resources.getString(m_titleResourceId);

		return m_titleString;
	}

	public DateTime getCurrentTime()
	{
		return m_currentTime;
	}

	public void setCurrentTime(DateTime currentTime)
	{
		if (currentTime == null)
			throw new IllegalArgumentException("currentTime is null");

		m_currentTime = currentTime;
		m_isInPast = TIInfo.DateTILocalTime.isBefore(m_currentTime);
		m_rendered = render();
	}

	public String getCurrentTimeRendered()
	{
		return m_rendered;
	}

	public boolean isInPast()
	{
		return m_isInPast;
	}

	protected abstract String render();

	protected Resources getResources()
	{
		return m_resources;
	}

	private final CountdownDisplayKind m_kind;
	private final Resources m_resources;
	private final int m_titleResourceId;
	private String m_titleString;
	private DateTime m_currentTime;
	private String m_rendered;
	private boolean m_isInPast;
}