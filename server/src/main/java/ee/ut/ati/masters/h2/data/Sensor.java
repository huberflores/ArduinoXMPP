package ee.ut.ati.masters.h2.data;

public class Sensor {

	public static final int TYPE_TEMPERATURE = 1;
	public static final int TYPE_HALL = 2;
	public static final int TYPE_LIGHT = 3;

	private long id;
	private String description;
	private double regressionError;
	private double measureError;

	public Sensor(long id, String description, double regressionError, double measureError) {
		this.id = id;
		this.description = description;
		this.regressionError = regressionError;
		this.measureError = measureError;
	}

	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public double getRegressionError() {
		return regressionError;
	}

	public double getMeasureError() {
		return measureError;
	}


}
