package com.appenjoyment.ticompanion;

import android.content.res.Resources;

public final class StaticTextCountdownDisplay extends CountdownDisplay
{
	public StaticTextCountdownDisplay(CountdownDisplayKind kind, Resources resources, int titleResourceId, int textResourceId)
	{
		super(kind, resources, titleResourceId);

		m_textResourceId = textResourceId;
	}

	@Override
	protected String render()
	{
		return getResources().getString(m_textResourceId);
	}

	private final int m_textResourceId;
}