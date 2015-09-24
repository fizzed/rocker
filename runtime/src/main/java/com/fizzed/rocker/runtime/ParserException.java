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
package com.fizzed.rocker.runtime;

/**
 *
 * @author joelauer
 */
public class ParserException extends RuntimeException {

    private final int lineNumber;
    private final int columnNumber;
    private final String templatePath;

    public ParserException(int lineNumber, int columnNumber, String templatePath, String msg, Throwable cause) {
        super(buildMessage(lineNumber, columnNumber, templatePath, msg, cause), cause);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.templatePath = templatePath;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public String getTemplatePath() {
        return templatePath;
    }
    
    private static String buildMessage(int lineNumber, int columnNumber, String templatePath, String message, Throwable cause) {
        StringBuilder s = new StringBuilder();
       
        if (templatePath != null) {
            s.append(templatePath);
        }
        
        if (lineNumber >= 0) {
            s.append(":[");
            s.append(lineNumber);
            s.append(",");
            s.append(columnNumber);
            s.append("] ");
        } else {
            s.append(" ");
        }
        
        if (message != null) {
            s.append(message);
        } else if (cause != null) {
            s.append(cause.getClass().getSimpleName());
        }
        
        return s.toString();
    }
    
}
