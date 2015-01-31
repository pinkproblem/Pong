package de.pinkproblem.multipong;

import static de.pinkproblem.multipong.Direction.BOTTOM;
import static de.pinkproblem.multipong.Direction.LEFT;
import static de.pinkproblem.multipong.Direction.RIGHT;
import static de.pinkproblem.multipong.Direction.TOP;

public abstract class Player {

	// upper border
	private final double shieldxPosition;
	private double shieldyPosition;

	public Player(Direction horizontal, Direction vertical) {

		if (horizontal == TOP || horizontal == BOTTOM || vertical == LEFT
				|| vertical == RIGHT) {
			throw new IllegalArgumentException();
		}

		if (horizontal == LEFT) {
			shieldxPosition = 1;
		} else {
			shieldxPosition = PongGame.fieldSize - 2;
		}
		if (vertical == TOP) {
			shieldyPosition = PongGame.fieldSize / 4 + PongGame.shieldSize / 2;
		} else {
			shieldyPosition = PongGame.fieldSize - PongGame.fieldSize / 4
					+ PongGame.shieldSize / 2;
		}
	}

	public Player(double shieldxPosition, double shieldyPosition) {
		super();
		this.shieldxPosition = shieldxPosition;
		this.shieldyPosition = shieldyPosition;
	}

	public double getShieldyPosition() {
		return shieldyPosition;
	}

	public void setShieldyPosition(double pos) {
		shieldyPosition = pos;
	}

	public double getShieldxPosition() {
		return shieldxPosition;
	}

	// refresh shield position and stuff
	abstract void process(long deltaTime, Ball ball);

}
