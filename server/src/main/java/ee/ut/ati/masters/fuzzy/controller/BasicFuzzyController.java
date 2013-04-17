package ee.ut.ati.masters.fuzzy.controller;


import ee.ut.ati.masters.fuzzy.deffuzifyer.DefuzzyerMethod;
import ee.ut.ati.masters.fuzzy.deffuzifyer.PredictionDefuzzifierMethod;
import ee.ut.ati.masters.fuzzy.rules.FuzzyTerm;
import ee.ut.ati.masters.fuzzy.rules.Rule;
import ee.ut.ati.masters.fuzzy.variables.Constants;
import ee.ut.ati.masters.fuzzy.variables.FuzzySet;
import ee.ut.ati.masters.fuzzy.variables.LinguisticVariable;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BasicFuzzyController extends FuzzyController {

	/*
	 * DefuzzyerMethod
	 */
	DefuzzyerMethod defMethod;
	/*
	 * List of Variables
	 */
	Map<String, LinguisticVariable> variables;
	/*
	 * list of rules.
	 */
	List<Rule> rulesList;

	Logger log;

	/**
	 * Create a new instance of the BasicFuzzyController
	 */
	public BasicFuzzyController() {
		variables = new HashMap<String, LinguisticVariable>();
		rulesList = new ArrayList<Rule>();
		defMethod = new PredictionDefuzzifierMethod();
		log = Logger.getLogger(this.getClass());
	}

	/**
	 * add a new Variable to the Controller.
	 *
	 * @param lv the Variable to add.
	 */
	public void addVariable(LinguisticVariable lv) {
		variables.put(lv.getLabel(), lv);
	}

	/**
	 * Set the DefuzzifyerMethod to the Controller. By default, this references
	 * to an instance of CentroidMethod
	 *
	 * @param defMethod the metod to set.
	 */
	public void setDefuzzifyerMethod(DefuzzyerMethod defMethod) {
		this.defMethod = defMethod;
	}

	/**
	 * create and add a new Rule to the Controller.
	 *
	 * @param ant Antecedent of the Rule
	 * @param con Consequent of the Rule
	 */
	public void addRule(FuzzyTerm ant, FuzzySet con) {
		rulesList.add(new Rule(ant, con));
	}

	/**
	 * fuzzify the variable desgined by the label with the given value
	 *
	 * @param label the label of the variable to fuzzify
	 * @param val   the input
	 */
	public void fuzzify(String label, double val) {
		//System.out.println("FUZZIFYING...");
		LinguisticVariable lv = variables.get(label);
		lv.fuzziffy(val);
	}

	/**
	 * defuzzify the variable designed by the label
	 *
	 * @param label the label of the variable to fuzzify
	 * @return the puntual value.
	 */
	public double defuzzify(String label) {
		//System.out.println("DEFUZZIFYING...");
		setConfidencesOfConsequentsToZero();
		LinguisticVariable lv = variables.get(label);

		if (lv == null) {
			throw new UnsupportedOperationException("HACER NUEVA EXCEPCION PARA ESTO");
		}

		// Add all the rules together...
		Map<String, FuzzySet> lvSets = lv.getSets();

		FuzzySet requestSet = null;
		FuzzySet predictSet = null;

		for (Rule r : rulesList) {
			FuzzySet result = r.calculate();
			if (result.getDOM() == 0) {
				// This rule did not fire, skip the result
				continue;
			}
			if (Constants.LBL_SET_PREDICT.equals(result.getLabel())) {
				if (predictSet == null) {
					predictSet = result;
				} else {
					predictSet.and(result);
				}
			} else if (Constants.LBL_SET_REQUEST.equals(result.getLabel())) {
				if (requestSet == null) {
					requestSet = result;
				} else {
					requestSet.or(result);
				}
			}
			log.debug("Rule result = " + result + " | Updated " + label + " values: " + predictSet + ", " + requestSet);
		}
		log.debug("Defuzziefied values for variable " + label + ": " + predictSet + ", " + requestSet);
		return defMethod.getDefuzziedValue(predictSet, requestSet);
	}

	/**
	 * Private method: used to defuzzify
	 */
	private void setConfidencesOfConsequentsToZero() {
		for (Rule r : rulesList) {
			r.getConsequent().clearDOM();
		}
	}
}