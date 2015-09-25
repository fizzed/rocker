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
package com.fizzed.rocker.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class ReloadMain {
    static private final Logger log = LoggerFactory.getLogger(ReloadMain.class);
    
    static public void main(String[] args) throws Exception {
        
        while (true) {
            try {
                System.out.println("Press any key to render()");
                System.in.read();
                
                String out = views.index.template("Home", "Joe")
                        .render()
                        .toString();

                System.out.println("render: " + out);
                
            } catch (Throwable t) {
                log.error("", t);
            }
        }
        
    }
    
}
