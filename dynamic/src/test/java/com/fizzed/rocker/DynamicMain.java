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
package com.fizzed.rocker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class DynamicMain {
    static private final Logger log = LoggerFactory.getLogger(DynamicMain.class);
    
    static public void main(String[] args) throws Exception {
        
        //File templateFile = new File("compiler/src/test/resources/rocker/parser/NoHeader.rocker.html");

        /**
        TemplateCompiler compiler = new TemplateCompiler()
                .setTemplateBaseDirectory(new File("compiler/src/test/java"))
                .setJavaGenerateDirectory(new File("compiler/target/generated-test-sources/rocker"))
                .setOutputDirectory(new File("compiler/target/test-classes"));

        File templateFile = new File("compiler/src/test/java/com/fizzed/rocker/compiler/Compile.rocker.html");
        */
        
        while (true) {

            String out = views.index.template("Home", "Joe")
                    .render()
                    .toString();
            
            System.out.println("render: " + out);
        
            System.out.println("waitng for input for next render...");
            
            System.in.read();
            
            //List<TemplateModel> templateModels = compiler.parseTemplates(Arrays.asList(templateFile));

            //List<File> javaFiles = compiler.generateJavaFiles(templateModels);

            //List<File> javaFiles = Arrays.asList(
                    //new File("compiler/target/generated-test-sources/rocker/com/fizzed/rocker/compiler/Compile2.java")
            //        new File("compiler/src/test/java/com/fizzed/rocker/compiler/DynamicTemplate.java")
            //);

            //compiler.compileJavaFiles(javaFiles);


            //compile(templateFile);



            //Class<RockerTemplate> templateClass;

            //ReloadableClassLoader rcl = new ReloadableClassLoader(CompileMain.class.getClassLoader());

            //templateClass = rcl.loadClass("com.fizzed.rocker.compiler.DynamicTemplate");

            //DynamicTemplate template = DynamicTemplate.template("test");
            /**
            DynamicTemplate template = new DynamicTemplate().val("test");
            
            String out = template.render().toString();

            
            */
            
        }
        
        
    }
    
}
