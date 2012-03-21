package ru.ifmo.ctddev.rybak.collections;

import java.util.AbstractCollection;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class LinkedBag<T> extends AbstractCollection<T> {

	private int size;
	private long revision;

	private final Map<T, Map<Integer, Item>> multiSet;
	private final Map<T, Integer> numbers;
	private Item lastItem, firstItem;

	public LinkedBag() {
		multiSet = new HashMap<T, Map<Integer, Item>>();
		numbers = new HashMap<T, Integer>();
	}

	public LinkedBag(final Collection<? extends T> collection) {
		this();
		addAll(collection);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean add(final T element) {
		if (!multiSet.containsKey(element)) {
			multiSet.put(element, new HashMap<Integer, Item>());
			numbers.put(element, Integer.MIN_VALUE);
		}
		int number = numbers.get(element);
		Item newItem = new Item(element, lastItem, number);
		numbers.put(element, number + 1);
		multiSet.get(element).put(number, newItem);
		if (lastItem != null) {
			lastItem.next = newItem;
		}
		if (firstItem == null) {
			firstItem = newItem;
		}
		lastItem = newItem;
		size++;
		revision++;
		return true;
	}

	@Override
	public boolean contains(final Object o) {
		return multiSet.containsKey(o);
	}

	@Override
	public Iterator<T> iterator() {
		return new LinkedBagIterator();
	}

	public boolean remove(final Object obj) {
		if (multiSet.containsKey(obj)) {
			Map<Integer, Item> map = multiSet.get(obj);
			removeImpl(map.get(map.keySet().iterator().next()), map);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean isChanged = false;
		for (final Object o : c) {
			isChanged |= remove(o);
		}
		return isChanged;
	}

	private void removeImpl(Item remItem, Map<Integer, Item> map) {
		if (remItem.prev != null) {
			remItem.prev.next = remItem.next;
		}
		if (remItem.next != null) {
			remItem.next.prev = remItem.prev;
		}
		if (remItem == lastItem) {
			lastItem = remItem.prev;
		}
		if (remItem == firstItem) {
			firstItem = remItem.next;
		}
		map.remove(remItem.number);
		if (map.isEmpty()) {
			multiSet.remove(remItem.data);
			numbers.remove(remItem.data);
		}
		size--;
		revision++;
	}

	private class Item {
		private T data;
		private Item next;
		private Item prev;
		private int number;

		public Item(T data, Item prev, int number) {
			this.data = data;
			this.prev = prev;
			this.number = number;
		}
	}

	private class LinkedBagIterator implements Iterator<T> {

		private Item nextItem;
		private boolean legalState;
		private long goodRevision;

		public LinkedBagIterator() {
			this.nextItem = firstItem;
			this.goodRevision = revision;
		}

		public boolean hasNext() {
			checkRevision();
			return nextItem != null;
		}

		public T next() {
			checkRevision();
			if (nextItem == null) {
				throw new NoSuchElementException();
			}
			T res = nextItem.data;
			nextItem = nextItem.next;
			legalState = true;
			return res;
		}

		public void remove() {
			checkRevision();
			if (!legalState) {
				throw new IllegalStateException();
			}
			Item remItem = nextItem == null ? lastItem : nextItem.prev;
			legalState = false;
			removeImpl(remItem, multiSet.get(remItem.data));
			goodRevision = revision;
		}

		private void checkRevision() {
			if (revision != goodRevision) {
				throw new ConcurrentModificationException();
			}
		}
	}
}