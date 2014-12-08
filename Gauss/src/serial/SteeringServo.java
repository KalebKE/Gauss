package serial;

public class SteeringServo
{
	private final int STANDARD_SERVO_CHANNEL = 1;

	private AdafruitPCA9685 servoBoard;

	public SteeringServo()
	{
		initServo();
	}

	private void initServo()
	{
		servoBoard = new AdafruitPCA9685();

		// The Traxxus servo uses a frequency of 50Hz and a 180 degree rotation
		// range with a 100 centisecond to 200 centisecond pulse range.
		servoBoard.setPWMFreq(50);
	}

	private void testServo()
	{
		int servoMin = 100; // was 150. Min pulse length out of 4096
		int servoMax = 200; // was 600. Max pulse length out of 4096
		int servoMid = 150;

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
