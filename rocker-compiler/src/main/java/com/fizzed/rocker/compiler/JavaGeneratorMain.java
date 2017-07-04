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

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 *
 * @author joelauer
 */
public class JavaGeneratorMain {

    
    static public void main(String[] a) throws Exception {

        JavaGeneratorRunnable jgr = new JavaGeneratorRunnable();
        
        // process command-line arguments
        ArrayDeque<String> args = new ArrayDeque<>(a.length);
        args.addAll(Arrays.asList(a));
        while (args.size() > 0) {
            String n = args.poll();
            if (args.isEmpty()) {
                System.err.println("Not enough arguments");
                System.exit(1);
            }
            
            String v = args.poll();
            
            switch (n) {
                case "-t":
                    jgr.setTemplateDirectory(new File(v));
                    break;
                case "-o":
                    jgr.setOutputDirectory(new File(v));
                    break;
            }
        }
        
        jgr.run();
    }
}
