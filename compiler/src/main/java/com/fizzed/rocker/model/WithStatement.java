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

public class WithStatement {
    
    private final JavaVariable variable;
    private final String valueExpression;

    public WithStatement(JavaVariable variable, String valueExpression) {
        this.variable = variable;
        this.valueExpression = valueExpression;
    }

    public JavaVariable getVariable() {
        return variable;
    }

    public String getValueExpression() {
        return valueExpression;
    }
    
    static public WithStatement parse(String statement) throws TokenException
    {
        if (!statement.startsWith("(")) {
            throw new TokenException("With block does not start with parenthese");
        }
        
        if (!statement.endsWith(")")) {
            throw new TokenException("With block does not end with parenthese");
        }
        
        // chomp parentheses
        statement = statement.substring(1, statement.length() - 1);
        
        int equalsPos = statement.indexOf('=');
        if (equalsPos < 0) {
            throw new TokenException("With block invalid: no equals symbol found");
        }
        
        // multiple equals chars?
        if (statement.indexOf('=', equalsPos+1) > 0) {
            throw new TokenException("With block invalid: multiple equals symbols found");
        }
        
        String varPart = statement.substring(0, equalsPos);

        // parse variable
        JavaVariable variable = JavaVariable.parse(varPart);

        String valueExpression = statement.substring(equalsPos+1).trim();

        // verify its not an empty string
        if (valueExpression.equals("")) {
            throw new TokenException("With block contains an empty string for value part (e.g. var = value)");
        }

        return new WithStatement(variable, valueExpression);
    }
}
