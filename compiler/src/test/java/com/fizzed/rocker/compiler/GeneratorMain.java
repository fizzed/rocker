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

import static com.fizzed.rocker.compiler.ParserMain.logModel;
import static com.fizzed.rocker.compiler.ParserMain.parse;
import com.fizzed.rocker.model.TemplateModel;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class GeneratorMain {
    static private final Logger log = LoggerFactory.getLogger(GeneratorMain.class);
    
    static public void main(String[] args) throws Exception {
        TemplateParser parser = new TemplateParser();
        parser.setBaseDirectory(new File("compiler/src/test/resources"));
        
        File f = new File("compiler/src/test/resources/rocker/parser/LauerMain.rocker.html");
        
        TemplateModel model = parse(parser, f);
        
        logModel(model);
        
        JavaGenerator generator = new JavaGenerator();
        
        generator.setOutputDirectory(new File("compiler/target/generated-test-sources/rocker"));
        
        File sourceFile = generator.generate(model);
    }
}
