package de.pinkproblem.multipong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends Activity {

	private LEDMatrixBTConn btConnection;
	protected static final String REMOTE_BT_DEVICE_NAME = "ledpi-teco";

	// Remote display x and y size.
	protected static final int X_SIZE = 24;
	protected static final int Y_SIZE = 24;

	// Remote display color mode. 0 = red, 1 = green, 2 = blue, 3 = RGB.
	protected static final int COLOR_MODE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		btConnection = new LEDMatrixBTConn(this, REMOTE_BT_DEVICE_NAME, X_SIZE,
				Y_SIZE, COLOR_MODE, "MultiPong");

	}

	public void hostGame(View view) {
		Intent intent = new Intent(this, IngameActivity.class);
		startActivity(intent);
	}
}
