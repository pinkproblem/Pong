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
	public void process() {

		final long timeNow = SystemClock.uptimeMillis();
		final long deltaTime = timeNow - timeStamp;

		for (int i = 0; i < numberOfPlayers; i++) {
			player[i].process(deltaTime, ball);
		}

		ball.setxPosition(ball.getxPosition() + deltaTime * ball.getxVelocity());
		ball.setyPosition(ball.getyPosition() + deltaTime * ball.getyVelocity());

		if (ball.getxPosition() <= 0 || ball.getxPosition() >= fieldSize) {
			endTurn();
			return;
		} else if (ball.getyPosition() >= fieldSize) {
			reflect(Direction.BOTTOM);
		} else if (ball.getyPosition() <= 0) {
			reflect(Direction.TOP);
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
