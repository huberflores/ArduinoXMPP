package ee.ut.ati.masters.h2.data;

import java.sql.Timestamp;

public class Data {

	public static final int TYPE_TEMPERATURE = 1;
	public static final int TYPE_HALL = 2;
	public static final int TYPE_LIGHT = 3;

	private int type;
	private double value;
	private Timestamp measureTime;
	private boolean measured;

	public Data() {}

	public Data(int type, double value) {
		this.type = type;
		this.value = value;
		this.measureTime = new Timestamp(System.currentTimeMillis());
		this.measured = false;
	}

	public Timestamp getMeasureTime() {
		return measureTime;
	}

	public void setMeasureTime(Timestamp measureTime) {
		this.measureTime = measureTime;
	}

	public boolean isMeasured() {
		return measured;
	}

	public void setMeasured(boolean measured) {
		this.measured = measured;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Data{" +
				"type=" + type +
				", value=" + value +
				", measureTime=" + measureTime +
				", measured=" + measured +
				'}';
	}
}
