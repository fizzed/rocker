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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class PlainTextUnloadedClassLoaderTest {
    
    static public class PlainText1 {
        private static final String TEXT1 = "hello world!";
        private static final String TEXT2 = "yo?";
    }
    
    static public class PlainText2 {
        // nothing
    }
    
    @Test
    public void load() throws Exception {
        String charsetName = "UTF-8";
        
        PlainTextUnloadedClassLoader loader
                = PlainTextUnloadedClassLoader.load(this.getClass().getClassLoader(), this.getClass().getName() + "$PlainText1", charsetName);
        
        Assert.assertEquals(2, loader.size());
        Assert.assertArrayEquals("hello world!".getBytes(charsetName), loader.get("TEXT1"));
        Assert.assertArrayEquals("yo?".getBytes(charsetName), loader.get("TEXT2"));
        
        
        loader
                = PlainTextUnloadedClassLoader.load(this.getClass().getClassLoader(), this.getClass().getName() + "$PlainText2", charsetName);
        
        Assert.assertEquals(0, loader.size());
        
        // try field that doesn't exist
        try {
            loader.get("FIELD_DOES_NOT_EXIST");
            Assert.fail();
        } catch (NoSuchFieldException e) {
            // expected
        }
        
        
        // try class that doesn't exist
        try {
            PlainTextUnloadedClassLoader.load(this.getClass().getClassLoader(), this.getClass().getName() + "PlainText2", charsetName);
            Assert.fail();
        } catch (ClassNotFoundException e) {
            // expected
        }
        
        
    }
    
}
