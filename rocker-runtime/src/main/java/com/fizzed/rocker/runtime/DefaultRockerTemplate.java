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

import com.fizzed.rocker.BindableRockerModel;
import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.RockerStringify;
import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.RockerContent;
import com.fizzed.rocker.RockerModel;
import com.fizzed.rocker.RockerOutputFactory;
import java.io.IOException;

public abstract class DefaultRockerTemplate extends RockerTemplate {
    
    protected Internal __internal;
    
    public DefaultRockerTemplate(RockerModel model) {
        super(model);
        this.__internal = new Internal();
    }
    
    @Override
    protected void __associate(RockerTemplate context) {
        // safe to assume its a default instance
        // subclasses should verify what type is supplied though
        DefaultRockerTemplate otherContext = (DefaultRockerTemplate)context;
        
        // configure this template from another template
        // internally the out, content type, and stringify are all shared
        __internal.setOut(otherContext.__internal.getOut());
        __internal.setContentType(otherContext.__internal.getContentType(), otherContext.__internal.getStringify());
    }
    
    @Override
    protected RockerOutput __newOutput() {
        return new ArrayOfByteArraysOutput(__internal.getContentType(), __internal.getCharset());
    }

    /**
     * Executes template and renders content to output.
     * @param context The optional context if this template is being rendered within another template
     * @param outputFactory Factory for creating output if necessary
     * @return The output of rendering process
     * @throws RenderingException Thrown if any error encountered while rendering
     *      template. Exception will include underlying cause as well as line
     *      and position of original template source that triggered exception.
     */
    final public RockerOutput __render(DefaultRockerTemplate context,
                                       RockerOutputFactory outputFactory) throws RenderingException {
        // associate with a context of another template
        if (context != null) {
            this.__associate(context);
        }
        
        //
        // verify things are setup correctly
        //
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
            if (outputFactory != null) {
                this.__internal.out = outputFactory.create(this.__internal.contentType, this.__internal.charset);
            } else {
                this.__internal.out = __newOutput();
            }
        }
        
        // make sure not previously used
        __internal.verifyOkToBeginRendering();

        try {
            this.__doRender();
        } catch (CompileDiagnosticException e) {
            // do not wrap the underlying exception
            throw e;
        } catch (Throwable t) {
            // include info on source line + pos where execution failed
            String templatePath = __internal.templatePackageName.replace('.', '/');
            throw new RenderingException(__internal.sourceLine, __internal.sourcePosInLine, __internal.templateName, templatePath, t.getMessage(), t);
        }
        
        return __internal.out;
    }
    
    abstract protected void __doRender() throws IOException, RenderingException;
    
    //
    // implicits to rocker templates
    //
    
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

    /**
     * Internal state of a template.
     * 
     * Simple way to hide internal variables from template (which of course
     * are subclasses of this class).
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
        // break, continue support
        //
        public void throwBreakException() throws BreakException {
            throw new BreakException();
        }
        
        public void throwContinueException() throws ContinueException {
            throw new ContinueException();
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
        public boolean renderValue(RockerContent c, boolean nullSafe) throws RenderingException, IOException {
            if (nullSafe && c == null) {
                return false;
            }
            
            // delegating rendering this chunk of content to itself
            c.render();
            
            return true;
        }
        
        public boolean renderValue(DefaultRockerModel model, boolean nullSafe) throws RenderingException, IOException {
            model.doRender(DefaultRockerTemplate.this, null, null);
            
            return true;
        }
        
        public boolean renderValue(BindableRockerModel model, boolean nullSafe) throws RenderingException, IOException {
            // delegating rendering this model to itself BUT under a context
            DefaultRockerModel underlyingModel = (DefaultRockerModel)model.getModel();
            
            return this.renderValue(underlyingModel, nullSafe);
        }
        
        public boolean renderValue(Raw raw, boolean nullSafe) throws RenderingException, IOException {
            if (nullSafe && raw.getValue() == null) {
                return false;
            }
            
            // no stringify for raws
            out.w(raw.toString());
            
            return true;
        }
        
        public boolean renderValue(String value, boolean nullSafe) throws IOException {
            if (nullSafe && value == null) {
                return false;
            }
            
            String s = stringify.s(value);
            
            // also, null safe protects against a toString() that returns a null
            if (nullSafe && s == null) {
                return false;
            }
            
            out.w(s);
            
            return true;
        }
        
        public boolean renderValue(Object value, boolean nullSafe) throws IOException {
            if (nullSafe && value == null) {
                return false;
            }
            
            String s = stringify.s(value);
            
            // also, null safe protects against a toString() that returns a null
            if (nullSafe && s == null) {
                return false;
            }
            
            out.w(s);
            
            return true;
        }
        
        public boolean renderValue(byte v, boolean nullSafe) throws IOException {
            // ignore nullSafe since a primitive cannot be null
            out.w(stringify.s(v));
            
            return true;
        }
        
        public boolean renderValue(short v, boolean nullSafe) throws IOException {
            // ignore nullSafe since a primitive cannot be null
            out.w(stringify.s(v));
            
            return true;
        }

        public boolean renderValue(int v, boolean nullSafe) throws IOException {
            // ignore nullSafe since a primitive cannot be null
            out.w(stringify.s(v));
            
            return true;
        }
        
        public boolean renderValue(long v, boolean nullSafe) throws IOException {
            // ignore nullSafe since a primitive cannot be null
            out.w(stringify.s(v));
            
            return true;
        }

        public boolean renderValue(float v, boolean nullSafe) throws IOException {
            // ignore nullSafe since a primitive cannot be null
            out.w(stringify.s(v));
            
            return true;
        }

        public boolean renderValue(double v, boolean nullSafe) throws IOException {
            // ignore nullSafe since a primitive cannot be null
            out.w(stringify.s(v));
            
            return true;
        }

        public boolean renderValue(char v, boolean nullSafe) throws IOException {
            // ignore nullSafe since a primitive cannot be null
            out.w(stringify.s(v));
            
            return true;
        }

        public boolean renderValue(boolean v, boolean nullSafe) throws IOException {
            // ignore nullSafe since a primitive cannot be null
            out.w(stringify.s(v));
            
            return true;
        }
        
    }
}
