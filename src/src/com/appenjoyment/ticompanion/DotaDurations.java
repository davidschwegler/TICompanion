package com.appenjoyment.ticompanion;

/**
 * In seconds
 */
public class DotaDurations 
{
	// public static final float TI3Game = TI3GameLength = 33.98f; // can't remember what I included in this
	public static final float TI4Game = 38.05f * 60; // includes group stages, see http://www.dotabuff.com/esports/leagues/600
	public static final float PreviousTIGame = TI4Game; // convenience
	public static final float Teleport = 3;
	public static final float BlackHole = 7;
	public static final float PGGBlackHole = 0.001f; // 1ms, huehuehue
	public static final float DeathWard = 8;
	public static final float DreamCoil = 6;
	public static final float NagaSleep = 7;
	public static final float RoshanTimer = 9.5f * 60; // on avg
	
	
	private DotaDurations()
	{
	}
}
