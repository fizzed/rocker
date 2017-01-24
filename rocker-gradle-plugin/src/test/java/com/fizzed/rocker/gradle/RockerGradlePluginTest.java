package com.fizzed.rocker.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RockerGradlePluginTest {

    @Test
    public void testCompileTaskExists() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(RockerGradlePlugin.class);
        Task rocker = project.getTasks().getByName("rockerCompile");
        assertTrue(rocker instanceof RockerGradleTask);
    }

    @Test
    public void testJavaFileIsCreatedInOutputDirectory() throws ClassNotFoundException {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(RockerGradlePlugin.class);
        setUpDirectories(project);

        RockerGradleTask.doCompileRocker(project);
        File templateFile =
                new File("build/generated/source/apt/main/com/fizzed/rocker/gradle/views/HelloTemplate.java");
        assertTrue(templateFile.exists());
    }

    @Test
    public void testRockerCompilerConfIsGeneratedInClassesDirectory() throws ClassNotFoundException {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(RockerGradlePlugin.class);
        setUpDirectories(project);

        RockerGradleTask.doCompileRocker(project);
        File templateFile =
                new File("build/classes/main/rocker-compiler.conf");
        assertTrue(templateFile.exists());
    }

    @Test
    public void testNullOutputDirectoryThrowsException() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(RockerGradlePlugin.class);
        RockerGradleExtension extensions = (RockerGradleExtension) project.getExtensions().getByName("rocker");

        extensions.outputDirectory = null;
        extensions.classDirectory = new File("build/classes/main");
        extensions.templateDirectory = new File("src/test/java");

        try {
            RockerGradleTask.doCompileRocker(project);
        } catch (RockerGradleException e) {
            assertTrue(e.getMessage().contains("outputDirectory"));
            return;
        }
        fail("Exception not thrown for outputDirectory");
    }

    @Test
    public void testNullClassDirectoryThrowsException() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(RockerGradlePlugin.class);
        RockerGradleExtension extensions = (RockerGradleExtension) project.getExtensions().getByName("rocker");

        extensions.outputDirectory = new File("build/generated/source/apt/main");
        extensions.classDirectory =  null;
        extensions.templateDirectory = new File("src/test/java");

        try {
            RockerGradleTask.doCompileRocker(project);
        } catch (RockerGradleException e) {
            assertTrue(e.getMessage().contains("classDirectory"));
            return;
        }
        fail("Exception not thrown for classDirectory");
    }

    @Test
    public void testNullTemplateDirectoryThrowsException() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(RockerGradlePlugin.class);
        RockerGradleExtension extensions = (RockerGradleExtension) project.getExtensions().getByName("rocker");

        extensions.outputDirectory = new File("build/generated/source/apt/main");
        extensions.classDirectory = new File("build/classes/main");
        extensions.templateDirectory = null;

        try {
            RockerGradleTask.doCompileRocker(project);
        } catch (RockerGradleException e) {
            assertTrue(e.getMessage().contains("templateDirectory"));
            return;
        }
        fail("Exception not thrown for templateDirectory");
    }

    @Test
    public void testAddAsSourcesThrowsException() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(RockerGradlePlugin.class);
        RockerGradleExtension extensions = (RockerGradleExtension) project.getExtensions().getByName("rocker");

        extensions.outputDirectory = new File("build/generated/source/apt/main");
        extensions.classDirectory = new File("build/classes/main");
        extensions.templateDirectory = new File("src/test/java");
        extensions.addAsSources = true;

        try {
            RockerGradleTask.doCompileRocker(project);
        } catch (RockerGradleException e) {
            assertTrue(e.getMessage().contains("addAsSources"));
            return;
        }
        fail("Exception not thrown for addAsSources");
    }

    @Test
    public void testAddAsTestSourcesThrowsException() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply(RockerGradlePlugin.class);
        RockerGradleExtension extensions = (RockerGradleExtension) project.getExtensions().getByName("rocker");

        extensions.outputDirectory = new File("build/generated/source/apt/main");
        extensions.classDirectory = new File("build/classes/main");
        extensions.templateDirectory = new File("src/test/java");
        extensions.addAsTestSources = true;

        try {
            RockerGradleTask.doCompileRocker(project);
        } catch (RockerGradleException e) {
            assertTrue(e.getMessage().contains("addAsTestSources"));
            return;
        }
        fail("Exception not thrown for addAsTestSources");
    }

    private void setUpDirectories(Project project) {
        RockerGradleExtension extensions = (RockerGradleExtension) project.getExtensions().getByName("rocker");
        extensions.outputDirectory = new File("build/generated/source/apt/main");
        extensions.classDirectory = new File("build/classes/main");
        extensions.templateDirectory = new File("src/test/java");
    }
}
