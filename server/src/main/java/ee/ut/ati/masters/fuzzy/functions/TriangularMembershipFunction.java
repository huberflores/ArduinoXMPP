
package ee.ut.ati.masters.fuzzy.functions;


public class TriangularMembershipFunction implements MembershipFunction {

    /**
     * The left point of the triangle
     */
    private double a;
    /**
     * The right point of the triangle
     */
    private double b;
    /**
     * The central point of the triangle or its center
     */
    private double c;

    /**
     * 
     * @param a The left point of the triangle x when y=0
     * @param b The right point of the triangle x when y=0
     * @param c The central point of the triangle x when y=1
     * @exception FunctionException if at least one of the values in the 
     * constructor is infinity
     */
    public TriangularMembershipFunction(double a, double b, double c) {
	    setPoints(a, b, c);
    }

    /**
     * This constructor create a new instance of <code> TriangularMembershipFunction</code>
     * taking the central point as the middle point between a and b.
     * c=(a+b)/2
     * @param a The left point of the triangle x when y=0
     * @param b The right point of the triangle x when y=0
     * @exception FunctionException if at least one of the values in the 
     * constructor is infinity
     */
    public TriangularMembershipFunction(double a, double b) {
	    setPoints(a, b, (a + b) / 2.0);
    }

	public void setPoints(double a, double b, double c) {
		this.a = capValue(a);
		this.b = capValue(b);
		this.c = capValue(c);
	}

	private double capValue(double value) {
		if (value == Double.POSITIVE_INFINITY) {
			return Double.MAX_VALUE;
		} else if (value == Double.NEGATIVE_INFINITY) {
			return Double.MIN_VALUE;
		}
		return value;
	}

    /**
     * @param input the value x of f(x). 
     * @return double the result of the function.
     */
    @Override
    public double getValue(double input) {
        double result = 0;
        if (input >= a && input <= c) {
            result = (input - a) / (c - a);
            return result;
        } else if (input >= c && input <= b) {
            result = (input - b) / (c - b);
            return result;
        }
        return result;
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
        double result = 0;
        if (input >= a && input <= c) {
            result = (input - a) / (c - a);
            if (result < clip) {
                return result;
            } else {
                return clip;
            }
        } else if (input >= c && input <= b) {
            result = (input - b) / (c - b);
            if (result < clip) {
                return result;
            } else {
                return clip;
            }
        }
        return result;
    }

    /**
     * @return the maximum (not infinity) value of the function, 
     * b-point in this case.
     */
    @Override
    public double getMax() {
        return b;
    }

    /**
     * @return the minimum (not infinity) value of the function, 
     * a-point in this case.
     */
    @Override
    public double getMin() {
        return a;
    }

	@Override
	public double getXAtValue(double fnValue) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
