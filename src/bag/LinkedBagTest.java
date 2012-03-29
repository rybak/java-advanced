package bag;

import org.junit.Test;
import ru.ifmo.ctddev.rybak.collections.LinkedBag;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

public class LinkedBagTest extends BagTest {

    {
        classForTest = LinkedBag.class;
    }

    @Test
    public void iterate() throws Exception {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                bag.add(j);
                assertEquals(i * 100 + j + 1, bag.size());
            }
        }
        int i = 0;
        for (final Integer number : bag) {
            assertEquals(i % 100, (int) number);
            i++;
        }
    }

    private class Point {
        int x, y;

        private Point(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Point point = (Point) o;

            return x == point.x;

        }

        @Override
        public int hashCode() {
            return x;
        }
    }

    @Test
    public void complicatedTypeLinkedBag() {
        final Collection<Point> bagPoint = newInstance();
        bagPoint.add(new Point(1, 0));
        bagPoint.add(new Point(1, 1));
        bagPoint.add(new Point(1, 2));

        final int[] a = new int[3];
        final Iterator<Point> iterator = bagPoint.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            assertTrue(bagPoint.contains(new Point(1, 42)));
            int y = iterator.next().y;
            assertEquals(i++, y);
            a[y]++;
            iterator.remove();
        }
        assertFalse(bagPoint.contains(new Point(1, 42)));
        assertArrayEquals(new int[]{1, 1, 1}, a);
    }

}