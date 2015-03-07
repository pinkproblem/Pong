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

		while (tmpDelta > 0 && state == RUNNING) {
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
		double deltaX = ball.getxVelocity() * deltaTime;
		double deltaY = ball.getyVelocity() * deltaTime;

		ball.move(deltaX, deltaY);
	}

	// next x value that can cause a collision
	private double getNextXCollision() {
		double x;
		if (ball.getxDirection() > 0) {
			x = fieldSize - shieldDistance;
		} else {
			x = shieldDistance;
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
		long yTime = deltaTime;
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

	// return true if ball is at height(!) of any shield right now
	private boolean isShieldCollision() {
		// get player for possible collision
		Player p = player[getAreaIndex()];
		double dstToShield = Math.abs(ball.getxPosition()
				- p.getShieldxPosition());
		if (dstToShield < 0.5 && ball.getyPosition() >= p.getShieldyPosition()
				&& ball.getyPosition() <= p.getShieldyPosition() + shieldSize) {
			return true;
		}
		return false;
	}

	private boolean isInShieldXRange() {
		double dst1 = Math.abs(ball.getxPosition() - shieldDistance);
		double dst2 = Math.abs(ball.getxPosition()
				- (fieldSize - shieldDistance));
		return dst1 < 0.2 || dst2 < 0.2;
	}

	// returns the index of the player in whose area the ball is
	private int getAreaIndex() {
		if (ball.getxPosition() < fieldSize / 2) {
			if (ball.getyPosition() < fieldSize / 2) {
				return 0;
			} else {
				return 2;
			}
		} else {
			if (ball.getyPosition() < fieldSize / 2) {
				return 1;
			} else {
				return 3;
			}
		}
	}

	private void testAndReflect() {

		if (isShieldCollision()) {
			switch (getAreaIndex()) {
			case 0:
				reflect(LEFT);
				break;
			case 1:
				reflect(RIGHT);
				break;
			case 2:
				reflect(LEFT);
				break;
			case 3:
				reflect(RIGHT);
				break;
			}
		} else if (isInShieldXRange()) {
			// somebody failed here (in shield range but no collision)
			int playerIndex = getAreaIndex();
			// set to edge
			if (playerIndex == 0 || playerIndex == 2) {
				ball.setxPosition(0);
			} else if (playerIndex == 1 || playerIndex == 3) {
				ball.setxPosition(fieldSize);
			}
			testAndEnd();
		}

		// reflect at top and bottom edge
		if (ball.getyPosition() < 0.1) {
			reflect(TOP);
		} else if (fieldSize - ball.getyPosition() < 0.1) {
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
		for (int i = 0; i < numberOfPlayers; i++) {
			Player p = player[i];
			if (i != playerIndex) {
				p.increaseScore();
			}
		}
		// reset to start
		// ball.setxPosition(fieldSize / 2);
		// ball.setyPosition(fieldSize / 2);
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

	public void setPlayer(int index, Player newPlayer) {
		player[index] = newPlayer;
	}

	public Player getPlayer(int index) {
		return player[index];
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void reset() {
		for (int i = 0; i < numberOfPlayers; i++) {
			player[i].setScore(0);
		}
		ball.setxPosition(fieldSize / 2);
		ball.setyPosition(fieldSize / 2);
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
		int x = (int) Math.floor(ball.getxPosition());
		int y = (int) Math.floor(ball.getyPosition());
		array[x][y] = (byte) 255;

		// set shields
		for (int i = 0; i < numberOfPlayers; i++) {
			int yShield = (int) Math.floor(player[i].getShieldyPosition());
			int xShield = (int) Math.floor(player[i].getShieldxPosition());
			for (int j = yShield; j < yShield + shieldSize && j < 24; j++) {
				byte[] b = array[xShield];
				b[j] = (byte) 255;
			}
		}

		return array;

	}

}
