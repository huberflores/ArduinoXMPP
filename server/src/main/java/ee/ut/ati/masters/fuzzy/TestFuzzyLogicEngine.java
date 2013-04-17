package ee.ut.ati.masters.fuzzy;


import ee.ut.ati.masters.fuzzy.controller.BasicFuzzyController;
import ee.ut.ati.masters.fuzzy.deffuzifyer.TempMethod;
import ee.ut.ati.masters.fuzzy.functions.FunctionException;
import ee.ut.ati.masters.fuzzy.functions.TrapezoidalMembershipFunction;
import ee.ut.ati.masters.fuzzy.functions.TriangularMembershipFunction;
import ee.ut.ati.masters.fuzzy.modifier.FzSet;
import ee.ut.ati.masters.fuzzy.variables.Constants;
import ee.ut.ati.masters.fuzzy.variables.FuzzySet;
import ee.ut.ati.masters.fuzzy.variables.LinguisticVariable;

import java.util.InputMismatchException;
import java.util.Scanner;


/**
 * 
 * @author Huber Flores
 * Fuzzy Logic Model for Code Offloading - Simple version
 *
 */

public class TestFuzzyLogicEngine implements Constants {
	private BasicFuzzyController controller;

	public TestFuzzyLogicEngine() {
		controller = new BasicFuzzyController();
		controller.setDefuzzifyerMethod(new TempMethod());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws FunctionException {
		TestFuzzyLogicEngine fle = new TestFuzzyLogicEngine();

		Scanner scan = new Scanner(System.in);
		String input;
		while (!"exit".equalsIgnoreCase(input = scan.next())) {
			try {
				String[] split = input.split(",");
				double temp = Double.valueOf(split[0]);
				double light = Double.valueOf(split[1]);
				int time = fle.calculatePredictTime(new DataHolder(temp, 0.91), new DataHolder(light, 0.91));
				System.out.println("Prediction time: " + time);
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
			} catch (NumberFormatException ex) {
				System.out.println("Invalid input");
			}
		}
	}

	public int calculatePredictTime(DataHolder temp, DataHolder light) {
		controller.fuzzify(LBL_TEMPERATURE, temp.getMeasuredValue());
		controller.fuzzify(LBL_LIGHT, light.getMeasuredValue());
		return controller.defuzzify(LBL_RESULT);
	}

	public void initialize(double prevTemp, double prevLight) {
		double tempChangeSlopeWidth = 0.5;
		double tempSameSlopeWidth = 0.5;
		double tempSensorError = 0.1; // To both sides

		LinguisticVariable temp = new LinguisticVariable(LBL_TEMPERATURE);
		FuzzySet tempChangedLeft = temp.addSet(LBL_TEMP_CHANGED_LEFT, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, prevTemp - tempChangeSlopeWidth, prevTemp, false));
		FuzzySet tempChangedRight = temp.addSet(LBL_TEMP_CHANGED_RIGHT, new TrapezoidalMembershipFunction(prevTemp, prevTemp + tempChangeSlopeWidth, Double.POSITIVE_INFINITY, true));
		FuzzySet tempSame = temp.addSet(LBL_TEMP_SAME, new TrapezoidalMembershipFunction(prevTemp - tempSameSlopeWidth, prevTemp - tempSensorError, prevTemp + tempSensorError, prevTemp + tempSameSlopeWidth));
		controller.addVariable(temp);

		double lightChangeSlopeWidth = 25;
		double lightSameSlopeWidth = 25;
		double lightSensorError = 5; // To both sides

		LinguisticVariable light = new LinguisticVariable(LBL_LIGHT);
		FuzzySet lightChangedLeft = light.addSet(LBL_LIGHT_CHANGED_LEFT, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, prevLight - lightChangeSlopeWidth, prevLight, false));
		FuzzySet lightChangedRight = light.addSet(LBL_LIGHT_CHANGED_RIGHT, new TrapezoidalMembershipFunction(prevLight, prevLight + lightChangeSlopeWidth, Double.POSITIVE_INFINITY, true));
		FuzzySet lightSame = light.addSet(LBL_LIGHT_SAME, new TrapezoidalMembershipFunction(prevLight - lightSameSlopeWidth, prevLight - lightSensorError, prevLight + lightSensorError, prevLight + lightSameSlopeWidth));
		controller.addVariable(light);

		LinguisticVariable decision = new LinguisticVariable(LBL_RESULT);
		FuzzySet requestData = decision.addSet(LBL_REQUEST, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, 0, 10, false));
		FuzzySet predictData = decision.addSet(LBL_PREDICT, new TrapezoidalMembershipFunction(10, 60, 60, 60));
		controller.addVariable(decision);

		controller.addRule(new FzSet(tempChangedLeft), new FzSet(requestData));
		controller.addRule(new FzSet(tempChangedRight), new FzSet(requestData));
		controller.addRule(new FzSet(tempSame), new FzSet(predictData));
		controller.addRule(new FzSet(lightChangedLeft), new FzSet(requestData));
		controller.addRule(new FzSet(lightChangedRight), new FzSet(requestData));
		controller.addRule(new FzSet(lightSame), new FzSet(predictData));
	}

	public static class DataHolder {
		private double measuredValue;
		private double predictability;

		public DataHolder(double measuredValue, double predictability) {
			this.measuredValue = measuredValue;
			this.predictability = predictability;
		}

		public double getMeasuredValue() {
			return measuredValue;
		}

		public double getPredictability() {
			return predictability;
		}
	}
}