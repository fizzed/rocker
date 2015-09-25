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
 * Runtime exception while binding values to a RockerTemplate.
 * 
 * @author joelauer
 */
public class TemplateBindException extends RuntimeException {

    private final String templatePath;
    private final String templateClassName;
    
    public TemplateBindException(String templatePath, String templateClassName, String message) {
        this(templatePath, templateClassName, message, null);
    }

    public TemplateBindException(String templatePath, String templateClassName, String message, Throwable cause) {
        super(buildMessage(templatePath, message), cause);
        this.templatePath = templatePath;
        this.templateClassName = templateClassName;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public String getTemplateClassName() {
        return templateClassName;
    }
    
    static private String buildMessage(String templatePath, String message) {
        return new StringBuilder()
                .append(templatePath)
                .append(": ")
                .append(message)
                .toString();
    }
    
}
