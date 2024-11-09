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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompiledTemplateTest {
    static private final Logger log = LoggerFactory.getLogger(CompiledTemplateTest.class);
    
    @Test
    public void switchBlockExpressionWithDefault() throws Exception {
        String html = new rocker17.SwitchBlockExpressionWithDefault().s("test").render().toString();
        
        Assert.assertEquals(
                """
                
 this is a test 


this is a test
                """, html);

    }

    @Test
    public void switchBlockExpressionWithDefaultAndCommaDelimited() throws Exception {
        String html = new rocker17.SwitchBlockExpressionWithDefaultAndCommaDelimited().s("test").render().toString();

        Assert.assertEquals(
                """
                
 this is a test or test1


this is a test
                """, html);

    }

    @Test
    public void switchBlockExpressionWithDefaultSpaces() throws Exception {
        String html = new rocker17.SwitchBlockExpressionWithDefaultSpaces().s("test").render().toString();

        Assert.assertEquals(
                """

 this is a test 


this is a test
""", html);
    }

    @Test
    public void switchBlockExpressionWithoutDefault() throws Exception {
        String html = new rocker17.SwitchBlockExpressionWithoutDefault().s("test").render().toString();

        Assert.assertEquals(
                """
                        
 this is a test


this is a test

                        """, html);
    }
}
