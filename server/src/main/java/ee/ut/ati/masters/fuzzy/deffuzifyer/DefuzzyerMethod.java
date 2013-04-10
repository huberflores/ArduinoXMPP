
package ee.ut.ati.masters.fuzzy.deffuzifyer;

import ee.ut.ati.masters.fuzzy.variables.LinguisticVariable;


public interface DefuzzyerMethod {

    public double getDefuzziedValue(LinguisticVariable lv);
}
