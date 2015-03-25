package ca.ualberta.ev3ye.logic.control;

import android.view.MotionEvent;

/**
 * Created by Yuey on 2015-03-25.
 */
public abstract class ControlHandler
{
	protected ControlEventCallbacks callbackTarget;

	public ControlHandler( ControlEventCallbacks callbackTarget )
	{
		this.callbackTarget = callbackTarget;
	}

	public abstract void receiveMotionEvent( MotionEvent event );
	public abstract String getName();

	public interface ControlEventCallbacks
	{
		public void onDispatcherSetupFailure( String msg );
		public void onDispatcherFailure( String msg );
		public void onControlEventResult( String controlMessage );
	}
}
