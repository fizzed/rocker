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
package com.fizzed.rocker.dynamic;

import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.compiler.TemplateCompiler;
import com.fizzed.rocker.model.TemplateModel;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.DefaultRockerTemplate;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
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
        this.templateBaseDirectory = new File("dynamic/src/test/java");
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

    // views.index$Template
    // views.index$Template$1 (if something like an inner class)
    public boolean isDynamicTemplateClass(String className) {
        System.out.println("isDynamicTemplateClass: " + className);
        
        // find first occurrence of $
        int pos = className.indexOf('$');
        if (pos < 0) {
            return false;
        }
        
        String modelClassName = className.substring(0, pos);
        
        return this.templates.containsKey(modelClassName);
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
        
        if (template != null) {
            long modifiedAt = template.templateFile.lastModified();
            
            if (modifiedAt != template.modifiedAt) {
            
                log.debug("template file " + template.templateFile + " changed -- recompiling...");

                TemplateCompiler compiler = new TemplateCompiler()
                    .setTemplateBaseDirectory(this.templateBaseDirectory)
                    .setJavaGenerateDirectory(new File("dynamic/target/generated-test-sources/rocker"))
                    .setOutputDirectory(new File("dynamic/target/test-classes"));

                try {
                    List<TemplateModel> templateModels
                            = compiler.parseTemplates(Arrays.asList(template.templateFile));

                    List<File> javaFiles = compiler.generateJavaFiles(templateModels);

                    compiler.compileJavaFiles(javaFiles);
                } catch (Exception e) {
                    throw new RenderingException("Unable to compile template " + template.templateFile, e);
                }

                // save current modifiedAt!
                template.modifiedAt = modifiedAt;

                // create new classloader to force new class load
                this.classLoader = buildClassLoader();
            }
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
