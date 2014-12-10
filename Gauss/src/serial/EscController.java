package serial;

public class EscController
{
	private final int STANDARD_SERVO_CHANNEL = 0;

	private final int escMin = 100;
	private final int escMax = 200;

	private final int escRange = escMax - escMin;

	private final int escMid = (escMax + escMin) / 2;

	private final int speedMax = 100;

	private final double PerPulse = (double) escRange / (double) speedMax;

	private AdafruitPCA9685 servoBoard;

	public EscController()
	{
		initServo();

		testEsc();
	}

	public void setSpeed(int speed)
	{
		int pulse = (int) ((PerPulse * speed) + escMin);

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
		servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 221);
		servoBoard.waitfor(10000);
	}

	private void testEsc()
	{
		servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, 400);
	}
}
