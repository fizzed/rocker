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

import com.fizzed.rocker.RockerRuntime;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class RockerConfiguration {
    static private final Logger log = LoggerFactory.getLogger(RockerConfiguration.class);
    
    static public final String TEMPLATE_DIR = "rocker.template.dir";
    static public final String OUTPUT_DIR = "rocker.output.dir";
    static public final String COMPILE_DIR = "rocker.class.dir";
    static public final String OPTION_PREFIX = "rocker.option.";
    
    private File templateDirectory;
    private File outputDirectory;
    private File compileDirectory;
    private RockerOptions options;
    
    public RockerConfiguration() {
        this.templateDirectory = new File("src/main/resources/rocker");
        this.outputDirectory = new File("target/generated-sources/rocker");
        this.compileDirectory = new File("target/classes");
        this.options = new RockerOptions();
        // merge in system properties
        merge(System.getProperties());
        // merge in rocker.conf from classpath
        mergeFromClassPath();
    }
    
    final public void mergeFromClassPath() {
        InputStream is = this.getClass().getResourceAsStream(RockerRuntime.CONF_RESOURCE_NAME);
        if (is != null) {
            Properties properties = new Properties();
            try {
                properties.load(is);
                merge(properties);
            } catch (Exception e) {
                log.warn("Unable to load rocker.conf from classpath", e);
            }
        }
    }
    
    final public void merge(Properties properties) {
        if (properties.containsKey(TEMPLATE_DIR)) {
            this.templateDirectory = new File(properties.getProperty(TEMPLATE_DIR));
            log.debug("templateDirectory = " + this.templateDirectory);
        }
        
        if (properties.containsKey(OUTPUT_DIR)) {
            this.outputDirectory = new File(properties.getProperty(OUTPUT_DIR));
            log.debug("outputDirectory = " + this.outputDirectory);
        }
        
        if (properties.containsKey(COMPILE_DIR)) {
            this.compileDirectory = new File(properties.getProperty(COMPILE_DIR));
            log.debug("compileDirectory = " + this.compileDirectory);
        }
        
        // find all keys starting with rocker.options prefix and process them as strings
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = (String)propertyNames.nextElement();
            if (name.startsWith(OPTION_PREFIX)) {
                String optionName = name.replace(OPTION_PREFIX, "");
                String optionValue = properties.getProperty(name);
                if (optionValue != null) {
                    try {
                        log.debug("option " + optionName + " = " + optionValue);
                        options.set(optionName, optionValue);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Property " + name + " invalid: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    public void write(File file) throws IOException {
        Properties properties = new Properties();
        properties.put(TEMPLATE_DIR, this.templateDirectory.getPath());
        properties.put(OUTPUT_DIR, this.outputDirectory.getPath());
        properties.put(COMPILE_DIR, this.compileDirectory.getPath());
        this.options.write(properties);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, OUTPUT_DIR);
        }
    }

    public File getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(File templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getCompileDirectory() {
        return compileDirectory;
    }

    public void setCompileDirectory(File compileDirectory) {
        this.compileDirectory = compileDirectory;
    }

    public RockerOptions getOptions() {
        return options;
    }

    public void setOptions(RockerOptions options) {
        this.options = options;
    }
    
}
