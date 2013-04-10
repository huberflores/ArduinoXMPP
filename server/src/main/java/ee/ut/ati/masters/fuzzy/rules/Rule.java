
package ee.ut.ati.masters.fuzzy.rules;

import org.apache.log4j.Logger;

import java.io.Serializable;


public class Rule implements Serializable {

    private FuzzyTerm antecedent;
    private FuzzyTerm consequent;
/**
 * 
 * Construct a Rule. 
 * @param antecedent the antecedent of the Rule 
 * @param consequent the consequent of the Rule
 */
    public Rule(FuzzyTerm antecedent, FuzzyTerm consequent) {
        this.antecedent = antecedent;
        this.consequent = consequent;
    }

	/**
	 * Calculate the consequent of the rule, based on the degrees of the antecedent.
	 */
    public void calculate() {
        //System.out.println("RULE "+antecedent.getDOM());
	    Logger log = Logger.getLogger(this.getClass());
		log.debug("Firing a rule, initial consequentDOM= " + consequent.getDOM());
        consequent.orWithDOM(getAntecedent().getDOM());
	    log.debug("After consequentDOM= " + consequent.getDOM());
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
    public void setConsequent(FuzzyTerm consequent) {
        this.consequent = consequent;
    }
}
