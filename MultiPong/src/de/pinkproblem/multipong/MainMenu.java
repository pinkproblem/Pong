package de.pinkproblem.multipong;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainMenu extends Activity {

	private LEDMatrixBTConn btConnection;
	protected static final String REMOTE_BT_DEVICE_NAME = "ledpi-teco";

	// Remote display x and y size.
	protected static final int X_SIZE = 24;
	protected static final int Y_SIZE = 24;

	// Remote display color mode. 0 = red, 1 = green, 2 = blue, 3 = RGB.
	protected static final int COLOR_MODE = 0;

	private static final int REQUEST_ENABLE_BT_HOST = 1;
	private static final int REQUEST_ENABLE_BT_GUEST = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);

		btConnection = new LEDMatrixBTConn(this, REMOTE_BT_DEVICE_NAME, X_SIZE,
				Y_SIZE, COLOR_MODE, "MultiPong");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu_action_bar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.menu_settings:
			openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void hostGame(View view) {
		if (!btConnection.prepare()) {
			Toast.makeText(this, "Bluetooth is not available on this device.",
					Toast.LENGTH_LONG).show();
			return;
		}
		if (!btConnection.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_HOST);
			return;
		}
		if (!btConnection.checkIfDeviceIsPaired()) {
			Toast.makeText(this, "Please pair the device.", Toast.LENGTH_LONG)
					.show();
			return;
		}
		startHost();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT_HOST:
			if (resultCode == RESULT_OK) {
				startHost();
			} else if (resultCode == RESULT_CANCELED) {
				showEnableBtToast();
			}
		}
	}

	private void startHost() {
		Intent intent = new Intent(this, HostIngameActivity.class);
		startActivity(intent);
	}

	private void showEnableBtToast() {
		Toast toast = Toast.makeText(this, R.string.enableBt,
				Toast.LENGTH_SHORT);
		toast.show();
	}

	private void openSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
}
