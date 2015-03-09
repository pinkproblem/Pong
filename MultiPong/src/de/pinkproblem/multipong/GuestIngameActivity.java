package de.pinkproblem.multipong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;

public class GuestIngameActivity extends IngameActivity {

	private final ArrayList<UUID> uuid;
	private BroadcastReceiver receiver;
	private BluetoothAdapter adapter;

	public GuestIngameActivity() {
		super();
		uuid = new ArrayList<UUID>();
		uuid.add(UUID.fromString("2eaa658c-bda9-451d-a942-d7d16705b373"));
		uuid.add(UUID.fromString("6bb266a3-bd93-4783-b298-501cf4dabb8e"));
		uuid.add(UUID.fromString("b4b8ba7e-36b2-4a7c-ad2b-2349479852d9"));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ingame);

		adapter = BluetoothAdapter.getDefaultAdapter();

		// Create a BroadcastReceiver for ACTION_FOUND
		receiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					ParcelUuid[] uuids = device.getUuids();

					for (ParcelUuid parcelUuid : uuids) {
						for (UUID ownUuid : uuid) {
							UUID remoteUuid = parcelUuid.getUuid();
							if (remoteUuid.equals(ownUuid)) {
								// falls übereinstimmende uuid gefunden:
								// verbinde
								Thread connectThread = new ConnectThread(
										device, remoteUuid);
								connectThread.start();
							}
						}
					}
				}
			}
		};
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onStart() {
		adapter.startDiscovery();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket socket;
		private final BluetoothDevice device;
		private UUID tempUuid;

		public ConnectThread(BluetoothDevice remoteDevice, UUID uuidToTry) {
			device = remoteDevice;
			BluetoothSocket tmp = null;
			tempUuid = uuidToTry;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = remoteDevice.createRfcommSocketToServiceRecord(uuidToTry);
			} catch (IOException e) {
				Log.e("", "create() failed", e);
			}
			socket = tmp;
		}

		public void run() {
			Log.i("", "BEGIN mConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			adapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				socket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					socket.close();
				} catch (IOException e2) {
					Log.e("",
							"unable to close() socket during connection failure",
							e2);
				}
				// connectionFailed();
				// TODO
			}

			// Start the connected thread
			// connected(socket, device);
			// TODO
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e("", "close() of connect socket failed", e);
			}
		}
	}
}
