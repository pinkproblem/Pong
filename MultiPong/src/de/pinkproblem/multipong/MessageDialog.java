package de.pinkproblem.multipong;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Dialog with a simple message and one okay button.
 * 
 * @author iris
 * 
 */
public class MessageDialog extends DialogFragment {

	private DialogInterface.OnClickListener confirmAction;
	private String message;

	public MessageDialog(String message,
			DialogInterface.OnClickListener confirmAction) {
		this.confirmAction = confirmAction;
		this.message = message;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
				.setNeutralButton(R.string.ok, confirmAction);
		// Create the AlertDialog object and return it
		return builder.create();
	}

}
