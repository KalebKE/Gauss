package serial;

import java.io.IOException;

import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class SerialCom
{
	public static void main(String args[]) throws InterruptedException,
			IOException
	{

		// !! ATTENTION !!
		// By default, the serial port is configured as a console port
		// for interacting with the Linux OS shell. If you want to use
		// the serial port in a software program, you must disable the
		// OS from using this port. Please see this blog article by
		// Clayton Smith for step-by-step instructions on how to disable
		// the OS console for this port:
		// http://www.irrational.net/2012/04/19/using-the-raspberry-pis-serial-port/

		System.out
				.println("<--Pi4J--> Serial Communication Example ... started.");
		System.out.println(" ... connect using settings: 5, N, 8, 1.");
		System.out
				.println(" ... data received on serial port should be displayed below.");

		// create an instance of the serial communications class
		final Serial serialIMU = SerialFactory.createInstance();
		final Serial serialGPS = SerialFactory.createInstance();

		// Use bus number = 1 for new Raspberry Pi's (512MB with mounting holes)
		final Adafruit16PwmProvider i2c = new Adafruit16PwmProvider(1, 0x40);

		i2c.setMode(Adafruit16PwmPin.GPIO_00, PinMode.PWM_OUTPUT);

		i2c.setPwm(Adafruit16PwmPin.GPIO_00, 600);

		// create and register the serial data listener
		serialIMU.addListener(new SerialDataListener()
		{
			@Override
			public void dataReceived(SerialDataEvent event)
			{
				// print out the data received to the console
				System.out.print(event.getData());
			}
		});

		// create and register the serial data listener
		serialGPS.addListener(new SerialDataListener()
		{
			@Override
			public void dataReceived(SerialDataEvent event)
			{
				// print out the data received to the console
				System.out.print(event.getData());
			}
		});

		try
		{
			// open the default serial port provided on the GPIO header
			serialIMU.open(Serial.DEFAULT_COM_PORT, 57600);

			// Send a command for RAW SENSOR data of all 9 axes.
			// acc, mag, gyr.
			serialIMU.write("#osrt");

			serialGPS.open("/dev/ttyUSB0", 9600);

		} catch (SerialPortException ex)
		{
			System.out
					.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
	}
}