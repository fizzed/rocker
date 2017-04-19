package com.fizzed.rocker.gradle;

import java.io.File;

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
        RockerConfiguration rockerConfig = project.getExtensions()
            .create("rocker", RockerConfiguration.class);
        rockerConfig.setProject(project);
        rockerConfig.setOutputBaseDirectory(new File(
            project.getBuildDir(), "generated-src/rocker"));
        rockerConfig.setClassBaseDirectory(new File(
            project.getBuildDir(), "classes"));

        // Create own source set extension
        SourceSetContainer sourceSets = project.getConvention().getPlugin(
            JavaPluginConvention.class).getSourceSets();
        sourceSets.all(sourceSet -> processSourceSet(project, sourceSet, rockerConfig));

        // Complete configuration after evaluation
        project.afterEvaluate(RockerPlugin::completeConfiguration);
    }

    private static void processSourceSet(Project project, SourceSet sourceSet,
    		RockerConfiguration rockerConfig) {
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
        rockerTask.setRockerProjectConfig(rockerConfig);

        // 3) Set source set and sources for task (avoids lookup when executing)
        rockerTask.setSourceSet(sourceSet);
        rockerTask.setTemplateDirs(rockerProperty.getRocker().getSrcDirs());

        // 4) Make sure that the rocker task is run before compiling
        //    Java sources
        project.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME)
            .dependsOn(rockerTask);
    }

    private static void completeConfiguration (Project project) {
        // Output directory and class directory can be configured on
        // a per (generated) task basis. So add them only if not set.
        RockerConfiguration rockerConfig
            = project.getExtensions().findByType(RockerConfiguration.class);
        for (RockerTask rockerTask:
            project.getTasks().withType(RockerTask.class)) {
            // For each rocker task
            // 1) Check if output directory was set
            if (rockerTask.getOutputDir() == null) {
                // else set to default
                rockerTask.setOutputDir(new File(
                    rockerConfig.getOutputBaseDirectory(),
                    rockerTask.sourceSet().getName()));
            }

            // 2) Add input information for incremental build
            //    (there doesn't seem to be an @InputDirectories annotation)
            for (File templateDir: rockerTask.getTemplateDirs()) {
                rockerTask.getInputs().dir(templateDir);
            }

            // 3) Add output directory to java sources
            rockerTask.sourceSet().getJava().srcDir(
                rockerTask.getOutputDir());

            // 4) Check if classes directory was set
            if (rockerTask.getClassDir() == null) {
                // else set to default
                rockerTask.setClassDir(new File(
                    rockerConfig.getClassBaseDirectory(),
                    rockerTask.sourceSet().getName()));
            }
        }
    }
}
