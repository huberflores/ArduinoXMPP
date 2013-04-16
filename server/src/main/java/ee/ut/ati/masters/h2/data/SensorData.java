package ee.ut.ati.masters.h2.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public Map<Integer, Data> getDataMap() {
		if (data == null) {
			return null;
		}
		HashMap<Integer, Data> map = new HashMap<Integer, Data>(data.size());
		for (Data listData : data) {
			map.put(listData.getType(), listData);
		}
		return map;
	}
}
