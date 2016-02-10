/*
 * Copyright 2016 Fendler Consulting cc.
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
package com.fizzed.rocker.model;

import com.fizzed.rocker.compiler.JavaGenerator;
import com.fizzed.rocker.compiler.RockerOptions;

/**
 * Classes implementing the {@link TemplateModelPostProcessor} interface can 
 * be used as post-processors of {@link TemplateModel}s.
 * 
 * To activate such post-processing, Rocker templates must be provided with
 * the "postProcessing" option (@see {@link RockerOptions#POST_PROCESSING}), 
 * containing a comma-separated list of class names implementing this interface.
 * 
 * The {@link JavaGenerator} then calls each given post-processor with the 
 * {@link TemplateModel} as argument immediately before the resulting template
 * Java code is created. 
 * 
 * @author jensfendler
 */
public interface TemplateModelPostProcessor {

    /**
     * Performs any post-processing on the given {@link TemplateModel} as defined 
     * by the implementing class. 
     * Only the returned instance shall be used further by the caller. Implementing
     * classes may choose to return the provided instance, or create a completely new 
     * instance for further processing.
     *   
     * @param templateModel the original template model (which might have already been 
     *     processed by previous {@link TemplateModelPostProcessor}s.
     * @param ppIndex the index (starting from 0) of the post-processor as it appears in
     *     the list of given post-processor class names.  
     * @return the resulting {@link TemplateModel} which shall be used for further 
     *     processing by following post-processors, or finally by the {@link JavaGenerator}.
     */
    public TemplateModel process( TemplateModel templateModel, int ppIndex ) throws PostProcessorException;
    
}
