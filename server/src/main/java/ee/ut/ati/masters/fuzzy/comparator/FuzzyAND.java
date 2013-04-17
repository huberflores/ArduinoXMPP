/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.ut.ati.masters.fuzzy.comparator;


import ee.ut.ati.masters.fuzzy.rules.FuzzyTerm;

/**
 * @author Root
 */
public class FuzzyAND implements FuzzyTerm {

	FuzzyTerm[] terms;

	public FuzzyAND(FuzzyTerm... termList) {
		this.terms = termList;
	}

	@Override
	public double getDOM() {
		if (terms.length == 0) {
			return 0;
		}
		double minDOM = 1;
		for (FuzzyTerm t : terms) {
			if (t.getDOM() < minDOM) {
				minDOM = t.getDOM();
			}
		}
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AND[" + getDOM() + "] ( ");
		for (FuzzyTerm t : terms) {
			builder.append(t.toString());
			builder.append(", ");
		}
		builder.replace(builder.length() - 2, builder.length(), " )");
		return builder.toString();
	}
}
