package com.github.iojjj.bootstrap.adapters.selection.selections

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * JUnit test suite for [SingleSelection] class.
 */
class SingleSelectionTest {

    private lateinit var selection: SingleSelection<Int>

    @Before
    fun setUp() {
        selection = SingleSelection()
    }

    @Test
    fun getSize() {
        assertEquals(0, selection.size)
        selection.add(1)
        assertEquals(1, selection.size)
        selection.add(5)
        assertEquals(1, selection.size)
        selection.remove(5)
        assertEquals(0, selection.size)
        selection.add(1)
        assertEquals(1, selection.size)
        selection.clear()
        assertEquals(0, selection.size)
    }

    @Test
    fun isEmpty() {
        assertTrue(selection.isEmpty())
        selection.add(1)
        assertFalse(selection.isEmpty())
        selection.remove(1)
        assertTrue(selection.isEmpty())
    }

    @Test
    fun contains() {
        assertFalse(selection.contains(1))
        selection.add(5)
        assertTrue(selection.contains(5))
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
    }

    @Test
    fun snapshotNonEmpty() {
        selection.add(1)
        val snapshot = selection.snapshot()
        // snapshot in the same empty state
        assertFalse(snapshot.isEmpty())
        assertEquals(1, snapshot.size)
        // mutating snapshot doesn't affect selection
        snapshot.add(4)
        assertEquals(4, snapshot.iterator().next())
        assertEquals(1, selection.snapshot().iterator().next())
        snapshot.clear()
        assertTrue(snapshot.isEmpty())
        assertEquals(0, snapshot.size)
        assertFalse(selection.isEmpty())
        assertEquals(1, selection.size)
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
        val iterator = selection.iterator()
        assertTrue(iterator.hasNext())
        assertEquals(1, iterator.next())
        assertFalse(iterator.hasNext())
        iterator.next()
    }

    @Test
    fun add() {
        assertTrue(selection.add(1))
        assertTrue(selection.add(5))
        assertFalse(selection.add(5))
    }

    @Test
    fun remove() {
        assertFalse(selection.remove(5))
        selection.add(5)
        assertFalse(selection.remove(1))
        assertTrue(selection.remove(5))
        assertFalse(selection.remove(5))
    }

    @Test
    fun clear() {
        selection.add(1)
        assertEquals(1, selection.size)
        selection.clear()
        assertEquals(0, selection.size)
    }
}