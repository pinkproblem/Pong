package de.pinkproblem.multipong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

public class ConnectionManager {

	private Context context;

	private LEDMatrixBTConn cmConnection;
	protected static final String REMOTE_BT_DEVICE_NAME = "ledpi-teco";
	// Remote display x and y size.
	protected static final int X_SIZE = 24;
	protected static final int Y_SIZE = 24;
	// Remote display color mode. 0 = red, 1 = green, 2 = blue, 3 = RGB.
	protected static final int COLOR_MODE = 0;

	// The name this app uses to identify with the server.
	protected static final String APP_NAME = "de.pinkproblem.MultiPong";

	private final ArrayList<UUID> uuid;
	private final BluetoothAdapter adapter;

	public ConnectionManager() {
		cmConnection = new LEDMatrixBTConn(context, REMOTE_BT_DEVICE_NAME,
				X_SIZE, Y_SIZE, COLOR_MODE, APP_NAME);

		adapter = BluetoothAdapter.getDefaultAdapter();

		// unique uuid for each possible client
		uuid = new ArrayList<UUID>();
		uuid.add(UUID.fromString("2eaa658c-bda9-451d-a942-d7d16705b373"));
		uuid.add(UUID.fromString("6bb266a3-bd93-4783-b298-501cf4dabb8e"));
		uuid.add(UUID.fromString("b4b8ba7e-36b2-4a7c-ad2b-2349479852d9"));
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

	private class ListenThread extends Thread {
		private BluetoothServerSocket serverSocket;

		public ListenThread() {
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
					// manageConnectedSocket(socket);
					// TODO
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

}
