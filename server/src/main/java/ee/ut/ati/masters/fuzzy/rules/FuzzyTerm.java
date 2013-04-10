
package ee.ut.ati.masters.fuzzy.rules;

import java.io.Serializable;


public interface FuzzyTerm extends Serializable {

    double getDOM();

    void clearDOM();

    void orWithDOM(double val);
    
}
