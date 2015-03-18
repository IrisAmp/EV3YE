package ca.ualberta.ev3ye.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.ev3ye.R;
import ca.ualberta.ev3ye.auxiliary.AppState;
import ca.ualberta.ev3ye.auxiliary.TwoLineArrayAdapter;
import ca.ualberta.ev3ye.auxiliary.WiFiP2PBroadcastReceiver;
import ca.ualberta.ev3ye.logic.BluetoothCom;


public class MainActivity
		extends Activity
		implements WiFiP2PBroadcastReceiver.WiFiP2PBroadcastCallbacks
{
	protected ViewHolder               viewHolder            = null;
	protected BluetoothCom             com                   = null;
	protected WifiP2pManager           p2pManager            = null;
	protected WifiP2pManager.Channel   p2pChannel            = null;
	protected WifiP2pConfig            p2pConfig             = null;
	protected WiFiP2PBroadcastReceiver p2pBroadcastReceiver  = null;
	protected IntentFilter             p2pIntentFilter       = null;
	protected P2PDiscoveryReceiver     p2pDiscoveryReceiver  = null;
	protected P2PPeerListReceiver      p2pPeerListReceiver   = null;
	protected P2PConnectionReceiver    p2pConnectionReceiver = null;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		Log.v( AppState.LOG_TAG, "[INFO] > ----- MainActivity onCreate() -----" );
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		viewHolder = new ViewHolder();
		viewHolder.init();

		initWiFiP2p();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.registerReceiver( p2pBroadcastReceiver, p2pIntentFilter );

		//viewHolder.bluetoothRefreshButton.callOnClick();
		viewHolder.wifiP2pRefreshButton.callOnClick();
	}

	@Override
	protected void onSaveInstanceState( Bundle outState )
	{
		super.onSaveInstanceState( outState );
		// TODO
	}

	@Override
	protected void onRestoreInstanceState( Bundle savedInstanceState )
	{
		super.onRestoreInstanceState( savedInstanceState );
		// TODO
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		this.unregisterReceiver( p2pBroadcastReceiver );
	}

	@Override
	protected void onDestroy()
	{
		Log.v( AppState.LOG_TAG, "[INFO] > ----- MainActivity onDestroy() -----" );
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		getMenuInflater().inflate( R.menu.menu_main, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch ( item.getItemId() )
		{
		case R.id.action_settings:
			return true;

		default:
			return super.onOptionsItemSelected( item );
		}
	}

	@Override
	public void onP2pStateChanged( Context context, Intent intent )
	{
		Log.v( AppState.LOG_TAG, "[WIFI] > P2P state changed." );
	}

	@Override
	public void onP2pPeersChanged( Context context, Intent intent )
	{
		Log.v( AppState.LOG_TAG, "[WIFI] > P2P peers changed." );
		if ( p2pManager != null )
		{
			p2pManager.requestPeers( p2pChannel, p2pPeerListReceiver );
		}
	}

	@Override
	public void onP2pConnectionChanged( Context context, Intent intent )
	{
		Log.v( AppState.LOG_TAG, "[WIFI] > P2P connection changed." );
	}

	@Override
	public void onP2pThisDeviceChanged( Context context, Intent intent )
	{
		Log.v( AppState.LOG_TAG, "[WIFI] > P2P local device changed." );
	}

	public void connectBT()
	{
		com = new BluetoothCom();
		com.enableBT( this );
		if ( com.connectToNXTs() )
		{
			try
			{
				com.writeMessage( "Hello EV3" );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	private void initWiFiP2p()
	{
		p2pManager = (WifiP2pManager) getSystemService( Context.WIFI_P2P_SERVICE );
		p2pChannel = p2pManager.initialize( this, getMainLooper(), null );
		p2pBroadcastReceiver = new WiFiP2PBroadcastReceiver( this );
		p2pDiscoveryReceiver = new P2PDiscoveryReceiver();
		p2pPeerListReceiver = new P2PPeerListReceiver();
		p2pConnectionReceiver = new P2PConnectionReceiver();

		p2pIntentFilter = new IntentFilter();
		p2pIntentFilter.addAction( WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION );
		p2pIntentFilter.addAction( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION );
		p2pIntentFilter.addAction( WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION );
		p2pIntentFilter.addAction( WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION );
	}

	/**
	 * A receiver for P2P discovery results. Simply notifies success or failure.
	 */
	protected class P2PDiscoveryReceiver
			implements WifiP2pManager.ActionListener
	{

		@Override
		public void onSuccess()
		{
			Log.v( AppState.LOG_TAG, "[WIFI] > P2P discovery success." );
		}

		@Override
		public void onFailure( int reason )
		{
			Log.e( AppState.LOG_TAG, "[WIFI] > P2P discovery failed with reason: " + reason );
		}
	}

	/**
	 * A receiver for P2P peer lists. Provides the list of all available WiFi peers.
	 */
	protected class P2PPeerListReceiver
			implements WifiP2pManager.PeerListListener
	{
		@Override
		public void onPeersAvailable( WifiP2pDeviceList peers )
		{
			Log.v( AppState.LOG_TAG, "[WIFI] > P2P peers available." );
			viewHolder.populateP2pList( peers );
		}
	}

	/**
	 * A receiver for P2P connection events.
	 */
	protected class P2PConnectionReceiver
			implements WifiP2pManager.ActionListener
	{
		@Override
		public void onSuccess()
		{
			Log.v( AppState.LOG_TAG, "[WIFI] > P2P connection accepted!" );
		}

		@Override
		public void onFailure( int reason )
		{
			Log.e( AppState.LOG_TAG, "[WIFI] > P2P connection failed with reason: " + reason );
		}
	}

	protected class ViewHolder
	{
		public              ImageButton                         bluetoothAcceptButton  = null;
		public              ImageButton                         bluetoothRefreshButton = null;
		public              ImageButton                         wifiP2pAcceptButton    = null;
		public              ImageButton                         wifiP2pRefreshButton   = null;
		public              Spinner                             bluetoothSpinner       = null;
		public              Spinner                             wifiP2pSpinner         = null;
		public              TwoLineArrayAdapter                 bluetoothArrayAdapter  = null;
		public              TwoLineArrayAdapter                 wifiP2pArrayAdapter    = null;
		public              List< Pair< String, String > >      bluetoothDevices       = null;
		public              ArrayList< Pair< String, String > > wifiP2pDevices         = null;

		public ViewHolder()
		{
			bluetoothAcceptButton = (ImageButton) findViewById( R.id.a_main_bluetooth_accept_button );
			bluetoothRefreshButton = (ImageButton) findViewById( R.id.a_main_bluetooth_refresh_button );
			wifiP2pAcceptButton = (ImageButton) findViewById( R.id.a_main_wifi_accept_button );
			wifiP2pRefreshButton = (ImageButton) findViewById( R.id.a_main_wifi_refresh_button );
			bluetoothSpinner = (Spinner) findViewById( R.id.a_main_bluetooth_spinner );
			wifiP2pSpinner = (Spinner) findViewById( R.id.a_main_wifi_spinner );

			bluetoothDevices = new ArrayList<>();
			bluetoothArrayAdapter = new TwoLineArrayAdapter( MainActivity.this, bluetoothDevices );
			bluetoothArrayAdapter.setDropDownViewResource( R.layout.list_item_spinner );
			bluetoothSpinner.setAdapter( bluetoothArrayAdapter );

			wifiP2pDevices = new ArrayList<>();
			wifiP2pArrayAdapter = new TwoLineArrayAdapter( MainActivity.this, wifiP2pDevices );
			wifiP2pArrayAdapter.setDropDownViewResource( R.layout.list_item_spinner );
			wifiP2pSpinner.setAdapter( wifiP2pArrayAdapter );
		}

		public void init()
		{
			populateBtList();
			populateP2pList( new WifiP2pDeviceList() );
			setupListeners();
		}

		private void populateBtList()
		{
			bluetoothDevices.clear();

			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if ( adapter != null )
			{
				Log.v( AppState.LOG_TAG, "[BLUE] > Found bonded devices:" );
				for ( BluetoothDevice device : adapter.getBondedDevices() )
				{
					Log.v( AppState.LOG_TAG,
						   "[BLUE] >     " + device.getName() + " at " + device.getAddress() );
					bluetoothDevices.add( new Pair<>( device.getName(), device.getAddress() ) );
				}
			}

			bluetoothArrayAdapter.notifyDataSetChanged();
		}

		private void populateP2pList( WifiP2pDeviceList peers )
		{
			wifiP2pDevices.clear();

			if ( !peers.getDeviceList().isEmpty() )
			{
				Log.v( AppState.LOG_TAG, "[WIFI] > Found peers:" );
			}

			for ( WifiP2pDevice device : peers.getDeviceList() )
			{
				Log.v( AppState.LOG_TAG,
					   "[WIFI] >     " + device.deviceName + " at " + device.deviceAddress );
				wifiP2pDevices.add( new Pair<>( device.deviceName, device.deviceAddress ) );
			}

			wifiP2pArrayAdapter.notifyDataSetChanged();
		}

		private void setupListeners()
		{
			bluetoothRefreshButton.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					populateBtList();
				}
			} );

			bluetoothAcceptButton.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					AsyncTask< Void, Void, Void > task = new AsyncTask< Void, Void, Void >()
					{
						@Override
						protected Void doInBackground( Void... params )
						{
							connectBT();
							return null;
						}
					};

					task.execute();
				}
			} );

			wifiP2pRefreshButton.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					p2pManager.discoverPeers( p2pChannel, p2pDiscoveryReceiver );
				}
			} );

			wifiP2pAcceptButton.setOnClickListener( new View.OnClickListener()
			{
				@Override
				public void onClick( View v )
				{
					String address = wifiP2pArrayAdapter.getItem( wifiP2pSpinner.getSelectedItemPosition() ).second;

					p2pConfig = new WifiP2pConfig();
					p2pConfig.deviceAddress = address;
					p2pManager.connect( p2pChannel, p2pConfig, p2pConnectionReceiver );
				}
			} );

			bluetoothSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected( AdapterView< ? > parent,
											View view,
											int position,
											long id )
				{
					Log.v( AppState.LOG_TAG,
						   "[ UI ] > bluetoothSpinner.setOnItemSelectedListener.onItemSelected()" );
				}

				@Override
				public void onNothingSelected( AdapterView< ? > parent )
				{
					Log.v( AppState.LOG_TAG,
						   "[ UI ] > bluetoothSpinner.setOnItemSelectedListener.onNothingSelected()" );
				}
			} );

			wifiP2pSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected( AdapterView< ? > parent,
											View view,
											int position,
											long id )
				{
					Log.v( AppState.LOG_TAG,
						   "[ UI ] > wifiP2pSpinner.setOnItemSelectedListener.onItemSelected()" );
				}

				@Override
				public void onNothingSelected( AdapterView< ? > parent )
				{
					Log.v( AppState.LOG_TAG,
						   "[ UI ] > wifiP2pSpinner.setOnItemSelectedListener.onNothingSelected()" );
				}
			} );
		}
	}
}
