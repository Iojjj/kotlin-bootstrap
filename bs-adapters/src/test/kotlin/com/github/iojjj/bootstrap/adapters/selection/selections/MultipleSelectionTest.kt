package com.github.iojjj.bootstrap.adapters.selection.selections

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * JUnit test suite for [MultipleSelection] class.
 */
class MultipleSelectionTest {

    private lateinit var selection: MultipleSelection<Int>

    @Before
    fun setUp() {
        selection = MultipleSelection()
    }

    @Test
    fun getSize() {
        assertEquals(0, selection.size)
        selection.add(1)
        assertEquals(1, selection.size)
        selection.add(2)
        assertEquals(2, selection.size)
        selection.add(3)
        assertEquals(3, selection.size)
        selection.remove(2)
        assertEquals(2, selection.size)
        selection.clear()
        assertEquals(0, selection.size)
    }

    @Test
    fun isEmpty() {
        assertTrue(selection.isEmpty())
        selection.add(1)
        assertFalse(selection.isEmpty())
        selection.clear()
        assertTrue(selection.isEmpty())
    }

    @Test
    fun contains() {
        assertFalse(selection.contains(1))
        selection.add(1)
        assertTrue(selection.contains(1))
        assertFalse(selection.contains(2))
        selection.clear()
        assertFalse(selection.contains(1))
    }

    @Test
    fun snapshotEmpty() {
        val snapshot = selection.snapshot()
        // snapshot in the same empty state
        assertTrue(snapshot.isEmpty())
        assertEquals(0, snapshot.size)
        // mutating snapshot doesn't affect selection
        snapshot.add(1)
        assertFalse(snapshot.isEmpty())
        assertEquals(1, snapshot.size)
        assertTrue(selection.isEmpty())
        assertEquals(0, selection.size)

        snapshot.add(2)
        assertFalse(snapshot.isEmpty())
        assertEquals(2, snapshot.size)
        assertTrue(selection.isEmpty())
        assertEquals(0, selection.size)
    }

    @Test
    fun snapshotNonEmpty() {
        selection.add(1)
        selection.add(2)
        selection.add(3)
        val snapshot = selection.snapshot()
        // snapshot in the same empty state
        assertFalse(snapshot.isEmpty())
        assertEquals(3, snapshot.size)
        // mutating snapshot doesn't affect selection
        snapshot.add(4)
        assertEquals(4, snapshot.size)
        assertEquals(3, selection.size)

        snapshot.clear()
        assertTrue(snapshot.isEmpty())
        assertEquals(0, snapshot.size)
        assertFalse(selection.isEmpty())
        assertEquals(3, selection.size)
    }

    @Test(expected = NoSuchElementException::class)
    fun iteratorEmpty() {
        val iterator = selection.iterator()
        assertFalse(iterator.hasNext())
        iterator.next()
    }

    @Test(expected = NoSuchElementException::class)
    fun iteratorNonEmpty() {
        selection.add(1)
        selection.add(2)
        selection.add(3)
        val iterator = selection.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(1, iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(2, iterator.next())
        assertTrue(iterator.hasNext())
        assertEquals(3, iterator.next())
        assertFalse(iterator.hasNext())
        iterator.next()
    }

    @Test
    fun add() {
        assertTrue(selection.add(1))
        assertTrue(selection.add(2))
        assertFalse(selection.add(2))
        assertTrue(selection.add(3))
    }

    @Test
    fun remove() {
        assertFalse(selection.remove(1))
        selection.add(1)
        selection.add(5)
        assertFalse(selection.remove(3))
        assertTrue(selection.remove(5))
        assertTrue(selection.remove(1))
    }

    @Test
    fun clear() {
        selection.add(1)
        assertFalse(selection.isEmpty())
        selection.clear()
        assertTrue(selection.isEmpty())
    }
}