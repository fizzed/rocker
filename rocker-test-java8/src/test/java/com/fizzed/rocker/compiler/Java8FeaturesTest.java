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
package com.fizzed.rocker.compiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class Java8FeaturesTest {
    static private final Logger log = LoggerFactory.getLogger(Java8FeaturesTest.class);
    
    @Test
    public void java8ContentClosure() throws Exception {
        String html = new rocker.Java8ContentClosureA()
            .render()
            .toString();

        // newlines don't really matter - just verify what it did
        Assert.assertEquals("i am a block of content\n\n\ni am another block of content", html.trim());
    }
    
    @Test
    public void java8ValueClosure() throws Exception {
        String html = new rocker.Java8ValueClosureA()
            .s("a")
            .i(1)
            .render()
            .toString();

        // newlines don't really matter - just verify what it did
        Assert.assertEquals("a\ninside-a-closure\n1", html.trim().replace(" ", ""));
    }
    
    @Test
    public void forBlockEnhancedUntypedArray() throws Exception {
        String html = new rocker.ForBlockEnhancedUntypedArray()
            .booleans(new boolean[] { false })
            .chars(new char[] { 'a' })
            .bytes(new byte[] { (byte)0x40 })
            .shorts(new short[] { (short)1 })
            .ints(new int[] { 2 })
            .longs(new long[] { 3L })
            .floats(new float[] { 1.1f })
            .doubles(new double[] { 2.2d })
            .strings(new String[] { "hello" })
            .objects(new Object[] { new Object() { @Override public String toString() { return "obj"; } } })
            .render()
            .toString()
            .trim();
        
        assertThat(html, is("false\na\n64\n1\n2\n3\n1.1\n2.2\nhello\nobj"));
    }
    
    @Test
    public void forBlockEnhancedUntypedCollection() throws Exception {
        
        String html = new rocker.ForBlockEnhancedUntypedCollection()
            .items(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("abc", html);
        
    }
    
    @Test
    public void forBlockEnhancedUntypedCollectionWithIterator() throws Exception {
        
        String html = new rocker.ForBlockEnhancedUntypedCollectionWithIterator()
            .items(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("first 0:a 1:b 2:c last", html);
        
    }
    
    @Test
    public void forBlockEnhancedUntypedMap() throws Exception {
        
        Map<Integer,String> items = new HashMap<>();
        items.put(1, "a");
        items.put(2, "b");
        items.put(3, "c");
        
        String html = new rocker.ForBlockEnhancedUntypedMap()
            .items(items)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("1: a, 2: b, 3: c,", html);
        
    }
    
    @Test
    public void forBlockEnhancedUntypedMapWithIterator() throws Exception {
        
        Map<Integer,String> items = new HashMap<>();
        items.put(1, "a");
        items.put(2, "b");
        items.put(3, "c");
        
        String html = new rocker.ForBlockEnhancedUntypedMapWithIterator()
            .items(items)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("first 0: 1=a, 1: 2=b, 2: 3=c", html);
        
    }
    
    @Test
    public void breakStatement() throws Exception {
        String out = rocker.BreakStatement.template()
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("012", out);
    }
    
    @Test
    public void continueStatement() throws Exception {
        String out = rocker.ContinueStatement.template()
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("013", out);
    }
    
    @Test
    public void withBlock() throws Exception {
        List<String> strings = Arrays.asList("b", "a", "c");
        
        String html = new rocker.WithBlock()
            .strings(strings)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("b", html);
    }
    
    @Test
    public void withBlockUsingLambda() throws Exception {
        List<String> strings = Arrays.asList("a", "c", "b");
        
        String html = new rocker.WithBlockUsingLamda()
            .strings(strings)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("b", html);
    }
    
    @Test
    public void withBlockNested() throws Exception {
        List<String> strings = Arrays.asList("b", "a", "c");
        
        String html = new rocker.WithBlockNested()
            .strings(strings)
            .render()
            .toString()
            .trim();
        
        Assert.assertEquals("b a c", html);
    }
    
    @Test
    public void withBlockNullSafeButWithValue() throws Exception {
        String html = new rocker.WithBlockNullSafe()
            .strings(Arrays.asList("a", "b", "c"))
            .render()
            .toString()
            .trim();
        
        assertThat(html, is("a\n\nskipped"));
    }
    
    @Test
    public void withBlockNullSafeExpressionReturnsNull() throws Exception {
        String html = new rocker.WithBlockNullSafe()
            .strings(Arrays.asList(null, "b", "c"))
            .render()
            .toString()
            .trim();
        
        assertThat(html, is("skipped"));
    }
    
    @Test
    public void withBlockElse() throws Exception {
        String html = new rocker.WithBlockElse()
            .a(Arrays.asList(null, "b", "c"))
            .i(0)
            .render()
            .toString()
            .trim();
        
        assertThat(html, is("in-with-else-block"));
        
        html = new rocker.WithBlockElse()
            .a(Arrays.asList(null, "b", "c"))
            .i(1)
            .render()
            .toString()
            .trim();
        
        assertThat(html, is("b"));
    }
}
