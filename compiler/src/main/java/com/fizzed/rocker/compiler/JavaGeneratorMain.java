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

import com.fizzed.rocker.model.TemplateModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class JavaGeneratorMain {
    private static final Logger log = LoggerFactory.getLogger(JavaGeneratorMain.class);
    
    private final TemplateParser parser;
    private final JavaGenerator generator;
    private final List<File> templateFiles;
    private String suffixRegex;
    private boolean failOnError;
    
    public JavaGeneratorMain() {
        this.parser = new TemplateParser();
        this.generator = new JavaGenerator();
        this.templateFiles = new ArrayList<>();
        this.suffixRegex = ".*\\.rocker\\.(raw|html)$";
        this.failOnError = true;
    }

    public String getSuffixRegex() {
        return suffixRegex;
    }

    public void setSuffixRegex(String suffixRegex) {
        this.suffixRegex = suffixRegex;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public TemplateParser getParser() {
        return parser;
    }

    public JavaGenerator getGenerator() {
        return generator;
    }
    
    public void run() throws Exception {
        if (parser.getBaseDirectory() == null) {
            throw new Exception("Template/base directory was null");
        }
        
        if (!parser.getBaseDirectory().exists() || !parser.getBaseDirectory().isDirectory()) {
            throw new Exception("Template/base directory does not exist: " + parser.getBaseDirectory());
        }
        
        // loop thru template directory and match templates
        Collection<File> allFiles = RockerUtil.listFileTree(parser.getBaseDirectory());
        for (File f : allFiles) {
            if (f.getName().matches(suffixRegex)) {
                templateFiles.add(f);
            }
        }
        
        log.info("Parsing " + templateFiles.size() + " rocker template files");
        
        int errors = 0;
        int generated = 0;
        
        for (File f : templateFiles) { 
            TemplateModel model = null;
        
            try {
                // parse model
                model = parser.parse(f); 
            } catch (IOException | ParserException e) {
                if (e instanceof ParserException) {
                    ParserException pe = (ParserException)e;
                    log.error("Parsing failed for " + f + ":[" + pe.getLine() + "," + pe.getPosInLine() + "] " + pe.getMessage());
                } else {
                    log.error("Unable to parse template", e);
                }
                errors++;
            }
            
            try {
                File outputFile = generator.generate(model);
                generated++;

                log.debug("Generated java source: " + outputFile);
            } catch (GeneratorException | IOException e) {
                throw new Exception("Generating java source failed for " + f + ": " + e.getMessage(), e);
            }
            
        }
        
        log.info("Generated " + generated + " rocker java source files");
        
        if (errors > 0 && failOnError) {
            throw new Exception("Caught " + errors + " errors.");
        }
    }
    
    static public void main(String[] a) throws Exception {

        JavaGeneratorMain jgm = new JavaGeneratorMain();
        
        // process command-line arguments
        ArrayDeque<String> args = new ArrayDeque<>(a.length);
        args.addAll(Arrays.asList(a));
        while (args.size() > 0) {
            String n = args.poll();
            if (args.isEmpty()) {
                System.err.println("Not enough arguments");
                System.exit(1);
            }
            
            String v = args.poll();
            
            switch (n) {
                case "-t":
                    jgm.parser.setBaseDirectory(new File(v));
                    break;
                case "-o":
                    jgm.generator.setOutputDirectory(new File(v));
                    break;
            }
        }
        
        jgm.run();
    }
}
