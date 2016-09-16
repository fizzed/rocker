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

import java.util.List;
import java.util.ArrayList;
import com.fizzed.rocker.compiler.TokenException;

/**
 *
 * @author joelauer
 */
public class ForStatement {
    
    // http://docs.oracle.com/javase/tutorial/java/nutsandbolts/for.html
    public enum Form {
        GENERAL,               // for (int i = 0; i < 10; i++) { // code }
        ENHANCED               // for (User u : users) { // code }
    }
    
    private final Form form;

    // general form parts (e.g. for (part1; part2; part3) { })
    private final List<String> generalParts;

    // enhanced form arguments + value expression (e.g. for ((arg1, arg2, arg3) : valueExpression) { })
    private final List<JavaVariable> arguments;
    private final String valueExpression;

    public ForStatement(List<String> generalParts) {
        this.form = Form.GENERAL;
        this.generalParts = generalParts;
        this.arguments = null;
        this.valueExpression = null;
    }
    
    public ForStatement(List<JavaVariable> arguments, String valueExpression) {
        this.form = Form.ENHANCED;
        this.generalParts = null;
        this.arguments = arguments;
        this.valueExpression = valueExpression;
    }

    public Form getForm() {
        return form;
    }

    public List<String> getGeneralParts() {
        return generalParts;
    }

    public List<JavaVariable> getArguments() {
        return arguments;
    }
    
    public boolean hasAnyUntypedArguments() {
        if (this.arguments != null) {
            for (JavaVariable jv : arguments) {
                if (jv.getType() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getValueExpression() {
        return valueExpression;
    }
    
    static public ForStatement parse(String statement) throws TokenException
    {
        if (!statement.startsWith("(")) {
            throw new TokenException("For block does not start with parenthese");
        }
        
        if (!statement.endsWith(")")) {
            throw new TokenException("For block does not end with parenthese");
        }
        
        // chomp parentheses
        statement = statement.substring(1, statement.length() - 1);
        
        if (statement.contains(";")) {
            String[] generalParts = statement.split(";");
            if (generalParts.length != 3) {
                throw new TokenException("For block has invalid general form [e.g. for (initialization; termination; increment) {]");
            }

            List<String> parts = new ArrayList<>();
            for (String s : generalParts) {
                parts.add(s.trim());
            }

            return new ForStatement(parts);
        }
        
        if (statement.contains(":")) {
            String[] enhancedParts = statement.split(":");
            if (enhancedParts.length != 2) {
                throw new TokenException("For block has invalid enhanced form [e.g. for (item : items) {]");
            }

            String argsPart = enhancedParts[0].trim();

            // verify its not an empty string
            if (argsPart.equals("")) {
                throw new TokenException("For block with enhanced form contains an empty string for arguments part (e.g. for (arg : list)");
            }

            // a single argument does not need parenthese
            boolean hasParenthese = false;

            if (argsPart.startsWith("(")) {
                if (!argsPart.endsWith(")")) {
                    throw new TokenException("For block with enhanced form contains invalid arguments section (closing parenthese not found)");
                }
                hasParenthese = true;

                // strip starting/closing parenthese
                argsPart = argsPart.substring(1, argsPart.length() - 1);
            }

            // parse underlying arguments
            List<JavaVariable> arguments = null;
            try {
                arguments = JavaVariable.parseList(argsPart);
            } catch (TokenException e) {
                throw new TokenException(e.getMessage());
            }
            
            // anything of more than 1 argument requires parenthese
            if (arguments.size() > 1 && !hasParenthese) {
                throw new TokenException("For block with enhanced form contains invalid arguments section (more than 1 argument must be enclosed with parenthese e.g. (k,v))");
            }

            // at most 3 arguments supported
            if (arguments.size() > 3) {
                throw new TokenException("For block with enhanced form contains " + arguments.size() + " arguments (at most 3 supported)");
            }
            
            String valuePart = enhancedParts[1].trim();

            // verify its not an empty string
            if (valuePart.equals("")) {
                throw new TokenException("For block with enhanced form contains an empty string for value part (e.g. for (item : value)");
            }

            return new ForStatement(arguments, valuePart.trim());
        }
        
        throw new TokenException("For block appears to be invalid (neither general or enhanced form detected)");
    }
    
    
}
