package org.dllearner.utilities.datastructures;

/**
 * A container which can hold two Strings, mainly used as a helper.
 * Also used as pre form, if you want to create triple, that have the same subject
 * @author Sebastian Hellmann
 */
public class StringTuple implements Comparable<StringTuple>{

	public String a;
	public String b;

	public StringTuple(String a, String b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return "<" + a + "|" + b + ">";
	}

	public boolean equals(StringTuple t) {
		return ((b.equals(t.b)) && (a.equals(t.a)));
	}
	
	public int compareTo(StringTuple t){
		int comp = a.compareTo(t.a);
		if( comp == 0 ){
			return b.compareTo(t.b);
		}else return comp;
	}

}
