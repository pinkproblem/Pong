package de.pinkproblem.multipong;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);

			ListPreference prefDevice = (ListPreference) findPreference("pref_device");
			Set<BluetoothDevice> pairedDevices = BluetoothAdapter
					.getDefaultAdapter().getBondedDevices();
			String[] deviceNames;
			// If there are paired devices
			if (pairedDevices.size() > 0) {
				int i = 0;
				deviceNames = new String[pairedDevices.size()];
				for (BluetoothDevice device : pairedDevices) {
					deviceNames[i] = device.getName();
					i++;
				}
			} else {
				deviceNames = new String[1];
				deviceNames[0] = getString(R.string.pref_device_default);
			}
			prefDevice.setEntries(deviceNames);
			prefDevice.setEntryValues(deviceNames);
		}
	}
}
