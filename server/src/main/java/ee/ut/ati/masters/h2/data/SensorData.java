package ee.ut.ati.masters.h2.data;

import java.util.List;

public class SensorData {

	private int location;
	private List<Data> data;

	public SensorData() {
	}

	public SensorData(int location, List<Data> data) {
		this.location = location;
		this.data = data;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public List<Data> getData() {
		return data;
	}

}
