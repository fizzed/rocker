package com.fizzed.rocker.gradle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;

/**
 * Bean for the configuration options of Rocker Compiler
 */
public class RockerExtension {
    private Project project;
    private boolean skip = false;
    private boolean failOnError = true;
    private boolean skipTouch = true;
    private String touchFile;
    private String javaVersion;
    private String extendsClass;
    private String extendsModelClass;
    private Boolean optimize;
    private Boolean discardLogicWhitespace;
    private String targetCharset;
    private String suffixRegex;
    private File outputBaseDirectory;
    private File classBaseDirectory;
    private String[] postProcessing;

    /**
     * Generate a map that reflects the current state of all properties
     * that are relevant as input for the incremental build.
     * 
     * @return the map
     */
    Map<String,?> inputProperties() {
        Map<String,? super Object> result = new HashMap<>();
        result.put("javaVersion", javaVersion);
        result.put("extendsClass", extendsClass);
        result.put("extendsModelClass", extendsModelClass);
        result.put("optimize", optimize);
        result.put("discardLogicWhitespace", discardLogicWhitespace);
        result.put("targetCharset", targetCharset);
        result.put("suffixRegex", suffixRegex);
        result.put("postProcessinf", postProcessing);
        return result;
    }

    /**
     * @param project the project to set
     */
    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isSkipTouch() {
        return skipTouch;
    }

    public void setSkipTouch(boolean skipTouch) {
        this.skipTouch = skipTouch;
    }

    public String getTouchFile() {
        return touchFile;
    }

    public void setTouchFile(String touchFile) {
        this.touchFile = touchFile;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
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

    public Boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(Boolean optimize) {
        this.optimize = optimize;
    }

    public Boolean getDiscardLogicWhitespace() {
        return discardLogicWhitespace;
    }

    public void setDiscardLogicWhitespace(Boolean discardLogicWhitespace) {
        this.discardLogicWhitespace = discardLogicWhitespace;
    }

    public String getTargetCharset() {
        return targetCharset;
    }

    public void setTargetCharset(String targetCharset) {
        this.targetCharset = targetCharset;
    }

    public String getSuffixRegex() {
        return suffixRegex;
    }

    public void setSuffixRegex(String suffixRegex) {
        this.suffixRegex = suffixRegex;
    }

    public File getOutputBaseDirectory() {
        return outputBaseDirectory;
    }

    public void setOutputBaseDirectory(File outputBaseDirectory) {
        this.outputBaseDirectory = project.file(outputBaseDirectory);
    }

    public File getClassBaseDirectory() {
        return classBaseDirectory;
    }

    public void setClassBaseDirectory(File classBaseDirectory) {
        this.classBaseDirectory = project.file(classBaseDirectory);
    }

    public String[] getPostProcessing() {
        return postProcessing;
    }

    public void setPostProcessing(String[] postProcessing) {
        this.postProcessing = postProcessing;
    }
}
