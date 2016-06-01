import com.fizzed.blaze.Contexts;
import com.fizzed.blaze.core.Actions;
import static com.fizzed.blaze.Contexts.fail;
import static com.fizzed.blaze.Contexts.withBaseDir;
import static com.fizzed.blaze.Systems.exec;
import com.fizzed.blaze.util.Streamables;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

public class blaze {
    static private final Logger log = Contexts.logger();

    public void run_parser() {
        exec("mvn", "-pl", "compiler", "-am", "test", "-Pexec-compiler",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.compiler.ParserMain")
            .run();
    }
    
    public void run_generator() {
        exec("mvn", "-pl", "compiler", "-am", "test", "-Pexec-compiler",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.compiler.GeneratorMain")
            .run();
    }
    
    /**
    public void run_compile() {
        exec("mvn", "-pl", "compiler", "-am", "test", "-Pexec-compiler",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.compiler.CompileMain")
            .run();
    }
    */
    
    public void run_render() {
        exec("mvn", "-pl", "java6test", "-am", "test", "-Pexec-java6test",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.RenderMain")
            .run();
    }
    
    /**
    public void run_reload() {
        exec("mvn", "-pl", "reloadtest", "-am", "test", "-Pexec-reloadtest",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.reload.ReloadMain")
            .run();
    }
    */
    
    public void run_reload() {
        exec("mvn", "-pl", "reloadtest", "-am", "test", "-Pexec-reloadtest",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.reload.ReloadMain")
            .run();
    }
    
    public void run_undertow() {
        exec("mvn", "-pl", "java8test", "-am", "test", "-Pexec-java8test",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.UndertowMain")
            .run();
    }
    
    public void run_netty() {
        exec("mvn", "-pl", "java6test", "-am", "test", "-Pexec-java6test",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.NettyMain")
            .run();
    }
    
    public void run_constant_pool() {
        exec("mvn", "-pl", "java6test", "-am", "test", "-Pexec-java6test",
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.ConstantPoolMain")
            .run();
    }
    
    private String latest_tag() {
        // get latest tag and trim off "v"
        String latestTag
            = exec("git", "describe", "--abbrev=0", "--tags")
                .pipeOutput(Streamables.captureOutput())
                .runResult()
                .map(Actions::toCaptureOutput)
                .toString()
                .trim()
                .substring(1);
        
        return latestTag;
    }
    
    public void after_release() throws IOException {
        Integer exitValue
            = exec("git", "diff-files", "--quiet")
                .exitValues(0,1)
                .run();
        
        if (exitValue == 1) {
            fail("Uncommitted changes in git. Commit them first then re-run this task");
        }

        update_readme();
        
        exec("git", "commit", "-am", "Updated README with latest version").run();
        exec("git", "push", "origin").run();
    }
    
    public void update_readme() throws IOException {
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
        Files.move(newReadmeFile, readmeFile, StandardCopyOption.REPLACE_EXISTING);
    }
    
}