package de.pinkproblem.multipong;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class HostIngameActivity extends IngameActivity {

	private PongGame game;
	private Player me;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_ingame);

		game = new PongGame();
		me = new RealPlayer();
		game.setPlayer(0, me);
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
