package com.fizzed.rocker.gradle;

import java.io.File;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * A plugin that for gradle that provides the `generateRockerTemplateSource`
 * tasks.
 */
public class RockerPlugin implements Plugin<Project> {

    /**
     * Create `rockerCompile` task in group build and describe the task
     * 
     * @param project - gradle project
     */
    @Override
    public void apply(Project project) {
    	// Make sure that we have the objects from the java task available
    	project.getPluginManager().apply(JavaPlugin.class);

    	// Create own project extension (configuration)
    	RockerExtension rockerExtension = project.getExtensions()
    			.create("rocker", RockerExtension.class);
    	rockerExtension.setProject(project);
    	rockerExtension.setOutputBaseDirectory(new File(
				project.getBuildDir(), "generated-src/rocker"));
    	rockerExtension.setClassBaseDirectory(new File(
				project.getBuildDir(), "classes"));
        
        // Create own source set extension
        SourceSetContainer sourceSets = project.getConvention().getPlugin(
        		JavaPluginConvention.class).getSourceSets();
        sourceSets.all(new Action<SourceSet>() {

			@Override
			public void execute(SourceSet sourceSet) {
				processSourceSet(project, sourceSet);
			}
		});

        // Complete configuartion after evaluation
        project.afterEvaluate(new Action<Project>() {

			@Override
			public void execute(Project project) {
				completeConfiguration(project);
			}
        	
        });
    }
    
    private static void processSourceSet(Project project, SourceSet sourceSet) {
    	// for each source set we will:
    	// 1) Add a new 'rocker' property to the source set
    	RockerSourceSetProperty rockerProperty 
    		= new RockerSourceSetProperty(project);
    	new DslObject(sourceSet).getConvention().getPlugins().put(
    		  "rocker", rockerProperty);

    	// 2) Create a rocker task for this sourceSet following the gradle
    	//    naming conventions
    	final String taskName = sourceSet.getTaskName(
    			"generate", "RockerTemplateSource");
    	RockerTask rockerTask = project.getTasks().create(
    			taskName, RockerTask.class);
    	rockerTask.setGroup("build");
        rockerTask.setDescription("Generate Sources from " 
        		+ sourceSet.getName() + " Rocker Templates");

        // 3) Set source set and sources for task (avoids lookup when executing)
        rockerTask.setSourceSet(sourceSet);
        rockerTask.setTemplateDirs(rockerProperty.getRocker().getSrcDirs());
        
        // 4) Make sure that the rocker task is run run before compiling
        //    Java sources
        project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME)
        	.dependsOn(rockerTask);
    }
    
    private static void completeConfiguration (Project project) {
    	// Output directory and class directory can be configured on
    	// a per (generated) task basis. So add them only if not set.
    	RockerExtension rockerExtension 
    		= project.getExtensions().findByType(RockerExtension.class);
    	for (RockerTask rockerTask: 
    		project.getTasks().withType(RockerTask.class)) {
    		// For each rocker task
    		// 1) Check if output directory was set
    		if (rockerTask.getOutputDir() == null) {
    			// else set to default
    			rockerTask.setOutputDir(new File(
    					rockerExtension.getOutputBaseDirectory(),
    					rockerTask.getSourceSet().getName()));
    		}
    		
    		// 2) Inform gradle about outputs for incremental build
    		rockerTask.getOutputs().dir(rockerTask.getOutputDir());
    		
    		// 3) Add input information for incremental build
            for (File templateDir: rockerTask.getTemplateDirs()) {
            	rockerTask.getInputs().dir(templateDir);
            }
            rockerTask.getInputs().properties(rockerExtension.inputProperties());
            
    		// 4) Add output directory to java sources
            rockerTask.getSourceSet().getJava().srcDir(
            		rockerTask.getOutputDir());
            
            // 5) Check if classes directory was set
    		if (rockerTask.getClassDir() == null) {
    			// else set to default
    			rockerTask.setClassDir(new File(
    					rockerExtension.getClassBaseDirectory(),
    					rockerTask.getSourceSet().getName()));
    		}
    	}
    }
}
