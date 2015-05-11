package org.dllearner.algorithms.tdts.utils;
/**
 * A class for represent a sixth-pla
 * @author Utente
 *
 * @param <S>
 * @param <T>
 * @param <U>
 * @param <W>
 * @param <V>
 * @param <Z>
 */
public class Npla<S, T, U, W, V, Z> {
	S first;
	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}

	public U getThird() {
		return third;
	}

	public W getFourth() {
		return fourth;
	}

	public V getFifth() {
		return fifth;
	}

	public Z getSixth() {
		return sixth;
	}

	T second;
	U third;
	W fourth;
	V fifth;
	Z sixth;

	public Npla(S f, T s, U t,W ff, V fff, Z sx) {
		first=f;
		second=s;
		third= t;
		fourth=ff;
		fifth=fff;
		sixth=sx;
	}

}
