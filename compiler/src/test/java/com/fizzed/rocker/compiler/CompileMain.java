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

import com.fizzed.rocker.RockerTemplate;
import com.fizzed.rocker.model.TemplateModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class CompileMain {
    static private final Logger log = LoggerFactory.getLogger(CompileMain.class);
    
    static public void main(String[] args) throws Exception {
        
        //File templateFile = new File("compiler/src/test/resources/rocker/parser/NoHeader.rocker.html");
        File templateFile = new File("compiler/src/test/resources/rocker/parser/UnescapeAtAt.rocker.html");
        
        
        while (true) {
        
            compile(templateFile);

            Class<RockerTemplate> templateClass;

            ReloadableClassLoader rcl = new ReloadableClassLoader(CompileMain.class.getClassLoader());

            templateClass = rcl.loadClass("rocker.parser.UnescapeAtAt");

            RockerTemplate template = templateClass.newInstance();

            String out = template.render().toString();

            System.out.println("render: " + out);
        
            System.out.println("waitng for input for next render...");
            System.in.read();
            
        }
        
        
    }
    
    
    static public void compile(File templateFile) throws Exception {
        
        // build classpath...
        StringBuilder buffer = new StringBuilder();
        for (URL url :
            ((URLClassLoader) (Thread.currentThread()
                .getContextClassLoader())).getURLs()) {
          buffer.append(new File(url.getPath()));
          buffer.append(File.pathSeparator);
        }
        String classpath = buffer.toString();
        
        List<String> javacOptions = new ArrayList<>();
        javacOptions.add("-cp");
        javacOptions.add(classpath);
        
        // directory output to
        javacOptions.add("-d");
        javacOptions.add("compiler/target/test-classes");
        
        
        
        
        
        //
        // template -> model
        //
        TemplateParser parser = new TemplateParser();
        
        parser.setBaseDirectory(new File("compiler/src/test/resources"));
        
        
        TemplateModel model = parser.parse(templateFile);

        
        //
        // model -> java
        //
        JavaGenerator generator = new JavaGenerator();
        
        generator.setOutputDirectory(new File("compiler/target/test-classes"));
        
        File javaFile = generator.generate(model);
        
        //String javaSource = IOUtils.toString(new FileInputStream(javaFile));
        
        
        //
        // java -> class
        //
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        
        Iterable<? extends JavaFileObject> compilationUnits =
            fileManager.getJavaFileObjectsFromFiles(Arrays.asList(javaFile));
        
        
        
        CompilationTask task = compiler.getTask(null, null, diagnostics, javacOptions, null, compilationUnits);

        boolean success = task.call();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            System.out.println(diagnostic.getCode());
            System.out.println(diagnostic.getKind());
            System.out.println(diagnostic.getPosition());
            System.out.println(diagnostic.getStartPosition());
            System.out.println(diagnostic.getEndPosition());
            System.out.println(diagnostic.getSource());
            System.out.println(diagnostic.getMessage(null));

        }
        System.out.println("Success: " + success);
        
        /**
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        compiler.
        */
        
        
        /**
        Class<?> helloClass = InMemoryJavaCompiler.compile("rocker.parser.NoHeader", javaSource);
        
        System.out.println("class: " + helloClass.getName());
        */
        
        /**
        ASTParser jdtParser = ASTParser.newParser(AST.JLS3);
      
        jdtParser.setSource(javaSource.toCharArray());

        jdtParser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit)jdtParser.createAST(null);

        cu.accept(new ASTVisitor() {

            Set names = new HashSet();

            @Override
            public boolean visit(LineComment node) {
                
                System.out.println("line comment: " + node.toString());
                
                return false;
            }

            
            
            @Override
            public boolean visit(VariableDeclarationFragment node) {
                
                SimpleName name = node.getName();
                this.names.add(name.getIdentifier());
                System.out.println("Declaration of '" + name + "' at line" + cu.getLineNumber(name.getStartPosition()));
                
                return false; // do not continue to avoid usage info
            }

            @Override
            public boolean visit(SimpleName node) {
                
                System.out.println("FQDN: " + node.getFullyQualifiedName());
                
                System.out.println("Usage of '" + node + "' at line " + cu.getLineNumber(node.getStartPosition()));
                
                return true;
            }

        });
        */
    }
    
    
    static public class ReloadableClassLoader extends ClassLoader{

        public ReloadableClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class loadClass(String name) throws ClassNotFoundException {
            
            if (!name.startsWith("rocker")) {
                return super.loadClass(name);
            }

            try {
                URL myUrl = new File("compiler/target/test-classes/" + name.replace(".", "/") + ".class").toURI().toURL();
                System.out.println("Loading: " + myUrl);
                URLConnection connection = myUrl.openConnection();
                
                
                InputStream input = connection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int data = input.read();

                while(data != -1){
                    buffer.write(data);
                    data = input.read();
                }

                input.close();

                byte[] classData = buffer.toByteArray();

                return defineClass(name, classData, 0, classData.length);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    }
    
}
