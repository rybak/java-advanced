package ru.ifmo.ctddev.rybak.collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Artem Popov (jambo@yandex-team.ru) date 26.02.12
 */
public class LinkedBagTest extends Assert {
	LinkedBag<Integer> bag;

	@Before
	public void setUp() throws Exception {
		bag = new LinkedBag<Integer>();
	}

	@Test
	public void test1() throws Exception {
		bag.add(2);
		bag.add(3);
		bag.add(4);
		assertTrue(bag.contains(2));
		assertTrue(bag.contains(3));
		assertTrue(bag.contains(4));
		assertFalse(bag.contains(5));
	}

	@Test
	public void clearTest() throws Exception {
		bag.add(1);
		bag.add(1);
		bag.clear();
		assertEquals(bag.size(), 0);
		bag.clear();
		assertEquals(bag.size(), 0);
		bag.add(1);
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);
		bag.removeAll(l);
		assertEquals(bag.size(), 0);
		assertFalse(bag.contains(1));
	}

	@Test
	public void test2() throws Exception {
		bag.add(2);
		bag.add(2);
		bag.add(2);
		assertTrue(bag.contains(2));
		assertFalse(bag.contains(3));
		assertFalse(bag.contains(4));
		assertFalse(bag.contains(5));

		assertFalse(bag.remove(3));
		assertFalse(bag.remove(4));
		assertFalse(bag.remove(5));

		assertTrue(bag.remove(2));
		assertTrue(bag.remove(2));
		assertTrue(bag.remove(2));
		assertFalse(bag.remove(2));
	}

	@Test
	public void remove() throws Exception {
		bag.add(2);
		bag.add(2);
		bag.add(2);
		assertTrue(bag.contains(2));
		bag.remove(2);
		assertTrue(bag.contains(2));
		bag.remove(2);
		assertTrue(bag.contains(2));
		bag.remove(2);
		assertFalse(bag.contains(2));
	}

	@Test
	public void iterate() throws Exception {
		final int SIZE = 100;
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				bag.add(j);
				assertEquals(i * SIZE + j + 1, bag.size());
			}
		}
		int i = 0;
		for (final Integer number : bag) {
			assertEquals((int) (i % SIZE), (int) number);
			i++;
		}

	}

	@Test(expected = ConcurrentModificationException.class)
	public void twoIterators() {
		// try {
		// Set<Integer> set = new HashSet<Integer>();
		// set.add(1);
		// Iterator<Integer> it = set.iterator();
		// Iterator<Integer> it2 = set.iterator();
		// it.next();
		// it2.next();
		// it.remove();
		// it2.remove();
		// } catch (Exception e) {
		// System.err.println(e);
		// }
		bag.add(1);
		Iterator<Integer> it = bag.iterator();
		Iterator<Integer> it2 = bag.iterator();
		it.next();
		it2.next();
		it.remove();
		it2.remove();

	}

	private class Point {
		int x, y;

		private Point(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final Point point = (Point) o;

			if (x != point.x)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			return x;
		}
	}

	@Test
	public void complicatedType() {
		final LinkedBag<Point> bagPoint = new LinkedBag<Point>();
		bagPoint.add(new Point(1, 0));
		bagPoint.add(new Point(1, 1));
		bagPoint.add(new Point(1, 2));

		final int[] a = new int[3];
		final Iterator<Point> iterator = bagPoint.iterator();
		while (iterator.hasNext()) {
			assertTrue(bagPoint.contains(new Point(1, 42)));
			a[iterator.next().y]++;
			iterator.remove();
		}
		assertFalse(bagPoint.contains(new Point(1, 42)));
		Assert.assertArrayEquals(new int[] { 1, 1, 1 }, a);
	}

	@Test
	public void collectionConstructor() {
		final LinkedBag<Integer> bag1 = new LinkedBag<Integer>(Arrays.asList(2,
				3, 4));
		assertTrue(bag1.contains(2));
		assertTrue(bag1.contains(3));
		assertTrue(bag1.contains(4));
		assertFalse(bag1.contains(5));
	}

	@Test(expected = NoSuchElementException.class)
	public void testNextException() throws Exception {
		bag.add(1);
		final Iterator<Integer> iterator = bag.iterator();
		iterator.next();
		iterator.next();
	}

	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentModificationException() throws Exception {
		bag.add(1);
		bag.add(2);
		bag.add(3);
		for (final Integer next : bag) {
			bag.remove(1);
			next.byteValue();
		}
	}

	@Test
	public void testIteratorRemove() throws Exception {
		bag.add(1);
		bag.add(2);
		bag.add(3);
		assertTrue(bag.contains(1));
		assertTrue(bag.contains(2));
		assertTrue(bag.contains(3));
		for (Iterator<Integer> iterator = bag.iterator(); iterator.hasNext();) {
			final Integer next = iterator.next();
			next.byteValue();
			iterator.remove();
		}
		assertFalse(bag.contains(1));
		assertFalse(bag.contains(2));
		assertFalse(bag.contains(3));
	}

	@Test
	public void testBadRemove() throws Exception {
		assertFalse(bag.remove(0));
	}
}