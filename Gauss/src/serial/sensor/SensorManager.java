package serial.sensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class SensorManager
{
	private double[] acceleration = new double[3];
	private double[] magnetic = new double[3];
	private double[] rotation = new double[3];
	
	private float startTime = 0;
	
	private int count = 1;
	
	private ArrayList<SensorEventListener> listeners;
	
	private Queue<SensorEvent> sensorEventQueue;
	
	// create an instance of the serial communications class
	private final Serial serialIMU = SerialFactory.createInstance();
	
	private final Sensor accelerationSensor = new Sensor(Sensor.TYPE_ACCELEROMETER);
	private final Sensor gyroscopeSensor = new Sensor(Sensor.TYPE_GYROSCOPE);
	private final Sensor magneticSensor = new Sensor(Sensor.TYPE_MAGNETIC);
	
	public SensorManager()
	{
		listeners = new ArrayList<SensorEventListener>();
		
		sensorEventQueue = new LinkedList<SensorEvent>();
		
		for(int i = 0; i < 20; i++)
		{
			sensorEventQueue.add(new SensorEvent());
		}

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
		if(startTime == 0)
		{
			startTime = System.nanoTime();
		}
		
		float hz = ((System.nanoTime() - startTime)/1000000)/count++;
		
		System.out.println("Hz: " + hz);
		
		data = data.replace("A-R=", "");

		String[] dataList = data.split(",");

		for (int i = 0; i < dataList.length; i++)
		{
			// Scale our acceleration units to meters/sec
			acceleration[i] = Double.valueOf(dataList[i]) / 24.5;
		}

		SensorEvent event = sensorEventQueue.remove();
		
		event.sensor = accelerationSensor;
		event.values = acceleration;

		notifyObservers(event);
		
		sensorEventQueue.add(event);
	}

	private void parseGyroscope(String data)
	{
		data = data.replace("G-R=", "");

		String[] dataList = data.split(",");

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

		SensorEvent event = sensorEventQueue.remove();

		event.sensor = gyroscopeSensor;
		event.values = rotation;

		notifyObservers(event);
		
		sensorEventQueue.add(event);
	}
	
	private void parseMagnetic(String data)
	{
		data = data.replace("M-R=", "");

		String[] dataList = data.split(",");

		for (int i = 0; i < dataList.length; i++)
		{
			magnetic[i] = Double.valueOf(dataList[i]);
		}

		SensorEvent event = sensorEventQueue.remove();

		event.sensor = magneticSensor;
		event.values = magnetic;

		notifyObservers(event);
		
		sensorEventQueue.add(event);
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
