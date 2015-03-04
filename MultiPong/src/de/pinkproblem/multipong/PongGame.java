package de.pinkproblem.multipong;

import static de.pinkproblem.multipong.Direction.BOTTOM;
import static de.pinkproblem.multipong.Direction.LEFT;
import static de.pinkproblem.multipong.Direction.RIGHT;
import static de.pinkproblem.multipong.Direction.TOP;

public class PongGame {

	public static int fieldSize = 24;
	public static int shieldSize = 4;
	// distance from edge
	public static double shieldDistance = 1;
	public static int numberOfPlayers = 4;

	public static final int RUNNING = 0;
	public static final int PAUSED = 1;

	private int state;
	private Player[] player;
	private Ball ball;

	public PongGame() {
		player = new Player[numberOfPlayers];
		ball = new Ball();

		// initialize with four AI Players
		player[0] = new AIPlayer(LEFT, TOP);
		player[1] = new AIPlayer(RIGHT, TOP);
		player[2] = new AIPlayer(LEFT, BOTTOM);
		player[3] = new AIPlayer(RIGHT, BOTTOM);

		state = PAUSED;
	}

	// refresh positions and stuff using passed time
	public void process(long deltaTime) {

		long tmpDelta = deltaTime;

		while (tmpDelta > 0) {
			long t = minTimeTillCollision(tmpDelta);
			for (int i = 0; i < numberOfPlayers; i++) {
				player[i].process(t, ball);
			}
			setBallPosition(t);
			testAndReflect();
			testAndEnd();
			tmpDelta -= t;
		}
	}

	private void setBallPosition(long deltaTime) {
		double deltaX = ball.getxVelocity();
		double deltaY = ball.getyVelocity();

		ball.move(deltaX, deltaY);
	}

	// prognose of x position
	private double getNextXPosition(long deltaTime) {
		return ball.getxPosition() + ball.getxVelocity() * deltaTime;
	}

	private double getNextYPosition(long deltaTime) {
		return ball.getyPosition() + ball.getyVelocity() * deltaTime;
	}

	// next x value that can cause a collision
	// (either shield position or edge)
	private double getNextXCollision() {
		double x;
		if (ball.getxDirection() > 0) {
			if (ball.getxPosition() < fieldSize - shieldDistance) {
				x = fieldSize - shieldDistance;
			} else {
				x = fieldSize;
			}
		} else {
			if (ball.getxPosition() > shieldDistance) {
				x = shieldDistance;
			} else {
				x = 0;
			}
		}
		return x;
	}

	// time till next collision, deltaTime if no collision within deltaTime
	private long minTimeTillCollision(long deltaTime) {
		final double nextXCollision = getNextXCollision();

		long xTime = deltaTime;
		if (ball.getxDirection() > 0) {
			xTime = (long) ((nextXCollision - ball.getxPosition()) / ball
					.getxVelocity());
		} else if (ball.getxDirection() < 0) {
			xTime = (long) ((ball.getxPosition() - nextXCollision) / -ball
					.getxVelocity());
		}
		long yTime = Integer.MAX_VALUE;
		if (ball.getyDirection() > 0) {
			yTime = (long) ((fieldSize - ball.getyPosition()) / ball
					.getyVelocity());
		} else if (ball.getyDirection() < 0) {
			yTime = (long) (ball.getyPosition() / -ball.getyVelocity());
		}

		if (xTime < deltaTime || yTime < deltaTime) {
			return Math.min(xTime, yTime);
		} else {
			return deltaTime;
		}
	}

	void reflect(Direction dir) {
		switch (dir) {
		case BOTTOM:
			ball.setyDirection(-ball.getyDirection());
			break;
		case LEFT:
			ball.setxDirection(-ball.getxDirection());
			break;
		case RIGHT:
			ball.setxDirection(-ball.getxDirection());
			break;
		case TOP:
			ball.setyDirection(-ball.getyDirection());
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	// return true if ball is at height(!) of any shield right now
	private boolean isShieldCollision() {
		for (int i = 0; i < numberOfPlayers; i++) {
			double yShield = player[i].getShieldyPosition();
			if (ball.getyPosition() > yShield
					&& ball.getyPosition() < yShield + shieldSize) {
				return true;
			}
		}
		return false;
	}

	private void testAndReflect() {
		final double gap = 0.1;
		if (ball.getxPosition() < shieldDistance + gap) {
			if (isShieldCollision()) {
				reflect(LEFT);
			}
		} else if (fieldSize - ball.getxPosition() < shieldDistance + gap) {
			if (isShieldCollision()) {
				reflect(RIGHT);
			}
		} else if (ball.getyPosition() < gap) {
			reflect(TOP);
		} else if (fieldSize - ball.getyPosition() < gap) {
			reflect(BOTTOM);
		}
	}

	private void testAndEnd() {
		if (ball.getxPosition() <= 0) {
			if (ball.getyPosition() <= fieldSize / 2) {
				endTurn(0);
			} else {
				endTurn(2);
			}
		} else if (ball.getxPosition() >= fieldSize) {
			if (ball.getyPosition() <= fieldSize / 2) {
				endTurn(1);
			} else {
				endTurn(3);
			}
		}
	}

	// end turn, with index of the player who lost a point
	void endTurn(int playerIndex) {
		// TODO
	}

	public void setPlayer(int index, Player newPlayer) {
		player[index] = newPlayer;
	}

	public Player getPlayer(int index) {
		return player[index];
	}

	/**
	 * Will return value if it's a correct coordinate, will return the nearest
	 * border's value (correct value) if outside of field
	 * 
	 * @param value
	 * @return
	 */
	static double correctToFieldBounds(double value) {
		if (value < 0) {
			return 0;
		} else if (value > fieldSize) {
			return fieldSize;
		} else {
			return value;
		}
	}

	public byte[] getOutputArray() {
		byte[] output = new byte[fieldSize * fieldSize];
		byte[][] field = getFieldArray();
		for (int i = 0; i < fieldSize; i++) {
			for (int j = 0; j < fieldSize; j++) {
				output[j * fieldSize + i] = field[i][j];
			}
		}

		return output;
	}

	byte[][] getFieldArray() {
		byte[][] array = new byte[fieldSize][fieldSize];
		// set ball
		int x = (int) Math.round(ball.getxPosition());
		int y = (int) Math.round(ball.getyPosition());
		array[x][y] = (byte) 255;

		// set shields
		for (int i = 0; i < numberOfPlayers; i++) {
			int yShield = (int) Math.round(player[i].getShieldyPosition());
			int xShield = (int) Math.round(player[i].getShieldxPosition());
			for (int j = yShield; j < yShield + shieldSize; j++) {
				array[xShield][j] = (byte) 255;
			}
		}

		return array;

	}

}
