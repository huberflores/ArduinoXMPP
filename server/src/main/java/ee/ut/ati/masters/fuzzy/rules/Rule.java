
package ee.ut.ati.masters.fuzzy.rules;

import ee.ut.ati.masters.fuzzy.variables.FuzzySet;
import org.apache.log4j.Logger;

import java.io.Serializable;


public class Rule implements Serializable {

	private FuzzyTerm antecedent;
	private FuzzySet consequent;

	/**
	 * Construct a Rule.
	 *
	 * @param antecedent the antecedent of the Rule
	 * @param consequent the consequent of the Rule
	 */
	public Rule(FuzzyTerm antecedent, FuzzySet consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
	}

	/**
	 * Calculate the consequent of the rule, based on the degrees of the antecedent.
	 */
	public FuzzySet calculate() {
		//System.out.println("RULE "+antecedent.getDOM());
		Logger log = Logger.getLogger(this.getClass());
		log.debug("Firing rule: " + toString());
		consequent.orWithDOM(getAntecedent().getDOM());
		return consequent;
	}

	/**
	 * @return the antecedent
	 */
	public FuzzyTerm getAntecedent() {
		return antecedent;
	}

	/**
	 * @param antecedent the antecedent to set
	 */
	public void setAntecedent(FuzzyTerm antecedent) {
		this.antecedent = antecedent;
	}

	/**
	 * @return the consequent
	 */
	public FuzzyTerm getConsequent() {
		return consequent;
	}

	/**
	 * @param consequent the consequent to set
	 */
	public void setConsequent(FuzzySet consequent) {
		this.consequent = consequent;
	}

	@Override
	public String toString() {
		return "IF " + antecedent.toString() + " THEN " + consequent.toString();
	}
}
