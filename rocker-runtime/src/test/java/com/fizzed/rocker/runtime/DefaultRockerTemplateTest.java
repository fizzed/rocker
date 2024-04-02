/*
 * Copyright 2016 Fizzed, Inc.
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
import com.fizzed.rocker.RockerContent;
import com.fizzed.rocker.RockerOutput;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DefaultRockerTemplateTest {
    
    @Test
    public void charsetRequiredBeforeRender() throws Exception {
        // bare minimum model + template
        DefaultRockerModel model = new DefaultRockerModel() {
            @Override
            protected DefaultRockerTemplate buildTemplate() throws RenderingException {
                return new DefaultRockerTemplate(this) {
                    @Override
                    protected void __doRender() throws IOException, RenderingException {
                        // do nothing
                    }
                };
            }
            
        };
        
        try {
            RockerOutput out = model.render();
            fail();
        } catch (RenderingException e) {
            assertThat(e.getMessage(), containsString("charset must be initialized"));
        }
    }
    
    @Test
    public void contentTypeRequiredBeforeRender() throws Exception {
        // bare minimum model + template
        DefaultRockerModel model = new DefaultRockerModel() {
            @Override
            protected DefaultRockerTemplate buildTemplate() throws RenderingException {
                return new DefaultRockerTemplate(this) {
                    // anonymous initializer!
                    {
                        this.__internal.setCharsetName("UTF-8");
                    }
                    
                    @Override
                    protected void __doRender() throws IOException, RenderingException {
                        // do nothing
                    }
                };
            }
            
        };
        
        try {
            RockerOutput out = model.render();
            fail();
        } catch (RenderingException e) {
            assertThat(e.getMessage(), containsString("Content type must be initialized"));
        }
    }
    
    @Test
    public void basicRender() throws Exception {
        // bare minimum model + template
        DefaultRockerModel model = new DefaultRockerModel() {
            @Override
            protected DefaultRockerTemplate buildTemplate() throws RenderingException {
                return new DefaultRockerTemplate(this) {
                    // anonymous initializer!
                    {
                        this.__internal.setCharsetName("UTF-8");
                        this.__internal.setContentType(ContentType.HTML);
                    }
                    
                    @Override
                    protected void __doRender() throws IOException, RenderingException {
                        this.__internal.writeValue("Hello!");
                    }
                };
            }
        };
        
        RockerOutput out = model.render();
        
        assertThat(out.toString(), is("Hello!"));
        assertThat(out, instanceOf(ArrayOfByteArraysOutput.class));
    }
    
    @Test
    public void customOutput() throws Exception {
        // bare minimum model + template
        DefaultRockerModel model = new DefaultRockerModel() {
            @Override
            protected DefaultRockerTemplate buildTemplate() throws RenderingException {
                return new DefaultRockerTemplate(this) {
                    // anonymous initializer!
                    {
                        this.__internal.setCharsetName("UTF-8");
                        this.__internal.setContentType(ContentType.HTML);
                    }
                    
                    @Override
                    protected void __doRender() throws IOException, RenderingException {
                        this.__internal.writeValue("Hello!");
                    }
                };
            }
        };
        
        StringBuilderOutput out = model.render(StringBuilderOutput.FACTORY);
        
        assertThat(out.toString(), is("Hello!"));
        assertThat(out, instanceOf(StringBuilderOutput.class));
    }

    @Test
    public void nullSafeRockerContent() throws Exception {
        // bare minimum model + template
        DefaultRockerModel model = new DefaultRockerModel() {
            @Override
            protected DefaultRockerTemplate buildTemplate() throws RenderingException {
                return new DefaultRockerTemplate(this) {
                    // anonymous initializer!
                    {
                        this.__internal.setCharsetName("UTF-8");
                        this.__internal.setContentType(ContentType.HTML);
                    }

                    @Override
                    protected void __doRender() throws IOException, RenderingException {
                        RockerContent c = null;
                        this.__internal.renderValue(c, true, this);
                    }
                };
            }
        };

        RockerOutput out = model.render();
        assertThat(out.toString(), is(""));
        assertThat(out, instanceOf(ArrayOfByteArraysOutput.class));
    }
}
