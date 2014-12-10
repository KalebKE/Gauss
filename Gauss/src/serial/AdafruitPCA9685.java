package serial;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

/**
 * @see https 
 *      ://code.google.com/p/raspberry-pi4j-samples/source/browse/AdafruitI2C
 *      /src/adafruiti2c/servo/AdafruitPCA9685.java
 * 
 * @see http://www.lediouris.net/RaspberryPI/servo/readme.html
 * @author root
 * 
 */
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
	private int freq = 60;

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

	/**
	 * A number representing the frequency in Hz
	 * 
	 * @param freq
	 *            a number between 40 and 1000
	 */
	public void setPWMFreq(int freq)
	{
		// Issue #11 states that there is an overshoot of the requested
		// frequency by a factor of 1/0.9... You can add freq = freq*0.9 to
		// compensate if required. This doesn't seem to work with the PI.
		this.freq = freq;

		float prescaleval = 25000000.0f; // 25MHz
		prescaleval /= 4096.0; // 12-bit
		prescaleval /= this.freq;
		prescaleval -= 1.0;
		if (verbose)
		{
			System.out.println("Setting PWM frequency to " + this.freq + " Hz");
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
	 * The following example will cause channel 15 to start low, go high around
	 * 25% into the pulse (tick 1024 out of 4096), transition back to low 75%
	 * into the pulse (tick 3072), and remain low for the last 25% of the pulse.
	 * 
	 * pwm.setPWM(15,1024,3072);
	 * 
	 * If you need to calculate pulse-width in microseconds, you can do that by
	 * first figuring out how long each cycle is. That would be 1/freq where
	 * freq is the PWM frequency you set. For 1000Hz, that would be 1
	 * millisecond (0.001 second). For 60Hz, that would be 16 milliseconds
	 * (0.016 seconds). Then divide by 4096 to get the time per tick, e.g 1
	 * millisecond/4096 = 0.25 microseconds. For 60Hz, it would be 16
	 * milliseconds/4096 = 244 microseconds. If you want a pulse that is 10
	 * microseconds long at 1000Hz, divide the time by the time-per-tick
	 * (10us/0.25us=40) then turn on at tick 0 and turn off at tick 40. At 60Hz,
	 * (10us/244us = 0.04).
	 * 
	 * @param channel
	 *            The channel that should be updated h the new values 0..15
	 * @param on
	 *            The tick (between 0..4095) when the signal should transition
	 *            from low to high 0..4095 (2^12 positions)
	 * @param off
	 *            The tick (between 0..4095) when the signal should transition
	 *            from high to low 0..4095 (2^12 positions)
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

	public void setServoPulse(int channel, int pulseMS)
	{
		int pulseLength = 1000000; // 1,000,000 us per pulse. "us" is to be read
									// "micro (mu) sec".
		pulseLength /= this.freq; // 60 Hz

		pulseLength /= 4096; // 12 bits of resolution

		int pulse = (int) pulseMS * 1000;

		pulseMS /= pulseLength;

		if (verbose)
		{
			System.out.println(pulseLength + " us per bit, pulse: " + pulse);
		}

		this.setPWM(channel, 0, pulse);
	}
}
