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

import com.fizzed.rocker.RenderingException;
import java.lang.reflect.Constructor;

/**
 *
 * @author joelauer
 */

public class DefaultRockerBootstrap implements RockerBootstrap {
    
    public DefaultRockerBootstrap() {
        // nothing to do
    }
    
    protected DefaultRockerTemplate buildTemplate(Class modelType, DefaultRockerModel model, ClassLoader classLoader) throws RenderingException {
        try {
            Class<?> templateType = Class.forName(modelType.getName() + "$Template", false, classLoader);

            Constructor<?> templateConstructor = templateType.getConstructor(modelType);

            return (DefaultRockerTemplate)templateConstructor.newInstance(model);
        } catch (Exception e) {
            throw new RenderingException("Unable to load template class", e);
        }
    }
    
    @Override
    public DefaultRockerTemplate template(Class modelType, DefaultRockerModel model) throws RenderingException {
        
        return buildTemplate(modelType, model, DefaultRockerBootstrap.class.getClassLoader());

    }    
}
