package de.pinkproblem.multipong;

public class Ball {

	private double xPosition;
	private double yPosition;

	private double xDirection;
	private double yDirection;

	private double velocity;

	public Ball() {
		this(PongGame.fieldSize / 2, PongGame.fieldSize / 2, 0, 0, 1);
		// double xDir = Math.random();
		// double yDir = Math.random();
		double xDir = 1;
		double yDir = 0;
		xDirection = normalizeX(xDir, yDir);
		yDirection = normalizeY(xDir, yDir);

	}

	public Ball(double xPosition, double yPosition, double xDirection,
			double yDirection, double velocity) {
		super();
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.xDirection = xDirection;
		this.yDirection = yDirection;
		this.velocity = velocity;
	}

	public void move(double x, double y) {
		// TODO
	}

	// normalize the direction vectors
	private double normalizeX(double x, double y) {
		// TODO
		return x;
	}

	private double normalizeY(double x, double y) {
		// TODO
		return y;
	}

	public double getxVelocity() {
		return xDirection * velocity;
	}

	public double getyVelocity() {
		return yDirection * velocity;
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
