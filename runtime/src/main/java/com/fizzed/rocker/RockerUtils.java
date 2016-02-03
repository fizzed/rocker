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
package com.fizzed.rocker;

/**
 * Utilities for working with Rocker.
 * 
 * @author joelauer
 */
public class RockerUtils {
    
    static public <T extends RockerTemplate> T requireTemplateClass(RockerTemplate template, Class<T> clazz) {
        if (clazz.isAssignableFrom(template.getClass())) {
            return (T)template;
        } else {
            throw new IllegalArgumentException("Unable to cast. Template was not an instance of " + clazz.getCanonicalName());
        }
    }
    
}
