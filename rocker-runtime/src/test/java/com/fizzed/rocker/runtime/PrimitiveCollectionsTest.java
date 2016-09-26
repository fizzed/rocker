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

import com.fizzed.rocker.runtime.PrimitiveCollections.BooleanCollection;
import com.fizzed.rocker.runtime.PrimitiveCollections.ByteCollection;
import com.fizzed.rocker.runtime.PrimitiveCollections.CharacterCollection;
import com.fizzed.rocker.runtime.PrimitiveCollections.DoubleCollection;
import com.fizzed.rocker.runtime.PrimitiveCollections.FloatCollection;
import com.fizzed.rocker.runtime.PrimitiveCollections.IntegerCollection;
import com.fizzed.rocker.runtime.PrimitiveCollections.LongCollection;
import com.fizzed.rocker.runtime.PrimitiveCollections.ObjectCollection;
import com.fizzed.rocker.runtime.PrimitiveCollections.ShortCollection;
import java.util.Iterator;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class PrimitiveCollectionsTest {
    
    @Test
    public void booleans() {
        BooleanCollection collection = new BooleanCollection(new boolean[] { false, true });
        
        assertThat(collection.size(), is(2));
        assertThat(collection.get(0), is(false));
        assertThat(collection.get(1), is(true));
        
        Iterator<Boolean> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(false));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(true));
        assertThat(iterator.hasNext(), is(false));
    }
    
    @Test
    public void chars() {
        char[] vs = new char[] { 'a' };
        
        CharacterCollection collection = new CharacterCollection(vs);
        
        assertThat(collection.size(), is(vs.length));
        assertThat(collection.get(0), is(vs[0]));
        
        Iterator<Character> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(vs[0]));
        assertThat(iterator.hasNext(), is(false));
    }
    
    @Test
    public void bytes() {
        byte[] vs = new byte[] { 0x40 };
        
        ByteCollection collection = new ByteCollection(vs);
        
        assertThat(collection.size(), is(vs.length));
        assertThat(collection.get(0), is(vs[0]));
        
        Iterator<Byte> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(vs[0]));
        assertThat(iterator.hasNext(), is(false));
    }
    
    @Test
    public void shorts() {
        short[] vs = new short[] { 1 };
        
        ShortCollection collection = new ShortCollection(vs);
        
        assertThat(collection.size(), is(vs.length));
        assertThat(collection.get(0), is(vs[0]));
        
        Iterator<Short> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(vs[0]));
        assertThat(iterator.hasNext(), is(false));
    }
    
    @Test
    public void ints() {
        int[] vs = new int[] { 1 };
        
        IntegerCollection collection = new IntegerCollection(vs);
        
        assertThat(collection.size(), is(vs.length));
        assertThat(collection.get(0), is(vs[0]));
        
        Iterator<Integer> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(vs[0]));
        assertThat(iterator.hasNext(), is(false));
    }
    
    @Test
    public void longs() {
        long[] vs = new long[] { 1 };
        
        LongCollection collection = new LongCollection(vs);
        
        assertThat(collection.size(), is(vs.length));
        assertThat(collection.get(0), is(vs[0]));
        
        Iterator<Long> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(vs[0]));
        assertThat(iterator.hasNext(), is(false));
    }
    
    @Test
    public void floats() {
        float[] vs = new float[] { 1 };
        
        FloatCollection collection = new FloatCollection(vs);
        
        assertThat(collection.size(), is(vs.length));
        assertThat(collection.get(0), is(vs[0]));
        
        Iterator<Float> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(vs[0]));
        assertThat(iterator.hasNext(), is(false));
    }
    
    @Test
    public void doubles() {
        double[] vs = new double[] { 1 };
        
        DoubleCollection collection = new DoubleCollection(vs);
        
        assertThat(collection.size(), is(vs.length));
        assertThat(collection.get(0), is(vs[0]));
        
        Iterator<Double> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(vs[0]));
        assertThat(iterator.hasNext(), is(false));
    }
    
    @Test
    public void strings() {
        String[] vs = new String[] { "a" };
        
        ObjectCollection collection = new ObjectCollection(vs);
        
        assertThat(collection.size(), is(vs.length));
        assertThat(collection.get(0), is((Object)vs[0]));
        
        Iterator<Object> iterator = collection.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is((Object)vs[0]));
        assertThat(iterator.hasNext(), is(false));
    }
    
}
