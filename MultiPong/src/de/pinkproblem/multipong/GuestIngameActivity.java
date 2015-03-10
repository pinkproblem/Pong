package de.pinkproblem.multipong;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class GuestIngameActivity extends IngameActivity {

	private final ArrayList<UUID> uuid;
	private BroadcastReceiver receiver;
	private BluetoothAdapter adapter;

	private ConnectThread connectThread;
	private ConnectedThread connectedThread;

	private byte[] score;

	private Dialog connectingDialog;

	public GuestIngameActivity() {
		super();

		score = new byte[PongGame.numberOfPlayers];

		uuid = new ArrayList<UUID>();
		uuid.add(UUID.fromString("2eaa658c-bda9-451d-a942-d7d16705b373"));
		uuid.add(UUID.fromString("6bb266a3-bd93-4783-b298-501cf4dabb8e"));
		uuid.add(UUID.fromString("b4b8ba7e-36b2-4a7c-ad2b-2349479852d9"));

		inputView.setOnTouchListener(new OnTouchListener() {

			float last;
			int lastAction = MotionEvent.ACTION_UP;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO
				hideHint();

				float y = event.getRawY();
				if (lastAction == MotionEvent.ACTION_UP) {
					last = y;
				}
				float dst = last - y;
				// move own player shield by calculated distance
				// TODO some scaling
				final double scaling = 0.05;
				me.move(scaling * -dst);
				last = y;
				lastAction = event.getActionMasked();

				if (connectedThread != null) {
					byte[] pos = Utility.toByteArray(me.getShieldyPosition());
					connectedThread.write(pos);
				}

				return true;
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = BluetoothAdapter.getDefaultAdapter();

		// Create a BroadcastReceiver for ACTION_FOUND
		receiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_UUID.equals(action)) {
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
								// Thread connectThread = new ConnectThread(
								// device, remoteUuid);
								// connectThread.start();
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
		super.onStart();

		if (adapter == null) {
			connectionFailed();
		}

		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
		for (BluetoothDevice device : pairedDevices) {
			device.fetchUuidsWithSdp();
		}
		try {
			wait(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (BluetoothDevice device : pairedDevices) {
			ParcelUuid[] uuids = device.getUuids();
			for (ParcelUuid remoteUuid : uuids) {
				if (remoteUuid.getUuid().equals(uuid.get(0))) {
					connectThread = new ConnectThread(device,
							remoteUuid.getUuid());
					connectThread.start();
				}
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	void showConnectingDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();

		builder.setView(inflater.inflate(R.layout.connecting_dialog, null));
		connectingDialog = builder.create();
		connectingDialog.show();
	}

	void hideConnectionDialog() {
		if (connectingDialog != null) {
			connectingDialog.hide();
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket socket;
		private BluetoothDevice device;
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
				connectionFailed();
			}

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					hideConnectionDialog();
				}
			});
			connectedThread = new ConnectedThread(socket);
			connectedThread.start();
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e("", "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket socket;
		private final InputStream inStream;
		private final OutputStream outStream;

		public ConnectedThread(BluetoothSocket socket) {
			Log.d("", "create ConnectedThread");
			this.socket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e("", "temp sockets not created", e);
				connectionFailed();
			}
			inStream = tmpIn;
			outStream = tmpOut;
		}

		public void run() {
			Log.i("", "BEGIN mConnectedThread");
			byte[] buffer = new byte[16];
			int bytes;
			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = inStream.read(buffer);
					if (bytes != 4) {
						throw new IllegalStateException(
								"Expected 4 bytes but got:" + bytes);
					}
					for (int i = 0; i < score.length; i++) {
						score[i] = buffer[i];
					}
				} catch (IOException e) {
					Log.e("", "disconnected", e);
					connectionFailed();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				outStream.write(buffer);
			} catch (IOException e) {
				Log.e("", "Exception during write", e);
			}
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
