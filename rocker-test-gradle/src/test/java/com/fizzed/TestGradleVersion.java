package com.fizzed;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TestGradleVersion {

	private String version;

	@Parameterized.Parameters(name = "gradle version {0}")
	public static Collection<Object[]> data(){
		return Arrays.asList(new Object[][] {
				{"3.5.1"},
				{"4.0"},
				{"4.1"},
				{"4.2"},
				{"4.3"},
				{"4.4"},
				{"4.5"},
				{"4.6"},
				{"4.7"},
				{"4.8"},
				{"4.9"},
				{"4.10"},
		});
	}

	public TestGradleVersion(String version) {

		this.version = version;
	}


	@Test
	public void testGradleVersion() throws Exception {
		testGradleVersion(version);
	}

	private void testGradleVersion(String version) throws InterruptedException {
		Path gradleDir = null;
		try {
			int result = 0;

			// publish to mavenLocal new gradle plugin
			Path workDir = Paths.get(System.getProperty("user.dir"));
			System.out.println("workDir = " + workDir);

			result = new ProcessBuilder()
					.command("./gradlew", "publishToMavenLocal", "--console=plain")
					.inheritIO()
					.directory(workDir.getParent().resolve("rocker-gradle-plugin").toFile())
					.start()
					.waitFor();

			Assert.assertEquals(0, result);

			// create gradle project with specific version

			gradleDir = workDir.resolve("src/test/resources/gradle_" + version);
			gradleDir.toFile().deleteOnExit();

			FileUtils.copyDirectory(workDir.resolve("src/test/resources/example").toFile(), gradleDir.toFile());
			System.out.println("Gradle project home = " + gradleDir);
			for (File file : FileUtils.listFiles(gradleDir.toFile(), null, true)) {
				System.out.println(file.toPath().toAbsolutePath());
			}

			result = new ProcessBuilder()
					.command(workDir.getParent().resolve("rocker-gradle-plugin").toAbsolutePath().toString() + "/gradlew", "wrapper", "--gradle-version", version, "--console=plain")
					.directory(gradleDir.toFile())
					.inheritIO()
					.start()
					.waitFor();
			Assert.assertEquals(0, result);

			// build project

			result = new ProcessBuilder()
					.command("./gradlew", "clean", "build", "--console=plain")
					.directory(gradleDir.toFile())
					.inheritIO()
					.start()
					.waitFor();

			// checks that gradle-rocker-plugin works
			Assert.assertEquals(0, result);
			final File generatedView = gradleDir.resolve("build/classes/java/main/views/HelloWorld.class").toFile();
			Assert.assertTrue(generatedView.exists());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (gradleDir != null) {
				try {
					FileUtils.deleteDirectory(gradleDir.toFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


	}
}
