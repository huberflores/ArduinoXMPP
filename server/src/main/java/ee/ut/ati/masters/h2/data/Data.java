package ee.ut.ati.masters.h2.data;

public class Data {

	private int type;
	private double value;

	public Data() {
	}

	public Data(int type, double value) {
		this.type = type;
		this.value = value;
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
}
