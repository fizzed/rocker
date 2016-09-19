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
import com.fizzed.rocker.ContentType;
import com.fizzed.rocker.model.JavaVersion;
import com.fizzed.rocker.model.Option;

import java.nio.charset.Charset;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author joelauer
 */
public class RockerOptions {
    
    static public final String JAVA_VERSION = "javaVersion";
    static public final String DISCARD_LOGIC_WHITESPACE = "discardLogicWhitespace";
    static public final String COMBINE_ADJACENT_PLAIN = "combineAdjacentPlain";
    static public final String EXTENDS_CLASS = "extendsClass";
    static public final String EXTENDS_MODEL_CLASS = "extendsModelClass";
    static public final String TARGET_CHARSET = "targetCharset";
    static public final String OPTIMIZE = "optimize";
    static public final String POST_PROCESSING = "postProcessing";
    
    // generated source compatiblity
    private JavaVersion javaVersion;
    // discard lines consisting of only logic/block
    private Boolean discardLogicWhitespace;
    // combine adjacent plain text elements together to form a single one
    // almost should never be disabled -- much more efficient templates
    private Boolean combineAdjacentPlain;
    // parent class of template
    private String extendsClass;
    // parent class of template model
    private String extendsModelClass;
    // target charset template will render with
    private String targetCharset;
    // templates optimized for production
    private Boolean optimize;
    // names of classes to be used for post-processing the model in order of appearance
    private String[] postProcessing;
    
    public RockerOptions() {
        this.javaVersion = JavaVersion.v1_8;
        this.discardLogicWhitespace = null;                 // will default to default of content type
        this.combineAdjacentPlain = Boolean.TRUE;
        this.extendsClass = com.fizzed.rocker.runtime.DefaultRockerTemplate.class.getName();
        this.extendsModelClass = com.fizzed.rocker.runtime.DefaultRockerModel.class.getName();
        this.targetCharset = "UTF-8";
        this.optimize = Boolean.FALSE;
        this.postProcessing = new String[0];
    }
    
    public RockerOptions copy() {
        RockerOptions options = new RockerOptions();
        options.javaVersion = this.javaVersion;
        options.discardLogicWhitespace = this.discardLogicWhitespace;
        options.combineAdjacentPlain = this.combineAdjacentPlain;
        options.extendsClass = this.extendsClass;
        options.extendsModelClass = this.extendsModelClass;
        options.targetCharset = this.targetCharset;
        options.optimize = this.optimize;
        // do not copy post-processor class names from global configuration.
        // these need to be kept separate from per-template configurations.
        options.postProcessing = new String[0];
        return options;
    }

    public JavaVersion getJavaVersion() {
        return javaVersion;
    }
    
    public boolean isGreaterThanOrEqualToJavaVersion(JavaVersion javaVersion) {
        return this.javaVersion.getVersion() >= javaVersion.getVersion();
    }

    public void setJavaVersion(JavaVersion javaVersion) {
        this.javaVersion = javaVersion;
    }
    
    public void setJavaVersion(String javaVersion) throws TokenException {
        if (javaVersion == null) {
            throw new TokenException("javaVersion was null");
        }
        
        JavaVersion jv = JavaVersion.findByLabel(javaVersion);
        if (jv == null) {
            throw new TokenException("Unsupported javaVersion [" + javaVersion + "]");
        }
        
        this.javaVersion = jv;
    }

    public Boolean getDiscardLogicWhitespace() {
        return discardLogicWhitespace;
    }

    public boolean getDiscardLogicWhitespaceForContentType(ContentType type) {
        if (this.discardLogicWhitespace == null) {
            return ContentType.discardLogicWhitespace(type);
        } else {
            return discardLogicWhitespace;
        }
    }
    
    public void setDiscardLogicWhitespace(Boolean discardLogicWhitespace) {
        this.discardLogicWhitespace = discardLogicWhitespace;
    }

    public Boolean getCombineAdjacentPlain() {
        return combineAdjacentPlain;
    }

    public void setCombineAdjacentPlain(Boolean combineAdjacentPlain) {
        this.combineAdjacentPlain = combineAdjacentPlain;
    }

    public String getExtendsClass() {
        return extendsClass;
    }

    public void setExtendsClass(String extendsClass) {
        this.extendsClass = extendsClass;
    }

    public String getExtendsModelClass() {
        return extendsModelClass;
    }

    public void setExtendsModelClass(String extendsModelClass) {
        this.extendsModelClass = extendsModelClass;
    }

    public String getTargetCharset() {
        return targetCharset;
    }

    public void setTargetCharset(String targetCharset) {
        // verify this charset exists... (will throw unchecked exception)
        Charset.forName(targetCharset);
        
        this.targetCharset = targetCharset;
    }

    public Boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(Boolean optimize) {
        this.optimize = optimize;
    }

    public String[] getPostProcessing() {
    	return postProcessing;
    }
    
    public void setPostProcessing( String[] postProcessing ) {
        this.postProcessing = postProcessing;
    }

    public void set(String name, String value) throws TokenException {
        String optionName = name.trim();
        String optionValue = value.trim();
        
        switch (optionName) {
            case DISCARD_LOGIC_WHITESPACE:
                this.setDiscardLogicWhitespace(parseBoolean(optionValue));
                break;
            case COMBINE_ADJACENT_PLAIN:
                this.setCombineAdjacentPlain(parseBoolean(optionValue));
                break;
            case JAVA_VERSION:
                this.setJavaVersion(optionValue);
                break;
            case EXTENDS_CLASS:
                this.setExtendsClass(optionValue);
                break;
            case EXTENDS_MODEL_CLASS:
                this.setExtendsModelClass(optionValue);
                break;
            case TARGET_CHARSET:
                this.setTargetCharset(optionValue);
                break;
            case OPTIMIZE:
                this.setOptimize(parseBoolean(optionValue));
                break;
            case POST_PROCESSING:
            	this.setPostProcessing(parseStringArrayFromList(optionValue));
            	break;
            default:
                throw new TokenException("Invalid option (" + optionName + ") is not a property)");
        }
    }
    
    public void write(Properties properties) {
        if (this.optimize != null) {
            properties.put(RockerConfiguration.OPTION_PREFIX + OPTIMIZE, this.optimize.toString());
        }
        if (this.discardLogicWhitespace != null) {
            properties.put(RockerConfiguration.OPTION_PREFIX + DISCARD_LOGIC_WHITESPACE, this.discardLogicWhitespace.toString());
        }
        if (this.combineAdjacentPlain != null) {
            properties.put(RockerConfiguration.OPTION_PREFIX + COMBINE_ADJACENT_PLAIN, this.combineAdjacentPlain.toString());
        }
        if (this.javaVersion != null) {
            properties.put(RockerConfiguration.OPTION_PREFIX + JAVA_VERSION, this.javaVersion.getLabel());
        }
        properties.put(RockerConfiguration.OPTION_PREFIX + EXTENDS_CLASS, this.extendsClass);
        properties.put(RockerConfiguration.OPTION_PREFIX + EXTENDS_MODEL_CLASS, this.extendsModelClass);
        properties.put(RockerConfiguration.OPTION_PREFIX + TARGET_CHARSET, this.targetCharset);
        if (this.postProcessing != null && postProcessing.length != 0) {
            properties.put(RockerConfiguration.OPTION_PREFIX + POST_PROCESSING, StringUtils.join(this.postProcessing,","));
        }
    }
    
    public void parseOption(Option option) throws ParserException {
        String statement = option.getStatement();
        if (!statement.contains("=")) {
            throw TemplateParser.buildParserException(option.getSourceRef(), null, "Invalid option (missing = token; format name=value)");
        }
        
        String[] nameValuePair = statement.split("=");
        if (nameValuePair == null || nameValuePair.length != 2) {
            throw TemplateParser.buildParserException(option.getSourceRef(), null, "Invalid option (must have only a single = token)");
        }
        
        try {
            set(nameValuePair[0], nameValuePair[1]);
        } catch (TokenException e) {
            throw TemplateParser.buildParserException(option.getSourceRef(), null, e.getMessage(), e);
        }
    }
    
    private Boolean parseBoolean(String value) throws TokenException {
        if (value == null) {
            throw new TokenException("Boolean option cannot be null");
        }
        if (value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        else if (value.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        else {
            throw new TokenException("Unparseable boolean");
        }
    }
    
    /**
     * Create an array of sub-strings from a given comma-separated string.
     * The contents of each string in the returned array will be trimmed of leading and trailing spaces.
     * @param value the original string, containing individual comma-separated tokens
     * @return an array of 
     * @throws TokenException
     */
    private String[] parseStringArrayFromList( String value ) throws TokenException {
        if ( value == null ) {
            throw new TokenException("List of strings cannot be null");
        }
        StringTokenizer st = new StringTokenizer(value, ",");
        String[] tokens = new String[st.countTokens()];
        int i = 0;
        while ( st.hasMoreTokens() ) {
            tokens[i++] = st.nextToken().trim(); 
        }
        return tokens;
    }

}
