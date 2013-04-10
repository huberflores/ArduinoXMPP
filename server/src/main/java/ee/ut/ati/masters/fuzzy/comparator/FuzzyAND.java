/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.ut.ati.masters.fuzzy.comparator;


import ee.ut.ati.masters.fuzzy.rules.FuzzyTerm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Root
 */
public class FuzzyAND implements FuzzyTerm {

    List<FuzzyTerm> terms;

    public FuzzyAND(FuzzyTerm termA, FuzzyTerm termB) {
        terms = new ArrayList<FuzzyTerm>();
        terms.add(termA);
        terms.add(termB);
    }

    public FuzzyAND(FuzzyTerm termA, FuzzyTerm termB, FuzzyTerm termC) {
        terms = new ArrayList<FuzzyTerm>();
        terms.add(termA);
        terms.add(termB);
        terms.add(termC);
    }

    public FuzzyAND(FuzzyTerm termA, FuzzyTerm termB, FuzzyTerm termC, FuzzyTerm termD) {
        terms = new ArrayList<FuzzyTerm>();
        terms.add(termA);
        terms.add(termB);
        terms.add(termC);
        terms.add(termD);
    }

	public FuzzyAND(FuzzyTerm... termList) {
		terms = new ArrayList<FuzzyTerm>();
		terms.addAll(Arrays.asList(termList));
	}

    @Override
    public double getDOM() {
        double minDOM = Double.POSITIVE_INFINITY;
        for (FuzzyTerm t : terms) {
            //System.out.println("AND: "+t.getDOM());
            if (t.getDOM() < minDOM) {
                minDOM = t.getDOM();
            }
        }
        //System.out.println("ANDRESULT: "+minDOM);
        return minDOM;
    }

    @Override
    public void clearDOM() {
        for (FuzzyTerm t : terms) {
            t.clearDOM();
        }
    }

    @Override
    public void orWithDOM(double val) {
        for (FuzzyTerm t : terms) {
            t.orWithDOM(val);
        }
    }
}
