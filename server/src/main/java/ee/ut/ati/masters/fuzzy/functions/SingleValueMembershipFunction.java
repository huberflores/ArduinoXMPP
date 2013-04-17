package ee.ut.ati.masters.fuzzy.functions;


public class SingleValueMembershipFunction implements MembershipFunction {

	private double value;

	public SingleValueMembershipFunction(double value) {
		this.value = value;
	}

    /**
     * @param input the value f(x)
     * @return double the result of the function.
     */
    @Override
    public double getValue(double input) {
       return 1;
    }

    /**
     * 
     * @param input the value x of f(x). 
     * @param clip the value to clip: if result > clip then return clip.
     * @return double the result of the function if it is under clip,
     * else return the clip value
     */
    @Override
    public double getClippedValue(double input, double clip) {
        return 1 > clip ? clip : 1;
    }

    /**
     * @return the maximum (not infinity) value of the function, 
     * warning
     */
    @Override
    public double getMax() {
        return value;
    }

    /**
     * @return the minimum (not infinity) value of the function, 
     * warning
     */
    @Override
    public double getMin() {
        return value;
    }

	@Override
	public double getXAtValue(double fnValue) {
		return value;
	}
}
