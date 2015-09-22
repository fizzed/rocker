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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.*;

/**
 *
 * @author joelauer
 */
public class TemplateCompiler {
    private static final Logger log = LoggerFactory.getLogger(TemplateCompiler.class);

    // directory with templates
    private File templateBaseDirectory;

    // directory compiled classes will be output to
    private File javaGenerateDirectory;

    // directory compiled classes will be output to
    private File outputDirectory;

    public TemplateCompiler() {
        
    }

    public File getTemplateBaseDirectory() {
        return templateBaseDirectory;
    }

    public TemplateCompiler setTemplateBaseDirectory(File templateBaseDirectory) {
        this.templateBaseDirectory = templateBaseDirectory;
        return this;
    }

    public File getJavaGenerateDirectory() {
        return javaGenerateDirectory;
    }

    public TemplateCompiler setJavaGenerateDirectory(File javaGenerateDirectory) {
        this.javaGenerateDirectory = javaGenerateDirectory;
        return this;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public TemplateCompiler setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public List<TemplateModel> parseTemplates(List<File> templateFiles) throws Exception {
        //
        // template -> model
        //
        TemplateParser parser = new TemplateParser();

        parser.setBaseDirectory(templateBaseDirectory);

        List<TemplateModel> models = new ArrayList<>();

        for (File templateFile : templateFiles) {
            TemplateModel model = parser.parse(templateFile);
            models.add(model);
        }

        return models;
    }

    public List<File> generateJavaFiles(List<TemplateModel> models) throws Exception {
        //
        // model -> java
        //
        JavaGenerator generator = new JavaGenerator();

        generator.setOutputDirectory(this.javaGenerateDirectory);

        List<File> javaFiles = new ArrayList<>();

        for (TemplateModel model : models) {
            File javaFile = generator.generate(model);
            javaFiles.add(javaFile);
        }

        return javaFiles;
    }


    public void compileJavaFiles(List<File> javaFiles) throws Exception {
        //
        // build javac options
        //

        // under maven or other build tools, java.class.path is wrong
        // build our own from current context
        StringBuilder classpath = new StringBuilder();
        URL[] classpathUrls = ((URLClassLoader)(Thread.currentThread().getContextClassLoader())).getURLs();
        for (URL url : classpathUrls) {
            if (classpath.length() > 0) {
                classpath.append(File.pathSeparator);
            }
            classpath.append(new File(url.toURI()).getAbsolutePath());
        }

        List<String> javacOptions = new ArrayList<>();

        // classpath to compile templates with
        javacOptions.add("-classpath");
        javacOptions.add(classpath.toString());

        // directory to output compiles classes
        javacOptions.add("-d");
        javacOptions.add(outputDirectory.getAbsolutePath());

        javacOptions.add("-Xlint:unchecked");
        
        //
        // java -> class
        //
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(javaFiles);

        JavaCompiler.CompilationTask task
                = compiler.getTask(null, null, diagnostics, javacOptions, null, compilationUnits);

        boolean success = task.call();

        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            log.debug("code: {}", diagnostic.getCode());
            log.debug("kind: {}", diagnostic.getKind());
            log.debug("line num: {}", diagnostic.getLineNumber());
            log.debug("col num: {}", diagnostic.getColumnNumber());
            log.debug("pos: {}", diagnostic.getPosition());
            log.debug("start pos: {}", diagnostic.getStartPosition());
            log.debug("end pos: {}", diagnostic.getEndPosition());
            log.debug("source: {}", diagnostic.getSource());
            log.debug("message: {}", diagnostic.getMessage(null));
        }

        log.debug("Success: " + success);
    }
    
}
