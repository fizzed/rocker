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
package com.fizzed.rocker.bin;

import com.fizzed.rocker.compiler.JavaGenerator;
import com.fizzed.rocker.compiler.RockerConfiguration;
import com.fizzed.rocker.compiler.TemplateParser;
import static com.fizzed.rocker.bin.ParserMain.parse;
import com.fizzed.rocker.model.TemplateModel;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateMain {
    static private final Logger log = LoggerFactory.getLogger(GenerateMain.class);
    
    static public void main(String[] args) throws Exception {
        String rockerFile = System.getProperty("rocker.file");
        
        RockerConfiguration configuration = new RockerConfiguration();
        
        configuration.setTemplateDirectory(new File("."));
        configuration.setOutputDirectory(new File("target/generated-sources"));
        
        TemplateParser parser = new TemplateParser(configuration);
        
        File templateFile = new File(rockerFile);
        
        TemplateModel model = parse(parser, templateFile);
        
        JavaGenerator generator = new JavaGenerator(configuration);
        
        File sourceFile = generator.generate(model);
        
        log.info("Parsed template {}", templateFile);
        log.info("Generated source {}", sourceFile);
    }
}
