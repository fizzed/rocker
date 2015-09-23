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
package com.fizzed.rocker.reload;

import com.fizzed.rocker.RenderingException;
import com.fizzed.rocker.compiler.CompileDiagnosticException;
import com.fizzed.rocker.compiler.CompileUnrecoverableException;
import com.fizzed.rocker.compiler.GeneratorException;
import com.fizzed.rocker.compiler.ParserException;
import com.fizzed.rocker.compiler.RockerConfiguration;
import com.fizzed.rocker.compiler.TemplateCompiler;
import com.fizzed.rocker.model.TemplateModel;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.fizzed.rocker.runtime.DefaultRockerTemplate;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */

public class RockerReloadingBootstrap {
    static private final Logger log = LoggerFactory.getLogger(RockerReloadingBootstrap.class);
    
    static public class LoadedTemplate {
        public File file;                           // resolved path to template file
        public long modifiedAt;                     // modified date of version loaded
        //public String hash;
        //private String interfaceHash;
        //private File javaFile;                  // resolved path to generated java file
        //private File javaClassFile;             // resolved path to compile java class
    }
    
    static private RockerReloadingBootstrap INSTANCE = new RockerReloadingBootstrap(new RockerConfiguration());
    
    private final RockerConfiguration configuration;
    private RockerClassLoader classLoader;
    private ConcurrentHashMap<String,LoadedTemplate> templates;
    
    public RockerReloadingBootstrap(RockerConfiguration configuration) {
        this.configuration = configuration;
        this.classLoader = buildClassLoader();
        this.templates = new ConcurrentHashMap<>();
    }
    
    static public RockerReloadingBootstrap getInstance() {
        return INSTANCE;
    }

    private RockerClassLoader buildClassLoader() {
        return new RockerClassLoader(this, RockerReloadingBootstrap.class.getClassLoader());
    }
    
    private DefaultRockerTemplate buildTemplate(Class modelType, DefaultRockerModel model) throws RenderingException {
        try {
            Class<?> templateType = Class.forName(modelType.getName() + "$Template", false, this.classLoader);

            Constructor<?> templateConstructor = templateType.getConstructor(modelType);

            return (DefaultRockerTemplate)templateConstructor.newInstance(model);
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RenderingException("Unable to load template class", e);
        }
    }

    // views.index$Template
    // views.index$Template$1 (if something like an inner class)
    public boolean isTemplateClass(String className) {
        //log.debug("isDynamicTemplateClass: " + className);
        
        // find first occurrence of $
        int pos = className.indexOf('$');
        if (pos < 0) {
            return false;
        }
        
        String modelClassName = className.substring(0, pos);
        
        return this.templates.containsKey(modelClassName);
    }
    
    public File getTemplateFile(String templatePackageName, String templateName) {
        File templateFileDirectory = new File(this.configuration.getTemplateDirectory(), templatePackageName.replace('.', '/'));
        return new File(templateFileDirectory, templateName);
    }
    
    private long getModelClassModifiedAt(Class modelType) throws RenderingException {
        try {
            Field f = modelType.getField("MODIFIED_AT");
            return f.getLong(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RenderingException("Unable to read MODIFIED_AT static field from class " + modelType.getName());
        }
    }
    
    
    
    public DefaultRockerTemplate template(Class modelType, DefaultRockerModel model, String templatePackageName, String templateName) throws RenderingException {
        
        LoadedTemplate template = templates.get(modelType.getName());
        
        if (template == null) {
            
            File templateFile = getTemplateFile(templatePackageName, templateName);
            
            if (!templateFile.exists()) {
                log.warn("Rocker template [{}] does not exist for model class [{}]", templateFile, modelType);
                log.warn("Unable to check if recompile is needed, but still returning instance");
                return buildTemplate(modelType, model);
            }
            
            template = new LoadedTemplate();
            template.file = templateFile;
            template.modifiedAt = getModelClassModifiedAt(modelType);
            
            templates.put(modelType.getName(), template);
            
        } else {
            
            if (!template.file.exists()) {
                log.warn("Rocker template [{}] no longer exists for model class [{}]", template.file, modelType);
                log.warn("Did you delete it?");
                return buildTemplate(modelType, model);
            }
            
        }

        // recompile needed?
        long modifiedAt = template.file.lastModified();

        if (modifiedAt != template.modifiedAt) {

            log.debug("Rocker template change detected [{}] (recompiling)", template.file);

            TemplateCompiler compiler = new TemplateCompiler(this.configuration);

            try {
                List<TemplateCompiler.CompilationUnit> units
                        = compiler.parse(Arrays.asList(template.file));

                compiler.generate(units);

                compiler.compile(units);
            } catch (ParserException | IOException | GeneratorException | CompileUnrecoverableException | CompileDiagnosticException e) {
                throw new RenderingException("Unable to compile rocker template [" + template.file + "]", e);
            }

            // save current modifiedAt!
            template.modifiedAt = modifiedAt;

            // create new classloader to force a new class load
            this.classLoader = buildClassLoader();
        }
        
        return buildTemplate(modelType, model);
    }    
}
