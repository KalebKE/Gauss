package serial;

public class EscController
{
	private final int STANDARD_SERVO_CHANNEL = 0;

	private final int escMin = 122;
	private final int escMax = 615;

	private final int escRange = escMax - escMin;

	private final int escMid = (escMax + escMin) / 2;

	private final int speedMax = 100;

	private final double perPulse = (double) escRange / (double) speedMax;

	private AdafruitPCA9685 servoBoard;

	public EscController()
	{
		initServo();

		testEsc();
	}

	/**
	 * Forward = 50-100. Backwards = 0-50. Netural is 50.
	 * 
	 * Note that the brake is apparently 0ms and 252ms, but I haven't found the
	 * full range of the ESC yet, so I am unsure what they are in terms of an
	 * int for speed. Brake is also hard on the engine.
	 * 
	 * @param speed
	 */
	public void setSpeed(int speed)
	{
		int pulse = (int) ((perPulse * speed) + escMin);

		if (pulse <= escMax)
		{
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, pulse);
		}
	}

	private void initServo()
	{
		servoBoard = new AdafruitPCA9685();

		// The Traxxus servo uses a ency of 50Hz and a 180 degree rotation
		// range with a 100 centisecond to 200 centisecond pulse range.
		servoBoard.setPWMFreq(60);

		// ESC must be plugged in within two seconds of starting the code.
		// The ESC is looking of the neutral position, so we send it that...
		setSpeed(50);
		servoBoard.waitfor(2000);
	}

	private void testEsc()
	{
		setSpeed(60);
		servoBoard.waitfor(5000);
		setSpeed(50);
	}
}
