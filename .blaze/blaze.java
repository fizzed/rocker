import com.fizzed.blaze.Config;
import com.fizzed.blaze.Contexts;
import static com.fizzed.blaze.Contexts.fail;
import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.exec;
import static java.util.Arrays.asList;

import com.fizzed.blaze.Task;
import com.fizzed.blaze.util.Streamables;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fizzed.buildx.Buildx;
import com.fizzed.buildx.Target;
import com.fizzed.jne.JavaHome;
import com.fizzed.jne.JavaHomeFinder;
import org.slf4j.Logger;

public class blaze {
    private final Logger log = Contexts.logger();
    private final Config config = Contexts.config();
    private final Path projectDir = withBaseDir("../").toAbsolutePath();
    
    @Task(order=1, value="Demo of parsing a template and logs its structure. Argument of -Drocker.file=<file>")
    public void parser() {
        String rockerFile = config.value("rocker.file").get();
        exec("mvn", "-pl", "rocker-compiler", "-am", "test", "-Pexec-compiler",
            "-DskipTests=true", "-Dexec.classpathScope=test",
            "-Dexec.mainClass=com.fizzed.rocker.bin.ParserMain",
            "-Drocker.file=" + rockerFile).run();
    }
    
    @Task(order=2, value="Demo of generating a .java source file from a template. Argument of -Drocker.file=<file>")
    public void generate() {
        String rockerFile = config.value("rocker.file").get();
        exec("mvn", "-pl", "rocker-compiler", "-am", "test", "-Pexec-compiler",
            "-DskipTests=true", "-Dexec.classpathScope=test",
            "-Dexec.mainClass=com.fizzed.rocker.bin.GenerateMain",
            "-Drocker.file=" + rockerFile).run();
    }
    
    @Task(order=3, value="Demo of rendering a template into a string.")
    public void render() {
        exec("mvn", "-pl", "rocker-test-template", "-am", "test", "-Pexec-java6test",
            "-DskipTests=true", "-Dexec.classpathScope=test",
            "-Dexec.mainClass=com.fizzed.rocker.bin.RenderMain").run();
    }
    
    @Task(order=4, value="Demo of a template that can be reloaded on-the-fly")
    public void hot_reload() {
        exec("mvn", "-pl", "rocker-test-reload", "-am", "test", "-Pexec-reloadtest",
            "-DskipTests=true", "-Dexec.classpathScope=test",
            "-Dexec.mainClass=com.fizzed.rocker.bin.HotReloadMain").run();
    }
    
    @Task(order=5, value="Demo of asynchronously sending a template in an Undertow-based HTTP server.")
    public void undertow() {
        exec("mvn", "-pl", "rocker-test-template", "-am", "test", "-Pexec-java8test",
            "-DskipTests=true", "-Dexec.classpathScope=test",
            "-Dexec.mainClass=com.fizzed.rocker.bin.UndertowMain").run();
    }
    
    @Task(order=6, value="Demo of asynchronously sending a template in an Netty-based HTTP server.")
    public void netty() {
        exec("mvn", "-pl", "rocker-test-template", "-am", "test", "-Pexec-java8test",
            "-DskipTests=true", "-Dexec.classpathScope=test",
            "-Dexec.mainClass=com.fizzed.rocker.bin.NettyMain").run();
    }

    @Task(order = 20)
    public void test_all_jdks() throws Exception {
        // collect and find all the jdks we will test on
        final List<JavaHome> jdks = new ArrayList<>();
        for (int jdkVersion : asList(21, 17, 11, 8)) {
            jdks.add(new JavaHomeFinder()
                .jdk()
                .version(jdkVersion)
                .preferredDistributions()
                .sorted()
                .find());
        }

        log.info("Detected JDKs:");
        jdks.forEach(jdk -> log.info("  {}", jdk));

        for (JavaHome jdk : jdks) {
            try {
                log.info("");
                log.info("Using JDK {}", jdk);
                log.info("");

                exec("mvn", "clean", "test")
                    .workingDir(this.projectDir)
                    .env("JAVA_HOME", jdk.getDirectory().toString())
                    .verbose()
                    .run();
            } catch (Exception e) {
                log.error("");
                log.error("Failed on JDK " + jdk);
                log.error("");
                throw e;
            }
        }

        log.info("Success on JDKs:");
        jdks.forEach(jdk -> log.info("  {}", jdk));
    }

    @Task(order = 98)
    public void release() throws Exception {
        // we MUST be running on Java 17+
        final JavaHome javaHome = new JavaHomeFinder()
            .jdk()
            .version(17)
            .preferredDistributions()
            .find();

        log.info("");
        log.info("Using JDK {}", javaHome);
        log.info("");

        exec("mvn", "release:prepare", "release:perform")
            .workingDir(this.projectDir)
            .env("JAVA_HOME", javaHome.getDirectory().toString())
            .verbose()
            .run();
    }

    @Task(order=99, value="Use by maintainers only. Updates REAME.md with latest git tag.")
    public void after_release() throws Exception {
        int exitValue = (int)exec("git", "diff-files", "--quiet")
            .exitValues(0,1)
            .run();
        
        if (exitValue == 1) {
            fail("Uncommitted changes in git. Commit them first then re-run this task");
        }

        update_readme();
        
        exec("git", "commit", "-am", "Updated README with latest version").run();
        exec("git", "push", "origin").run();
    }
    
    private String latest_tag() {
        // get latest tag and trim off "v"
        String latestTag = exec("git", "describe", "--abbrev=0", "--tags")
            .pipeOutput(Streamables.captureOutput())
            .runCaptureOutput()
            .toString()
            .trim()
            .substring(1);
        
        return latestTag;
    }
    
    public void update_readme() throws Exception {
        Path readmeFile = withBaseDir("../README.md");
        Path newReadmeFile = withBaseDir("../README.md.new");
        
        // find latest version via git tag
        String latestVersion = latest_tag();
        
        log.info("Latest version in git {}", latestVersion);
        
        // find current version in readme
        final Pattern versionPattern = Pattern.compile(".*<version>(\\d+\\.\\d+\\.\\d+)</version>.*");
        
        String currentVersion
            = Files.lines(readmeFile)
                .map((l) -> {
                    Matcher matcher = versionPattern.matcher(l);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    } else {
                        return null;
                    }
                })
                .filter((l) -> l != null)
                .findFirst()
                .get();
        
        log.info("Current version in README {}", currentVersion);
        
        if (currentVersion.equals(latestVersion)) {
            log.info("Versions match (no need to update README)");
            return;
        }
        
        final Pattern replacePattern = Pattern.compile(currentVersion);
        
        try (BufferedWriter writer = Files.newBufferedWriter(newReadmeFile)) {
            Files.lines(readmeFile)
                .forEach((l) -> {
                    Matcher matcher = replacePattern.matcher(l);
                    String newLine = matcher.replaceAll(latestVersion);
                    try {
                        writer.append(newLine);
                        writer.append("\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                });
            writer.flush();
        }
        
        // replace readme with updated version
        Files.delete(readmeFile);
        Thread.sleep(2000L);
        Files.move(newReadmeFile, readmeFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    private final List<Target> crossTestTargets = asList(
        new Target("linux", "x64").setTags("test").setHost("bmh-build-x64-linux-latest"),
        new Target("linux", "arm64").setTags("test").setHost("bmh-build-arm64-linux-latest"),
        new Target("linux", "riscv64").setTags("extended-test").setHost("bmh-build-riscv64-linux-latest"),
        new Target("linux_musl", "x64").setTags("test").setHost("bmh-build-x64-linux-musl-latest"),
        new Target("macos", "x64").setTags("test").setHost("bmh-build-x64-macos-latest"),
        new Target("macos", "arm64").setTags("test").setHost("bmh-build-arm64-macos-latest"),
        new Target("windows", "x64").setTags("test").setHost("bmh-build-x64-windows-latest"),
        new Target("windows", "arm64").setTags("test").setHost("bmh-build-arm64-windows-latest"),
        new Target("freebsd", "x64").setTags("test").setHost("bmh-build-x64-freebsd-latest"),
        new Target("openbsd", "x64").setTags("test").setHost("bmh-build-x64-openbsd-latest")
    );

    @Task(order = 100)
    public void cross_tests() throws Exception {
        new Buildx(crossTestTargets)
            .tags("test")
            .execute((target, project) -> {
                project.action("mvn", "clean", "test")
                    .run();
            });
    }

}