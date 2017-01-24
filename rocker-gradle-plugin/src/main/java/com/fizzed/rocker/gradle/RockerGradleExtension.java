package com.fizzed.rocker.gradle;

import java.io.File;

/**
 * Bean for the configuration options of Rocker Compiler
 */
@SuppressWarnings("unused")
public class RockerGradleExtension {

    boolean skip = false;

    boolean failOnError = true;

    boolean skipTouch = true;

    String touchFile;

    boolean addAsSources = false;

    boolean addAsTestSources = false;

    String javaVersion;

    String extendsClass;

    String extendsModelClass;

    Boolean optimize;

    Boolean discardLogicWhitespace;

    String targetCharset;

    String suffixRegex;

    /**
     * Directory containing templates. The base directory to search -- which is
     * also how their "package" name is determined.
     */
    File templateDirectory;

    /**
     * Directory to output generated Java source files.
     */
    File outputDirectory;

    /**
     * Directory where classes are compiled to (for placing rocker config file).
     */
    File classDirectory;

    String[] postProcessing;

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

    public boolean isAddAsSources() {
        return addAsSources;
    }

    public void setAddAsSources(boolean addAsSources) {
        this.addAsSources = addAsSources;
    }

    public boolean isAddAsTestSources() {
        return addAsTestSources;
    }

    public void setAddAsTestSources(boolean addAsTestSources) {
        this.addAsTestSources = addAsTestSources;
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

    public File getTemplateDirectory() {
        return templateDirectory;
    }

    public void setTemplateDirectory(File templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getClassDirectory() {
        return classDirectory;
    }

    public void setClassDirectory(File classDirectory) {
        this.classDirectory = classDirectory;
    }

    public String[] getPostProcessing() {
        return postProcessing;
    }

    public void setPostProcessing(String[] postProcessing) {
        this.postProcessing = postProcessing;
    }

}
