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
import ee.ut.ati.masters.h2.data.Sensor;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 * @author Huber Flores
 * Fuzzy Logic Model for Code Offloading - Simple version
 *
 */

public class TestFuzzyLogicEngine implements Constants {
	private BasicFuzzyController controller;
	private Map<Integer, Sensor> sensorMap;

	public TestFuzzyLogicEngine(Map<Integer, Sensor> sensorMap) {
		this.sensorMap = sensorMap;
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
//				TestFuzzyLogicEngine fle = new TestFuzzyLogicEngine();
//				fle.initialize(prevTemp, prevLight);
//				String[] split = input.split(";");
//				System.out.println(Arrays.toString(split));
//				double temp = Double.valueOf(split[0]);
//				double tempPred = Double.valueOf(split[1]);
//				double light = Double.valueOf(split[2]);
//				double lightPred = Double.valueOf(split[3]);
//				int time = fle.calculatePredictTime(new DataHolder(temp, tempPred), new DataHolder(light, lightPred));
//				prevTemp = temp;
//				prevLight = light;
//				System.out.println("Prediction time: " + time);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Invalid input");
			}
		}
	}

	public int calculatePredictTime(DataHolder temp, DataHolder light, DataHolder hall) {
		controller.fuzzify(LBL_TEMPERATURE, temp.getMeasuredValue());
		controller.fuzzify(LBL_TEMPERATURE_PREDICTABLE, temp.getPredictability());
		controller.fuzzify(LBL_LIGHT, light.getMeasuredValue());
		controller.fuzzify(LBL_LIGHT_PREDICTABLE, light.getPredictability());
		controller.fuzzify(LBL_HALL, hall.getMeasuredValue());
		controller.fuzzify(LBL_HALL_PREDICTABLE, hall.getPredictability());
		return (int) controller.defuzzify(LBL_RESULT);
	}

	public void initialize(double prevTemp, double prevLight, double prevHall) {
		Logger log = Logger.getLogger("FuzzyEngine");
		log.debug("Initializing temp = " + prevTemp + ", light = " + prevLight + ", hall = " + prevHall);

		Sensor tempSensor = sensorMap.get(Sensor.TYPE_TEMPERATURE);
		Sensor lightSensor = sensorMap.get(Sensor.TYPE_LIGHT);
		Sensor hallSensor = sensorMap.get(Sensor.TYPE_HALL);

		double tempSensorError = tempSensor.getMeasureError();
		double tempChangeSlopeWidth = tempSensorError * 5;
		double tempSameSlopeWidth = tempSensorError * 5;

		LinguisticVariable temp = new LinguisticVariable(LBL_TEMPERATURE);
		FuzzySet tempChangedLeft = temp.addSet(createChangedLeftSet(LBL_SET_TEMP_CHANGED_LEFT, prevTemp, tempSensorError, tempChangeSlopeWidth));
		FuzzySet tempChangedRight = temp.addSet(createChangedRightSet(LBL_SET_TEMP_CHANGED_RIGHT,prevTemp, tempSensorError, tempChangeSlopeWidth));
		FuzzySet tempSame = temp.addSet(createSameSet(LBL_SET_TEMP_SAME, prevTemp, tempSameSlopeWidth, tempSensorError));
		controller.addVariable(temp);

		double lightSensorError = lightSensor.getMeasureError(); // To both sides
		double lightChangeSlopeWidth = lightSensorError * 5;
		double lightSameSlopeWidth = lightSensorError * 5;

		LinguisticVariable light = new LinguisticVariable(LBL_LIGHT);
		FuzzySet lightChangedLeft = light.addSet(createChangedLeftSet(LBL_SET_LIGHT_CHANGED_LEFT, prevLight, lightSensorError, lightChangeSlopeWidth));
		FuzzySet lightChangedRight = light.addSet(createChangedRightSet(LBL_SET_LIGHT_CHANGED_RIGHT, prevLight, lightSensorError, lightChangeSlopeWidth));
		FuzzySet lightSame = light.addSet(createSameSet(LBL_SET_LIGHT_SAME, prevLight, lightSameSlopeWidth, lightSensorError));
		controller.addVariable(light);

		double hallSensorError = hallSensor.getMeasureError(); // To both sides
		double hallChangeSlopeWidth = hallSensorError * 5;
		double hallSameSlopeWidth = hallSensorError * 5;

		LinguisticVariable hall = new LinguisticVariable(LBL_HALL);
		FuzzySet hallChangedLeft = hall.addSet(createChangedLeftSet(LBL_SET_HALL_CHANGED_LEFT, prevHall, hallSensorError, hallChangeSlopeWidth));
		FuzzySet hallChangedRight = hall.addSet(createChangedRightSet(LBL_SET_HALL_CHANGED_RIGHT, prevHall, hallSensorError, hallChangeSlopeWidth));
		FuzzySet hallSame = hall.addSet(createSameSet(LBL_SET_HALL_SAME, prevHall, hallSameSlopeWidth, hallSensorError));
		controller.addVariable(hall);

		// Add predictability sets here
		LinguisticVariable tempPredictableVar = new LinguisticVariable(LBL_TEMPERATURE_PREDICTABLE);
		FuzzySet tempNotPredictable = tempPredictableVar.addSet(createNotPredictableSet(LBL_SET_TEMP_NOT_PREDICTABLE));
		FuzzySet tempPredictable = tempPredictableVar.addSet(createPredictableSet(LBL_SET_TEMP_PREDICTABLE));
		controller.addVariable(tempPredictableVar);

		LinguisticVariable lightPredictableVar = new LinguisticVariable(LBL_LIGHT_PREDICTABLE);
		FuzzySet lightNotPredictable = lightPredictableVar.addSet(createNotPredictableSet(LBL_SET_LIGHT_NOT_PREDICTABLE));
		FuzzySet lightPredictable = lightPredictableVar.addSet(createPredictableSet(LBL_SET_LIGHT_PREDICTABLE));
		controller.addVariable(lightPredictableVar);

		LinguisticVariable hallPredictableVar = new LinguisticVariable(LBL_HALL_PREDICTABLE);
		FuzzySet hallNotPredictable = hallPredictableVar.addSet(createNotPredictableSet(LBL_SET_HALL_NOT_PREDICTABLE));
		FuzzySet hallPredictable = hallPredictableVar.addSet(createPredictableSet(LBL_SET_HALL_PREDICTABLE));
		controller.addVariable(hallPredictableVar);

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

		// Hall rules
		controller.addRule(new FuzzyAND(new FuzzyOR(hallChangedLeft, hallChangedRight), hallNotPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(new FuzzyOR(hallChangedLeft, hallChangedRight), hallPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(hallSame, hallNotPredictable), new FuzzySet(requestData));
		controller.addRule(new FuzzyAND(hallSame, hallPredictable), new FuzzySet(predictData));
	}

	private static FuzzySet createChangedLeftSet(String label, double value, double error, double slopeWidth) {
		return new FuzzySet(label, new TrapezoidalMembershipFunction(Double.NEGATIVE_INFINITY, value - error - slopeWidth, value - error, false));
	}

	private static FuzzySet createChangedRightSet(String label, double value, double error, double slopeWidth) {
		return new FuzzySet(label, new TrapezoidalMembershipFunction(value + error, value + error + slopeWidth, Double.POSITIVE_INFINITY, true));
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