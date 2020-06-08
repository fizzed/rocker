/*
 * Copyright 2015 Fizzed Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.rocker.runtime;

import com.fizzed.rocker.ForIterator;

import java.util.Iterator;

/**
 * ForIterator implementation that wraps an <code>Iterable</code> and actually
 * handles the iteration of elements.
 */
public class IterableForIterator<T> implements ForIterator {

    private final Iterator<? extends T> iterator;
    private int index;

    public IterableForIterator(Iterable<? extends T> c) {
        this(c.iterator());
    }

    public IterableForIterator(T[] a) {
        this((Iterable<T>) new PrimitiveCollections.ObjectCollection<T>(a));
    }

    public IterableForIterator(boolean[] a) {
        this((Iterable<T>) new PrimitiveCollections.BooleanCollection(a));
    }

    public IterableForIterator(byte[] a) {
        this((Iterable<T>) new PrimitiveCollections.ByteCollection(a));
    }

    public IterableForIterator(char[] a) {
        this((Iterable<T>) new PrimitiveCollections.CharacterCollection(a));
    }

    public IterableForIterator(short[] a) {
        this((Iterable<T>) new PrimitiveCollections.ShortCollection(a));
    }

    public IterableForIterator(int[] a) {
        this((Iterable<T>) new PrimitiveCollections.IntegerCollection(a));
    }

    public IterableForIterator(long[] a) {
        this((Iterable<T>) new PrimitiveCollections.LongCollection(a));
    }

    public IterableForIterator(float[] a) {
        this((Iterable<T>) new PrimitiveCollections.FloatCollection(a));
    }

    public IterableForIterator(double[] a) {
        this((Iterable<T>) new PrimitiveCollections.DoubleCollection(a));
    }

    public IterableForIterator(Iterator<T> iterator) {
        this.iterator = iterator;
        this.index = -1;
    }

    public T next() {
        T t = iterator.next();
        this.index++;
        return t;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public boolean first() {
        return index == 0;
    }

    @Override
    public boolean last() {
        return !iterator.hasNext();
    }

}
