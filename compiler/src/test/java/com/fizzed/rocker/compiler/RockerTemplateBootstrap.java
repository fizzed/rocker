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
package com.fizzed.rocker.compiler;

import com.fizzed.rocker.RockerTemplate;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author joelauer
 */

public class RockerTemplateBootstrap {
    
    static public class LoadedTemplate {
        
        private String hash;
        private String interfaceHash;
        private File templateFile;              // resolved path to template file
        private File javaFile;                  // resolved path to generated java file
        private File javaClassFile;             // resolved path to compile java class
        
    }
    
    private File javaClassDirectory;
    private ConcurrentHashMap<String,LoadedTemplate> loadedTemplates;
    
    public RockerTemplateBootstrap() {
        
    }

    public File getJavaClassDirectory() {
        return javaClassDirectory;
    }

    public void setJavaClassDirectory(File javaClassDirectory) {
        this.javaClassDirectory = javaClassDirectory;
    }

    public ConcurrentHashMap<String, LoadedTemplate> getLoadedTemplates() {
        return loadedTemplates;
    }

    public void setLoadedTemplates(ConcurrentHashMap<String, LoadedTemplate> loadedTemplates) {
        this.loadedTemplates = loadedTemplates;
    }
    
    public boolean isTemplateClass(String name) {
        return this.loadedTemplates.containsKey(name);
    }
    
    
    /**
    public RockerTemplate createTemplate(Class<RockerTemplateModel> modelType) throws Exception {
        return createTemplate(modelType.getName());
    }
    */
 
/**
    // views.index
    public RockerTemplate createTemplate(String modelTypeName) throws Exception {
        // implementing template is an inner static class of the model
        String templateTypeName = modelTypeName + "$Template";
        
        // already loaded?
        LoadedTemplate loadedTemplate = loadedTemplates.get(templateTypeName);
        
        if (loadedTemplate != null) {
            
            // compare template to what's in source?
            
        }
        
        URL myUrl = new File("compiler/target/test-classes/" + name.replace(".", "/") + ".class").toURI().toURL();
        
        
        // register this template with dynamic class loader
        RockerTemplateClassLoader.TEMPLATES.put(templateClassName, "");
        
        RockerTemplateClassLoader classLoader = new RockerTemplateClassLoader(this.getClass().getClassLoader());
        
        //Class<?> templateType = Class.forName(DynamicTemplate.class.getName() + "$Template", false, classLoader);
        
        Class<?> templateType = classLoader.loadClass(templateTypeName);
        
        Constructor<?> constructor = templateType.getConstructor(DynamicTemplate.class);
        
        RockerTemplate template = (RockerTemplate)constructor.newInstance(this);
    }
    */
    
}
