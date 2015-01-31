package de.pinkproblem.multipong;

public class AIPlayer extends Player {

	// the AI's movement speed
	private double velocity;

	public AIPlayer(double shieldxPosition, double shieldyPosition) {
		super(shieldxPosition, shieldyPosition);
		velocity = 1;
	}

	public AIPlayer(Direction horizontal, Direction vertical) {
		super(horizontal, vertical);
		velocity = 1;
	}

	@Override
	void process(long deltaTime, Ball ball) {
		double dst = ball.getyPosition() - getShieldCenter();
		// normalize distance
		// -> -1 = up, +1 = down
		dst /= dst;
		move(dst * deltaTime * velocity);
	}
}
