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
				double temp = Double.valueOf(input);
				int time = fle.calculatePredictTime(temp);
				System.out.println("Prediction time: " + time);
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
			} catch (NumberFormatException ex) {
				System.out.println("Invalid input");
			}
		}
	}

	public int calculatePredictTime(double temp) {
		controller.fuzzify(LBL_TEMPERATURE, temp);
		return controller.defuzzify(LBL_RESULT);
	}

	public void initialize(double prevTemp, double prevLight) {
		double changeSlopeWidth = 0.5;
		double sameSlopeWidth = 0.5;
		double sensorError = 0.1; // To both sides

		LinguisticVariable temp = new LinguisticVariable(LBL_TEMPERATURE);
		FuzzySet tempChangedLeft = temp.addSet(LBL_TEMP_CHANGED_LEFT, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, prevTemp - changeSlopeWidth, prevTemp, false));
		FuzzySet tempChangedRight = temp.addSet(LBL_TEMP_CHANGED_RIGHT, new TrapezoidalMembershipFunction(prevTemp, prevTemp + changeSlopeWidth, Double.POSITIVE_INFINITY, true));
		FuzzySet tempSame = temp.addSet(LBL_TEMP_SAME, new TrapezoidalMembershipFunction(prevTemp - sameSlopeWidth, prevTemp - sensorError, prevTemp + sensorError, prevTemp + sameSlopeWidth));
		controller.addVariable(temp);

		//LinguisticVariable light = new LinguisticVariable(LBL_TEMPERATURE);
		//FuzzySet lightChangedLeft = temp.addSet(LBL_LIGHT_CHANGED_LEFT, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, prevLight - changeSlopeWidth, prevLight, false));
		//FuzzySet lightChangedRight = temp.addSet(LBL_LIGHT_CHANGED_RIGHT, new TrapezoidalMembershipFunction(prevLight, prevLight + changeSlopeWidth, Double.POSITIVE_INFINITY, true));
		//FuzzySet lightSame = temp.addSet(LBL_LIGHT_SAME, new TrapezoidalMembershipFunction(prevLight - sameSlopeWidth, prevLight - sensorError, prevLight + sensorError, prevLight + sameSlopeWidth));
		//controller.addVariable(temp);

		LinguisticVariable decision = new LinguisticVariable(LBL_RESULT);
		FuzzySet requestData = decision.addSet(LBL_REQUEST, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, 0, 10, false));
		FuzzySet predictData = decision.addSet(LBL_PREDICT, new TrapezoidalMembershipFunction(10, 60, 60, 60));
		controller.addVariable(decision);

		controller.addRule(new FzSet(tempChangedLeft), new FzSet(requestData));
		controller.addRule(new FzSet(tempChangedRight), new FzSet(requestData));
		controller.addRule(new FzSet(tempSame), new FzSet(predictData));
	}
}