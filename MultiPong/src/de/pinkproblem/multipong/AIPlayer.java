package de.pinkproblem.multipong;

public class AIPlayer extends Player {

	public AIPlayer(double shieldxPosition, double shieldyPosition) {
		super(shieldxPosition, shieldyPosition);
	}

	public AIPlayer(Direction horizontal, Direction vertical) {
		super(horizontal, vertical);
		// TODO Auto-generated constructor stub
	}

	private double velocity;

	@Override
	void process(long deltaTime, Ball ball) {
		double pos = getShieldyPosition();
		if (ball.getyPosition() > pos) {

		}
	}
}
