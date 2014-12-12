package serial.sensor;

import java.util.Arrays;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class SensorManager
{
	// create an instance of the serial communications class
	private final Serial serialIMU = SerialFactory.createInstance();

	public SensorManager()
	{
		// create and register the serial data listener
		serialIMU.addListener(new SerialDataListener()
		{
			@Override
			public void dataReceived(SerialDataEvent event)
			{
				// print out the data received to the console
				System.out.print(event.getData());

				//parseEventData(event.getData());
			}
		});

		try
		{
			// open the default serial port provided on the GPIO header
			serialIMU.open(Serial.DEFAULT_COM_PORT, 57600);

			// Send a command for RAW SENSOR data of all 9 axes.
			// acc, mag, gyr.
			serialIMU.write("#osrt");

		} catch (SerialPortException ex)
		{
			System.out
					.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
			return;
		}
	}

	private void parseEventData(String eventData)
	{
		String[] data = eventData.split("#");

		for (int i = 0; i < data.length; i++)
		{
			if (data[i].contains("A-R="))
			{
				data[i] = data[i].replace("A-R=", "");

				String[] dataList = data[i].split(",");
				
				double[] acceleration = new double[3];

				for (int j = 0; j < dataList.length; j++)
				{
					acceleration[j] = Double.valueOf(dataList[j]);
				}

				System.out.println(Arrays.toString(acceleration));

			}
		}

	}

}
