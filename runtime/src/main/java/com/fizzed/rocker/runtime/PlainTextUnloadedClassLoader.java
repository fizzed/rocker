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

import com.fizzed.rocker.RenderingException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads a map of field names -> byte arrays from a standard compiled Java class
 * but uses a temporary ClassLoader so that its class may be immediately unloaded
 * from the JVM and it's memory + constant pool can be used for other things.
 * 
 * @author joelauer
 */
public class PlainTextUnloadedClassLoader {
    
    private final String className;
    private final Map<String,byte[]> fields;

    public PlainTextUnloadedClassLoader(String className, Map<String, byte[]> fieldNames) {
        this.className = className;
        this.fields = fieldNames;
    }
    
    public byte[] tryGet(String fieldName) {
        try {
            return get(fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public int size() {
        return fields.size();
    }
    
    public byte[] get(String fieldName) throws NoSuchFieldException {
        byte[] bytes = fields.get(fieldName);
        if (bytes == null) {
            throw new NoSuchFieldException("Class " + className + " does not define a String field of " + fieldName);
        }
        return bytes;
    }
    
    static public PlainTextUnloadedClassLoader tryLoad(String classBinaryName, String charsetName) {
        try {
            return load(classBinaryName, charsetName);
        } catch (Exception e) {
            throw new RenderingException(e.getMessage(), e);
        }
    }
    
    static public PlainTextUnloadedClassLoader load(String classBinaryName, String charsetName)
            throws ClassNotFoundException, MalformedURLException, IllegalArgumentException, UnsupportedEncodingException, IllegalAccessException {
        // find class as though it was any other resource
        // this will search the classpath
        String resourceName = '/' + classBinaryName.replace('.', '/') + ".class";
        URL url = PlainTextUnloadedClassLoader.class.getResource(resourceName);
        if (url == null) {
            throw new ClassNotFoundException("Unable to find class as resource [" + resourceName + "]");
        }

        // chop off path at end to get the base url we will use to load
        // the class via a temporary URLClassLoader below
        String resourcePath = url.toString();
        int pos = resourcePath.lastIndexOf(resourceName);
        if (pos < 0) {
            throw new ClassNotFoundException("Unable to compute resource base for [" + resourceName + "]");
        }

        // we always want to keep trailing '/'
        pos += 1;

        String resourceBasePath = resourcePath.substring(0, pos);
        URL resourceBase = new URL(resourceBasePath);
        
        // create classloader w/ no parent class
        URLClassLoader classLoader = new URLClassLoader(new URL[] { resourceBase }, null);

        Class<?> type = classLoader.loadClass(classBinaryName);

        // load each declared field into map
        Map<String,byte[]> fields = new HashMap<String,byte[]>();
        
        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            
            // field should be static
            String s = (String)field.get(null);

            byte[] bytes = s.getBytes(charsetName);

            fields.put(field.getName(), bytes);
        }
        
        return new PlainTextUnloadedClassLoader(classBinaryName, fields);
    }
    
}
