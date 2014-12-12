package serial.sensor;

public class Sensor
{
	public static final int TYPE_ACCELEROMETER = 0 ;
	public static final int TYPE_MAGNETIC = 1;
	public static final int TYPE_GYROSCOPE = 2;
	
	private int type;
	
	public Sensor(int type)
	{
		this.type = type;
	}
	
	public int getType()
	{
		return type;
	}
}
