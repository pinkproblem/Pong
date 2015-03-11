package de.pinkproblem.multipong;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class ConnectionManager {

	private Context context;

	private final ArrayList<UUID> uuid;
	private final BluetoothAdapter adapter;
	private ListenThread listenThread;
	private ConnectedThread connectedThread1;
	private Handler handler;

	private LEDMatrixBTConn cmConnection;
	// protected String cmDeviceName;
	// Remote display x and y size.
	protected static final int X_SIZE = 24;
	protected static final int Y_SIZE = 24;
	// Remote display color mode. 0 = red, 1 = green, 2 = blue, 3 = RGB.
	protected static final int COLOR_MODE = 0;

	// The name this app uses to identify with the server.
	protected static final String APP_NAME = "de.pinkproblem.MultiPong";

	public static final int MESSAGE_ERROR = 0;
	public static final int MESSAGE_RECEIVE = 1;
	public static final int MESSAGE_NEW_PLAYER = 2;
	public static final int MESSAGE_PLAYER_LEFT = 3;

	public ConnectionManager(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
		// get connection device from settings
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String cmDeviceName = sharedPref.getString("Connection Machine Device",
				"ledpi-teco");

		cmConnection = new LEDMatrixBTConn(context, cmDeviceName, X_SIZE,
				Y_SIZE, COLOR_MODE, APP_NAME);

		adapter = BluetoothAdapter.getDefaultAdapter();

		// unique uuid for each possible client
		uuid = new ArrayList<UUID>();
		uuid.add(UUID.fromString("2eaa658c-bda9-451d-a942-d7d16705b373"));
		uuid.add(UUID.fromString("6bb266a3-bd93-4783-b298-501cf4dabb8e"));
		uuid.add(UUID.fromString("b4b8ba7e-36b2-4a7c-ad2b-2349479852d9"));
	}

	public void setCmDeviceName(String name) {
		cmConnection.setCmDeviceName(name);
	}

	public boolean connectToCM() {
		return cmConnection.connect();
	}

	public boolean sendToCm(byte[] msg) {
		return cmConnection.write(msg);
	}

	public void closeCMConnection() {
		cmConnection.closeConnection();
	}

	public LEDMatrixBTConn getCmConnection() {
		return cmConnection;
	}

	public void startListening() {
		if (listenThread != null) {
			listenThread.cancel();
			listenThread = null;
		}
		listenThread = new ListenThread(1);
		listenThread.start();
	}

	public void stop() {
		if (cmConnection != null) {
			cmConnection.closeConnection();
		}
		if (listenThread != null) {
			listenThread.cancel();
			listenThread = null;
		}
		if (connectedThread1 != null) {
			connectedThread1.cancel();
			connectedThread1 = null;
		}
	}

	public void sendGameInfo(byte[] gameInfo) {
		if (connectedThread1 != null) {
			connectedThread1.write(gameInfo);
		}
	}

	private class ListenThread extends Thread {
		private BluetoothServerSocket serverSocket;
		// indicating which game place is advertised
		private int index;

		public ListenThread(int index) {
			this.index = index;
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
				tmp = adapter.listenUsingRfcommWithServiceRecord(APP_NAME,
						uuid.get(0));
			} catch (IOException e) {
			}
			serverSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					Log.d("", "Connected successfully with third party");
					handler.obtainMessage(MESSAGE_NEW_PLAYER).sendToTarget();
					connectedThread1 = new ConnectedThread(socket);
					connectedThread1.start();
					try {
						serverSocket.close();
					} catch (IOException e) {
					}
					break;
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}
	}

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
			}
			inStream = tmpIn;
			outStream = tmpOut;
		}

		public void run() {
			Log.i("", "BEGIN mConnectedThread");
			byte[] buffer = new byte[8];
			int bytes;
			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = inStream.read(buffer);
					if (bytes != 8) {
						throw new IllegalStateException(
								"Expected 8 bytes but got:" + bytes);
					}
					Message msg = handler.obtainMessage(MESSAGE_RECEIVE);
					Bundle bundle = new Bundle();
					bundle.putByteArray("MESSAGE_RECEIVE", buffer);
					msg.setData(bundle);
					handler.sendMessage(msg);
				} catch (IOException e) {
					Log.e("", "disconnected", e);
					handler.obtainMessage(MESSAGE_PLAYER_LEFT).sendToTarget();
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
