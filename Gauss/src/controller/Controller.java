package controller;

import java.util.Arrays;

import serial.sensor.Sensor;
import serial.sensor.SensorEvent;
import serial.sensor.SensorEventListener;
import serial.sensor.SensorManager;

public class Controller implements SensorEventListener
{
	private SensorManager sensorManager;

	private double[] magnetic = new double[3];

	public Controller()
	{
		sensorManager = new SensorManager();

		sensorManager.registerListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			System.out.println(Arrays.toString(event.values));
		}
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC)
		{
			// System.out.println(Arrays.toString(event.values));

			System.arraycopy(event.values, 0, magnetic, 0, magnetic.length);

			//System.out.println("Heading: " + calculateBearing());
		}
	}

	private double calculateBearing()
	{
		//  Forward the X axis, not Y. The board is backwards, so we add 180. The additional 180 comes from
		// converting from -180 to 180... to 360 degrees.
		double bearing = (Math.toDegrees(Math.atan2(magnetic[1], magnetic[0])) + 360) % 360;

		return bearing;
	}

}
