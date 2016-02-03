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

public abstract class RockerTemplate {

    /**
     * Create a new RockerTemplate for the model.
     * @param model The model to create the template with
     */
    public RockerTemplate(RockerModel model) {
        // do nothing
    }
    
    /**
     * Associates this template for processing within the context of another
     * template.  This happens when TemplateA calls/includes TemplateB.
     * TemplateB needs to share variables from TemplateA before itself is
     * rendered.
     * 
     * @param template The template calling this template during a render
     */
    abstract protected void __associate(RockerTemplate template);
    
    /**
     * Creates a new RockerOutput that the template will render to.
     * @return A new RockerOutput
     */
    abstract protected RockerOutput __newOutput();
    
    @Override
    public String toString() {
        throw new UnsupportedOperationException("toString() not permitted on a RockerTemplate. Use render() method.");
    }
            
}
