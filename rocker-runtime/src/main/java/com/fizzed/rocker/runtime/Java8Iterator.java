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
import com.fizzed.rocker.RenderingException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class Java8Iterator {
    
    static public interface ConsumeCollection<V> {
        void accept(V v) throws RenderingException, IOException;
    }
    
    static public interface ConsumeCollectionWithIterator<V> {
        void accept(ForIterator i, V v) throws RenderingException, IOException;
    }
    
    static public interface ConsumeMap<K,V> {
        void accept(K k, V v) throws RenderingException, IOException;
    }
    
    static public interface ConsumeMapWithIterator<K,V> {
        void accept(ForIterator i, K k, V v) throws RenderingException, IOException;
    }
    
    static public <V> void forEach(Collection<V> items, ConsumeCollection<V> consumer)  throws RenderingException, IOException {
        for (V item : items) {
            consumer.accept(item);
        }
    }
    
    static public <V> void forEach(Collection<V> items, ConsumeCollectionWithIterator<V> consumer) throws RenderingException, IOException {
        IndexOnlyForIterator it = new IndexOnlyForIterator(items.size());
        for (V item : items) {
            it.increment();
            consumer.accept(it, item);
        }
    }
    
    static public <K,V> void forEach(Map<K,V> items, ConsumeMap<K,V> consumer)  throws RenderingException, IOException {
        for (Map.Entry<K,V> item : items.entrySet()) {
            consumer.accept(item.getKey(), item.getValue());
        }
    }
    
    static public <K,V> void forEach(Map<K,V> items, ConsumeMapWithIterator<K,V> consumer)  throws RenderingException, IOException {
        IndexOnlyForIterator it = new IndexOnlyForIterator(items.size());
        for (Map.Entry<K,V> item : items.entrySet()) {
            it.increment();
            consumer.accept(it, item.getKey(), item.getValue());
        }
    }
    
    // support for primitive arrays w/o iterator
    
    static public <V> void forEach(boolean[] items, ConsumeCollection<Boolean> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.BooleanCollection(items), consumer);
    }
    
    static public <V> void forEach(byte[] items, ConsumeCollection<Byte> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.ByteCollection(items), consumer);
    }
    
    static public <V> void forEach(char[] items, ConsumeCollection<Character> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.CharacterCollection(items), consumer);
    }
    
    static public <V> void forEach(short[] items, ConsumeCollection<Short> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.ShortCollection(items), consumer);
    }
    
    static public <V> void forEach(int[] items, ConsumeCollection<Integer> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.IntegerCollection(items), consumer);
    }
    
    static public <V> void forEach(long[] items, ConsumeCollection<Long> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.LongCollection(items), consumer);
    }
    
    static public <V> void forEach(float[] items, ConsumeCollection<Float> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.FloatCollection(items), consumer);
    }
    
    static public <V> void forEach(double[] items, ConsumeCollection<Double> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.DoubleCollection(items), consumer);
    }
    
    // support for primitive array w/ iterators
    
    static public <V> void forEach(boolean[] items, ConsumeCollectionWithIterator<Boolean> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.BooleanCollection(items), consumer);
    }
    
    static public <V> void forEach(byte[] items, ConsumeCollectionWithIterator<Byte> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.ByteCollection(items), consumer);
    }
    
    static public <V> void forEach(char[] items, ConsumeCollectionWithIterator<Character> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.CharacterCollection(items), consumer);
    }
    
    static public <V> void forEach(short[] items, ConsumeCollectionWithIterator<Short> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.ShortCollection(items), consumer);
    }
    
    static public <V> void forEach(int[] items, ConsumeCollectionWithIterator<Integer> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.IntegerCollection(items), consumer);
    }
    
    static public <V> void forEach(long[] items, ConsumeCollectionWithIterator<Long> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.LongCollection(items), consumer);
    }
    
    static public <V> void forEach(float[] items, ConsumeCollectionWithIterator<Float> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.FloatCollection(items), consumer);
    }
    
    static public <V> void forEach(double[] items, ConsumeCollectionWithIterator<Double> consumer)  throws RenderingException, IOException {
        forEach(new PrimitiveCollections.DoubleCollection(items), consumer);
    }
    
}
