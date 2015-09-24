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

import java.io.File;

/**
 *
 * @author joelauer
 */
public class CompileDiagnostic {
    
    private final File templateFile;
    private final File javaFile;
    private final long templateLineNumber;
    private final long templateColumnNumber;
    private final long javaLineNumber;
    private final long javaColumnNumber;
    private final String message;

    public CompileDiagnostic(File templateFile, File javaFile, long templateLineNumber, long templateColumnNumber, long javaLineNumber, long javaColumnNumber, String message) {
        this.templateFile = templateFile;
        this.javaFile = javaFile;
        this.templateLineNumber = templateLineNumber;
        this.templateColumnNumber = templateColumnNumber;
        this.javaLineNumber = javaLineNumber;
        this.javaColumnNumber = javaColumnNumber;
        this.message = message;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public File getJavaFile() {
        return javaFile;
    }

    public long getTemplateLineNumber() {
        return templateLineNumber;
    }

    public long getTemplateColumnNumber() {
        return templateColumnNumber;
    }

    public long getJavaLineNumber() {
        return javaLineNumber;
    }

    public long getJavaColumnNumber() {
        return javaColumnNumber;
    }

    public String getMessage() {
        return message;
    }
    
}
