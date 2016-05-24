package de.uniulm.in.ki.mbrenner.fame.util;

import java.util.Iterator;

/**
 * Created by spellmaker on 24.05.2016.
 */
public class ArrayIterator<T> implements Iterator<T> {
	private final T[] array;
	private int position;

	public ArrayIterator(T[] array){
		this.array = array;
		this.position = -1;
	}

	@Override
	public boolean hasNext() {
		if(array == null) return false;
		return position < array.length - 1;
	}

	@Override
	public T next() {
		return array[++position];
	}

}
