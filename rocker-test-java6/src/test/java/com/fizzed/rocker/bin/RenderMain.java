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
package com.fizzed.rocker.bin;

import com.fizzed.rocker.RockerOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderMain {
    static private final Logger log = LoggerFactory.getLogger(RenderMain.class);
    
    static public void main(String[] args) {
        /*
        String html = new rocker.Args()
            .s("Hi")
            .i(1)
            .render()
            .toString();
        
        System.out.println("---- render output -----");
        System.out.println(html);
        */
        
        long start = System.currentTimeMillis();
        
        for (int i = 0; i < 1000000; i++) {
            RockerOutput out = new rocker.Args()
                .s("<>&'\"€")
                //.s("")
                .i(1)
                .render();
                //.toString();
        }
        
        long stop = System.currentTimeMillis();
        
        System.out.println("Finished in " + (stop-start) + " ms");
        
        //System.out.println("---- render output -----");
        //System.out.println(html);
    }
    
}
