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
package com.fizzed.rocker.model;

import com.fizzed.rocker.compiler.RockerOptions;
import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.compiler.RockerUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class TemplateModel {
    
    // e.g. "views.system"
    private final String packageName;
    // e.g. "index.rocker.html"
    private final String templateName;
    private final ContentType contentType;
    private final long modifiedAt;
    // e.g. "index"
    private final String name;
    private final List<JavaImport> imports;
    private final List<Argument> arguments;
    private final List<TemplateUnit> units;
    private final RockerOptions options;
    
    public TemplateModel(String packageName, String templateName, long modifiedAt, RockerOptions defaultOptions) {
        this.packageName = packageName;
        this.templateName = templateName;
        this.name = RockerUtil.templateNameToName(templateName);
        this.contentType = RockerUtil.templateNameToContentType(templateName);
        this.modifiedAt = modifiedAt;
        this.imports = new ArrayList<>();
        this.arguments = new ArrayList<>();
        this.units = new ArrayList<>();
        this.options = defaultOptions;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }
    
    public List<JavaImport> getImports() {
        return imports;
    }

    public List<Argument> getArguments() {
        return arguments;
    }
    
    public boolean hasRockerBodyArgument() {
        return getRockerBodyArgument() != null;
    }
    
    public Argument getRockerBodyArgument() {
        if (!arguments.isEmpty()) {
            
            Argument lastArgument = arguments.get(arguments.size() - 1);

            if (lastArgument.getType().equals("RockerBody")) {
                return lastArgument;
            }
        }
        return null;
    }
    
    public List<Argument> getArgumentsWithoutRockerBody() {
        if (hasRockerBodyArgument()) {
            return arguments.subList(0, arguments.size() - 1);
        } else {
            return arguments;
        }
    }

    public RockerOptions getOptions() {
        return options;
    }

    public List<TemplateUnit> getUnits() {
        return units;
    }
    
    public <T extends TemplateUnit> T getUnit(int index, Class<T> type) {
        return (T)units.get(index);
    }
    
    public LinkedHashMap<String,LinkedHashMap<String,String>> createPlainTextMap(int chunkSize) {        
        // optimize static plain text constants
        int index = 0;
        LinkedHashMap<String, LinkedHashMap<String,String>> plainTextMap = new LinkedHashMap<>();

        for (TemplateUnit unit : getUnits()) {
            if (unit instanceof PlainText) {
                PlainText plain = (PlainText)unit;

                if (!plainTextMap.containsKey(plain.getText())) {
                    
                    LinkedHashMap<String,String> chunkMap = new LinkedHashMap<>();
                    plainTextMap.put(plain.getText(), chunkMap);
                    
                    // split text into chunks
                    List<String> chunks = RockerUtil.stringIntoChunks(plain.getText(), chunkSize);
                    
                    for (int chunkIndex = 0; chunkIndex < chunks.size(); chunkIndex++) {

                        String varName = "PLAIN_TEXT_" + index + "_" + chunkIndex;
                        
                        String chunkText = chunks.get(chunkIndex);

                        chunkMap.put(varName, chunkText);
                    }

                    index++;
                }
            }
        }

        return plainTextMap;
    }
 
    /**
     * Build hash value representing all components from the "header" that would
     * break an "interface" (used for reloading).
     * @return 
     */
    public int createHeaderHash() {
        StringBuilder s = new StringBuilder();
        
        /**
        // append each java import
        for (JavaImport ji : imports) {
            s.append(ji.getStatement());
            s.append(";");
        }
        */
        
        s.append(this.contentType);
        s.append(";");
        
        for (Argument arg : arguments) {
            s.append(arg.getType());
            s.append(" ");
            s.append(arg.getName());
            s.append(";");
        }
        
        return s.toString().hashCode();
    }
    
}
