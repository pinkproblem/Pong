package de.pinkproblem.multipong;

import static de.pinkproblem.multipong.Direction.LEFT;
import static de.pinkproblem.multipong.Direction.TOP;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class HostIngameActivity extends IngameActivity {

	private ConnectionManager connectionManager;
	private Thread sendingThread;

	private PongGame game;
	private Player me;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_ingame);

		connectionManager = new ConnectionManager();
		sendingThread = new SendThread();

		game = new PongGame();
		me = new RealPlayer(LEFT, TOP);
		game.setPlayer(0, me);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// return to main menu if connection error
		if (!connectionManager.getCmConnection().prepare()
				|| !connectionManager.getCmConnection().isEnabled()
				|| !connectionManager.getCmConnection().checkIfDeviceIsPaired()) {
			connectionError();
		}

		sendingThread.start();
		Log.d("", "Thread started");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (connectionManager != null
				&& connectionManager.getCmConnection() != null) {
			connectionManager.closeCMConnection();
		}
	}

	private void connectionError() {
		MessageDialog dialog = new MessageDialog(getResources().getString(
				R.string.connection_error),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(HostIngameActivity.this,
								MainMenu.class);
						startActivity(intent);
					}
				});
		dialog.show(getFragmentManager(), "connection_error");
	}

	class SendThread extends Thread {

		boolean loop = true;
		int sendDelay;

		public void run() {

			// Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			// Try to connect.
			if (!connectionManager.connectToCM()) {
				loop = false;
			}

			// Connected. Calculate and set send delay from maximum FPS.
			// Negative maxFPS should not happen.
			int maxFPS = connectionManager.getCmConnection().getMaxFPS();
			if (maxFPS > 0) {
				sendDelay = (int) (1000.0 / maxFPS);
			} else {
				loop = false;
			}

			// Main sending loop.
			while (loop) {

				byte[] gameInfo = game.getOutputArray();

				// If write fails, the connection was probably closed by the
				// server.
				if (!connectionManager.sendToCm(gameInfo)) {
					loop = false;
				}

				try {
					// Delay for a moment.
					// Note: Delaying the same amount of time every frame will
					// not give you constant FPS.
					Thread.sleep(sendDelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Connection terminated or lost.
			connectionManager.getCmConnection().closeConnection();
		}
	}

	// gesture detection for shield movement
	class MovementDetector extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			return true;
		}
	}
}
