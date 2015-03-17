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

import com.fizzed.rocker.model.Options;
import java.io.File;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class RockerConfigurationKeys {
    static private final Logger log = LoggerFactory.getLogger(RockerConfigurationKeys.class);
    
    static public final String PARSER_BASE_DIR = "rocker.parser.base.dir";
    static public final String PARSER_OPTION_PREFIX = "rocker.parser.option.";
    
    static public final String GENERATOR_OUTPUT_DIR = "rocker.generator.output.dir";
    
    static public File getParserBaseDirectory() {
        String s = System.getProperty(PARSER_BASE_DIR, "src/main/resources/rocker");
        return new File(s);
    }
    
    static public File getGeneratorOutputDirectory() {
        String s = System.getProperty(GENERATOR_OUTPUT_DIR, "target/generated-sources/rocker");
        return new File(s);
    }
    
    static public Options getParserOptions() {
        Options options = new Options();

        // find all keys starting with options prefix and process them as strings
        Enumeration propertyNames = System.getProperties().propertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = (String)propertyNames.nextElement();
            if (name.startsWith(PARSER_OPTION_PREFIX)) {
                String optionName = name.replace(PARSER_OPTION_PREFIX, "");
                String optionValue = System.getProperty(name);
                if (optionValue != null) {
                    try {
                        log.info("Rocker parser default option: " + optionName + "=" + optionValue);
                        options.set(optionName, optionValue);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("System property " + name + " invalid: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        return options;
    }
    
    
}
