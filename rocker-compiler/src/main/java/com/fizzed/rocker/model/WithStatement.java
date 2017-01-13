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

import com.fizzed.rocker.antlr4.WithBlockLexer;
import com.fizzed.rocker.antlr4.WithBlockParser;
import com.fizzed.rocker.compiler.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;

public class WithStatement implements NullSafety {

    private static final WithBlockParserListener WITH_BLOCK_PARSER_LISTENER = new WithBlockParserListener();

    private final List<VariableWithExpression> variables;
    private final boolean nullSafe;
    
    public WithStatement(List<VariableWithExpression> variables) {
        this(variables, false);
    }
    
    public WithStatement(List<VariableWithExpression> variables, boolean nullSafe) {
        this.variables = variables;
        this.nullSafe = nullSafe;
    }

    public List<VariableWithExpression> getVariables() {
        return variables;
    }

    public boolean hasAnyVariableNullType() {
        for(VariableWithExpression v : variables) {
            if(v.getVariable().getType() == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNullSafe() {
        return nullSafe;
    }
    
    static public WithStatement parse(String statement, String templatePath) throws TokenException
    {
        // possible its starts with a '?' mark
        boolean nullSafe = false;
        
        if (statement.startsWith("?")) {
            nullSafe = true;
            // chop off leading ? then trim
            statement = statement.substring(1).trim();
        }
        
        if (!statement.startsWith("(")) {
            throw new TokenException("With block does not start with parenthese");
        }
        
        if (!statement.endsWith(")")) {
            throw new TokenException("With block does not end with parenthese");
        }
        
        // chomp parentheses
        statement = statement.substring(1, statement.length() - 1);

        // We now support multiple with statements, break them apart based on the comma.
        final List<VariableWithExpression> variables = new ArrayList<>();

        final List<String> withStatements = parseWithStatement(statement, templatePath);
        for(int i = 0; i < withStatements.size(); i++) {
            final String withStatement = withStatements.get(i);

            // The normal logic what a with variable assignment must look like applies for each separate assignment
            final int equalsPos = withStatement.indexOf('=');
            if (equalsPos < 0) {
                throw new TokenException("With block invalid: no equals symbol found for assignment: " + withStatement);
            }
            // multiple equals chars?
            if (withStatement.indexOf('=', equalsPos+1) > 0) {
                throw new TokenException("With block invalid: multiple equals symbols found for assignment " + withStatement);
            }

            final String varPart = withStatement.substring(0, equalsPos);

            // parse variable
            final JavaVariable variable = JavaVariable.parse(varPart);
            final String valueExpression = withStatement.substring(equalsPos+1).trim();

            // verify its not an empty string
            if (valueExpression.equals("")) {
                throw new TokenException("With block contains an empty string for value part (e.g. var = value)");
            }

            //if()

            variables.add(new VariableWithExpression(variable, valueExpression));
        }

        // We do not allow nullSafe with more than one argument
        if(nullSafe && variables.size() > 1) {
            throw new TokenException("Nullsafe option not allowed for with block with multiple arguments");
        }

        return new WithStatement(variables, nullSafe);
    }

    private static List<String> parseWithStatement(final String value, final String templatePath) {
        final Lexer lexer = new WithBlockLexer(new ANTLRInputStream(value));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new DescriptiveErrorListener());

        final CommonTokenStream in;

        try {
            in = new CommonTokenStream(lexer);

            final WithBlockParser parser = new WithBlockParser(in);
            parser.removeErrorListeners();
            parser.addErrorListener(new DescriptiveErrorListener());
            final WithBlockParser.StartContext start = parser.start();

            WITH_BLOCK_PARSER_LISTENER.clear();
            ParseTreeWalker.DEFAULT.walk(WITH_BLOCK_PARSER_LISTENER, start);
            return WITH_BLOCK_PARSER_LISTENER.getArguments();
        }
        catch (ParserRuntimeException e) {
            throw TemplateParser.unwrapParserRuntimeException(templatePath, e);
        }
    }

    /**
     * Wrapper around one variable and its expression
     */
    public static class VariableWithExpression {

        private final JavaVariable variable;
        private final String valueExpression;

        private VariableWithExpression(JavaVariable variable, String valueExpression) {
            this.variable = variable;
            this.valueExpression = valueExpression;
        }

        public JavaVariable getVariable() {
            return variable;
        }

        public String getValueExpression() {
            return valueExpression;
        }

        @Override
        public String toString() {
            return "VariableWithExpression{" +
              "variable=" + variable +
              ", valueExpression='" + valueExpression + '\'' +
              '}';
        }
    }
}
