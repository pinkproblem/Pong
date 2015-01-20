package de.pinkproblem.multipong;

import android.os.Bundle;

public class HostIngameActivity extends IngameActivity {

	private PongGame game;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_ingame);

		game = new PongGame();

	}
}
