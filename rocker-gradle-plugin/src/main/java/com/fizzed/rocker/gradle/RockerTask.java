package com.fizzed.rocker.gradle;

import com.fizzed.rocker.compiler.JavaGeneratorMain;
import com.fizzed.rocker.compiler.RockerOptions;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RockerTask extends DefaultTask {

    private static Logger logger;
    private SourceSet sourceSet;
    private Set<File> templateDirs = new HashSet<>();
    private File outputDir;
    private File classDir;

    /**
     * @return the sourceSet
     */
    public SourceSet getSourceSet() {
        return sourceSet;
    }

    /**
     * @param sourceSet the sourceSet to set
     */
    public void setSourceSet(SourceSet sourceSet) {
        this.sourceSet = sourceSet;
    }

    /**
     * @return the templateDirs
     */
    public Set<File> getTemplateDirs() {
        return templateDirs;
    }

    /**
     * @param templateDirs the templateDirs to set
     */
    public void setTemplateDirs(Set<File> templateDirs) {
        this.templateDirs = templateDirs;
    }

    /**
     * @return the outputDir
     */
    public File getOutputDir() {
        return outputDir;
    }

    /**
     * @param outputDir the outputDir to set
     */
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * @return the classDir
     */
    public File getClassDir() {
        return classDir;
    }

    /**
     * @param classDir the classDir to set
     */
    public void setClassDir(File classDir) {
        this.classDir = classDir;
    }

    /**
     * Sets up the logger and runs the rocker compiler
     */
    @TaskAction
    public void compileRocker() {
        logger = getProject().getLogger();
        for (File templateDir: templateDirs) {
            doCompileRocker(getProject(), templateDir, outputDir, classDir);
        }
    }

    /**
     * Uses the project to get GradleExtension and runs the generator
     *
     * @param project - gradle project
     */
    public static void doCompileRocker(Project project,
                                       File templateDir, File outputDir, File classDir) {
        RockerExtension ext = (RockerExtension)
            project.getExtensions().findByName("rocker");
        runJavaGeneratorMain(ext, templateDir, outputDir, classDir);
    }

    /**
     * Run the Rocker compiler with the RockerGradleExtension as configuration 
     * options.
     *
     * @param ext the extension from the project object
     */
    private static void runJavaGeneratorMain(RockerExtension ext,
                                             File templateDir, File outputDir, File classDir) {
        if (ext.isSkip()) {
            logInfo("Skip flag is on, will skip goal.");
            return;
        }

        if (ext.getJavaVersion() == null || ext.getJavaVersion().length() == 0) {
            // set to current jdk version
            ext.setJavaVersion(System.getProperty("java.version").substring(0, 3));
            logInfo("Property rocker.javaVersion not set. Using your JDK version "
                + ext.getJavaVersion());
        } else {
            logInfo("Targeting java version " + ext.getJavaVersion());
        }

        try {
            JavaGeneratorMain jgm = new JavaGeneratorMain();

            jgm.getParser().getConfiguration().setTemplateDirectory(templateDir);
            jgm.getGenerator().getConfiguration().setOutputDirectory(outputDir);
            jgm.getGenerator().getConfiguration().setClassDirectory(classDir);
            jgm.setFailOnError(ext.isFailOnError());

            // passthru other config
            if (ext.getSuffixRegex() != null) {
                jgm.setSuffixRegex(ext.getSuffixRegex());
            }
            RockerOptions rockerOptions
                = jgm.getParser().getConfiguration().getOptions();
            if (ext.getJavaVersion() != null) {
                rockerOptions.setJavaVersion(ext.getJavaVersion());
            }
            if (ext.getExtendsClass() != null) {
                rockerOptions.setExtendsClass(ext.getExtendsClass());
            }
            if (ext.getExtendsModelClass() != null) {
                rockerOptions.setExtendsModelClass(ext.getExtendsModelClass());
            }
            if (ext.getDiscardLogicWhitespace() != null) {
                rockerOptions.setDiscardLogicWhitespace(ext.getDiscardLogicWhitespace());
            }
            if (ext.getTargetCharset() != null) {
                rockerOptions.setTargetCharset(ext.getTargetCharset());
            }
            if (ext.getOptimize() != null) {
                rockerOptions.setOptimize(ext.getOptimize());
            }
            if (ext.getPostProcessing() != null ) {
                rockerOptions.setPostProcessing(ext.getPostProcessing());
            }

            jgm.run();

        }
        catch (Exception e) {
            throw new RockerGradleException(e.getMessage(), e);
        }

        if (!ext.isSkipTouch()) {
            if (ext.getTouchFile().length() == 0) {
                throw new RockerGradleException(
                    "If skipTouch is equal to false, then "
                        + "touchFile must not be empty");
            }
            if (ext.getTouchFile() != null) {
                File f = new File(ext.getTouchFile());
                logInfo("Touching file " + f);
                try {
                    if (!f.exists()) {
                        new FileOutputStream(f).close();
                    }
                    if (!f.setLastModified(System.currentTimeMillis())) {
                        throw new IOException("Could not set Last Modified");
                    }
                } catch (IOException e) {
                    logDebug("Unable to touch file: " + f.getAbsolutePath(), e);
                }
            }
        }
    }

    private static void logInfo(String msg) {
        if (logger != null) {
            logger.info(msg);
        }
    }

    private static void logDebug(String msg, Exception e) {
        if (logger != null) {
            logger.debug(msg, e);
        }
    }
}
