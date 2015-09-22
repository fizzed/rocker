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
public class RockerTemplateClassLoader extends ClassLoader {

    private final RockerTemplateBootstrap bootstrap;
    
    public RockerTemplateClassLoader(RockerTemplateBootstrap bootstrap, ClassLoader parent) {
        super(parent);
        this.bootstrap = bootstrap;
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        
        // if the class name is NOT registered with rocker bootstrap then 
        // delegate it's loading back up to super classloader
        if (!bootstrap.isTemplateClass(name)) {
            return super.loadClass(name);
        }

        // load 
        try {
            URL myUrl = new File("compiler/target/test-classes/" + name.replace(".", "/") + ".class").toURI().toURL();
            
            System.out.println("Loading: " + myUrl);
            
            URLConnection connection = myUrl.openConnection();

            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();

            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass(name, classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
