package de.pinkproblem.multipong;

import static de.pinkproblem.multipong.Direction.BOTTOM;
import static de.pinkproblem.multipong.Direction.LEFT;
import static de.pinkproblem.multipong.Direction.RIGHT;
import static de.pinkproblem.multipong.Direction.TOP;

public abstract class Player {

	private final double shieldxPosition;
	// upper border
	private double shieldyPosition;

	private int topBorder;
	private int bottomBorder;

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

		setBorders(vertical);
	}

	public Player(double shieldxPosition, double shieldyPosition) {
		super();
		this.shieldxPosition = shieldxPosition;
		this.shieldyPosition = shieldyPosition;
	}

	private void setBorders(Direction dir) {
		if (dir == TOP) {
			topBorder = 0;
			bottomBorder = PongGame.fieldSize / 2;
		} else if (dir == BOTTOM) {
			topBorder = PongGame.fieldSize / 2;
			bottomBorder = PongGame.fieldSize;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public double getShieldyPosition() {
		return shieldyPosition;
	}

	public void setShieldyPosition(double pos) {
		shieldyPosition = pos;
	}

	// returns the middle point of the shield
	public double getShieldCenter() {
		return shieldyPosition + PongGame.shieldSize / 2;
	}

	// negative to move up, positive to move down
	// wont move beyond borders
	public void move(double dst) {
		double tmpPosition = shieldyPosition + dst;
		if (tmpPosition < topBorder) {
			shieldyPosition = topBorder;
		} else if (tmpPosition > bottomBorder) {
			shieldyPosition = bottomBorder + PongGame.shieldSize;
		} else {
			shieldyPosition = tmpPosition;
		}
	}

	public double getShieldxPosition() {
		return shieldxPosition;
	}

	// refresh shield position and stuff
	abstract void process(long deltaTime, Ball ball);

}
