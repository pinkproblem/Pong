package de.pinkproblem.multipong;


public class AIPlayer extends Player {

	// the AI's movement speed
	private double velocity;

	public AIPlayer(double shieldxPosition, double shieldyPosition) {
		super(shieldxPosition, shieldyPosition);
		velocity = 0.01;
	}

	public AIPlayer(Direction horizontal, Direction vertical) {
		super(horizontal, vertical);
		velocity = 0.009;
	}

	@Override
	void process(long deltaTime, Ball ball) {
		double dst = Math.floor(ball.getyPosition()) - Math.floor(getShieldCenter());
		if(dst>=0){
			dst=1;
		}else{
			dst=-1;
		}
		move(dst * deltaTime * velocity);
	}
}
