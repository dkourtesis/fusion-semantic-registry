package org.seerc.fusion.sr.utils;

/**
 * Allows timing of the execution of any block of code. 
 * Copyright (c) 2005, John O'Hanley, Canada 
 * Code from http://www.javapractices.com/
 */
public final class Chronometer
{
	private long fStart;
	private long fStop;

	private boolean fIsRunning;
	private boolean fHasBeenUsedOnce;
	
	
	
	
	/**
	 * Start the stopwatch.
	 * @throws IllegalStateException if the stopwatch is already running.
	 */
	public void start()
	{
		if (fIsRunning)
		{
			throw new IllegalStateException("Must stop before calling start again.");
		}
		//reset both start and stop
		fStart = System.currentTimeMillis();
		fStop = 0;
		fIsRunning = true;
		fHasBeenUsedOnce = true;
	}
	
	
	
	
	
	/**
	 * Stop the stopwatch.
	 * @throws IllegalStateException if the stopwatch is not already running.
	 */
	public void stop()
	{
		if (!fIsRunning)
		{
			throw new IllegalStateException("Cannot stop if not currently running.");
		}
		fStop = System.currentTimeMillis();
		fIsRunning = false;
	}
	
	
	
	
	
	/**
	 * Express the "reading" on the stopwatch.
	 * @throws IllegalStateException if the Stopwatch has never been used,
	 * or if the stopwatch is still running.
	 */
	public String toString()
	{
		validateIsReadable();
		StringBuffer result = new StringBuffer();
		result.append(fStop - fStart);
		result.append(" ms");
		return result.toString();
	}

	
	
	
	
	/**
	 * Express the "reading" on the stopwatch as a numeric type.
	 * @throws IllegalStateException if the Stopwatch has never been used,
	 * or if the stopwatch is still running.
	 */
	public long toValue()
	{
		validateIsReadable();
		return fStop - fStart;
	}
	
	
	
	
	
	/**
	 * Throws IllegalStateException if the watch has never been started,
	 * or if the watch is still running.
	 */
	private void validateIsReadable()
	{
		if (fIsRunning)
		{
			String message = "Cannot read a stopwatch which is still running.";
			throw new IllegalStateException(message);
		}
		if (!fHasBeenUsedOnce)
		{
			String message = "Cannot read a stopwatch which has never been started.";
			throw new IllegalStateException(message);
		}
	}
}
