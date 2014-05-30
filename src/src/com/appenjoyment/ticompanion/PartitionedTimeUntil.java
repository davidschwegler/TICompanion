package com.appenjoyment.ticompanion;

public final class PartitionedTimeUntil
{
	public int Months;

	public int Days;

	public int Hours;

	public int Minutes;

	public int Seconds;

	public boolean hasMonths()
	{
		return Months > 0;
	}

	public boolean hasDays()
	{
		return Days > 0 || hasMonths();
	}

	public boolean hasHours()
	{
		return Hours > 0 || hasDays();
	}

	public boolean hasMinutes()
	{
		return Minutes > 0 || hasHours();
	}

	public boolean hasSeconds()
	{
		return Seconds > 0 || hasMinutes();
	}
}