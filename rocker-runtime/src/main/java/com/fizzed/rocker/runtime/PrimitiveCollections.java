/*
 * Copyright 2016 Fizzed, Inc.
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

import java.util.Collection;
import java.util.Iterator;

public class PrimitiveCollections {
    
    static public class ObjectCollection extends AbstractPrimitiveCollection<Object> {
        private final Object[] array;
        
        public ObjectCollection(Object[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Object get(int index) {
            return this.array[index];
        }
    }
    
    static public class BooleanCollection extends AbstractPrimitiveCollection<Boolean> {
        private final boolean[] array;
        
        public BooleanCollection(boolean[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Boolean get(int index) {
            return this.array[index];
        }
    }
    
    static public class ByteCollection extends AbstractPrimitiveCollection<Byte> {
        private final byte[] array;
        
        public ByteCollection(byte[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Byte get(int index) {
            return this.array[index];
        }
    }
    
    static public class CharacterCollection extends AbstractPrimitiveCollection<Character> {
        private final char[] array;
        
        public CharacterCollection(char[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Character get(int index) {
            return this.array[index];
        }
    }
    
    static public class ShortCollection extends AbstractPrimitiveCollection<Short> {
        private final short[] array;
        
        public ShortCollection(short[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Short get(int index) {
            return this.array[index];
        }
    }
    
    static public class IntegerCollection extends AbstractPrimitiveCollection<Integer> {
        private final int[] array;
        
        public IntegerCollection(int[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Integer get(int index) {
            return this.array[index];
        }
    }
    
    static public class LongCollection extends AbstractPrimitiveCollection<Long> {
        private final long[] array;
        
        public LongCollection(long[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Long get(int index) {
            return this.array[index];
        }
    }
    
    static public class FloatCollection extends AbstractPrimitiveCollection<Float> {
        private final float[] array;
        
        public FloatCollection(float[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Float get(int index) {
            return this.array[index];
        }
    }
    
    static public class DoubleCollection extends AbstractPrimitiveCollection<Double> {
        private final double[] array;
        
        public DoubleCollection(double[] array) {
            super(array.length);
            this.array = array;
        }
        
        @Override
        public Double get(int index) {
            return this.array[index];
        }
    }
    
    static public abstract class AbstractPrimitiveCollection<T> implements Collection<T> {
        
        private final int size;
        
        public AbstractPrimitiveCollection(int size) {
            this.size = size;
        }
        
        @Override
        public int size() {
            return this.size;
        }

        @Override
        public boolean isEmpty() {
            return this.size <= 0;
        }
        
        abstract public T get(int index);
        
        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int index = 0;
                
                @Override
                public boolean hasNext() {
                    return index < size;
                }

                @Override
                public T next() {
                    T t = get(index);
                    index++;
                    return t;
                }

                @Override
                public void remove() {
                    // do nothing
                }
            };
        }
        
        // we don't need any of these for iterating over a collection
        
        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean add(T e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
}
