package ee.ut.ati.masters.fuzzy;

import ee.ut.ati.masters.fuzzy.comparator.FuzzyAND;
import ee.ut.ati.masters.fuzzy.comparator.FuzzyOR;
import ee.ut.ati.masters.fuzzy.controller.BasicFuzzyController;
import ee.ut.ati.masters.fuzzy.deffuzifyer.PredictionDefuzzifierMethod;
import ee.ut.ati.masters.fuzzy.functions.FunctionException;
import ee.ut.ati.masters.fuzzy.functions.LinearMembershipFunction;
import ee.ut.ati.masters.fuzzy.functions.SingleValueMembershipFunction;
import ee.ut.ati.masters.fuzzy.functions.TrapezoidalMembershipFunction;
import ee.ut.ati.masters.fuzzy.variables.Constants;
import ee.ut.ati.masters.fuzzy.variables.FuzzySet;
import ee.ut.ati.masters.fuzzy.variables.LinguisticVariable;
import org.apache.log4j.Logger;

import java.util.Arrays;
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
		controller.setDefuzzifyerMethod(new PredictionDefuzzifierMethod());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws FunctionException {
		double prevTemp = 0;
		double prevLight = 0;
		Scanner scan = new Scanner(System.in);
		String input;
		while (!"exit".equalsIgnoreCase(input = scan.next())) {
			try {
				TestFuzzyLogicEngine fle = new TestFuzzyLogicEngine();
				fle.initialize(prevTemp, prevLight);
				String[] split = input.split(";");
				System.out.println(Arrays.toString(split));
				double temp = Double.valueOf(split[0]);
				double tempPred = Double.valueOf(split[1]);
				double light = Double.valueOf(split[2]);
				double lightPred = Double.valueOf(split[3]);
				int time = fle.calculatePredictTime(new DataHolder(temp, tempPred), new DataHolder(light, lightPred));
				prevTemp = temp;
				prevLight = light;
				System.out.println("Prediction time: " + time);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Invalid input");
			}
		}
	}

	public int calculatePredictTime(DataHolder temp, DataHolder light) {
		controller.fuzzify(LBL_TEMPERATURE, temp.getMeasuredValue());
		controller.fuzzify(LBL_TEMPERATURE_PREDICTABLE, temp.getPredictability());
		controller.fuzzify(LBL_LIGHT, light.getMeasuredValue());
		controller.fuzzify(LBL_LIGHT_PREDICTABLE, light.getPredictability());
		return (int) controller.defuzzify(LBL_RESULT);
	}

	public void initialize(double prevTemp, double prevLight) {
		Logger log = Logger.getLogger("FuzzyEngine");
		log.debug("Initializing temp = " + prevTemp + ", light = " + prevLight);

		double tempChangeSlopeWidth = 0.5;
		double tempSameSlopeWidth = 0.5;
		double tempSensorError = 0.1; // To both sides

		LinguisticVariable temp = new LinguisticVariable(LBL_TEMPERATURE);
		FuzzySet tempChangedLeft = temp.addSet(createChangedLeftSet(LBL_SET_TEMP_CHANGED_LEFT, prevTemp, tempChangeSlopeWidth));
		FuzzySet tempChangedRight = temp.addSet(createChangedRightSet(LBL_SET_TEMP_CHANGED_RIGHT,prevTemp, tempChangeSlopeWidth));
		FuzzySet tempSame = temp.addSet(createSameSet(LBL_SET_TEMP_SAME, prevTemp, tempSameSlopeWidth, tempSensorError));
		controller.addVariable(temp);

		double lightChangeSlopeWidth = 25;
		double lightSameSlopeWidth = 25;
		double lightSensorError = 5; // To both sides

		LinguisticVariable light = new LinguisticVariable(LBL_LIGHT);
		FuzzySet lightChangedLeft = light.addSet(createChangedLeftSet(LBL_SET_LIGHT_CHANGED_LEFT, prevLight, lightChangeSlopeWidth));
		FuzzySet lightChangedRight = light.addSet(createChangedRightSet(LBL_SET_LIGHT_CHANGED_RIGHT, prevLight, lightChangeSlopeWidth));
		FuzzySet lightSame = light.addSet(createSameSet(LBL_SET_LIGHT_SAME, prevLight, lightSameSlopeWidth, lightSensorError));
		controller.addVariable(light);

		// Add predictability sets here
		LinguisticVariable tempPredictableVar = new LinguisticVariable(LBL_TEMPERATURE_PREDICTABLE);
		FuzzySet tempNotPredictable = tempPredictableVar.addSet(createNotPredictableSet(LBL_SET_TEMP_NOT_PREDICTABLE));
		FuzzySet tempPredictable = tempPredictableVar.addSet(createPredictableSet(LBL_SET_TEMP_PREDICTABLE));
		controller.addVariable(tempPredictableVar);

		LinguisticVariable lightPredictableVar = new LinguisticVariable(LBL_LIGHT_PREDICTABLE);
		FuzzySet lightNotPredictable = lightPredictableVar.addSet(createNotPredictableSet(LBL_SET_LIGHT_NOT_PREDICTABLE));
		FuzzySet lightPredictable = lightPredictableVar.addSet(createPredictableSet(LBL_SET_LIGHT_PREDICTABLE));
		controller.addVariable(lightPredictableVar);

		LinguisticVariable decision = new LinguisticVariable(LBL_RESULT);
		FuzzySet requestData = decision.addSet(LBL_SET_REQUEST, new SingleValueMembershipFunction(10));
		FuzzySet predictData = decision.addSet(LBL_SET_PREDICT, new LinearMembershipFunction(0, 65));
		controller.addVariable(decision);

		// Temperature rules
		controller.addRule(new FuzzyAND(new FuzzyOR(tempChangedLeft, tempChangedRight), tempNotPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(new FuzzyOR(tempChangedLeft, tempChangedRight), tempPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(tempSame, tempNotPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(tempSame, tempPredictable), new FuzzySet(predictData));

		// Light rules
		controller.addRule(new FuzzyAND(new FuzzyOR(lightChangedLeft, lightChangedRight), lightNotPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(new FuzzyOR(lightChangedLeft, lightChangedRight), lightPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(lightSame, lightNotPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(lightSame, lightPredictable), new FuzzySet(predictData));
	}

	private static FuzzySet createChangedLeftSet(String label, double value, double slopeWidth) {
		return new FuzzySet(label, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, value - slopeWidth, value, false));
	}

	private static FuzzySet createChangedRightSet(String label, double value, double slopeWidth) {
		return new FuzzySet(label, new TrapezoidalMembershipFunction(value, value + slopeWidth, Double.POSITIVE_INFINITY, true));
	}

	private static FuzzySet createSameSet(String label, double value, double slopeWidth, double error) {
		return new FuzzySet(label, new TrapezoidalMembershipFunction(value - slopeWidth, value - error, value + error, value + slopeWidth));
	}

	private static FuzzySet createPredictableSet(String label) {
		return new FuzzySet(label, new TrapezoidalMembershipFunction(0.9 - 0.01, 0.95, Double.POSITIVE_INFINITY, true));
	}

	private static FuzzySet createNotPredictableSet(String label) {
		return new FuzzySet(label, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, 0.9 - 0.001, 0.9, false));
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