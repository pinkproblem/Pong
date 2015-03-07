package de.pinkproblem.multipong;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class IngameActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ingame);
		getActionBar().setDisplayHomeAsUpEnabled(true);
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
