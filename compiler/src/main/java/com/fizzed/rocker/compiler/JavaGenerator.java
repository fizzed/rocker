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

import com.fizzed.rocker.model.Argument;
import com.fizzed.rocker.model.Comment;
import com.fizzed.rocker.model.ContentClosureBegin;
import com.fizzed.rocker.model.ContentClosureEnd;
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
    
    private File outputDirectory;
    private PlainTextStrategy plainTextStrategy;
    
    public JavaGenerator() {
        this.outputDirectory = RockerConfigurationKeys.getGeneratorOutputDirectory();
        //this.plainTextStrategy = PlainTextStrategy.STATIC_FINAL_STRINGS;
        this.plainTextStrategy = PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public PlainTextStrategy getPlainTextStrategy() {
        return plainTextStrategy;
    }

    public void setPlainTextStrategy(PlainTextStrategy plainTextStrategy) {
        this.plainTextStrategy = plainTextStrategy;
    }
    
    public File generate(TemplateModel model) throws GeneratorException, IOException {
        if (this.outputDirectory == null) {
            throw new NullPointerException("Output dir was null");
        }
        
        if (model == null) {
            throw new NullPointerException("Model was null");
        }
        
        Path outputPath = outputDirectory.toPath();
        
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
        tab(w, tab).append("// ").append(unitName).append(CRLF);
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
        w.append("import ").append(com.fizzed.rocker.ContentType.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.ForIterator.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.RenderingException.class.getName()).append(";").append(CRLF);
        w.append("import ").append(com.fizzed.rocker.RockerContent.class.getName()).append(";").append(CRLF);
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
        
        
        // class definition
        w.append("public class ")
                .append(model.getName())
                .append(" extends ")
                .append(model.getOptions().getExtendsClass())
                .append("<").append(model.getName()).append("> ");
        
        if (model.getOptions().getImplementsInterface() != null) {
            w.append("implements ").append(model.getOptions().getImplementsInterface());
            w.append(" ");
        }
                
        w.append("{").append(CRLF);
        
        
        
        // plain text -> map of chunks of text (Java only supports 2-byte length of string constant)
        LinkedHashMap<String, LinkedHashMap<String,String>> plainTextMap
                = model.createPlainTextMap(PLAIN_TEXT_CHUNK_LENGTH);
        
        if (!plainTextMap.isEmpty()) {
            
            w.append(CRLF);
            
            for (String plainText : plainTextMap.keySet()) {
                
                // include static text as comments in source (limit to 500)
                tab(w, 1).append("// ")
                    .append(StringUtils.abbreviate(StringEscapeUtils.escapeJava(plainText), 500)).append(CRLF);
                
                for (Map.Entry<String,String> chunk : plainTextMap.get(plainText).entrySet()) {
                
                    if (this.plainTextStrategy == PlainTextStrategy.STATIC_STRINGS) {                   
                        tab(w, 1).append("static private final String ")
                            .append(chunk.getKey())
                            .append(" = \"")
                            .append(StringEscapeUtils.escapeJava(chunk.getValue()))
                            .append("\";")
                            .append(CRLF);
                    }
                    else if (this.plainTextStrategy == PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS) {
                        tab(w, 1).append("static private final byte[] ")
                            .append(chunk.getKey())
                            .append(";")
                            .append(CRLF);
                    }
 
                }
            }
            
            // generate the static initializer
            if (this.plainTextStrategy == PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS) {
                
                w.append(CRLF);
                
                tab(w, 1).append("static {").append(CRLF);
                
                String loaderClassName = RockerUtil.unqualifiedClassName(PlainTextUnloadedClassLoader.class);
                tab(w, 2).append(loaderClassName)
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
                            tab(w, 2).append(chunk.getKey())
                                .append(" = loader.tryGet(\"")
                                .append(chunk.getKey())
                                .append("\");")
                                .append(CRLF);
                        }

                    }
                    
                }
                
                tab(w, 1).append("}").append(CRLF);
            }
            
        }
        
        
        // arguments (including possible RockerBody)
        if (model.getArguments().size() > 0) {
            w.append(CRLF);
            for (Argument arg : model.getArguments()) {
                tab(w, 1).append("// argument ").append(sourceRef(arg)).append(CRLF);
                // swap in normal content type in place of body
                String type = arg.getType();
                if (type.equals("RockerBody")) {
                    type = "RockerContent";
                }
                tab(w, 1).append("protected ").append(type).append(" " + arg.getName()).append(";").append(CRLF);
            }
        }
        
        w.append(CRLF);
        
        tab(w, 1).append("public ").append(model.getName()).append("() {").append(CRLF);
        tab(w, 2).append("super();").append(CRLF);
        tab(w, 2).append("__internal.setCharset(\"").append(model.getOptions().getTargetCharset()).append("\");").append(CRLF);
        tab(w, 2).append("__internal.setContentType(ContentType.").append(model.getContentType().toString()).append(");").append(CRLF);
        tab(w, 2).append("__internal.setTemplateName(\"").append(model.getTemplateName()).append("\");").append(CRLF);
        tab(w, 2).append("__internal.setTemplatePackageName(\"").append(model.getPackageName()).append("\");").append(CRLF);
        tab(w, 1).append("}").append(CRLF);
        
        // setters with builder-style pattern
        if (model.getArguments().size() > 0) {
            for (Argument arg : model.getArgumentsWithoutRockerBody()) {
                w.append(CRLF);
                tab(w, 1).append("public ").append(model.getName()).append(" ").append(arg.getName())
                        .append("(" + arg.getType()).append(" ").append(arg.getName())
                        .append(") {").append(CRLF);
                tab(w, 2).append("this.").append(arg.getName()).append(" = ").append(arg.getName()).append(";").append(CRLF);
                tab(w, 2).append("return this;").append(CRLF);
                tab(w, 1).append("}").append(CRLF);
            }
        }
        
        // if RockerBody is supported then override the __body setter
        Argument rockerBodyArgument = model.getRockerBodyArgument();
        if (rockerBodyArgument != null) {
            w.append(CRLF);
                tab(w, 1).append("@Override").append(CRLF);
                tab(w, 1).append("public ").append(model.getName()).append(" __body(RockerContent body) {").append(CRLF);
                tab(w, 2).append("this.").append(rockerBodyArgument.getName()).append(" = body;").append(CRLF);
                tab(w, 2).append("return this;").append(CRLF);
                tab(w, 1).append("}").append(CRLF);
        }
        
        // static builder
        w.append(CRLF);
        tab(w, 1).append("static public ").append(model.getName()).append(" template(");
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
        tab(w, 2).append("return new ").append(model.getName()).append("()");
        if (model.getArguments().size() > 0) {
            int i = 0;
            for (Argument arg : model.getArgumentsWithoutRockerBody()) {
                w.append(CRLF);
                tab(w, 3).append(".").append(arg.getName()).append("(").append(arg.getName()).append(")");
                i++;
            }
        }
        w.append(";").append(CRLF);
        tab(w, 1).append("}").append(CRLF);
        
        
        w.append(CRLF);
        tab(w, 1).append("@Override").append(CRLF);
        tab(w, 1).append("protected void __render() throws IOException, RenderingException {").append(CRLF);
        
        // build rendering code
        int indent = 2;
        int depth = 0;
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
            }
            else if (unit instanceof ForBlockEnd) {
                depth--;
                
                tab(w, depth+indent)
                        .append(blockEnd.pop())
                        .append(" // for end ").append(sourceRef(unit)).append(CRLF);
            }
            //log.info(" src (@ {}): [{}]", unit.getSourceRef(), unit.getSourceRef().getConsoleFriendlyText());
        }
        tab(w, 1).append("}").append(CRLF);
        
        
        
        if (this.plainTextStrategy == PlainTextStrategy.STATIC_BYTE_ARRAYS_VIA_UNLOADED_CLASS &&
                !plainTextMap.isEmpty()) {
            
            w.append(CRLF);
            
            tab(w, 1).append("private static class PlainText {").append(CRLF);
            w.append(CRLF);
            
            for (String plainText : plainTextMap.keySet()) {

                for (Map.Entry<String,String> chunk : plainTextMap.get(plainText).entrySet()) {

                    tab(w, 2).append("static private final String ")
                        .append(chunk.getKey())
                        .append(" = \"")
                        .append(StringEscapeUtils.escapeJava(chunk.getValue()))
                        .append("\";")
                        .append(CRLF);

                }

            }
            
            w.append(CRLF);
            tab(w, 1).append("}").append(CRLF);
        }
        
        
        
        w.append(CRLF);
        w.append("}").append(CRLF);
    }
    
}
