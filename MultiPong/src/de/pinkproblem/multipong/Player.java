package de.pinkproblem.multipong;

public abstract class Player {

	// upper border
	private double shieldPosition;

	abstract double getShieldPosition();

	// refresh shield position and stuff
	abstract void process(long deltaTime);

}
