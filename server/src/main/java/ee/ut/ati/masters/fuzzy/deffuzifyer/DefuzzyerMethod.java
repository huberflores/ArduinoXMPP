
package ee.ut.ati.masters.fuzzy.deffuzifyer;

import ee.ut.ati.masters.fuzzy.variables.FuzzySet;

public interface DefuzzyerMethod {

    public double getDefuzziedValue(FuzzySet... results);
}
