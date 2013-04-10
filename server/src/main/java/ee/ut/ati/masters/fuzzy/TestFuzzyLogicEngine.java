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

	// Temperature
	private FuzzySet tempChangedLeft;
	private FuzzySet tempChangedRight;
	private FuzzySet tempSame;
	
	// Decision
	private FuzzySet requestData;
	private FuzzySet predictData;

	private BasicFuzzyController controller;

	public TestFuzzyLogicEngine() {
		controller = new BasicFuzzyController();
		createVariables(controller);
		createRules(controller);
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
				fle.adjustSets(temp);
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
			} catch (NumberFormatException ex) {
				System.out.println("Invalid input");
			}
		}
	}

	public int calculatePredictTime(double temp) {
		controller.fuzzify(KEY_TEMPERATURE, temp);
		int result = controller.defuzzify(KEY_RESULT);
		adjustSets(temp);
		return result;
	}

	public void adjustSets(double temp) {
		TriangularMembershipFunction sameFunction = (TriangularMembershipFunction) tempSame.getMembershipFunction();
		sameFunction.setPoints(temp - 1, temp + 1, temp);

		TrapezoidalMembershipFunction leftFunction = (TrapezoidalMembershipFunction) tempChangedLeft.getMembershipFunction();
		leftFunction.setPoints(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, temp - 3, temp);

		TrapezoidalMembershipFunction rightFunction = (TrapezoidalMembershipFunction) tempChangedRight.getMembershipFunction();
		rightFunction.setPoints(temp, temp + 3, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Create default sets
	 * @param bfc
	 * @throws FunctionException
	 */
	public void createVariables(BasicFuzzyController bfc) {
		LinguisticVariable mv1 = new LinguisticVariable(KEY_TEMPERATURE);
		tempChangedLeft = mv1.addSet(KEY_CHANGED_LEFT, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, 9, 12, false));
		tempChangedRight = mv1.addSet(KEY_CHANGED_RIGHT, new TrapezoidalMembershipFunction(12, 15, Double.POSITIVE_INFINITY, true));
		tempSame = mv1.addSet(KEY_SAME, new TriangularMembershipFunction(11, 13, 12));
		bfc.addVariable(mv1);

		LinguisticVariable decision = new LinguisticVariable(KEY_RESULT);
		requestData = decision.addSet(KEY_REQUEST, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, 0, 10, false));
		predictData = decision.addSet(KEY_PREDICT, new TrapezoidalMembershipFunction(10, 60, 60, 60));
		bfc.addVariable(decision);
	}

	public void createRules(BasicFuzzyController bfc) {
		bfc.addRule(new FzSet(tempChangedLeft), new FzSet(requestData));
		bfc.addRule(new FzSet(tempChangedRight), new FzSet(requestData));
		bfc.addRule(new FzSet(tempSame), new FzSet(predictData));
	}
}