package ee.ut.ati.masters.fuzzy.deffuzifyer;

import ee.ut.ati.masters.fuzzy.variables.FuzzySet;
import org.apache.log4j.Logger;

public class PredictionDefuzzifierMethod implements DefuzzyerMethod {


	/**
	 *
	 * @param results array [predictSet, requestSet]
	 * @return
	 */
	@Override
	public double getDefuzziedValue(FuzzySet... results) {
		Logger log = Logger.getLogger("PredictionDefuzzifierMethod");
		if (results.length < 2) {
			log.error("Invalid arguments for defuzzification...");
			return 0;
		}

		FuzzySet predictSet = results[0];
		FuzzySet requestSet = results[1];

		if (requestSet == null) {
			return predictSet.getMembershipFunction().getXAtValue(predictSet.getDOM());
		} else if (predictSet == null) {
			return requestSet.getMembershipFunction().getXAtValue(requestSet.getDOM());
		}

		// Both sets exist.. calculate the result
		double predictDom = Math.max(0, predictSet.getDOM() - requestSet.getDOM());
		if (predictDom > 0) {
			return predictSet.getMembershipFunction().getXAtValue(predictDom);
		}
		return requestSet.getMembershipFunction().getXAtValue(requestSet.getDOM());
	}
}
