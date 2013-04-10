package ee.ut.ati.masters.fuzzy.variables;

import ee.ut.ati.masters.fuzzy.functions.MembershipFunction;
import ee.ut.ati.masters.fuzzy.modifier.FzSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * author Huber Flores
 */

public class LinguisticVariable {

	/**
	 * Fuzzy variable
	 * (e.g. Bandwidth)
	 */
	private String label;
	
	/**
	 * Possible values of the Fuzzy variable
	 * (e.g. Bandwidth -> Slowest, Slow, Normal, Fast, Fastest)
	 */
	private Map<String, FuzzySet> possibleValues;
	
	/**
	 * Constructor
	 */
	public LinguisticVariable(String label){
		this.label = label;
		possibleValues = new HashMap<String, FuzzySet>();
	}
	
	
	/**
     * @return possible values of the variable
     */
	public Map<String, FuzzySet> getSets() {
        return possibleValues;
    }
	
	 /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Fuzzify the sets of the Variable given an input.
     *
     * @param input the input
     */
    public void fuzziffy(double input) {
	    Set<String> keys = possibleValues.keySet();
        for (String key : keys) {
            possibleValues.get(key).calculateDOM(input);
        }
    }
	
	/**
    *
    * Add a FuzzySet to the LinguisticVariable Object, also return a FzSet
    * reference FzSetReferences is used to create the Rules.
    *
    * @param label the label of the set
    * @param MembershipFunction the function of the set
    * @return FzSet reference, act as a proxy of the FuzzySet instance, used to
    * create the Rules
    * @throws IllegalSetException when the set is already in the Variable
    */
   public FuzzySet addSet(String setLabel, MembershipFunction MembershipFunction) {
       FuzzySet set = new FuzzySet(setLabel, MembershipFunction);
       possibleValues.put(setLabel, set);
	   return set;
       //return new FzSet(set);
   }
   
   /**
    * @return The maximum (non infinity) value. Used to defuzzify proccess
    */
   public double getMax() {
       double flag = 0;
       double max = 0;
	   Set<String> keys = possibleValues.keySet();
	   for (String key : keys) {
           max = possibleValues.get(key).getMembershipFunction().getMax();
           if (flag < max) {
               flag = max;
           }
       }

       return flag;
   }

   /**
    *
    * @return The minimum (non infinity) value. Used to defuzzify proccess
    */
   public double getMin() {
       double flag = 0;
       double min = 0;
	   Set<String> keys = possibleValues.keySet();
	   for (String key : keys) {
           min = possibleValues.get(key).getMembershipFunction().getMin();
           if (flag > min) {
               flag = min;
           }
       }
       return flag;
   }

   /**
    * @return the label of the set with the highest degree of membership
    */
   public String getBestLabel() {
       double flag = 0;
       String labelFlag = "";
	   Set<String> keys = possibleValues.keySet();
	   for (String key : keys) {
		   FuzzySet fs = possibleValues.get(key);
           double value = fs.getDOM();
           if (value > flag) {
               labelFlag = fs.getLabel();
           }
       }
       return labelFlag;
   }
}
