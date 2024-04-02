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
    private RenderingStrategy renderingStrategy = new DefaultRenderingStrategy(); // Default strategy

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
        return new ArrayOfByteArraysOutput(__internal.getContentType(), __internal.getCharsetName());
    }

    public void setRenderingStrategy(RenderingStrategy renderingStrategy) {
        this.renderingStrategy = renderingStrategy;
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
        if (this.__internal.getCharsetName() == null) {
            throw new RenderingException("Template charset must be initialized before render");
        }
        
        if (this.__internal.getContentType() == null) {
            throw new RenderingException("Content type must be initialized before render");
        }
        
        if (this.__internal.getStringify() == null) {
            throw new RenderingException("Stringify must be initialized before render");
        }
        
        if (this.__internal.getOut() == null) {
            if (outputFactory != null) {
                this.__internal.setOut(outputFactory.create(this.__internal.getContentType(), this.__internal.getCharsetName()));
            } else {
                this.__internal.setOut(__newOutput());
            }
        }
        
        // make sure not previously used
        __internal.verifyOkToBeginRendering();

        try {
            this.renderingStrategy.render(this); // Delegate rendering to the strategy
        } catch (CompileDiagnosticException e) {
            // do not wrap the underlying exception
            throw e;
        } catch (Throwable t) {
            // include info on source line + pos where execution failed
            String templatePath = __internal.getTemplatePackageName().replace('.', '/');
            throw new RenderingException(__internal.getSourceLine(), __internal.getSourcePosInLine(), __internal.getTemplateName(), templatePath, t.getMessage(), t);
        }
        
        return __internal.getOut();
    }
    
    abstract protected void __doRender() throws IOException, RenderingException;
}
