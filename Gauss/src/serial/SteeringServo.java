package serial;

public class SteeringServo
{
	private final int STANDARD_SERVO_CHANNEL = 1;

	private final int servoMin = 122;
	private final int servoMax = 615;

	private final int servoRange = servoMax - servoMin;

	private final int servoMid = (servoMax + servoMin) / 2;

	private final int rangeDegrees = 180;

	private final double degreesPerPulse = (double) servoRange
			/ (double) rangeDegrees;

	private AdafruitPCA9685 servoBoard;

	public SteeringServo()
	{
		initServo();

		testSteeringAngle();

		//testServo();
	}

	public void setSteeringAngle(int angle)
	{
		int pulse = (int) ((degreesPerPulse * angle) + servoMin);

		if (pulse <= servoMax)
		{
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, pulse);
		}
	}

	private void initServo()
	{
		servoBoard = new AdafruitPCA9685();

		// NOTE: If the steering servo doesn't respond, try a frequency of 20Hz
		// instead of 50Hz... it seems to respond at 20Hz and then 50Hz will
		// work again.

		servoBoard.setPWMFreq(60);
	}

	private void testSteeringAngle()
	{
		System.out.println("Test Steering...");

		setSteeringAngle(0);
		servoBoard.waitfor(1000);
		setSteeringAngle(180);
		servoBoard.waitfor(1000);
		setSteeringAngle(90);
	}

	private void testServo()
	{
		// servoBoard.setServoPulse(STANDARD_SERVO_CHANNEL, 1500);
		for (int i = 0; i < 3; i++)
		{
			System.out.println("i=" + i);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMin);
			servoBoard.waitfor(1000);
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMax);
			servoBoard.waitfor(1000);
		}

		servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, servoMid);

		System.out.println("Done with test.");

	}
}
