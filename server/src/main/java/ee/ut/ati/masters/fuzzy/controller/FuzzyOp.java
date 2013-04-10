
package ee.ut.ati.masters.fuzzy.controller;

import ee.ut.ati.masters.fuzzy.comparator.FuzzyAND;
import ee.ut.ati.masters.fuzzy.comparator.FuzzyNOT;
import ee.ut.ati.masters.fuzzy.comparator.FuzzyOR;
import ee.ut.ati.masters.fuzzy.rules.FuzzyTerm;

public class FuzzyOp {

    public static double and(double a, double b) {
        return Math.min(a, b);
    }

    public static FuzzyTerm and(FuzzyTerm a, FuzzyTerm b) {
        return new FuzzyAND(a, b);
    }

    public static FuzzyTerm or(FuzzyTerm a, FuzzyTerm b) {
        return new FuzzyOR(a, b);
    }

    public static FuzzyTerm not(FuzzyTerm a) {
        return new FuzzyNOT(a);
    }

    public static double or(double a, double b) {
        return Math.max(a, b);
    }

    public static double not(double a) {
        return 1 - a;
    }
}
