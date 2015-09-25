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
package com.fizzed.rocker.model;

import com.fizzed.rocker.runtime.ParserException;
import com.fizzed.rocker.compiler.TokenException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joelauer
 */
public class ForStatementTest {
    
    @Test
    public void startAndEndingParenthese() throws Exception {
        
        try {
            // missing starting parenthese
            ForStatement.parse("int x = 0; x < users.size(); x++");
            Assert.fail("Expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // missing ending parenthese
            ForStatement.parse("  (int x = 0; x < users.size(); x++");
            Assert.fail("Expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // neither form detected
            ForStatement.parse("( )");
            Assert.fail("Expected exception");
        } catch (TokenException e) {
            // expected
        }
    }
    
    @Test
    public void generalForm() throws Exception {
        ForStatement fs1 = ForStatement.parse("(int x = 0; x < users.size(); x++)");
        Assert.assertEquals(ForStatement.Form.GENERAL, fs1.getForm());
        Assert.assertEquals("int x = 0", fs1.getGeneralParts().get(0));
        Assert.assertEquals("x < users.size()", fs1.getGeneralParts().get(1));
        Assert.assertEquals("x++", fs1.getGeneralParts().get(2));
        
        try {
            // not enough semi-colons
            ForStatement.parse("(int x = 0; x < users.size(), x++)");
            Assert.fail("Expected exception");
        } catch (TokenException e) {
            // expected
        }
    }
    
    @Test
    public void enhancedForm() throws Exception {
	
        ForStatement fs1;
        
        // Java 7 style for loop on list
        fs1 = ForStatement.parse("(User u : users)");
        
        Assert.assertEquals(ForStatement.Form.ENHANCED, fs1.getForm());
        Assert.assertNull(fs1.getGeneralParts());
        Assert.assertEquals(1, fs1.getArguments().size());
        Assert.assertEquals(new JavaVariable("User", "u"), fs1.getArguments().get(0));
        Assert.assertEquals("users", fs1.getValueExpression());
        
        // Java 7 style for loop with optional rocker-parenthese for single items
        fs1 = ForStatement.parse("((User u) : users)");
        
        Assert.assertEquals(ForStatement.Form.ENHANCED, fs1.getForm());
        Assert.assertNull(fs1.getGeneralParts());
        Assert.assertEquals(1, fs1.getArguments().size());
        Assert.assertEquals(new JavaVariable("User", "u"), fs1.getArguments().get(0));
        Assert.assertEquals("users", fs1.getValueExpression());

        
	// Java 7 style for loop on map
        fs1 = ForStatement.parse("(Map.Entry<String,Object> item : items.entrySet())");
        
        Assert.assertEquals(ForStatement.Form.ENHANCED, fs1.getForm());
        Assert.assertNull(fs1.getGeneralParts());
        Assert.assertEquals(1, fs1.getArguments().size());
        Assert.assertEquals(new JavaVariable("Map.Entry<String,Object>", "item"), fs1.getArguments().get(0));
        Assert.assertEquals("items.entrySet()", fs1.getValueExpression());
        
        
        // Java 8 for loop with no type
        fs1 = ForStatement.parse("(i : items)");
        
        Assert.assertEquals(ForStatement.Form.ENHANCED, fs1.getForm());
        Assert.assertNull(fs1.getGeneralParts());
        Assert.assertEquals(1, fs1.getArguments().size());
        Assert.assertEquals(new JavaVariable(null, "i"), fs1.getArguments().get(0));
        Assert.assertEquals("items", fs1.getValueExpression());
        
        
        // Java 8 style tuple, but with types
        fs1 = ForStatement.parse("((String k, String v) : map)");
        
        Assert.assertEquals(ForStatement.Form.ENHANCED, fs1.getForm());
        Assert.assertNull(fs1.getGeneralParts());
        Assert.assertEquals(2, fs1.getArguments().size());
        Assert.assertEquals(new JavaVariable("String", "k"), fs1.getArguments().get(0));
        Assert.assertEquals(new JavaVariable("String", "v"), fs1.getArguments().get(1));
        Assert.assertEquals("map", fs1.getValueExpression());
        
        
        // Java 8 style tuple w/ no types
        fs1 = ForStatement.parse("((k,v) : items)");
        
        Assert.assertEquals(ForStatement.Form.ENHANCED, fs1.getForm());
        Assert.assertNull(fs1.getGeneralParts());
        Assert.assertEquals(2, fs1.getArguments().size());
        Assert.assertEquals(new JavaVariable(null, "k"), fs1.getArguments().get(0));
        Assert.assertEquals(new JavaVariable(null, "v"), fs1.getArguments().get(1));
        Assert.assertEquals("items", fs1.getValueExpression());

        
        try {
            // not enough semi-colons
            ForStatement.parse("(int x = 0; x < users.size(), x++)");
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // nothing
            ForStatement.parse("( )");
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // empty stuff
            ForStatement.parse("( : )");
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
        
        try {
            // empty arguments
            ForStatement.parse("( : items)");
            Assert.fail("expected exception");
        } catch (TokenException e) {
            // expected
        }
    }
    
}
