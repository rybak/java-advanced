package ru.ifmo.ctddev.rybak.collections;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class Bag<T> extends AbstractCollection<T> {

	protected int size;

	private final Map<T, List<T>> multiSet;

	public Bag() {
		multiSet = new HashMap<T, List<T>>();
	}

	public Bag(final Collection<? extends T> collection) {
		this();
		addAll(collection);
	}

	@Override
	public boolean remove(final Object o) {
		if (!multiSet.containsKey(o)) {
			return false;
		}
		final List<T> list = multiSet.get(o);
		list.remove(o);
		if (list.size() == 0) {
			multiSet.remove(o);
		}
		size--;
		return true;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean isChanged = false;
		for (final Object o : c) {
			isChanged |= remove(o);
		}
		return isChanged;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean add(final T element) {
		if (!multiSet.containsKey(element)) {
			multiSet.put(element, new ArrayList<T>());
		}
		multiSet.get(element).add(element);
		size++;
		return true;
	}

	@Override
	public boolean contains(final Object o) {
		return multiSet.containsKey(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new BagIterator();
	}

	private class BagIterator implements Iterator<T> {

		Iterator<Map.Entry<T, List<T>>> multiSetIterator;
		Iterator<T> currEntryIterator;
		Map.Entry<T, List<T>> currEntry;

		public BagIterator() {
			multiSetIterator = multiSet.entrySet().iterator();
		}

		public boolean hasNext() {
			if (currEntryIterator == null || !currEntryIterator.hasNext()) {
				return multiSetIterator.hasNext();
			}
			return true;
		}

		public T next() {
			if (currEntryIterator == null || !currEntryIterator.hasNext()) {
				currEntry = multiSetIterator.next();
				currEntryIterator = currEntry.getValue().iterator();
			}
			return currEntryIterator.next();
		}

		public void remove() {
			currEntryIterator.remove();
			if (currEntry.getValue().isEmpty()) {
				multiSetIterator.remove();
			}
			size--;
		}
	}

}