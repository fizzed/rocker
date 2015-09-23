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

import java.io.File;

/**
 *
 * @author joelauer
 */
public class CompileDiagnostic {
    
    private final File templateFile;
    private final File javaFile;
    private final long lineNumber;
    private final long columnNumber;
    private final String message;

    public CompileDiagnostic(File templateFile, File javaFile, long lineNumber, long columnNumber, String message) {
        this.templateFile = templateFile;
        this.javaFile = javaFile;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.message = message;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public File getJavaFile() {
        return javaFile;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public long getColumnNumber() {
        return columnNumber;
    }

    public String getMessage() {
        return message;
    }
    
}
