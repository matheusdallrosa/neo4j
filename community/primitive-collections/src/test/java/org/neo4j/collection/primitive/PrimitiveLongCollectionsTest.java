/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.collection.primitive;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.collection.primitive.PrimitiveLongCollections.PrimitiveLongBaseIterator;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PrimitiveLongCollectionsTest
{
    @Test
    public void arrayOfItemsAsIterator()
    {
        // GIVEN
        long[] items = new long[] { 2, 5, 234 };

        // WHEN
        PrimitiveLongIterator iterator = PrimitiveLongCollections.iterator( items );

        // THEN
        assertItems( iterator, items );
    }

    @Test
    public void arrayOfReversedItemsAsIterator()
    {
        // GIVEN
        long[] items = new long[] { 2, 5, 234 };

        // WHEN
        PrimitiveLongIterator iterator = PrimitiveLongCollections.reversed( items );

        // THEN
        assertItems( iterator, reverse( items ) );
    }

    @Test
    public void concatenateTwoIterators()
    {
        // GIVEN
        PrimitiveLongIterator firstItems = PrimitiveLongCollections.iterator( 10, 3, 203, 32 );
        PrimitiveLongIterator otherItems = PrimitiveLongCollections.iterator( 1, 2, 5 );

        // WHEN
        PrimitiveLongIterator iterator = PrimitiveLongCollections.concat( asList( firstItems, otherItems ).iterator() );

        // THEN
        assertItems( iterator, 10, 3, 203, 32, 1, 2, 5 );
    }

    @Test
    public void prependItem()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 10, 23 );
        long prepended = 5;

        // WHEN
        PrimitiveLongIterator iterator = PrimitiveLongCollections.prepend( prepended, items );

        // THEN
        assertItems( iterator, prepended, 10, 23 );
    }

    @Test
    public void appendItem()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2 );
        long appended = 3;

        // WHEN
        PrimitiveLongIterator iterator = PrimitiveLongCollections.append( items, appended );

        // THEN
        assertItems( iterator, 1, 2, appended );
    }

    @Test
    public void filter()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        PrimitiveLongIterator filtered = PrimitiveLongCollections.filter( items, item -> item != 2 );

        // THEN
        assertItems( filtered, 1, 3 );
    }

    @Test
    public void deduplicate()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 1, 2, 3, 2 );

        // WHEN
        PrimitiveLongIterator deduped = PrimitiveLongCollections.deduplicate( items );

        // THEN
        assertItems( deduped, 1, 2, 3 );
    }

    @Test
    public void limit()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        PrimitiveLongIterator limited = PrimitiveLongCollections.limit( items, 2 );

        // THEN
        assertItems( limited, 1, 2 );
    }

    @Test
    public void skip()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3, 4 );

        // WHEN
        PrimitiveLongIterator skipped = PrimitiveLongCollections.skip( items, 2 );

        // THEN
        assertItems( skipped, 3, 4 );
    }

    // TODO paging iterator

    @Test
    public void range()
    {
        // WHEN
        PrimitiveLongIterator range = PrimitiveLongCollections.range( 5, 15, 3 );

        // THEN
        assertItems( range, 5, 8, 11, 14 );
    }

    @Test
    public void singleton()
    {
        // GIVEN
        long item = 15;

        // WHEN
        PrimitiveLongIterator singleton = PrimitiveLongCollections.singleton( item );

        // THEN
        assertItems( singleton, item );
    }

    @Test
    public void reversed()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        PrimitiveLongIterator reversed = PrimitiveLongCollections.reversed( items );

        // THEN
        assertItems( reversed, 3, 2, 1 );
    }

    @Test
    public void first()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2 );

        // WHEN
        try
        {
            PrimitiveLongCollections.first(  PrimitiveLongCollections.emptyIterator() );
            fail( "Should throw exception" );
        }
        catch ( NoSuchElementException e )
        {   // Good
        }
        long first = PrimitiveLongCollections.first( items );

        // THEN
        assertEquals( 1, first );
    }

    @Test
    public void firstWithDefault()
    {
        // GIVEN
        long defaultValue = 5;

        // WHEN
        long firstOnEmpty = PrimitiveLongCollections.first( PrimitiveLongCollections.emptyIterator(), defaultValue );
        long first = PrimitiveLongCollections.first( PrimitiveLongCollections.iterator( 1, 2 ), defaultValue );

        // THEN
        assertEquals( defaultValue, firstOnEmpty );
        assertEquals( 1, first );
    }

    @Test
    public void last()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2 );

        // WHEN
        try
        {
            PrimitiveLongCollections.last( PrimitiveLongCollections.emptyIterator() );
            fail( "Should throw exception" );
        }
        catch ( NoSuchElementException e )
        {   // Good
        }
        long last = PrimitiveLongCollections.last( items );

        // THEN
        assertEquals( 2, last );
    }

    @Test
    public void lastWithDefault()
    {
        // GIVEN
        long defaultValue = 5;

        // WHEN
        long lastOnEmpty = PrimitiveLongCollections.last( PrimitiveLongCollections.emptyIterator(), defaultValue );
        long last = PrimitiveLongCollections.last( PrimitiveLongCollections.iterator( 1, 2 ), defaultValue );

        // THEN
        assertEquals( defaultValue, lastOnEmpty );
        assertEquals( 2, last );
    }

    @Test
    public void single()
    {
        try
        {
            PrimitiveLongCollections.single( PrimitiveLongCollections.emptyIterator() );
            fail();
        }
        catch ( NoSuchElementException e )
        {
            assertThat( e.getMessage(), containsString( "No" ) );
        }

        assertEquals( 3, PrimitiveLongCollections.single( PrimitiveLongCollections.iterator( 3 ) ) );

        try
        {
            PrimitiveLongCollections.single( PrimitiveLongCollections.iterator( 1, 2 ) );
            fail( "Should throw exception" );
        }
        catch ( NoSuchElementException e )
        {
            assertThat( e.getMessage(), containsString( "More than one" ) );
        }
    }

    @Test
    public void singleWithDefault()
    {
        assertEquals( 5, PrimitiveLongCollections.single( PrimitiveLongCollections.emptyIterator(), 5 ) );
        assertEquals( 3, PrimitiveLongCollections.single( PrimitiveLongCollections.iterator( 3 ) ) );
        try
        {
            PrimitiveLongCollections.single( PrimitiveLongCollections.iterator( 1, 2 ) );
            fail( "Should throw exception" );
        }
        catch ( NoSuchElementException e )
        {   // Good
            assertThat( e.getMessage(), containsString( "More than one" ) );
        }
    }

    private static final class CountingPrimitiveLongIteratorResource implements PrimitiveLongIterator, AutoCloseable
    {
        private final PrimitiveLongIterator delegate;
        private final AtomicInteger closeCounter;

        private CountingPrimitiveLongIteratorResource( PrimitiveLongIterator delegate, AtomicInteger closeCounter )
        {
            this.delegate = delegate;
            this.closeCounter = closeCounter;
        }

        @Override
        public void close()
        {
            closeCounter.incrementAndGet();
        }

        @Override
        public boolean hasNext()
        {
            return delegate.hasNext();
        }

        @Override
        public long next()
        {
            return delegate.next();
        }
    }

    @Test
    public void singleMustAutoCloseIterator()
    {
        AtomicInteger counter = new AtomicInteger();
        CountingPrimitiveLongIteratorResource itr = new CountingPrimitiveLongIteratorResource(
                PrimitiveLongCollections.iterator( 13 ), counter );
        assertEquals( PrimitiveLongCollections.single( itr ), 13 );
        assertEquals( 1, counter.get() );
    }

    @Test
    public void singleWithDefaultMustAutoCloseIterator()
    {
        AtomicInteger counter = new AtomicInteger();
        CountingPrimitiveLongIteratorResource itr = new CountingPrimitiveLongIteratorResource(
                PrimitiveLongCollections.iterator( 13 ), counter );
        assertEquals( PrimitiveLongCollections.single( itr, 2 ), 13 );
        assertEquals( 1, counter.get() );
    }

    @Test
    public void singleMustAutoCloseEmptyIterator()
    {
        AtomicInteger counter = new AtomicInteger();
        CountingPrimitiveLongIteratorResource itr = new CountingPrimitiveLongIteratorResource(
                PrimitiveLongCollections.emptyIterator(), counter );
        try
        {
            PrimitiveLongCollections.single( itr );
            fail( "single() on empty iterator should have thrown" );
        }
        catch ( NoSuchElementException ignore )
        {
        }
        assertEquals( 1, counter.get() );
    }

    @Test
    public void singleWithDefaultMustAutoCloseEmptyIterator()
    {
        AtomicInteger counter = new AtomicInteger();
        CountingPrimitiveLongIteratorResource itr = new CountingPrimitiveLongIteratorResource(
                PrimitiveLongCollections.emptyIterator(), counter );
        assertEquals( PrimitiveLongCollections.single( itr, 2 ), 2 );
        assertEquals( 1, counter.get() );
    }

    @Test
    public void itemAt()
    {
        // GIVEN
        PrimitiveLongIterable items = () -> PrimitiveLongCollections.iterator( 10, 20, 30 );

        // THEN
        try
        {
            PrimitiveLongCollections.itemAt( items.iterator(), 3 );
            fail( "Should throw exception" );
        }
        catch ( NoSuchElementException e )
        {
            assertThat( e.getMessage(), containsString( "No element" ) );
        }
        try
        {
            PrimitiveLongCollections.itemAt( items.iterator(), -4 );
            fail( "Should throw exception" );
        }
        catch ( NoSuchElementException e )
        {
            assertThat( e.getMessage(), containsString( "not found" ) );
        }
        assertEquals( 10, PrimitiveLongCollections.itemAt( items.iterator(), 0 ) );
        assertEquals( 20, PrimitiveLongCollections.itemAt( items.iterator(), 1 ) );
        assertEquals( 30, PrimitiveLongCollections.itemAt( items.iterator(), 2 ) );
        assertEquals( 30, PrimitiveLongCollections.itemAt( items.iterator(), -1 ) );
        assertEquals( 20, PrimitiveLongCollections.itemAt( items.iterator(), -2 ) );
        assertEquals( 10, PrimitiveLongCollections.itemAt( items.iterator(), -3 ) );
    }

    @Test
    public void itemAtWithDefault()
    {
        // GIVEN
        PrimitiveLongIterable items = () -> PrimitiveLongCollections.iterator( 10, 20, 30 );
        long defaultValue = 55;

        // THEN
        assertEquals( defaultValue, PrimitiveLongCollections.itemAt( items.iterator(), 3, defaultValue ) );
        assertEquals( defaultValue, PrimitiveLongCollections.itemAt( items.iterator(), -4, defaultValue ) );
        assertEquals( 10, PrimitiveLongCollections.itemAt( items.iterator(), 0 ) );
        assertEquals( 20, PrimitiveLongCollections.itemAt( items.iterator(), 1 ) );
        assertEquals( 30, PrimitiveLongCollections.itemAt( items.iterator(), 2 ) );
        assertEquals( 30, PrimitiveLongCollections.itemAt( items.iterator(), -1 ) );
        assertEquals( 20, PrimitiveLongCollections.itemAt( items.iterator(), -2 ) );
        assertEquals( 10, PrimitiveLongCollections.itemAt( items.iterator(), -3 ) );
    }

    @Test
    public void indexOf()
    {
        // GIVEN
        PrimitiveLongIterable items = () -> PrimitiveLongCollections.iterator( 10, 20, 30 );

        // THEN
        assertEquals( -1, PrimitiveLongCollections.indexOf( items.iterator(), 55 ) );
        assertEquals( 0, PrimitiveLongCollections.indexOf( items.iterator(), 10 ) );
        assertEquals( 1, PrimitiveLongCollections.indexOf( items.iterator(), 20 ) );
        assertEquals( 2, PrimitiveLongCollections.indexOf( items.iterator(), 30 ) );
    }

    @Test
    public void iteratorsEqual()
    {
        // GIVEN
        PrimitiveLongIterable items1 = () -> PrimitiveLongCollections.iterator( 1, 2, 3 );
        PrimitiveLongIterable items2 = () -> PrimitiveLongCollections.iterator( 1, 20, 3 );
        PrimitiveLongIterable items3 = () -> PrimitiveLongCollections.iterator( 1, 2, 3, 4 );
        PrimitiveLongIterable items4 = () -> PrimitiveLongCollections.iterator( 1, 2, 3 );

        // THEN
        assertFalse( PrimitiveLongCollections.equals( items1.iterator(), items2.iterator() ) );
        assertFalse( PrimitiveLongCollections.equals( items1.iterator(), items3.iterator() ) );
        assertTrue( PrimitiveLongCollections.equals( items1.iterator(), items4.iterator() ) );
    }

    @Test
    public void iteratorAsSet()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        PrimitiveLongSet set = PrimitiveLongCollections.asSet( items );

        // THEN
        assertTrue( set.contains( 1 ) );
        assertTrue( set.contains( 2 ) );
        assertTrue( set.contains( 3 ) );
        assertFalse( set.contains( 4 ) );
    }

    @Test
    public void count()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        int count = PrimitiveLongCollections.count( items );

        // THEN
        assertEquals( 3, count );
    }

    @Test
    public void asArray()
    {
        // GIVEN
        PrimitiveLongIterator items = PrimitiveLongCollections.iterator( 1, 2, 3 );

        // WHEN
        long[] array = PrimitiveLongCollections.asArray( items );

        // THEN
        assertTrue( Arrays.equals( new long[] { 1, 2, 3 }, array ) );
    }

    @Test
    public void shouldDeduplicate()
    {
        // GIVEN
        long[] array = new long[] {1L, 1L, 2L, 5L, 6L, 6L};

        // WHEN
        long[] deduped = PrimitiveLongCollections.deduplicate( array );

        // THEN
        assertArrayEquals( new long[] {1L, 2L, 5L, 6L}, deduped );
    }

    @Test
    public void shouldNotContinueToCallNextOnHasNextFalse()
    {
        // GIVEN
        AtomicLong count = new AtomicLong( 2 );
        PrimitiveLongIterator iterator = new PrimitiveLongBaseIterator()
        {
            @Override
            protected boolean fetchNext()
            {
                return count.decrementAndGet() >= 0 && next( count.get() );
            }
        };

        // WHEN/THEN
        assertTrue( iterator.hasNext() );
        assertTrue( iterator.hasNext() );
        assertEquals( 1L, iterator.next() );
        assertTrue( iterator.hasNext() );
        assertTrue( iterator.hasNext() );
        assertEquals( 0L, iterator.next() );
        assertFalse( iterator.hasNext() );
        assertFalse( iterator.hasNext() );
        assertEquals( -1L, count.get() );
    }

    @Test
    public void copyPrimitiveSet()
    {
        PrimitiveLongSet longSet = PrimitiveLongCollections.setOf( 1L, 3L, 5L );
        PrimitiveLongSet copySet = PrimitiveLongCollections.asSet( longSet );
        assertNotSame( copySet, longSet );

        assertTrue( copySet.contains( 1L ) );
        assertTrue( copySet.contains( 3L ) );
        assertTrue( copySet.contains( 5L ) );
        assertEquals( 3, copySet.size() );
    }

    @Test
    public void convertJavaCollectionToSetOfPrimitives()
    {
        List<Long> longs = asList( 1L, 4L, 7L );
        PrimitiveLongSet longSet = PrimitiveLongCollections.asSet( longs );
        assertTrue( longSet.contains( 1L ) );
        assertTrue( longSet.contains( 4L ) );
        assertTrue( longSet.contains( 7L ) );
        assertEquals( 3, longSet.size() );
    }

    @Test
    public void convertPrimitiveSetToJavaSet()
    {
        PrimitiveLongSet longSet = PrimitiveLongCollections.setOf( 1L, 3L, 5L );
        Set<Long> longs = PrimitiveLongCollections.toSet( longSet );
        assertThat( longs, containsInAnyOrder(1L, 3L, 5L) );
    }

    @Test
    public void copyMap()
    {
        PrimitiveLongObjectMap<Object> originalMap = Primitive.longObjectMap();
        originalMap.put( 1L, "a" );
        originalMap.put( 2L, "b" );
        originalMap.put( 3L, "c" );
        PrimitiveLongObjectMap<Object> copyMap = PrimitiveLongCollections.copy( originalMap );
        assertNotSame( originalMap, copyMap );
        assertEquals( 3, copyMap.size() );
        assertEquals( "a", copyMap.get( 1L ) );
        assertEquals( "b", copyMap.get( 2L ) );
        assertEquals( "c", copyMap.get( 3L ) );
    }

    private void assertNoMoreItems( PrimitiveLongIterator iterator )
    {
        assertFalse( iterator + " should have no more items", iterator.hasNext() );
        try
        {
            iterator.next();
            fail( "Invoking next() on " + iterator +
                    " which has no items left should have thrown NoSuchElementException" );
        }
        catch ( NoSuchElementException e )
        {   // Good
        }
    }

    private void assertNextEquals( long expected, PrimitiveLongIterator iterator )
    {
        assertTrue( iterator + " should have had more items", iterator.hasNext() );
        assertEquals( expected, iterator.next() );
    }

    private void assertItems( PrimitiveLongIterator iterator, long... expectedItems )
    {
        for ( long expectedItem : expectedItems )
        {
            assertNextEquals( expectedItem, iterator );
        }
        assertNoMoreItems( iterator );
    }

    private long[] reverse( long[] items )
    {
        long[] result = new long[items.length];
        for ( int i = 0; i < items.length; i++ )
        {
            result[i] = items[items.length - i - 1];
        }
        return result;
    }
}
