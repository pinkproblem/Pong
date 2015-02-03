package de.pinkproblem.multipong;

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

	public ConnectionManager() {
		cmConnection = new LEDMatrixBTConn(context, REMOTE_BT_DEVICE_NAME,
				X_SIZE, Y_SIZE, COLOR_MODE, APP_NAME);
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

}
