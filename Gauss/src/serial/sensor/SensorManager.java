package serial.sensor;

import java.util.ArrayList;
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

	private ArrayList<SensorEventListener> listeners;

	public SensorManager()
	{
		listeners = new ArrayList<SensorEventListener>();

		initSensors();
	}

	public void registerListener(SensorEventListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(SensorEventListener listener)
	{
		int i = listeners.indexOf(listener);

		if (i >= 0)
		{
			listeners.remove(i);
		}
	}

	public void notifyObservers(SensorEvent event)
	{
		for (SensorEventListener listener : listeners)
		{
			listener.onSensorChanged(event);
		}
	}

	private void parseEventData(String eventData)
	{
		String[] data = eventData.split("#");

		for (int i = 0; i < data.length; i++)
		{
			if (data[i].contains("A-R="))
			{
				parseAcceleration(data[i]);
			}

			if (data[i].contains("G-R="))
			{
				parseGyroscope(data[i]);
			}
			
			if (data[i].contains("M-R="))
			{
				parseMagnetic(data[i]);
			}
		}
	}

	private void parseAcceleration(String data)
	{
		data = data.replace("A-R=", "");

		String[] dataList = data.split(",");

		double[] acceleration = new double[3];

		for (int i = 0; i < dataList.length; i++)
		{
			// Scale our acceleration units to meters/sec
			acceleration[i] = Double.valueOf(dataList[i]) / 24.5;
		}

		SensorEvent event = new SensorEvent();

		event.sensor = new Sensor(Sensor.TYPE_ACCELEROMETER);
		event.values = acceleration;

		notifyObservers(event);
	}

	private void parseGyroscope(String data)
	{
		data = data.replace("G-R=", "");

		System.out.println(data);

		String[] dataList = data.split(",");

		double[] rotation = new double[3];

		for (int i = 0; i < dataList.length; i++)
		{

			// BUGISH... randomly, a negative symbol "-" is sent without a
			// number, so we look for this and replace it with 0.
			dataList[i] = dataList[i].replaceAll("\\s", "");

			if (!dataList[i].equals("-") && !dataList[i].equals(""))
			{
				rotation[i] = Double.valueOf(dataList[i]);
			} else
			{
				rotation[i] = 0;
			}
		}

		SensorEvent event = new SensorEvent();

		event.sensor = new Sensor(Sensor.TYPE_GYROSCOPE);
		event.values = rotation;

		notifyObservers(event);
	}
	
	private void parseMagnetic(String data)
	{
		data = data.replace("M-R=", "");

		String[] dataList = data.split(",");

		double[] magnetic = new double[3];

		for (int i = 0; i < dataList.length; i++)
		{
			magnetic[i] = Double.valueOf(dataList[i]);
		}

		System.out.println(Arrays.toString(magnetic));

		SensorEvent event = new SensorEvent();

		event.sensor = new Sensor(Sensor.TYPE_MAGNETIC);
		event.values = magnetic;

		notifyObservers(event);
	}

	private void initSensors()
	{
		// create and register the serial data listener
		serialIMU.addListener(new SerialDataListener()
		{
			@Override
			public void dataReceived(SerialDataEvent event)
			{
				// print out the data received to the console
				// System.out.print(event.getData());

				parseEventData(event.getData());
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

}
