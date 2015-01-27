package de.pinkproblem.multipong;

public abstract class Player {

	// upper border
	private double shieldPosition;

	public double getShieldPosition() {
		return shieldPosition;
	}

	public void setShieldPosition(double pos) {
		shieldPosition = pos;
	}

	// refresh shield position and stuff
	abstract void process(long deltaTime, Ball ball);

}
