package ee.ut.ati.masters.fuzzy.functions;


public class LinearMembershipFunction implements MembershipFunction {


	/**
	 * The a point of the trapezoid
	 */
	private double a;
	/**
	 * The b point of the trapezoid
	 */
	private double b;

	/**
	 * @param a
	 * @param b
	 */
	public LinearMembershipFunction(double a, double b) {
		this.a = a;
		this.b = b;
	}

	/**
	 * @param input the value x of f(x).
	 * @return double the result of the function.
	 */
	@Override
	public double getValue(double input) {
		if (input >= a && input <= b) {
			return (input - a) / (b - a);
		}
		return 0;
	}

	/**
	 * @param input the value x of f(x).
	 * @param clip  the value to clip: if result > clip then return clip.
	 * @return double the result of the function if it is under clip,
	 *         else return the clip value
	 */
	@Override
	public double getClippedValue(double input, double clip) {
		double value = getValue(input);
		return value > clip ? clip : value;
	}

	@Override
	public double getMax() {
		return b;
	}

	@Override
	public double getMin() {
		return a;
	}

	@Override
	public double getXAtValue(double fnValue) {
		if (fnValue < 0 || fnValue > 1) {
			return Double.NaN;
		}
		return a + fnValue * (b - a);
	}
}
