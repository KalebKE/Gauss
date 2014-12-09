package serial;

public class SteeringServo
{
	private final int STANDARD_SERVO_CHANNEL = 1;

	private final int servoMin = 100;
	private final int servoMax = 200;
	
	private final int servoRange = servoMax - servoMin;
	
	private final int servoMid = (servoMax + servoMin) / 2;
	
	private final int rangeDegrees = 180;
	
	private final double degreesPerPulse = (double)servoRange/(double)rangeDegrees;

	private AdafruitPCA9685 servoBoard;

	public SteeringServo()
	{
		initServo();
		
		testSteeringAngle();
		
		//testServo();
	}

	public void setSteeringAngle(int angle)
	{
		int pulse = (int) ((degreesPerPulse*angle) + servoMin);
		
		if(pulse <= servoMax)
		{
			servoBoard.setPWM(STANDARD_SERVO_CHANNEL, 0, pulse);
		}
	}

	private void initServo()
	{
		servoBoard = new AdafruitPCA9685();

		// The Traxxus servo uses a frequency of 50Hz and a 180 degree rotation
		// range with a 100 centisecond to 200 centisecond pulse range.
		servoBoard.setPWMFreq(50);
	}

	private void testSteeringAngle()
	{
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
