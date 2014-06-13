package serial;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class AdafruitPCA9685
{
	public final static int PCA9685_ADDRESS = 0x40;

	public final static int SUBADR1 = 0x02;
	public final static int SUBADR2 = 0x03;
	public final static int SUBADR3 = 0x04;
	public final static int MODE1 = 0x00;
	public final static int PRESCALE = 0xFE;
	public final static int LED0_ON_L = 0x06;
	public final static int LED0_ON_H = 0x07;
	public final static int LED0_OFF_L = 0x08;
	public final static int LED0_OFF_H = 0x09;
	public final static int ALL_LED_ON_L = 0xFA;
	public final static int ALL_LED_ON_H = 0xFB;
	public final static int ALL_LED_OFF_L = 0xFC;
	public final static int ALL_LED_OFF_H = 0xFD;

	private static boolean verbose = true;

	private I2CBus bus;
	private I2CDevice servoDriver;

	public AdafruitPCA9685()
	{
		this(PCA9685_ADDRESS); // 0x40 obtained through sudo i2cdetect -y 1
	}

	public AdafruitPCA9685(int address)
	{
		try
		{
			// Get I2C bus
			bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends onthe RasPI
														// version
			if (verbose)
				System.out.println("Connected to bus. OK.");

			// Get the device itself
			servoDriver = bus.getDevice(address);
			if (verbose)
				System.out.println("Connected to device. OK.");
			// Reseting
			servoDriver.write(MODE1, (byte) 0x00);
		} catch (IOException e)
		{
			System.err.println(e.getMessage());
		}
	}

	public void setPWMFreq(int freq)
	{
		float prescaleval = 25000000.0f; // 25MHz
		prescaleval /= 4096.0; // 12-bit
		prescaleval /= freq;
		prescaleval -= 1.0;
		if (verbose)
		{
			System.out.println("Setting PWM frequency to " + freq + " Hz");
			System.out.println("Estimated pre-scale: " + prescaleval);
		}
		double prescale = Math.floor(prescaleval + 0.5);
		if (verbose)
			System.out.println("Final pre-scale: " + prescale);

		try
		{
			byte oldmode = (byte) servoDriver.read(MODE1);
			byte newmode = (byte) ((oldmode & 0x7F) | 0x10); // sleep
			servoDriver.write(MODE1, newmode); // go to sleep
			servoDriver.write(PRESCALE, (byte) (Math.floor(prescale)));
			servoDriver.write(MODE1, oldmode);
			waitfor(5);
			servoDriver.write(MODE1, (byte) (oldmode | 0x80));
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	/**
	 * 
	 * @param channel
	 *            0..15
	 * @param on
	 *            0..4095 (2^12 positions)
	 * @param off
	 *            0..4095 (2^12 positions)
	 */
	public void setPWM(int channel, int on, int off)
	{
		try
		{
			servoDriver.write(LED0_ON_L + 4 * channel, (byte) (on & 0xFF));
			servoDriver.write(LED0_ON_H + 4 * channel, (byte) (on >> 8));
			servoDriver.write(LED0_OFF_L + 4 * channel, (byte) (off & 0xFF));
			servoDriver.write(LED0_OFF_H + 4 * channel, (byte) (off >> 8));
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public static void waitfor(long howMuch)
	{
		try
		{
			Thread.sleep(howMuch);
		} catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}

	public void setServoPulse(int channel, int pulse)
	{
		int pulseLength = 1000000; // 1,000,000 us per pulse. "us" is to be read
									// "micro sec".
		pulseLength /= 60; // 60 Hz
		if (verbose)
			System.out.println(pulseLength + " us per period");
		pulseLength /= 4096; // 12 bits of resolution
		if (verbose)
			System.out.println(pulseLength + " us per bit");
		pulse *= 1000;
		pulse /= pulseLength;
		this.setPWM(channel, 0, pulse);
	}
}
