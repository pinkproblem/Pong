package de.pinkproblem.multipong;

public class Ball {

	private double xPosition;
	private double yPosition;

	private double xDirection;
	private double yDirection;

	private double velocity;

	public double getxVelocity() {
		return Math.cos(velocity);
	}

	public double getyVelocity() {
		return Math.sin(velocity);
	}

	public double getxPosition() {
		return xPosition;
	}

	public void setxPosition(double xPosition) {
		this.xPosition = xPosition;
	}

	public double getyPosition() {
		return yPosition;
	}

	public void setyPosition(double yPosition) {
		this.yPosition = yPosition;
	}

	public double getxDirection() {
		return xDirection;
	}

	public void setxDirection(double xDirection) {
		this.xDirection = xDirection;
	}

	public double getyDirection() {
		return yDirection;
	}

	public void setyDirection(double yDirection) {
		this.yDirection = yDirection;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

}
