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

import com.fizzed.rocker.model.SourceRef;

/**
 *
 * @author joelauer
 */
public class ParserException extends RuntimeException {

    private final int line;
    private final int posInLine;
    private final String templatePath;
    
    public ParserException(SourceRef sourceRef, String templatePath, String msg) {
        this(sourceRef.getBegin().getLineNumber(), sourceRef.getBegin().getPosInLine(), templatePath, msg, null);
    }
    
    public ParserException(SourceRef sourceRef, String templatePath, String msg, Throwable cause) {
        this(sourceRef.getBegin().getLineNumber(), sourceRef.getBegin().getPosInLine(), templatePath, msg, cause);
    }
    
    public ParserException(int line, int posInLine, String templatePath, String msg, Throwable cause) {
        super(buildMessage(line, posInLine, templatePath, msg, cause), cause);
        this.line = line;
        this.posInLine = posInLine;
        this.templatePath = templatePath;
    }
    
    public int getLine() {
        return line;
    }

    public int getPosInLine() {
        return posInLine;
    }

    public String getTemplatePath() {
        return templatePath;
    }
    
    private static String buildMessage(int sourceLine, int posInLine, String templatePath, String message, Throwable cause) {
        StringBuilder s = new StringBuilder();
        s.append("Parsing failure with ");
        
        if (templatePath != null) {
            s.append(templatePath);
            s.append(" ");
        }
        
        if (sourceLine >= 0) {
            s.append("[line ");
            s.append(sourceLine);
            s.append(":");
            s.append(posInLine);
            s.append("] ");
        }
        if (message != null) {
            s.append(message);
        } else if (cause != null) {
            s.append(cause.getClass().getSimpleName());
        }
        return s.toString();
    }
    
}
