package de.pinkproblem.multipong;

import static de.pinkproblem.multipong.Direction.BOTTOM;
import static de.pinkproblem.multipong.Direction.LEFT;
import static de.pinkproblem.multipong.Direction.RIGHT;
import static de.pinkproblem.multipong.Direction.TOP;

public class PongGame {

	public static final int fieldSize = 24;
	public static final int shieldSize = 4;
	// distance from edge
	public static final double shieldDistance = 2;
	public static final int numberOfPlayers = 4;
	// tolerance for double calculation
	private static final double eps = 0.1;

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

		state = RUNNING;
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
			// testAndEnd();
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
		if (dstToShield < eps && ball.getyPosition() >= p.getShieldyPosition()
				&& ball.getyPosition() <= p.getShieldyPosition() + shieldSize) {
			return true;
		}
		return false;
	}

	private boolean isInShieldXRange() {
		double dst1 = Math.abs(ball.getxPosition() - shieldDistance);
		double dst2 = Math.abs(ball.getxPosition()
				- (fieldSize - shieldDistance));
		return dst1 < eps || dst2 < eps;
	}

	private boolean isInShieldYRange() {
		Player p = player[getAreaIndex()];
		return ball.getyPosition() >= p.getShieldyPosition()
				&& ball.getyPosition() <= p.getShieldyPosition() + shieldSize;
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
				ball.setxPosition(fieldSize - eps);
			}
			testAndEnd();
		}

		// reflect at top and bottom edge
		if (ball.getyPosition() < eps) {
			reflect(TOP);
		} else if (fieldSize - ball.getyPosition() < eps) {
			reflect(BOTTOM);
		}

	}

	private void testAndEnd() {
		if (ball.getxPosition() <= eps) {
			if (ball.getyPosition() <= fieldSize / 2) {
				endTurn(0);
			} else {
				endTurn(2);
			}
		} else if (ball.getxPosition() >= fieldSize - eps) {
			if (ball.getyPosition() <= fieldSize / 2) {
				endTurn(1);
			} else {
				endTurn(3);
			}
		}
	}

	// end turn, with index of the player who lost a point
	void endTurn(int playerIndex) {
		for (int i = 0; i < numberOfPlayers; i++) {
			Player p = player[i];
			if (i != playerIndex) {
				p.increaseScore();
			}
		}
		state = PAUSED;
		// reset to start
		// ball.setxPosition(fieldSize / 2);
		// ball.setyPosition(fieldSize / 2);
	}

	void reflect(Direction dir) {
		// vary a bit when reflecting on shield, so the path wont repeat
		double random = Math.random() - 0.5;
		switch (dir) {
		case BOTTOM:
			ball.setyDirection(-ball.getyDirection());
			break;
		case LEFT:
			ball.setxDirection(-ball.getxDirection() + random);
			ball.normalize();
			break;
		case RIGHT:
			ball.setxDirection(-ball.getxDirection() + random);
			ball.normalize();
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
		state = PAUSED;
	}

	public void restart() {
		ball.setxPosition(fieldSize / 2);
		ball.setyPosition(fieldSize / 2);
		state = PAUSED;
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
			return fieldSize - eps;
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

		// dirty hack so the ball wont glitch into the shield
		// if (x == Math.floor(shieldDistance) && isInShieldYRange()) {
		// x++;
		// } else if (x == Math.floor(fieldSize - shieldDistance)
		// && isInShieldYRange()) {
		// x--;
		// }

		array[x][y] = (byte) 255;

		// set shields
		for (int i = 0; i < numberOfPlayers; i++) {
			int yShield = (int) Math.floor(player[i].getShieldyPosition());
			int xShield = (int) Math.floor(player[i].getShieldxPosition());

			// better dirty hack
			if (xShield == shieldDistance) {
				xShield--;
			}

			for (int j = yShield; j < yShield + shieldSize && j < 24; j++) {
				byte[] b = array[xShield];
				b[j] = (byte) 255;
			}
		}

		return array;

	}

}
