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

import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.runtime.RockerRuntime;
import com.fizzed.rocker.model.Argument;
import com.fizzed.rocker.model.BreakStatement;
import com.fizzed.rocker.model.Comment;
import com.fizzed.rocker.model.ContentClosureBegin;
import com.fizzed.rocker.model.ContentClosureEnd;
import com.fizzed.rocker.model.ContinueStatement;
import com.fizzed.rocker.model.ElseBlockBegin;
import com.fizzed.rocker.model.ForBlockBegin;
import com.fizzed.rocker.model.ForBlockEnd;
import com.fizzed.rocker.model.ForStatement;
import com.fizzed.rocker.model.IfBlockBegin;
import com.fizzed.rocker.model.IfBlockEnd;
import com.fizzed.rocker.model.JavaImport;
import com.fizzed.rocker.model.JavaVariable;
import com.fizzed.rocker.model.JavaVersion;
import com.fizzed.rocker.model.PlainText;
import com.fizzed.rocker.model.TemplateModel;
import com.fizzed.rocker.model.TemplateUnit;
import com.fizzed.rocker.model.ValueClosureBegin;
import com.fizzed.rocker.model.ValueClosureEnd;
import com.fizzed.rocker.model.ValueExpression;
import com.fizzed.rocker.runtime.BreakException;
import com.fizzed.rocker.runtime.ContinueException;
import com.fizzed.rocker.runtime.Java8Iterator;
import com.fizzed.rocker.runtime.PlainTextUnloadedClassLoader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joelauer
 */
public class JavaGenerator {
    static private final Logger log = LoggerFactory.getLogger(JavaGenerator.class);
    
    static public final String CRLF = "\r\n";
    static public final String TAB = "    ";
    
    // utf-8 constant string in jvm spec uses short for length -- since chars
    // that require lots of space (e.g. euro symbol) make sure chunk length can be expanded by 4
    static public final int PLAIN_TEXT_CHUNK_LENGTH = 16384;
    
    private final RockerConfiguration configuration;
    //private File outputDirectory;
    private PlainTextStrategy plainTextStrategy;
    
    public JavaGenerator(RockerConfiguration configuration) {
        this.configuration = configuration;
        //this.outputDirectory = RockerConfiguration.getInstance().getOutputDirectory();
        //this.plainTextStrategy = PlainTextStrategy.STATIC_FINAL_STRINGS;
        this.plainTextStrategy = PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS;
    }

    public RockerConfiguration getConfiguration() {
        return configuration;
    }
    
    

    public PlainTextStrategy getPlainTextStrategy() {
        return plainTextStrategy;
    }

    public void setPlainTextStrategy(PlainTextStrategy plainTextStrategy) {
        this.plainTextStrategy = plainTextStrategy;
    }
    
    public File generate(TemplateModel model) throws GeneratorException, IOException {
        if (configuration.getOutputDirectory() == null) {
            throw new NullPointerException("Output dir was null");
        }
        
        if (model == null) {
            throw new NullPointerException("Model was null");
        }
        
        Path outputPath = configuration.getOutputDirectory().toPath();
        
        // append package path
        Path packagePath = RockerUtil.packageNameToPath(model.getPackageName());
        if (packagePath != null) {
            outputPath = outputPath.resolve(packagePath);
        }
        
        File buildDir = outputPath.toFile();
        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }
        
        File outputFile = new File(buildDir, model.getName() + ".java");
        try (Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            createSourceTemplate(model, w);
            w.flush();
        }
        
        return outputFile;
    }
    
    public Writer tab(Writer w, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            w.append(TAB);
        }
        return w;
    }
    
    public String sourceRef(TemplateUnit unit) {
        return new StringBuilder()
            .append("@ ")
            .append("[").append(unit.getSourceRef().getBegin().toString()).append("]")
            .toString();
    }
    
    public String sourceRefLineCommaPosInLine(TemplateUnit unit) {
        return new StringBuilder()
            .append(unit.getSourceRef().getBegin().getLineNumber())
            .append(", ")
            .append(unit.getSourceRef().getBegin().getPosInLine())
            .toString();
    }
    
    public void appendCommentAndSourcePositionUpdate(Writer w, int tab, TemplateUnit unit) throws IOException {
        String unitName = RockerUtil.unqualifiedClassName(unit);
        //tab(w, tab).append("// ").append(unitName).append(sourceRef(unit)).append(CRLF);
        tab(w, tab).append("// ").append(unitName).append(" ").append(sourceRef(unit)).append(CRLF);
        tab(w, tab)
            .append("__internal.aboutToExecutePosInTemplate(")
            .append(sourceRefLineCommaPosInLine(unit))
            .append(");")
            .append(CRLF);
    }
    
    public boolean isForIteratorType(String type) {
        return type != null &&
                (type.equals("ForIterator") || type.equals(com.fizzed.rocker.ForIterator.class.getName()));
    }
    
    public void appendArgumentMembers(TemplateModel model, Writer w, String access, boolean finalModifier, int indent) throws IOException {
        // arguments (including possible RockerBody)
        if (model.getArguments().size() > 0) {
            w.append(CRLF);
            for (Argument arg : model.getArguments()) {
                tab(w, indent).append("// argument ").append(sourceRef(arg)).append(CRLF);
                tab(w, indent)
                    .append(access).append(" ")
                    .append((finalModifier ? "final " : ""))
                    .append(arg.getExternalType()).append(" " + arg.getName()).append(";").append(CRLF);
            }
        }
    }

    // TODO: square's JavaWriter looks like a possible replacement
    private void createSourceTemplate(TemplateModel model, Writer w) throws GeneratorException, IOException {
        // simple increment to help create unique var names
        int varCounter = -1;
        
        if (model.getPackageName() != null && !model.getPackageName().equals("")) {
            w.append("package ").append(model.getPackageName()).append(";").append(CRLF);
        }
       
        // imports regardless of template
        w.append(CRLF);
        w.append("import ").append(java.io.IOException.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.ForIterator.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.RenderingException.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.RockerContent.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.RockerOutput.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.runtime.DefaultRockerTemplate.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.runtime.PlainTextUnloadedClassLoader.class.getName()).append(";").append(CRLF);
        
        // template imports
        if (model.getImports().size() > 0) {
            for (JavaImport i : model.getImports()) {
                w.append("// import ").append(sourceRef(i)).append(CRLF);
                w.append("import ").append(i.getStatement()).append(";").append(CRLF);
            }
        }
        
        w.append(CRLF);
        w.append("/*").append(CRLF);
        w.append(" * Auto generated code to render template ")
                .append(model.getPackageName().replace('.', '/'))
                .append("/")
                .append(model.getTemplateName()).append(CRLF);
        w.append(" * Do not edit this file. Changes will eventually be overwritten by Rocker parser!").append(CRLF);
        w.append(" */").append(CRLF);
        
        
        int indent = 0;
        
        
        // MODEL CLASS
        
        // class definition
        tab(w, indent).append("public class ")
                .append(model.getName())
                .append(" extends ")
                .append(model.getOptions().getExtendsModelClass())
                .append(" {").append(CRLF);
        
        indent++;
        
        w.append(CRLF);
        
        // static info about this template
        tab(w, indent).append("static public final ")
                .append(ContentType.class.getCanonicalName()).append(" CONTENT_TYPE = ")
                .append(ContentType.class.getCanonicalName()).append(".").append(model.getContentType().toString()).append(";").append(CRLF);
        tab(w, indent).append("static public final String TEMPLATE_NAME = \"").append(model.getTemplateName()).append("\";").append(CRLF);
        tab(w, indent).append("static public final String TEMPLATE_PACKAGE_NAME = \"").append(model.getPackageName()).append("\";").append(CRLF);
        tab(w, indent).append("static public final String HEADER_HASH = \"").append(model.createHeaderHash()+"").append("\";").append(CRLF);
        tab(w, indent).append("static public final long MODIFIED_AT = ").append(model.getModifiedAt()+"").append("L;").append(CRLF);
        tab(w, indent).append("static public final String[] ARGUMENT_NAMES = {");
        StringBuilder argNameList = new StringBuilder();
        for (Argument arg : model.getArgumentsWithoutRockerBody()) {
            if (argNameList.length() > 0) { argNameList.append(","); }
            argNameList.append(" \"").append(arg.getExternalName()).append("\"");
        }
        w.append(argNameList).append(" };").append(CRLF);
        

        // model arguments as members of model class
        appendArgumentMembers(model, w, "private", false, indent);
        

        // model setters & getters with builder-style pattern
        // special case for the RockerBody argument which sorta "hides" its getter/setter
        if (model.getArguments().size() > 0) {
            for (Argument arg : model.getArguments()) {
                // setter
                w.append(CRLF);
                tab(w, indent)
                        .append("public ").append(model.getName()).append(" ").append(arg.getExternalName())
                        .append("(" + arg.getExternalType()).append(" ").append(arg.getName())
                        .append(") {").append(CRLF);
                tab(w, indent+1).append("this.").append(arg.getName()).append(" = ").append(arg.getName()).append(";").append(CRLF);
                tab(w, indent+1).append("return this;").append(CRLF);
                tab(w, indent).append("}").append(CRLF);
                
                // getter
                w.append(CRLF);
                tab(w, indent).append("public ").append(arg.getExternalType()).append(" ").append(arg.getExternalName()).append("() {").append(CRLF);
                tab(w, indent+1).append("return this.").append(arg.getName()).append(";").append(CRLF);
                tab(w, indent).append("}").append(CRLF);
            }
        }
        
        w.append(CRLF);
        
        //
        // model "template" static builder
        //
        tab(w, indent).append("static public ").append(model.getName()).append(" template(");
        
        if (model.getArguments().size() > 0) {
            int i = 0;
            // RockerBody is NOT included (it is passed via a closure block in other templates)
            // so we only care about the other arguments
            for (Argument arg : model.getArgumentsWithoutRockerBody()) {
                if (i != 0) { w.append(", "); }
                w.append(arg.getType()).append(" ").append(arg.getName());
                i++;
            }
        }
        w.append(") {").append(CRLF);
        
        tab(w, indent+1).append("return new ").append(model.getName()).append("()");
        if (model.getArguments().size() > 0) {
            int i = 0;
            for (Argument arg : model.getArgumentsWithoutRockerBody()) {
                w.append(CRLF);
                tab(w, indent+2).append(".").append(arg.getName()).append("(").append(arg.getName()).append(")");
                i++;
            }
        }
        w.append(";").append(CRLF);
        tab(w, indent).append("}").append(CRLF);
        
        //
        // render of model
        //
        w.append(CRLF);
              
        
        tab(w, indent).append("@Override").append(CRLF);
        tab(w, indent).append("protected DefaultRockerTemplate buildTemplate() throws RenderingException {").append(CRLF);
        
        if (model.getOptions().getOptimize()) {
            // model "template" static builder (not reloading support, fastest performance)
            tab(w, indent+1).append("// optimized for performance (via rocker.optimize flag; no auto reloading)").append(CRLF);
            tab(w, indent+1).append("return new Template(this);").append(CRLF);
            //tab(w, indent+1).append("return template.__render(context);").append(CRLF);
        } else {
            tab(w, indent+1).append("// optimized for convenience (runtime auto reloading enabled if rocker.reloading=true)").append(CRLF);
            // use bootstrap to create underlying template
            tab(w, indent+1)
                .append("return ")
                .append(RockerRuntime.class.getCanonicalName())
                .append(".getInstance().getBootstrap().template(this.getClass(), this);").append(CRLF);
            //tab(w, indent+1).append("return template.__render(context);").append(CRLF);
        }

        
        tab(w, indent).append("}").append(CRLF);
        
        
        //
        // TEMPLATE CLASS
        //
        
        w.append(CRLF);
        
        // class definition
        tab(w, indent)
            .append("static public class Template extends ")
            .append(model.getOptions().getExtendsClass());
                
        w.append(" {").append(CRLF);
        
        indent++;
        
        
        // plain text -> map of chunks of text (Java only supports 2-byte length of string constant)
        LinkedHashMap<String, LinkedHashMap<String,String>> plainTextMap
                = model.createPlainTextMap(PLAIN_TEXT_CHUNK_LENGTH);
        
        if (!plainTextMap.isEmpty()) {
            
            w.append(CRLF);
            
            for (String plainText : plainTextMap.keySet()) {
                
                // include static text as comments in source (limit to 500)
                tab(w, indent).append("// ")
                        .append(StringUtils.abbreviate(RockerUtil.ESCAPE_JAVA.translate(plainText), 500)).append(CRLF);
                for (Map.Entry<String,String> chunk : plainTextMap.get(plainText).entrySet()) {
                
                    if (this.plainTextStrategy == PlainTextStrategy.STATIC_STRINGS) {
                        tab(w, indent).append("static private final String ")
                            .append(chunk.getKey())
                            .append(" = \"")
                            .append( StringEscapeUtils.escapeJava( Boolean.TRUE == model.getOptions().getRemoveWhitespace() ?
                                RockerUtil.removeWhitespace(chunk.getValue()) :
                                chunk.getValue()) )
                            .append("\";")
                            .append(CRLF);
                    }
                    else if (this.plainTextStrategy == PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS) {
                        tab(w, indent).append("static private final byte[] ")
                            .append(chunk.getKey())
                            .append(";")
                            .append(CRLF);
                    }
 
                }
            }
            
            // generate the static initializer
            if (this.plainTextStrategy == PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS) {
                
                w.append(CRLF);
                
                tab(w, indent).append("static {").append(CRLF);
                
                String loaderClassName = RockerUtil.unqualifiedClassName(PlainTextUnloadedClassLoader.class);
                tab(w, indent+1).append(loaderClassName)
                        .append(" loader = ")
                        .append(loaderClassName)
                        .append(".tryLoad(")
                        .append(model.getName())
                        .append(".class.getName()")
                        .append(" + \"$PlainText\", \"")
                        .append(model.getOptions().getTargetCharset())
                        .append("\");")
                        .append(CRLF);
                
                for (String plainText : plainTextMap.keySet()) {
                
                    for (Map.Entry<String,String> chunk : plainTextMap.get(plainText).entrySet()) {

                        if (this.plainTextStrategy == PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS) {
                            tab(w, indent+1).append(chunk.getKey())
                                .append(" = loader.tryGet(\"")
                                .append(chunk.getKey())
                                .append("\");")
                                .append(CRLF);
                        }

                    }
                    
                }
                
                tab(w, indent).append("}").append(CRLF);
            }
            
        }
        
        
        // arguments as members of template class
        appendArgumentMembers(model, w, "protected", true, indent);
        
        w.append(CRLF);

        // constructor
        tab(w, indent).append("public Template(").append(model.getName()).append(" model) {").append(CRLF);
        
        tab(w, indent+1).append("super(model);").append(CRLF);
        tab(w, indent+1).append("__internal.setCharset(\"").append(model.getOptions().getTargetCharset()).append("\");").append(CRLF);
        tab(w, indent+1).append("__internal.setContentType(CONTENT_TYPE);").append(CRLF);
        tab(w, indent+1).append("__internal.setTemplateName(TEMPLATE_NAME);").append(CRLF);
        tab(w, indent+1).append("__internal.setTemplatePackageName(TEMPLATE_PACKAGE_NAME);").append(CRLF);
        
        // each model argument passed along as well
        for (Argument arg : model.getArguments()) {
            tab(w, indent+1).append("this.").append(arg.getName()).append(" = model.").append(arg.getExternalName()).append("();").append(CRLF);
        }
        
        tab(w, indent).append("}").append(CRLF);
        
        
        w.append(CRLF);
        
        tab(w, indent).append("@Override").append(CRLF);
        tab(w, indent).append("protected void __doRender() throws IOException, RenderingException {").append(CRLF);
        
        
        // build rendering code
        int depth = 1;
        Deque<String> blockEnd = new ArrayDeque<>();
        
        for (TemplateUnit unit : model.getUnits()) {
            if (unit instanceof Comment) {
                continue;
            }
            
            // something like
            // IfBeginBlock
            // __internal.aboutToExecutePosInSourceTemplate(5, 10);
            appendCommentAndSourcePositionUpdate(w, depth+indent, unit);
            
            if (unit instanceof PlainText) {
                PlainText plain = (PlainText)unit;
 
                LinkedHashMap<String,String> chunks = plainTextMap.get(plain.getText());

                for (String chunkName : chunks.keySet()) {

                    tab(w, depth+indent)
                        .append("__internal.writeValue(").append(chunkName).append(");").append(CRLF);

                }
                
            }
            else if (unit instanceof ValueExpression) {
                ValueExpression value = (ValueExpression)unit;
                tab(w, depth+indent)
                        .append("__internal.renderValue(")
                        .append(value.getExpression())
                        .append(");").append(CRLF);
            }
            else if (unit instanceof ValueClosureBegin) {
                
                ValueClosureBegin closure = (ValueClosureBegin)unit;
                tab(w, depth+indent)
                        .append("__internal.renderValue(")
                        .append(closure.getExpression())
                        .append(".__body(");
                
                // Java 1.8+ use lambda
                if (model.getOptions().isGreaterThanOrEqualToJavaVersion(JavaVersion.v1_8)) {
                    w.append("() -> {").append(CRLF);
                    
                    depth++;
                    
                    blockEnd.push("}));");
                }
                // Java 1.7- uses anonymous inner class
                else {
                    w.append("new ")
                        .append(RockerUtil.unqualifiedClassName(com.fizzed.rocker.RockerContent.class))
                        .append("() {").append(CRLF);
                
                    depth++;
                    
                    blockEnd.push("}));");
                
                    tab(w, depth+indent)
                            .append("@Override")
                            .append(CRLF);
                
                    tab(w, depth+indent)
                            .append("public void render() throws IOException, RenderingException {")
                            .append(CRLF);
                
                    depth++;
                    
                    blockEnd.push("}");
                }
            }
            else if (unit instanceof ValueClosureEnd) {
                // Java 1.8+ use lambda
                if (model.getOptions().isGreaterThanOrEqualToJavaVersion(JavaVersion.v1_8)) {
                    depth--;
                    
                    tab(w, depth+indent)
                        .append(blockEnd.pop())
                        .append(" // value closure end ").append(sourceRef(unit)).append(CRLF);
                }
                // Java 1.7- uses anonymous inner class
                else {
                    depth--;
                
                    tab(w, depth+indent)
                            .append(blockEnd.pop())
                            .append(CRLF);

                    depth--;

                    tab(w, depth+indent)
                            .append(blockEnd.pop())
                            .append(" // value closure end ").append(sourceRef(unit)).append(CRLF);
                }
            }
            else if (unit instanceof ContentClosureBegin) {
                
                ContentClosureBegin closure = (ContentClosureBegin)unit;
                tab(w, depth+indent)
                        .append("RockerContent ")
                        .append(closure.getIdentifier())
                        .append(" = ");
                
                // Java 1.8+ use lambda
                if (model.getOptions().isGreaterThanOrEqualToJavaVersion(JavaVersion.v1_8)) {
                    w.append("() -> {").append(CRLF);
                    
                    depth++;
                    
                    blockEnd.push("};");
                }
                // Java 1.7- uses anonymous inner class
                else {
                    w.append("new ")
                        .append(RockerUtil.unqualifiedClassName(com.fizzed.rocker.RockerContent.class))
                        .append("() {").append(CRLF);
                
                    depth++;
                    
                    blockEnd.push("};");
                
                    tab(w, depth+indent)
                            .append("@Override")
                            .append(CRLF);
                
                    tab(w, depth+indent)
                            .append("public void render() throws IOException, RenderingException {")
                            .append(CRLF);
                
                    depth++;
                    
                    blockEnd.push("}");
                }
            }
            else if (unit instanceof ContentClosureEnd) {
                // Java 1.8+ use lambda
                if (model.getOptions().isGreaterThanOrEqualToJavaVersion(JavaVersion.v1_8)) {
                    depth--;
                    
                    tab(w, depth+indent)
                        .append(blockEnd.pop())
                        .append(" // content closure end ").append(sourceRef(unit)).append(CRLF);
                }
                // Java 1.7- uses anonymous inner class
                else {
                    depth--;
                
                    tab(w, depth+indent)
                            .append(blockEnd.pop())
                            .append(CRLF);

                    depth--;

                    tab(w, depth+indent)
                            .append(blockEnd.pop())
                            .append(" // content closure end ").append(sourceRef(unit)).append(CRLF);
                }
            }
            else if (unit instanceof IfBlockBegin) {
                IfBlockBegin block = (IfBlockBegin)unit;
                
                tab(w, depth+indent)
                        .append("if ")
                        .append(block.getExpression())
                        .append(" {").append(CRLF);
                
                blockEnd.push("}");
                depth++;
            }
            else if (unit instanceof ElseBlockBegin) {
                depth--;
                
                tab(w, depth+indent)
                        .append("} else {")
                        .append(" // else ").append(sourceRef(unit)).append(CRLF);
                
                depth++;
            }
            else if (unit instanceof IfBlockEnd) {
                depth--;
                
                tab(w, depth+indent)
                        .append(blockEnd.pop())
                        .append(" // if end ").append(sourceRef(unit)).append(CRLF);
                
            }
            else if (unit instanceof ForBlockBegin) {
                ForBlockBegin block = (ForBlockBegin)unit;
                ForStatement stmt = block.getStatement();
                
                // break support via try and catch mechanism (works across lambdas!)
                tab(w, depth+indent)
                    .append("try {").append(CRLF);
                
                depth++;
                
                if (stmt.getForm() == ForStatement.Form.GENERAL) {
                    // print out raw statement including parentheses
                    tab(w, depth+indent)
                        .append("for ")
                        .append(block.getExpression())
                        .append(" {").append(CRLF);
                    
                    blockEnd.push("}");
                }
                else if (stmt.getForm() == ForStatement.Form.ENHANCED) {
                    // Java 1.8+ (use lambdas)
                    if (stmt.hasAnyUntypedArguments() &&
                            model.getOptions().isGreaterThanOrEqualToJavaVersion(JavaVersion.v1_8)) {
                        
                        // build list of lambda vars
                        String localVars = "";
                        for (JavaVariable arg : stmt.getArguments()) {
                            if (localVars.length() != 0) { localVars += ","; }
                            localVars += arg.getName();
                        }
                        
                        tab(w, depth+indent)
                            .append(Java8Iterator.class.getName())
                            .append(".forEach(")
                            .append(stmt.getValueExpression())
                            .append(", (").append(localVars).append(") -> {").append(CRLF);
                        
                        blockEnd.push("});");
                    }
                    else {
                    
                        // is the first argument a "ForIterator" ?
                        boolean forIterator = isForIteratorType(stmt.getArguments().get(0).getType());
                        int collectionCount = (forIterator ? 2 : 1);
                        int mapCount = (forIterator ? 3 : 2);

                        // type and value we are going to iterate thru
                        String iterateeType = null;
                        String valueExpression = null;
                        if (stmt.getArguments().size() == collectionCount) {
                            iterateeType = stmt.getArguments().get(collectionCount - 1).getTypeAsNonPrimitiveType();
                            valueExpression = stmt.getValueExpression();
                        }
                        else if (stmt.getArguments().size() == mapCount) {
                            iterateeType = "java.util.Map.Entry<"
                                    + stmt.getArguments().get(mapCount - 2).getTypeAsNonPrimitiveType()
                                    + ","
                                    + stmt.getArguments().get(mapCount - 1).getTypeAsNonPrimitiveType()
                                    + ">";
                            valueExpression = stmt.getValueExpression() + ".entrySet()";
                        }

                        // create unique variable name for iterator
                        String forIteratorVarName = "__forIterator" + (++varCounter);

                        // ForIterator for collection
                        tab(w, depth+indent)
                            .append(com.fizzed.rocker.runtime.CollectionForIterator.class.getName())
                            .append("<").append(iterateeType).append(">")
                            .append(" ")
                            .append(forIteratorVarName)
                            .append(" = new ")
                            .append(com.fizzed.rocker.runtime.CollectionForIterator.class.getName())
                            .append("<").append(iterateeType).append(">")
                            .append("(")
                            .append(valueExpression)
                            .append(");")
                            .append(CRLF);

                        // for loop same regardless of map vs. collection
                        tab(w, depth+indent)
                            .append("while (")
                            .append(forIteratorVarName)
                            .append(".hasNext()) {")
                            .append(CRLF);

                        // if forIterator request assign to local var
                        if (forIterator) {
                            tab(w, depth+indent+1)
                                .append(com.fizzed.rocker.ForIterator.class.getName())
                                .append(" ")
                                .append(stmt.getArguments().get(0).getName())
                                .append(" = ")
                                .append(forIteratorVarName)
                                .append(";")
                                .append(CRLF);
                        }

                        if (stmt.getArguments().size() == collectionCount) {
                            // assign item to local var
                            tab(w, depth+indent+1)
                                .append(stmt.getArguments().get(collectionCount - 1).toString())
                                .append(" = ")
                                .append(forIteratorVarName)
                                .append(".next();")
                                .append(CRLF);
                        }
                        else if (stmt.getArguments().size() == mapCount) {
                            // create unique variable name for iterator
                            String entryVarName = "__entry" + (++varCounter);

                            // assign map entry to local var
                            tab(w, depth+indent+1)
                                .append(iterateeType)
                                .append(" ")
                                .append(entryVarName)
                                .append(" = ")
                                .append(forIteratorVarName)
                                .append(".next();")
                                .append(CRLF);

                            // assign entry to local values
                            tab(w, depth+indent+1)
                                .append(stmt.getArguments().get(mapCount - 2).toString())
                                .append(" = ").append(entryVarName).append(".getKey();").append(CRLF);

                            tab(w, depth+indent+1)
                                .append(stmt.getArguments().get(mapCount - 1).toString())
                                .append(" = ").append(entryVarName).append(".getValue();").append(CRLF);
                        }
                        else {
                            throw new GeneratorException("Unsupported number of arguments for for loop");
                        }
                        
                        blockEnd.push("}");
                    }
                }
                
                depth++;
                
                // continue support via try and catch mechanism (works across lambdas!)
                tab(w, depth+indent)
                    .append("try {").append(CRLF);
                
                 depth++;
                
            }
            else if (unit instanceof ForBlockEnd) {
                depth--;
                
                // continue support via try and catch mechanism (works across lambdas!)
                tab(w, depth+indent)
                    .append("} catch (").append(ContinueException.class.getCanonicalName()).append(" e) {") .append(CRLF);
                
                tab(w, depth+indent+1)
                    .append("// support for continuing for loops").append(CRLF);

                tab(w, depth+indent)
                    .append("}").append(CRLF);
                
                
                
                depth--;
                
                tab(w, depth+indent)
                        .append(blockEnd.pop())
                        .append(" // for end ").append(sourceRef(unit)).append(CRLF);
                
                
                
                depth--;
                
                // break support via try and catch mechanism (works across lambdas!)
                tab(w, depth+indent)
                    .append("} catch (").append(BreakException.class.getCanonicalName()).append(" e) {") .append(CRLF);
                
                tab(w, depth+indent+1)
                    .append("// support for breaking for loops").append(CRLF);

                tab(w, depth+indent)
                    .append("}").append(CRLF);
            }
            else if (unit instanceof BreakStatement) {
                tab(w, depth+indent)
                        .append("__internal.throwBreakException();").append(CRLF);
            }
            else if (unit instanceof ContinueStatement) {
                tab(w, depth+indent)
                        .append("__internal.throwContinueException();").append(CRLF);
            }
            //log.info(" src (@ {}): [{}]", unit.getSourceRef(), unit.getSourceRef().getConsoleFriendlyText());
        }
        
        // end of render()
        tab(w, indent).append("}").append(CRLF);
        
        indent--;
        
        // end of template class
        tab(w, indent).append("}").append(CRLF);
        
        
        if (this.plainTextStrategy == PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS &&
                !plainTextMap.isEmpty()) {
            
            w.append(CRLF);
            
            tab(w, indent).append("private static class PlainText {").append(CRLF);
            w.append(CRLF);
            
            for (String plainText : plainTextMap.keySet()) {

                for (Map.Entry<String,String> chunk : plainTextMap.get(plainText).entrySet()) {
                    tab(w, indent+1).append("static private final String ")
                        .append(chunk.getKey())
                        .append(" = \"")
                        .append( StringEscapeUtils.escapeJava( 
                            Boolean.TRUE == model.getOptions().getRemoveWhitespace() ?
                                RockerUtil.removeWhitespace(chunk.getValue()) : 
                                chunk.getValue()))
                        .append("\";")
                        .append(CRLF);
                    
                }

            }
            
            w.append(CRLF);
            tab(w, indent).append("}").append(CRLF);
        }
        
        
        
        w.append(CRLF);
        w.append("}").append(CRLF);
    }
    
}
