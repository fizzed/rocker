package com.fizzed.rocker.gradle;

import com.fizzed.rocker.compiler.JavaGeneratorMain;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RockerGradleTask extends DefaultTask {

    private static Logger logger;

    /**
     * Sets up the logger and runs the rocker compiler
     */
    @TaskAction
    public void compileRocker() {
        logger = getProject().getLogger();
        doCompileRocker(getProject());
    }

    /**
     * Uses the project to get GradleExtension and runs the generator
     *
     * @param project - gradle project
     */
    public static void doCompileRocker(Project project) {
        RockerGradleExtension ext = (RockerGradleExtension) project.getExtensions().findByName("rocker");
        runJavaGeneratorMain(ext);
    }

    /**
     * Run the Rocker compiler with the RockerGradleExtension as configuration options
     * @param ext the extension from the project object
     */
    private static void runJavaGeneratorMain(RockerGradleExtension ext) {
        if (ext.skip) {
            logInfo("Skip flag is on, will skip goal.");
            return;
        }

        if (ext.addAsSources || ext.addAsTestSources) {
            throw new RockerGradleException("Properties addAsSources and addAsTestSources are not supported by gradle plugin");
        }

        if (ext.templateDirectory == null) {
            throw new RockerGradleException("Property templateDirectory cannot be null/empty");
        }

        if (ext.outputDirectory == null) {
            throw new RockerGradleException("Property outputDirectory cannot be null/empty");
        }

        if (ext.classDirectory == null) {
            throw new RockerGradleException("Property classDirectory cannot be null/empty");
        }

        if (ext.javaVersion == null || ext.javaVersion.length() == 0) {
            // set to current jdk version
            ext.javaVersion = System.getProperty("java.version").substring(0, 3);
            logInfo("Property rocker.javaVersion not set. Using your JDK version " + ext.javaVersion);
        } else {
            logInfo("Targeting java version " + ext.javaVersion);
        }

        try {
            JavaGeneratorMain jgm = new JavaGeneratorMain();

            jgm.getParser().getConfiguration().setTemplateDirectory(ext.templateDirectory);
            jgm.getGenerator().getConfiguration().setOutputDirectory(ext.outputDirectory);
            jgm.getGenerator().getConfiguration().setClassDirectory(ext.classDirectory);
            jgm.setFailOnError(ext.failOnError);

            // passthru other config
            if (ext.suffixRegex != null) {
                jgm.setSuffixRegex(ext.suffixRegex);
            }
            if (ext.javaVersion != null) {
                jgm.getParser().getConfiguration().getOptions().setJavaVersion(ext.javaVersion);
            }
            if (ext.extendsClass != null) {
                jgm.getParser().getConfiguration().getOptions().setExtendsClass(ext.extendsClass);
            }
            if (ext.extendsModelClass != null) {
                jgm.getParser().getConfiguration().getOptions().setExtendsModelClass(ext.extendsModelClass);
            }
            if (ext.discardLogicWhitespace != null) {
                jgm.getParser().getConfiguration().getOptions().setDiscardLogicWhitespace(ext.discardLogicWhitespace);
            }
            if (ext.targetCharset != null) {
                jgm.getParser().getConfiguration().getOptions().setTargetCharset(ext.targetCharset);
            }
            if (ext.optimize != null) {
                jgm.getParser().getConfiguration().getOptions().setOptimize(ext.optimize);
            }
            if (ext.postProcessing != null ) {
                jgm.getParser().getConfiguration().getOptions().setPostProcessing(ext.postProcessing);
            }

            jgm.run();
        }
        catch (Exception e) {
            throw new RockerGradleException(e.getMessage(), e);
        }

        if (!ext.skipTouch) {
            if (ext.touchFile.length() == 0) {
                throw new RockerGradleException("If skipTouch is equal to false, then touchFile must not be empty");
            }
            if (ext.touchFile != null) {
                File f = new File(ext.touchFile);
                logInfo("Touching file " + f);
                try {
                    if (!f.exists()) {
                        new FileOutputStream(f).close();
                    }
                    f.setLastModified(System.currentTimeMillis());
                } catch (IOException e) {
                    logDebug("Unable to touch file", e);
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
