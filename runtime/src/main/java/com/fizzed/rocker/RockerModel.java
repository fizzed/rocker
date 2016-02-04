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
 * Represents a model for a template.
 * 
 * @author joelauer
 */
public interface RockerModel {

    /**
     * Executes a template model and renders content to output. Single use only.
     * @return The output of rendering process
     * @throws RenderingException Thrown if any error encountered while rendering
     *      template. Exception will include underlying cause as well as line
     *      and position of original template source that triggered exception.
     */
    RockerOutput render() throws RenderingException;
    
    /**
     * Executes a template model and renders content to output. Single use only.
     * @param <O> The output type
     * @param outputFactory A factory to create a new RockerOutput. If null then
     *      the a default defined by the template will be used instead.
     * @return The output of rendering process
     * @throws RenderingException Thrown if any error encountered while rendering
     *      template. Exception will include underlying cause as well as line
     *      and position of original template source that triggered exception. 
     */
    <O extends RockerOutput> O render(RockerOutputFactory<O> outputFactory) throws RenderingException;
    
    /**
     * Executes a template model and renders content to output. Single use only.
     * @param <O> The output type
     * @param outputFactory A factory to create a new RockerOutput. If null then
     *      the a default defined by the template will be used instead.
     * @param templateCustomizer A customizer for last second changes to template
     *          between the time its built and the time it actually is rendered.
     *          Useful for injecting implicit variables in frameworks. If null
     *          then no customization will be done.
     * @return The output of rendering process
     * @throws RenderingException Thrown if any error encountered while rendering
     *      template. Exception will include underlying cause as well as line
     *      and position of original template source that triggered exception. 
     */
    <O extends RockerOutput> O render(RockerOutputFactory<O> outputFactory,
                                      RockerTemplateCustomizer templateCustomizer) throws RenderingException;
    
}
