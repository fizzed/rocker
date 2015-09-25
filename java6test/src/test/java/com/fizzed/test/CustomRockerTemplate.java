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
package com.fizzed.test;

import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerOutput;
import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.DefaultRockerTemplate;

/**
 *
 * @author joelauer
 */
public abstract class CustomRockerTemplate extends DefaultRockerTemplate {

    // implicit variables/functions
    protected String implicit;
    
    public CustomRockerTemplate(DefaultRockerModel model) {
        super(model);
        
        if (model instanceof CustomRockerModel) {
            CustomRockerModel customModel = (CustomRockerModel)model;
            this.implicit = customModel.implicit;
        }
        else {
            throw new IllegalArgumentException("Unable to create template (model was not an instance of " + CustomRockerModel.class.getName() + ")");
        }
    }
    
    @Override
    protected void __associate(RockerTemplate context) {
        super.__associate(context);
        
        if (context instanceof CustomRockerTemplate) {
            CustomRockerTemplate ninjaContext = (CustomRockerTemplate)context;
            this.implicit = ninjaContext.implicit;
        }
        else {
            throw new IllegalArgumentException("Unable to associate (context was not an instance of " + CustomRockerTemplate.class.getCanonicalName() + ")");
        }
    }
    
    // implicit method
    public Integer i() {
        return 1;
    }

    // example of render() providing its own output to tie into specific framework
    @Override
    protected RockerOutput __newOutput() {
        return new CustomRockerOutput(this.__internal.getContentType());
    }
    
}
