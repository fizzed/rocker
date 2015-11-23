import static com.fizzed.blaze.Systems.exec;

public class blaze {

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
            "-DskipTests=true", "-Dexec.classpathScope=test", "-Dexec.mainClass=com.fizzed.rocker.reload.ReloadServer")
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

}