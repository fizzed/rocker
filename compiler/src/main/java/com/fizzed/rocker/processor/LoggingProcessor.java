/*
 * Copyright 2015 Fendler Consulting cc.
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
package com.fizzed.rocker.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fizzed.rocker.model.PostProcessorException;
import com.fizzed.rocker.model.TemplateModel;
import com.fizzed.rocker.model.TemplateModelPostProcessor;

/**
 * This post-processor does not modify the model, but simply does some logging. 
 * It is intended for testing purposes only.
 *  
 * @author jensfendler
 */
public class LoggingProcessor implements TemplateModelPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LoggingProcessor.class);
    
    /**
     * @see com.fizzed.rocker.model.TemplateModelPostProcessor#process(com.fizzed.rocker.model.TemplateModel, int)
     */
    @Override
    public TemplateModel process(TemplateModel templateModel, int ppIndex) throws PostProcessorException {
        log.info("Template {} being post-processed by {} at index {}.", templateModel.getName(), getClass().getSimpleName(), ppIndex );
        return templateModel;
    }

}
