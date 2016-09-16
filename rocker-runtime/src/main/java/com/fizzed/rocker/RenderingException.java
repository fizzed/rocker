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

/**
 * Runtime exception while rendering a RockerTemplate.  If generated while
 * executing a template -- then the source info (line and position in line)
 * of the original source template will be populated.
 * 
 * @author joelauer
 */
public class RenderingException extends RuntimeException {
    
    private final int sourceLine;
    private final int sourcePosInLine;
    private final String templateName;
    private final String templatePath;

    public RenderingException(String message) {
        this(-1, -1, message, null);
    }

    public RenderingException(String message, Throwable cause) {
        this(-1, -1, message, cause);
    }
    
    public RenderingException(int sourceLine, int sourcePosInLine, String message, Throwable cause) {
        this(sourceLine, sourcePosInLine, null, null, message, cause);
    }
    
    public RenderingException(int sourceLine, int sourcePosInLine, String templateName, String templatePath, String message, Throwable cause) {
        super(buildMessage(sourceLine, sourcePosInLine, templateName, templatePath, message, cause), cause);
        this.sourceLine = sourceLine;
        this.sourcePosInLine = sourcePosInLine;
        this.templateName = templateName;
        this.templatePath = templatePath;
    }
    
    private static String buildMessage(int sourceLine, int sourcePosInLine, String templateName, String templatePath, String message, Throwable cause) {
        StringBuilder s = new StringBuilder();
        
        if (templatePath != null) {
            s.append(templatePath);
            s.append("/");
        }
        if (templateName != null) {
            s.append(templateName);
        }
        if (sourceLine >= 0) {
            s.append(" [line ");
            s.append(sourceLine);
            s.append(":");
            s.append(sourcePosInLine);
            s.append("]");
        }
        if (templateName != null) {
            s.append(": ");
        }
        if (message != null) {
            s.append(message);
        } else if (cause != null) {
            s.append(cause.getClass().getSimpleName());
            s.append(" in render");
        }
        return s.toString();
    }

    public int getSourceLine() {
        return sourceLine;
    }

    public int getSourcePosInLine() {
        return sourcePosInLine;
    }
    
    public boolean hasSourceInfo() {
        return sourceLine >= 0;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getTemplatePath() {
        return templatePath;
    }
    
}
