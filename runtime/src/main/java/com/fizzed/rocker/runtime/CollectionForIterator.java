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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.lang3.ArrayUtils;

/**
 * ForIterator implementation that wraps a <code>Collection</code> and actually
 * handles the iteration of elements.
 * 
 * @author joelauer
 */
public class CollectionForIterator<T> implements ForIterator {

    private final Iterator<?> iterator;
    private int index;
    private final int size;

    public CollectionForIterator(Collection<?> c) {
        this(c.iterator(), c.size());
    }
    
    public CollectionForIterator(Object[] a) {
        this(Arrays.asList(a));
    }
    
    public CollectionForIterator(int[] a) {
        this(ArrayUtils.toObject(a));
    }
    
    public CollectionForIterator(Iterator<?> iterator, int size) {
        this.iterator = iterator;
        this.index = -1;
        this.size = size;
    }
    
    public T next() {
        T t = (T)iterator.next();
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
        return index == (size - 1);
    }

    
    
}
