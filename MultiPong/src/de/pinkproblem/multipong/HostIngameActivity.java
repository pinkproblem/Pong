package de.pinkproblem.multipong;

import static de.pinkproblem.multipong.Direction.LEFT;
import static de.pinkproblem.multipong.Direction.TOP;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class HostIngameActivity extends IngameActivity {

	public static final int PLAYER_PC = 0;
	public static final int PLAYER_ME = 1;
	public static final int PLAYER_ELSE = 2;

	private ConnectionManager connectionManager;
	private Thread sendingThread;

	private PongGame game;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_ingame);

		// scrollHint = (TextView) findViewById(R.id.scroll_hint);

		connectionManager = new ConnectionManager(this, handler);
		sendingThread = new SendThread();

		topLeft.setImageResource(R.drawable.player2);
		inputView.setOnTouchListener(new OnTouchListener() {

			float last;
			int lastAction = MotionEvent.ACTION_UP;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO
				hideHint();
				if (game.getState() == PongGame.PAUSED
						&& lastAction == MotionEvent.ACTION_UP) {
					game.restart();
					game.setState(PongGame.RUNNING);
				}

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

		connectionManager.startListening();

		sendingThread.start();
		Log.d("", "Thread started");
	}

	@Override
	protected void onStop() {
		super.onStop();
		connectionManager.stop();
		sendingThread = null;
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

	public byte[] getSendBuffer() {
		byte[] info = new byte[8];
		info[0] = PLAYER_ELSE;
		info[1] = PLAYER_ME;
		info[3] = info[4] = PLAYER_PC;
		for (int i = 4; i < 8; i++) {
			info[i] = (byte) game.getPlayer(i - 4).getScore();
		}
		return info;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ConnectionManager.MESSAGE_ERROR:
				connectionError();
				break;
			case ConnectionManager.MESSAGE_RECEIVE:
				byte[] posBuffer = msg.getData()
						.getByteArray("MESSAGE_RECEIVE");
				double dst = Utility.toDouble(posBuffer);
				game.getPlayer(1).move(dst);
				break;
			case ConnectionManager.MESSAGE_NEW_PLAYER:
				topRight.setImageResource(R.drawable.player1);
				game.setPlayer(1,
						new RealPlayer(Direction.RIGHT, Direction.TOP));
				break;
			case ConnectionManager.MESSAGE_PLAYER_LEFT:
				topRight.setImageResource(R.drawable.pc);
				game.setPlayer(1, new AIPlayer(Direction.RIGHT, Direction.TOP));
				break;
			}
		};
	};

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

				connectionManager.sendGameInfo(getSendBuffer());

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
