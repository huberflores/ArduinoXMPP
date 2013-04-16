package ee.ut.ati.masters.fuzzy.deffuzifyer;

import ee.ut.ati.masters.fuzzy.variables.Constants;
import ee.ut.ati.masters.fuzzy.variables.FuzzySet;
import ee.ut.ati.masters.fuzzy.variables.LinguisticVariable;

import java.util.Map;

public class TempMethod implements DefuzzyerMethod {

	@Override
	public double getDefuzziedValue(LinguisticVariable lv) {
		Map<String, FuzzySet> sets = lv.getSets();
		FuzzySet requestSet = sets.get(Constants.LBL_REQUEST);
		FuzzySet predictSet = sets.get(Constants.LBL_PREDICT);
		if (requestSet == null) {
			throw new RuntimeException("Request set missing");
		}
		if (predictSet == null) {
			throw new RuntimeException("Predict set missing");
		}
		return Math.max(predictSet.getDOM() - requestSet.getDOM(), 0);
	}
}
