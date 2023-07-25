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

import com.fizzed.rocker.compiler.TokenException;
import com.fizzed.rocker.runtime.ParserException;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class WithStatementTest {
    
    @Test
    public void notStartOrEndWithParenthese() throws Exception {
        try {
            // missing starting parenthese
            WithStatement.parse("String s = \"test\"", "template-path");
            fail("Expected exception");
        } catch (TokenException e) {
            assertThat(e.getMessage(), containsString("start with parenthese"));
        }
        
        try {
            // missing ending parenthese
            WithStatement.parse("(String s = \"test\"", "template-path");
            fail("Expected exception");
        } catch (TokenException e) {
            assertThat(e.getMessage(), containsString("end with parenthese"));
        }
    }
    
    @Test
    public void noEqualsChar() throws Exception {
        try {
            // missing starting parenthese
            WithStatement.parse("(String s -> \"test\")", "template-path");
            fail("Expected exception");
        } catch (TokenException e) {
            assertThat(e.getMessage(), containsString("no equals symbol"));
        }
    }
    
    @Test
    public void multipleEqualsChar() throws Exception {
        try {
            // missing starting parenthese
            WithStatement.parse("(String s = \"test\"; String t = \"test\")", "template-path");
            fail("Expected exception");
        } catch (TokenException e) {
            assertThat(e.getMessage(), containsString("multiple equals symbol"));
        }
    }

    @Test
    public void endsWithComma() throws Exception {
        try {
            WithStatement.parse(("(String s = \"test\",)"), "template-path");
            fail("Expected exception");
        }
        catch(ParserException e) {
            assertThat(e.getMessage(), containsString("template-path:[1,18] extraneous input '<EOF>' expecting {ARGUMENT_COMMA, ARGUMENT}"));
        }
    }

    @Test
    public void works() throws Exception {
        WithStatement with = WithStatement.parse("(String s = \"test\")", "template-path");

        final List<WithStatement.VariableWithExpression> variablesWithExpressions = with.getVariables();
        final WithStatement.VariableWithExpression variableWithExpression = variablesWithExpressions.get(0);

        assertThat(variableWithExpression.getVariable().getName(), is("s"));
        assertThat(variableWithExpression.getVariable().getType(), is("String"));
        assertThat(variableWithExpression.getValueExpression(), is("\"test\""));
    }
    
}
