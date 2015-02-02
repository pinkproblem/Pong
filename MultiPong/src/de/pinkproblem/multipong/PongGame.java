package de.pinkproblem.multipong;

import static de.pinkproblem.multipong.Direction.BOTTOM;
import static de.pinkproblem.multipong.Direction.LEFT;
import static de.pinkproblem.multipong.Direction.RIGHT;
import static de.pinkproblem.multipong.Direction.TOP;
import android.os.SystemClock;

public class PongGame {

	public static int fieldSize = 24;
	public static int shieldSize = 4;
	public static int numberOfPlayers = 4;

	Player[] player;

	private Ball ball;

	// time of last processing step
	private long timeStamp;

	public PongGame() {
		player = new Player[numberOfPlayers];
		ball = new Ball();

		// initialize with four AI Players
		player[0] = new AIPlayer(LEFT, TOP);
		player[1] = new AIPlayer(RIGHT, TOP);
		player[2] = new AIPlayer(LEFT, BOTTOM);
		player[3] = new AIPlayer(RIGHT, BOTTOM);
	}

	// refresh positions and stuff using passed time
	public void process(long deltaTime) {

		for (int i = 0; i < numberOfPlayers; i++) {
			player[i].process(deltaTime, ball);
		}

		testAndReflect();

		while (willCollide(deltaTime)) {
			process(minTimeTillCollision(deltaTime));
		}
	}

	private long getDeltaTimeAndUpdate() {
		final long timeNow = SystemClock.uptimeMillis();
		final long deltaTime = timeNow - timeStamp;
		timeStamp = timeNow;
		return deltaTime;
	}

	// prognose of x position
	private double getNextXPosition(long deltaTime) {
		return ball.getxPosition() + ball.getxVelocity() * deltaTime;
	}

	private double getNextYPosition(long deltaTime) {
		return ball.getyPosition() + ball.getyVelocity() * deltaTime;
	}

	private boolean willCollide(long deltaTime) {
		return predictCollision(deltaTime) != null
				|| predictShieldCollision(deltaTime) != -1;
	}

	// return direction of next collision, null if no collision
	private Direction predictCollision(long deltaTime) {
		double x = getNextXPosition(deltaTime);
		double y = getNextYPosition(deltaTime);

		Direction poss1 = null;
		Direction poss2 = null;

		if (x <= 0) {
			poss1 = LEFT;
		} else if (x >= fieldSize) {
			poss1 = RIGHT;
		}
		if (y <= 0) {
			poss2 = TOP;
		} else if (y >= fieldSize) {
			poss2 = BOTTOM;
		}

		if (poss1 != null && poss2 != null) {
			if (timeTillCollision(poss1) <= timeTillCollision(poss2)) {
				return poss1;
			} else {
				return poss2;
			}
		} else if (poss1 != null) {
			return poss1;
		} else if (poss2 != null) {
			return poss2;
		} else {
			return null;
		}
	}

	// return index of player who reflects, -1 if no collision
	// there may be a former collision with walls
	private int predictShieldCollision(long deltaTime) {
		double x = getNextXPosition(deltaTime);
		double y = getNextYPosition(deltaTime);

		for (int i = 0; i < numberOfPlayers; i++) {
			double dst = x - player[i].getShieldxPosition();
			if (i % 2 == 0) {
				// player is left

			}
		}
		// TODO
		return 0;
	}

	private long timeTillCollision(int playerIndex) {
		double x = player[playerIndex].getShieldxPosition();
		return timeTillCollision(x, -1);
	}

	private long timeTillCollision(Direction dir) {
		switch (dir) {
		case BOTTOM:
			return timeTillCollision(-1, fieldSize);
		case LEFT:
			return timeTillCollision(0, -1);
		case RIGHT:
			return timeTillCollision(fieldSize, -1);
		case TOP:
			return timeTillCollision(-1, 0);
		default:
			throw new IllegalArgumentException();
		}
	}

	// One value marks the barrier, the other one must be -1
	// Only works if there will definitely be a collision!!!!
	private long timeTillCollision(double x, double y) {
		if (x != -1 && y != -1) {
			throw new IllegalArgumentException();
		}

		double xVector = ball.getxVelocity();
		double yVector = ball.getyVelocity();

		if (x != -1) {
			double dst = Math.abs(x - ball.getxPosition());
			return Math.round(dst / xVector);
		} else {
			double dst = Math.abs(y - ball.getyPosition());
			return Math.round(dst / yVector);
		}
	}

	private long minTimeTillCollision(long deltaTime) {
		Direction dir = predictCollision(deltaTime);
		int playerIndex = predictShieldCollision(deltaTime);
		return Math.min(timeTillCollision(dir), timeTillCollision(playerIndex));
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

	private void testAndReflect() {
		final double gap = 0.1;
		if (ball.getxPosition() < gap) {
			reflect(LEFT);
		} else if (fieldSize - ball.getxPosition() < gap) {
			reflect(RIGHT);
		} else if (ball.getyPosition() < gap) {
			reflect(TOP);
		} else if (fieldSize - ball.getyPosition() < gap) {
			reflect(BOTTOM);
		}
	}

	void reflectOnShield(int playerIndex) {
		if (playerIndex % 2 == 0) {
			// player is left
			reflect(LEFT);
		} else {
			reflect(RIGHT);
		}
	}

	void endTurn() {
		// TODO
	}

	public void setPlayer(int index, Player newPlayer) {
		player[index] = newPlayer;
	}

	public byte[] getOutputArray() {
		byte[] output = new byte[fieldSize * fieldSize];
		byte[][] field = getFieldArray();
		for (int i = 0; i < fieldSize; i++) {
			for (int j = 0; j < fieldSize; j++) {
				output[i * fieldSize + j] = field[i][j];
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
