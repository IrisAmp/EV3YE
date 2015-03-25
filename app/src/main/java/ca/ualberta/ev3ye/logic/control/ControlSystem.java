package ca.ualberta.ev3ye.logic.control;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ca.ualberta.ev3ye.auxiliary.Helper;

/**
 * Created by Yuey on 2015-03-25.
 * <p/>
 * A state machine that deals with control events.
 */
public class ControlSystem
{
	protected static final ControlHandler              DEFAULT_CONTROL_PROVIDER = null;
	protected static final int                         NUM_WORKER_THREADS       = 1;
	protected static final int                         DEFAULT_PERIOD_MS        = 1000;
	protected              ControlHandler              controlState             = null;
	protected              ScheduledThreadPoolExecutor threadPool               = null;
	protected              ScheduledFuture             updateTask               = null;

	public ControlSystem()
	{
		threadPool = new ScheduledThreadPoolExecutor( NUM_WORKER_THREADS );
	}

	public ControlSystem( ControlHandler provider )
	{
		this();
		setControlState( provider );
	}

	public void setControlState( ControlHandler newState )
	{
		String oldName = ( controlState != null ) ? controlState.getName() : "null";
		String newName = ( newState != null ) ? newState.getName() : "null";
		Helper.LogV( "CTRL", "Control state switching from " + oldName + " to " + newName );

		this.controlState = newState;
	}

	public void start()
	{
		if ( updateTask == null )
		{
			updateTask = threadPool.scheduleAtFixedRate( new Ticker(),
														 0,
														 DEFAULT_PERIOD_MS,
														 TimeUnit.MILLISECONDS );
		}
		else
		{
			Helper.LogW( "CTRL",
						 "start() called on the ControlHandler, but an update task has already started." );
		}
	}

	public void stop()
	{
		if ( updateTask != null )
		{
			updateTask.cancel( true );
			updateTask = null;
		}
		else
		{
			Helper.LogW( "CTRL",
						 "stop() called on the ControlHandler, but the update task is already null." );
		}
	}

	protected void tick()
	{
		// TODO
	}

	protected class Ticker
			implements Runnable
	{
		@Override
		public void run()
		{
			ControlSystem.this.tick();
		}
	}
}
