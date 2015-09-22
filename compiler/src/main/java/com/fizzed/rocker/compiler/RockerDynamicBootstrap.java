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

import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.compiler.TemplateParser.TemplateIdentity;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.DefaultRockerTemplate;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */

public class RockerDynamicBootstrap {
    static private final Logger log = LoggerFactory.getLogger(RockerDynamicBootstrap.class);
    
    static public class LoadedTemplate {
        
        public File templateFile;                   // resolved path to template file
        public long modifiedAt;                     // modified date of version loaded
        
        //public String hash;
        
        
        //private String interfaceHash;
        
        //
        //private File javaFile;                  // resolved path to generated java file
        //private File javaClassFile;             // resolved path to compile java class
        
    }
    
    static private RockerDynamicBootstrap INSTANCE = new RockerDynamicBootstrap();
    
    private RockerDynamicClassLoader classLoader;
    private ConcurrentHashMap<String,LoadedTemplate> templates;
    
    private File templateBaseDirectory;
    
    
    private File javaClassDirectory;
    
    
    public RockerDynamicBootstrap() {
        this.classLoader = buildClassLoader();
        this.templates = new ConcurrentHashMap<>();
        
        // these need to be set some way via property file?
        this.templateBaseDirectory = new File("java6test/src/test/java");
    }
    
    static public RockerDynamicBootstrap getInstance() {
        return INSTANCE;
    }

    public File getJavaClassDirectory() {
        return javaClassDirectory;
    }

    public void setJavaClassDirectory(File javaClassDirectory) {
        this.javaClassDirectory = javaClassDirectory;
    }

    private RockerDynamicClassLoader buildClassLoader() {
        return new RockerDynamicClassLoader(this, RockerDynamicBootstrap.class.getClassLoader());
    }

    public boolean isDynamicTemplateClass(String className) {
        System.out.println("isDynamicTemplateClass: " + className);
        
        if (className.endsWith("$Template")) {
            className = className.substring(0, className.length() - 9);
        } else {
            return false;
        }
        
        return this.templates.containsKey(className);
    }
    
    public DefaultRockerTemplate template(Class modelType, DefaultRockerModel model, String templatePackageName, String templateName) throws RenderingException {
        
        LoadedTemplate template = templates.get(modelType.getName());
        
        if (template == null) {
            File bd = new File(this.templateBaseDirectory, templatePackageName.replace('.', '/'));
            File templateFile = new File(bd, templateName);
            
            if (templateFile.exists()) {
                template = new LoadedTemplate();
                template.templateFile = templateFile;
                template.modifiedAt = templateFile.lastModified();
                templates.put(modelType.getName(), template);
            } else {
                log.warn("Template file " + templateFile + " does not exist");
                //throw new RenderingException("Template file " + templateFile + " does not exist");
            }
        }
        
        if (template != null && template.modifiedAt != template.templateFile.lastModified()) {
            log.debug("template file " + template.templateFile + " changed -- recompiling...");

            /**
            TemplateCompiler compiler = new TemplateCompiler()
                .setTemplateBaseDirectory(new File("compiler/src/test/java"))
                .setJavaGenerateDirectory(new File("compiler/target/generated-test-sources/rocker"))
                .setOutputDirectory(new File("compiler/target/test-classes"));
             */
            
            // save current modifiedAt!
            template.modifiedAt = template.templateFile.lastModified();
            
            // create new classloader to force new class load
            this.classLoader = buildClassLoader();
        }
        
        try {
            Class<?> templateType = Class.forName(modelType.getName() + "$Template", false, this.classLoader);

            Constructor<?> templateConstructor = templateType.getConstructor(modelType);

            return (DefaultRockerTemplate)templateConstructor.newInstance(model);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RenderingException("Unable to load template class", e);
        }
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
