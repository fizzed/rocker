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

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Wraps a template model to allow fields to be set dynamically via reflection.
 * 
 * @author joelauer
 */
public class BindableRockerModel implements RockerModel {
    
    private final String templatePath;
    private final String templateClassName;
    private final RockerModel model;

    public BindableRockerModel(String templatePath, String templateClassName, RockerModel model) {
        this.templatePath = templatePath;
        this.templateClassName = templateClassName;
        this.model = model;
    }
    
    public BindableRockerModel bind(Map<String,Object> values) {
        for (Map.Entry<String,Object> entry : values.entrySet()) {
            bind(entry.getKey(), entry.getValue());
        }
        return this;
    }
    
    public BindableRockerModel bind(String name, Object value) {
        Method setter = null;
        try {
            // find method matching name w/ a single parameter
            Method[] methods = this.model.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals(name) && method.getParameterCount() == 1) {
                    setter = method;
                    break;
                }
            }
        } catch (SecurityException e) {
            throw new TemplateBindException(templatePath, templateClassName, "Security exception while binding property '" + name + "'", e);
        }
        
        if (setter == null) {
            throw new TemplateBindException(templatePath, templateClassName, "Property '" + name + "' not found");
        }
        
        // assignable?
        if (value != null) {
            // get first parameter
            Class<?>[] types = setter.getParameterTypes();
            Class<?> firstType = types[0];
            
            if (!firstType.isAssignableFrom(value.getClass())) {
                throw new TemplateBindException(templatePath, templateClassName, "Value type " + value.getClass().getCanonicalName()
                        + " is not assignable to property " + name + "(" + firstType.getCanonicalName() + ")");
            }
        }
        
        try {
            setter.invoke(model, value);
        } catch (Exception e) {
            throw new TemplateBindException(templatePath, templateClassName, "Unable to set property '" + name + "'", e);
        }
        
        return this;
    }

    @Override
    public RockerOutput render() throws RenderingException {
        return this.model.render();
    }
    
    public RockerModel getModel() {
        return this.model;
    }
    
    /**
     * Do not use this method in your controller code. Intended for internal use.
     */
    public BindableRockerModel __body(RockerContent __body) {
        return bind("__body", __body);
    }
    
}
