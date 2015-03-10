package de.pinkproblem.multipong;

public class AIPlayer extends Player {

	// the AI's movement speed
	private double velocity;

	public AIPlayer(double shieldxPosition, double shieldyPosition) {
		super(shieldxPosition, shieldyPosition);
		velocity = 0.004;
	}

	public AIPlayer(Direction horizontal, Direction vertical) {
		super(horizontal, vertical);
		velocity = 0.004;
	}

	@Override
	void process(long deltaTime, Ball ball) {
		double dst = ball.getyPosition() - getShieldCenter();
		double dir;
		if (dst >= 0) {
			dir = 1;
		} else {
			dir = -1;
		}
		// move as far as possible in direction of ball, but not past it
		double maxDst = Math.min(deltaTime * velocity, Math.abs(dst));
		move(dir * maxDst);
	}
}
