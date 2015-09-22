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
package com.fizzed.rocker.dynamic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author joelauer
 */
public class RockerDynamicClassLoader extends ClassLoader {

    private final RockerDynamicBootstrap bootstrap;
    
    public RockerDynamicClassLoader(RockerDynamicBootstrap bootstrap, ClassLoader parent) {
        super(parent);
        this.bootstrap = bootstrap;
    }

    public boolean isClassLoaded(String className) {
        return this.findLoadedClass(className) != null;
    }
    
    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        
        // only load classes registered with rocker dynamic bootstrap
        if (!bootstrap.isDynamicTemplateClass(className)) {
            return super.loadClass(className);
        }

        // load as though class was a resource
        try {
            String resourceName = className.replace(".", "/") + ".class";

            URL url = this.getResource(resourceName);

            if (url == null) {
                throw new ClassNotFoundException("Class " + className + " not found");
            }

            //URL myUrl = new File("java6test/target/test-classes/" + className.replace(".", "/") + ".class").toURI().toURL();
            
            System.out.println("Loading: " + url);
            
            URLConnection connection = url.openConnection();

            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass(className, classData, 0, classData.length);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClassNotFoundException(e.getMessage(), e);
        }
    }

}
