package com.fizzed.rocker.maven;

import com.fizzed.rocker.compiler.JavaGeneratorRunnable;
import com.fizzed.rocker.model.JavaVersion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Maven plugin for parsing Rocker templates and generating Java source code.
 * 
 * @author joelauer
 */
@Mojo(name = "generate",  defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenerateMojo extends AbstractMojo {
    
    @Parameter ( property = "rocker.skip", defaultValue = "false")
    private boolean skip;
    
    @Parameter(property = "rocker.failOnError", defaultValue = "true")
    protected boolean failOnError;
    
    @Parameter(property = "rocker.skipTouch", defaultValue = "false")
    protected boolean skipTouch;
    
    @Parameter(property = "rocker.touchFile", defaultValue = "${basedir}/pom.xml")
    protected String touchFile;
    
    @Parameter(property = "rocker.addAsSources", defaultValue = "true")
    protected boolean addAsSources;
    
    @Parameter(property = "rocker.addAsTestSources", defaultValue = "false")
    protected boolean addAsTestSources;
    
    @Parameter(property = "rocker.javaVersion")
    protected String javaVersion;
    
    @Parameter(property = "rocker.extendsClass")
    protected String extendsClass;
    
    @Parameter(property = "rocker.extendsModelClass")
    protected String extendsModelClass;
    
    @Parameter(property = "rocker.optimize")
    protected Boolean optimize;
    
    @Parameter(property = "rocker.discardLogicWhitespace")
    protected Boolean discardLogicWhitespace;
    
    @Parameter(property = "rocker.targetCharset")
    protected String targetCharset;
    
    @Parameter(property = "rocker.suffixRegex")
    protected String suffixRegex;
    
    /**
     * Directory containing templates. The base directory to search -- which is
     * also how their "package" name is determined.
     */
    @Parameter(property = "rocker.templateDirectory", defaultValue = "${project.build.sourceDirectory}")
    protected File templateDirectory;
    
    /**
     * Directory to output generated Java source files.
     */
    @Parameter(property = "rocker.outputDirectory", defaultValue = "${project.build.directory}/generated-sources/rocker", required = true)
    protected File outputDirectory;
    
    /**
     * Directory where classes are compiled to (for placing rocker config file).
     */
    @Parameter(property = "rocker.classDirectory", defaultValue = "${project.build.outputDirectory}", required = true)
    protected File classDirectory;

    @Parameter(defaultValue = "${project}", readonly = true )
    protected MavenProject project;
    
    @Parameter( property = "rocker.postProcessing", required = false)
    protected String[] postProcessing;

    /**
     * Weather or not to mark the generated classes as {@code @Generated}. 
     * */
    @Parameter(property = "rocker.markAsGenerated")
    protected Boolean markAsGenerated;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            getLog().info("Skip flag is on, will skip goal.");
            return;
        }
        
        if (this.templateDirectory == null) {
            throw new MojoExecutionException("Property templateDirectory cannot be null/empty");
        }
        
        if (this.outputDirectory == null) {
            throw new MojoExecutionException("Property outputDirectory cannot be null/empty");
        }
        
        if (this.classDirectory == null) {
            throw new MojoExecutionException("Property classDirectory cannot be null/empty");
        }
        
        /**
        if (this.compileDirectory == null) {
            throw new MojoExecutionException("Property compileDirectory cannot be null/empty");
        }
        */
        
        if (javaVersion == null || javaVersion.length() == 0) {
            // set to current jdk version
            javaVersion = JavaVersion.current();
            getLog().info("Property rocker.javaVersion not set. Using your JDK version " + this.javaVersion);
        } else {
            getLog().info("Targeting java version " + this.javaVersion);
        }
        
        try {
            JavaGeneratorRunnable jgr = new JavaGeneratorRunnable();
            
            jgr.getParser().getConfiguration().setTemplateDirectory(templateDirectory);
            jgr.getGenerator().getConfiguration().setOutputDirectory(outputDirectory);
            jgr.getGenerator().getConfiguration().setClassDirectory(classDirectory);
            //jgr.getGenerator().getConfiguration().setCompileDirectory(compileDirectory);
            jgr.setFailOnError(failOnError);
            
            // passthru other config
            if (suffixRegex != null) {
                jgr.setSuffixRegex(suffixRegex);
            }
            if (javaVersion != null) {
                jgr.getParser().getConfiguration().getOptions().setJavaVersion(javaVersion);
            }
            if (extendsClass != null) {
                jgr.getParser().getConfiguration().getOptions().setExtendsClass(extendsClass);
            }
            if (extendsModelClass != null) {
                jgr.getParser().getConfiguration().getOptions().setExtendsModelClass(extendsModelClass);
            }
            if (discardLogicWhitespace != null) {
                jgr.getParser().getConfiguration().getOptions().setDiscardLogicWhitespace(discardLogicWhitespace);
            }
            if (targetCharset != null) {
                jgr.getParser().getConfiguration().getOptions().setTargetCharset(targetCharset);
            }
            if (optimize != null) {
                jgr.getParser().getConfiguration().getOptions().setOptimize(optimize);
            }
            if (postProcessing != null ) {
            	jgr.getParser().getConfiguration().getOptions().setPostProcessing(postProcessing);
            }
            if (markAsGenerated != null) {
                jgr.getParser().getConfiguration().getOptions().setMarkAsGenerated(markAsGenerated);
            }
            
            jgr.run();
        }
        catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
        // after generating templates THEN its safe to add a new source root
        // directory since it may not have existed before and the adding of it
        // would have silently failed
        if (addAsTestSources) {
            getLog().info("Added test sources with " + this.outputDirectory);
            project.addTestCompileSourceRoot(this.outputDirectory.getAbsolutePath());
        }
        else if (addAsSources) {
            getLog().info("Added sources with " + this.outputDirectory);
            project.addCompileSourceRoot(this.outputDirectory.getAbsolutePath());
        }
        
        if (!skipTouch) {
            if (touchFile != null && touchFile.length() > 0) {
                File f = new File(touchFile);
                getLog().info("Touching file " + f);
                try {
                    if (!f.exists()) {
                        new FileOutputStream(f).close();
                    }
                    f.setLastModified(System.currentTimeMillis());
                } catch (IOException e) {
                    getLog().debug("Unable to touch file", e);
                }
            }
        }
    }
    
}
