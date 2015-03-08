package de.pinkproblem.multipong;

public class Ball {

	private double xPosition;
	private double yPosition;

	private double xDirection;
	private double yDirection;

	private double velocity;

	public Ball() {
		this(PongGame.fieldSize / 2, PongGame.fieldSize / 2, 0, 0, 0.01);
		// double xDir = Math.random();
		// double yDir = Math.random();
		xDirection = Math.random() - 0.5;
		yDirection = Math.random() - 0.5;
		normalize();
	}

	public Ball(double xPosition, double yPosition, double xDirection,
			double yDirection, double velocity) {
		super();
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.xDirection = xDirection;
		this.yDirection = yDirection;
		this.velocity = velocity;
		normalize();
	}

	/**
	 * Move within legal borders
	 * 
	 * @param x
	 * @param y
	 */
	public void move(double x, double y) {
		xPosition = PongGame.correctToFieldBounds(xPosition + x);
		yPosition = PongGame.correctToFieldBounds(yPosition + y);
	}

	// normalize the direction vectors
	void normalize() {
		final double length = Math.sqrt(xDirection * xDirection + yDirection
				* yDirection);
		xDirection /= length;
		yDirection /= length;
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
