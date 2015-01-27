package de.pinkproblem.multipong;

public class AIPlayer extends Player {

	private double velocity;

	@Override
	void process(long deltaTime, Ball ball) {
		double pos = getShieldPosition();
		if (ball.getyPosition() > pos) {

		}
	}
}
