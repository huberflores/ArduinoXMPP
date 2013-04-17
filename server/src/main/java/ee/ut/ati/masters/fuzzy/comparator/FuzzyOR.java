/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.ut.ati.masters.fuzzy.comparator;


import ee.ut.ati.masters.fuzzy.rules.FuzzyTerm;

/**
 * @author Root
 */
public class FuzzyOR implements FuzzyTerm {

	FuzzyTerm[] terms;

	public FuzzyOR(FuzzyTerm... terms) {
		this.terms = terms;
	}

	@Override
	public double getDOM() {
		double maxDOM = 0;
		for (FuzzyTerm t : terms) {
			if (t.getDOM() > maxDOM) {
				maxDOM = t.getDOM();
			}
		}
		return maxDOM;
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
		builder.append("OR[" + getDOM() + "] ( ");
		for (FuzzyTerm t : terms) {
			builder.append(t.toString());
			builder.append(", ");
		}
		builder.replace(builder.length() - 2, builder.length(), " )");
		return builder.toString();
	}
}
