import static com.fizzed.blaze.Systems.exec;

import com.fizzed.blaze.Task;
import com.fizzed.blaze.project.PublicBlaze;
import com.fizzed.buildx.Buildx;
import com.fizzed.buildx.Target;

import java.util.List;
import java.util.stream.Collectors;

public class blaze extends PublicBlaze {

    @Override
    protected int minimumSupportedJavaVersion() {
        // we want to release with Java 11 (which ignores template17 tests, which also ruin the build order)
        return 11;
    }

    @Override
    protected List<Target> crossTestTargets() {
        // weird gradle test issue occurs only on riscv64
        return super.crossTestTargets().stream()
            .filter(v -> !v.getArch().contains("riscv64"))
            .collect(Collectors.toList());
    }

    // public demos

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

}