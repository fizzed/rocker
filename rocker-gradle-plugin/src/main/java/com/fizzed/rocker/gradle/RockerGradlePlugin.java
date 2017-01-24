package com.fizzed.rocker.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

/**
 * A plugin that for gradle that runs provides the `rockerCompile` task
 */
public class RockerGradlePlugin implements Plugin<Project> {

    /**
     * Create group rocker, place the `rockerCompile` task in it and describe the task
     * @param project - gradle project
     */
    @Override
    public void apply(Project project) {
        project.getExtensions().create("rocker", RockerGradleExtension.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("rockerCompile", RockerGradleTask.class)
                .setGroup("rocker");
        tasks.getByName("rockerCompile")
                .setDescription("Compile Rocker Templates");
    }
}
