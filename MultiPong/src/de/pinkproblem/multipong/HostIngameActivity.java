package de.pinkproblem.multipong;

import static de.pinkproblem.multipong.Direction.LEFT;
import static de.pinkproblem.multipong.Direction.TOP;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

public class HostIngameActivity extends IngameActivity {

	private ConnectionManager connectionManager;
	private Thread sendingThread;

	private PongGame game;
	private Player me;

	// ui stuff
	private ImageView topLeft;
	private TextView score0;
	private TextView score1;
	private TextView score2;
	private TextView score3;
	private View inputView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ingame);

		connectionManager = new ConnectionManager(this);
		sendingThread = new SendThread();

		// ui stuff
		topLeft = (ImageView) findViewById(R.id.topLeft);
		topLeft.setImageResource(R.drawable.player2);
		inputView = findViewById(R.id.inputView);

		score0 = (TextView) findViewById(R.id.score0);
		score1 = (TextView) findViewById(R.id.score1);
		score2 = (TextView) findViewById(R.id.score2);
		score3 = (TextView) findViewById(R.id.score3);

		inputView.setOnTouchListener(new OnTouchListener() {

			float last;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float y = event.getRawY();
				float dst = last - y;
				// move own player shield by calculated distance
				// TODO some scaling
				me.move(dst);
				last = y;
				return true;
			}
		});

		game = new PongGame();
		me = new RealPlayer(LEFT, TOP);
		game.setPlayer(0, me);
	}

	@Override
	protected void onStart() {
		super.onStart();
		connectionManager.setCmDeviceName(getCmDeviceName());

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
	protected void onStop() {
		super.onStop();
		if (connectionManager != null
				&& connectionManager.getCmConnection() != null) {
			connectionManager.closeCMConnection();
		}
	}

	private String getCmDeviceName() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		return sharedPref.getString("pref_device", "");
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
		long timeStamp = SystemClock.uptimeMillis();

		public void run() {

			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

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

				// process game logic
				final long timeNow = SystemClock.uptimeMillis();
				final long deltaTime = timeNow - timeStamp;
				timeStamp = timeNow;
				game.process(deltaTime);
				byte[] gameInfo = game.getOutputArray();

				// Log.d("", String.valueOf(deltaTime));

				// update ui
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						score0.setText(String.valueOf(game.getPlayer(0)
								.getScore()));
						score1.setText(String.valueOf(game.getPlayer(1)
								.getScore()));
						score2.setText(String.valueOf(game.getPlayer(2)
								.getScore()));
						score3.setText(String.valueOf(game.getPlayer(3)
								.getScore()));
					}
				});

				// send to cm
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
}
