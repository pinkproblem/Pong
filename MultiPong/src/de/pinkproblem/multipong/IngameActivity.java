package de.pinkproblem.multipong;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class IngameActivity extends Activity {

	protected TextView scrollHint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ingame);
		getActionBar().setDisplayHomeAsUpEnabled(true);

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
