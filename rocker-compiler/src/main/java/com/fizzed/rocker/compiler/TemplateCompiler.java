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

import com.fizzed.rocker.runtime.ParserException;
import com.fizzed.rocker.model.SourcePosition;
import com.fizzed.rocker.runtime.CompileDiagnostic;
import com.fizzed.rocker.runtime.CompileDiagnosticException;
import com.fizzed.rocker.runtime.CompileUnrecoverableException;
import com.fizzed.rocker.model.TemplateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.*;
import javax.tools.Diagnostic.Kind;

/**
 *
 * @author joelauer
 */
public class TemplateCompiler {
    private static final Logger log = LoggerFactory.getLogger(TemplateCompiler.class);

    private final RockerConfiguration configuration;

    public TemplateCompiler(RockerConfiguration configuration) {
        this.configuration = configuration;
    }
    
    static public class CompilationUnit {
        // parse
        private File templateFile;
        private TemplateModel templateModel;
        // generate
        private File javaFile;

        public File getTemplateFile() {
            return templateFile;
        }

        public TemplateModel getTemplateModel() {
            return templateModel;
        }

        public File getJavaFile() {
            return javaFile;
        }
        
    }

    public List<CompilationUnit> parse(List<File> templateFiles) throws ParserException, IOException {
        //
        // template -> model
        //
        TemplateParser parser = new TemplateParser(this.configuration);

        List<CompilationUnit> units = new ArrayList<>();

        for (File templateFile : templateFiles) {
            TemplateModel model = parser.parse(templateFile);
            CompilationUnit unit = new CompilationUnit();
            unit.templateFile = templateFile;
            unit.templateModel = model;
            units.add(unit);
        }

        return units;
    }

    public void generate(List<CompilationUnit> units) throws GeneratorException, IOException {
        //
        // model -> java
        //
        JavaGenerator generator = new JavaGenerator(this.configuration);

        for (CompilationUnit unit : units) {
            unit.javaFile = generator.generate(unit.templateModel);
        }
    }


    public void compile(List<CompilationUnit> units) throws CompileUnrecoverableException, CompileDiagnosticException {
        //
        // build javac options
        //

        // under maven or other build tools, java.class.path is wrong
        // build our own from current context
        // todo: there doesn't seem to be any test checking this assumption is correct.
        StringBuilder classpath = new StringBuilder();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        // starting in java 9, the class loader isn't necessarily an instance of URLClassLoader
        URL[] classpathUrls = new URL[0];
        if (contextClassLoader instanceof URLClassLoader) {
            classpathUrls = ((URLClassLoader) contextClassLoader).getURLs();
        } else {
            classpath.append(System.getProperty("java.class.path"));
        }
        for (URL url : classpathUrls) {
            if (classpath.length() > 0) {
                classpath.append(File.pathSeparator);
            }
            
            try {
                classpath.append(new File(url.toURI()).getAbsolutePath());
            } catch (Exception e) {
                throw new CompileUnrecoverableException("Unable to build javac classpath", e);
            }
        }
        
        // make sure compile directory exists
        this.configuration.getClassDirectory().mkdirs();
        

        List<String> javacOptions = new ArrayList<>();

        // classpath to compile templates with
        javacOptions.add("-classpath");
        javacOptions.add(classpath.toString());

        // directory to output compiles classes
        javacOptions.add("-d");
        javacOptions.add(this.configuration.getClassDirectory().getAbsolutePath());

        javacOptions.add("-Xlint:unchecked");
        
        
        //
        // java -> class
        //
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Map<File,CompilationUnit> unitsByJavaFile = new HashMap<>();
        for (CompilationUnit unit : units) {
            unitsByJavaFile.put(unit.javaFile.getAbsoluteFile(), unit);
        }
        
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(unitsByJavaFile.keySet());

        JavaCompiler.CompilationTask task
                = compiler.getTask(null, null, diagnostics, javacOptions, null, compilationUnits);

        boolean success = task.call();

        int errors = 0;
        
        List<CompileDiagnostic> cds = new ArrayList<>();
        
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            // file:/home/joelauer/workspace/fizzed/java-rocker/reloadtest/target/generated-test-sources/rocker/views/index.java
            JavaFileObject jfo = (JavaFileObject)diagnostic.getSource();
            //log.debug("java file: {}", jfo.toUri());
            //log.debug("source: {}", diagnostic.getSource());
            //log.debug("line num: {}", diagnostic.getLineNumber());
            //log.debug("col num: {}", diagnostic.getColumnNumber());
            //log.debug("code: {}", diagnostic.getCode());
            //log.debug("kind: {}", diagnostic.getKind());
            //log.debug("pos: {}", diagnostic.getPosition());
            //log.debug("start pos: {}", diagnostic.getStartPosition());
            //log.debug("end pos: {}", diagnostic.getEndPosition());
            //log.debug("message: {}", diagnostic.getMessage(null));
            
            if (diagnostic.getKind() == Kind.ERROR) {
                File javaFile = new File(jfo.toUri()).getAbsoluteFile();
                
                CompilationUnit unit = unitsByJavaFile.get(javaFile);
                
                int templateLineNumber = -1;
                int templateColumnNumber = -1;
                
                try {
                    // check if we can find the correlating template line & col
                    SourcePosition sourcePos = JavaSourceUtil.findSourcePosition(
                                                        javaFile,
                                                        (int)diagnostic.getLineNumber(),
                                                        (int)diagnostic.getColumnNumber());



                    if (sourcePos != null) {
                        templateLineNumber = sourcePos.getLineNumber();
                        templateColumnNumber = sourcePos.getPosInLine();
                    }
                } catch (IOException e) {
                    // do nothing
                }
                
                CompileDiagnostic cd = new CompileDiagnostic(
                        unit.templateFile, javaFile,
                        templateLineNumber, templateColumnNumber,
                        diagnostic.getLineNumber(), diagnostic.getColumnNumber(),
                        diagnostic.getMessage(null));
                
                cds.add(cd);
                
                errors++;
            }
        }
        
        /**
         [ERROR] /home/joelauer/workspace/fizzed/java-ninja-rocker/demo/target/generated-sources/rocker/views/index.java:[87,40] method template in class views.main cannot be applied to given types;
  required: java.lang.String,java.lang.String
  found: java.lang.String
  reason: actual and formal argument lists differ in length
         */
        
        if (!success || errors > 0) {
            // build large message of the errors
            StringBuilder sb = new StringBuilder();
        
            sb.append("Unable to compile rocker template(s) with ").append(errors).append(" errors.");
            
            for (CompileDiagnostic cd : cds) {
                sb.append("\r\n");
                
                sb.append("[ERROR] ").append(cd.getTemplateFile());
                if (cd.getTemplateLineNumber() >= 0) {
                    sb.append(":[").append(cd.getTemplateLineNumber()).append(",").append(cd.getTemplateColumnNumber()).append("] ");
                }
                sb.append("\r\n");
                
                sb.append("  java: ").append(cd.getJavaFile()).append(":[").append(cd.getJavaLineNumber()).append(",").append(cd.getJavaColumnNumber()).append("] ");
                sb.append(cd.getMessage().trim());
            }
            
            log.warn("{}", sb);
        
            throw new CompileDiagnosticException(sb.toString(), cds);
        }
    }
    
}
