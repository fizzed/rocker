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

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.RockerStringify;
import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.RockerContent;
import java.io.IOException;

/**
 *
 * @param <T>
 * @author joelauer
 */
public abstract class DefaultRockerTemplate<T extends DefaultRockerTemplate> implements RockerTemplate {
    
    // var name not likely to conflict with normal ones
    protected Internal __internal;
    
    public DefaultRockerTemplate() {
        this.__internal = new Internal();
    }

    // do not stringify a value
    
    public Raw raw(Object obj) throws IOException {
        if (obj == null) {
            throw new NullPointerException("Value was null");
        }
        return Raw.of(obj.toString());
    }
    
    public Raw raw(String s) throws IOException {
        if (s == null) {
            throw new NullPointerException("Value was null");
        }
        return Raw.of(s);
    }
    
    public T __body(RockerContent body) {
        throw new RenderingException("Template does not support a body. Forgot to declare RockerBody as a template argument?");
    }

    /**
     * Executes template and renders content to output.
     * @return The output of rendering process
     * @throws RenderingException Thrown if any error encountered while rendering
     *      template. Exception will include underlying cause as well as line
     *      and position of original template source that triggered exception.
     */
    @Override
    public RockerOutput render() throws RenderingException {
        // verify things are setup correctly
        
        if (this.__internal.charset == null) {
            throw new RenderingException("Template charset must be initialized before render");
        }
        
        if (this.__internal.contentType == null) {
            throw new RenderingException("Content type must be initialized before render");
        }
        
        if (this.__internal.stringify == null) {
            throw new RenderingException("Stringify must be initialized before render");
        }
        
        if (this.__internal.out == null) {
            // initialize with default output
            this.__internal.out = __newOutput();
        }
        
        // make sure not previously used
        __internal.verifyOkToBeginRendering();

        try {
            this.__render();
        } catch (Throwable t) {
            // include info on source line + pos where execution failed
            String templatePath = __internal.templatePackageName.replace('.', '/');
            throw new RenderingException(__internal.sourceLine, __internal.sourcePosInLine, __internal.templateName, templatePath, t.getMessage(), t);
        }
        
        return __internal.out;
    }
    
    abstract protected void __render() throws IOException, RenderingException;
    
    protected <T> void __configure(T other) throws RenderingException {
        if (other instanceof DefaultRockerTemplate) {
            DefaultRockerTemplate otherTemplate = (DefaultRockerTemplate)other;
            // configure this template from another template
            // internally the out, content type, and stringify are all shared
            __internal.setOut(otherTemplate.__internal.getOut());
            __internal.setContentType(otherTemplate.__internal.getContentType(),
                                        otherTemplate.__internal.getStringify());
        }
        else {
            throw new RenderingException("Unable to configure template (not an instance of " + this.getClass().getName() + ")");
        }
    }
    
    protected RockerOutput __newOutput() {
        return new ArrayOfByteArraysOutput(__internal.getCharset());
        //return new StringBuilderOutput(__internal.getCharset());
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("toString() not permitted on RockerTemplate. User render() method.");
    }
    
    /**
     * Internal state of a template.
     * 
     * Simple way to hide internal variables from template.
     * 
     * This is an internal API and it may radically change over time.  Using
     * this for workaround, etc. is not recommended.
     */
    protected class Internal {
        
        // shared vars (e.g. template A calls template B)
        private String charset;
        private ContentType contentType;
        private RockerStringify stringify;
        private RockerOutput out;
        
        private boolean rendered;
        // counters for where we are in original source (helps provide better runtime exceptions)
        private int sourceLine;
        private int sourcePosInLine;
        private String templateName;
        private String templatePackageName;
        
        private Internal() {
            this.sourceLine = -1;
            this.sourcePosInLine = -1;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public String getTemplatePackageName() {
            return templatePackageName;
        }

        public void setTemplatePackageName(String templatePackageName) {
            this.templatePackageName = templatePackageName;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }
        
        public void aboutToExecutePosInTemplate(int line, int posInLine) {
            this.sourceLine = line;
            this.sourcePosInLine = posInLine;
        }
        
        public ContentType getContentType() {
            return this.contentType;
        }

        public void setContentType(ContentType contentType) {
            // set default stringify by content type of template
            this.setContentType(contentType, ContentType.stringify(contentType));
        }
        
        public void setContentType(ContentType contentType, RockerStringify stringify) {
            this.contentType = contentType;
            this.stringify = stringify;
        }

        public RockerStringify getStringify() {
            return stringify;
        }

        public void setStringify(RockerStringify stringify) {
            this.stringify = stringify;
        }

        public RockerOutput getOut() {
            return out;
        }

        public void setOut(RockerOutput out) {
            this.out = out;
        }
        
        protected void verifyOkToBeginRendering() {
            if (this.rendered) {
                throw new RenderingException("Template already rendered (templates are single use only!)");
            }
            this.rendered = true;
        }
        
        //
        // method for write raw expressions
        //
        public void writeValue(String s) throws IOException {
            out.w(s);
        }
        
        public void writeValue(byte[] bytes) throws IOException {
            out.w(bytes);
        }
        
        //
        // methods for rendering value expressions
        //
        
        public void renderValue(Raw raw) throws RenderingException, IOException {
            // no stringify for raws
            out.w(raw.toString());
        }
        
        public void renderValue(RockerContent c) throws RenderingException, IOException {
            // delegating rendering this chunk of content to itself
            c.render();
        }
        
        public void renderValue(DefaultRockerTemplate otherTemplate) throws RenderingException, IOException {
            // configure template within this template's context
            otherTemplate.__configure(DefaultRockerTemplate.this);            
            // render new template
            otherTemplate.render();
        }
        
        public void renderValue(String s) throws IOException {
            out.w(stringify.s(s));
        }
        
        public void renderValue(Object obj) throws IOException {
            out.w(stringify.s(obj));
        }

        public void renderValue(int v) throws IOException {
            out.w(stringify.s(v));
        }

        public void renderValue(float v) throws IOException {
            out.w(stringify.s(v));
        }

        public void renderValue(double v) throws IOException {
            out.w(stringify.s(v));
        }

        public void renderValue(byte v) throws IOException {
            out.w(stringify.s(v));
        }

        public void renderValue(char v) throws IOException {
            out.w(stringify.s(v));
        }

        public void renderValue(boolean v) throws IOException {
            out.w(stringify.s(v));
        }
        
    }
}
