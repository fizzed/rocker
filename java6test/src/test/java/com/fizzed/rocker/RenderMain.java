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
package com.fizzed.rocker;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class RenderMain {
    static private final Logger log = LoggerFactory.getLogger(RenderMain.class);

    static interface Consumer<V> {
        void consume(int index, V value);
    }
    
    static public void main(String[] args) {
        List<String> users = Arrays.asList("Joe","John","Matt"); 
        
        /**
        String html = new rocker.MoreArgs()
                .title("My Title")
                .count(10)
                .users(users)
                .render()
                .toString();
        
        System.out.println("---- render output -----");
        System.out.println(html);
        */
        
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            RockerOutput out = new rocker.LargeContent()
                    .title("Blog Template for Bootstrap")
                    .twitter("jjlauer")
                    .render();
        }
        long stop = System.currentTimeMillis();
        log.info("Took " + (stop - start) + " millis");
        
    }
    
}
