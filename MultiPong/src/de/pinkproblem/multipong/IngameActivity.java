package de.pinkproblem.multipong;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

public class IngameActivity extends Activity {

	protected Player me;

	protected TextView scrollHint;

	// ui stuff
	protected ImageView topLeft;
	protected TextView score0;
	protected TextView score1;
	protected TextView score2;
	protected TextView score3;
	protected View inputView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ingame);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// ui stuff
		topLeft = (ImageView) findViewById(R.id.topLeft);
		topLeft.setImageResource(R.drawable.player2);
		inputView = findViewById(R.id.inputView);

		score0 = (TextView) findViewById(R.id.score0);
		score1 = (TextView) findViewById(R.id.score1);
		score2 = (TextView) findViewById(R.id.score2);
		score3 = (TextView) findViewById(R.id.score3);

		scrollHint = (TextView) findViewById(R.id.scroll_hint);
		// hide on touch
		scrollHint.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.d("", "hint touched");
				if (v.getVisibility() == View.VISIBLE) {
					scrollHint.setVisibility(View.INVISIBLE);
					return true;
				} else {
					return false;
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		scrollHint.setVisibility(View.VISIBLE);
	}

	protected void showHint() {
		scrollHint.setText(R.string.move_to_start);
	}

	protected void hideHint() {
		scrollHint.setText("");
	}

	// when the activity shouldnt be continued due to connection error
	protected void connectionFailed() {
		MessageDialog dialog = new MessageDialog(getResources().getString(
				R.string.connection_error),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(IngameActivity.this,
								MainMenu.class);
						startActivity(intent);
					}
				});
		dialog.show(getFragmentManager(), "connection_error");
	}

	// gesture detection for shield movement
	class MovementDetector extends GestureDetector.SimpleOnGestureListener {

		Player player;

		public MovementDetector(Player p) {
			player = p;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			player.setShieldyPosition(player.getShieldyPosition() + distanceY);
			return true;
		}
	}
}
